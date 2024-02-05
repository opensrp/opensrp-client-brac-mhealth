package org.smartregister.unicef.mis.utils;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.opensrp.api.constants.Gender;
import org.smartregister.growthmonitoring.R;
import org.smartregister.growthmonitoring.domain.Height;
import org.smartregister.growthmonitoring.domain.HeightZScore;
import org.smartregister.util.DateUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HeightUtils {
    public static void refreshPreviousHeightsTable(Context context,final  TableLayout tableLayout, Gender gender, Date dob, List<Height> heightList, Calendar maxHeighingDate) {
        for (Height height : heightList) {
            TableRow dividerRow = new TableRow(context);
            View divider = new View(context);
            TableRow.LayoutParams params = (TableRow.LayoutParams) divider.getLayoutParams();
            if (params == null) params = new TableRow.LayoutParams();
            params.width = TableRow.LayoutParams.MATCH_PARENT;
            params.height = context.getResources().getDimensionPixelSize(R.dimen.weight_table_divider_height);
            params.span = 3;
            divider.setLayoutParams(params);
            divider.setBackgroundColor(context.getResources().getColor(R.color.client_list_header_dark_grey));
            dividerRow.addView(divider);
            tableLayout.addView(dividerRow);

            TableRow curRow = new TableRow(context);

            TextView ageTextView = new TextView(context);
            ageTextView.setHeight(context.getResources().getDimensionPixelSize(R.dimen.table_contents_text_height));
            ageTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen.weight_table_contents_text_size));
            ageTextView.setText(DateUtil.getDuration(height.getDate().getTime() - dob.getTime()));
            ageTextView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            ageTextView.setTextColor(context.getResources().getColor(R.color.client_list_grey));
            curRow.addView(ageTextView);

            TextView HeightTextView = new TextView(context);
            HeightTextView.setHeight(context.getResources().getDimensionPixelSize(R.dimen.table_contents_text_height));
            HeightTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen.weight_table_contents_text_size));
            HeightTextView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            HeightTextView.setText(
                    String.format("%s %s", String.valueOf(height.getCm()), context.getString(R.string.cm)));
            HeightTextView.setTextColor(context.getResources().getColor(R.color.client_list_grey));
            curRow.addView(HeightTextView);

            TextView zScoreTextView = new TextView(context);
            zScoreTextView.setHeight(context.getResources().getDimensionPixelSize(R.dimen.table_contents_text_height));
            zScoreTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen.weight_table_contents_text_size));
            zScoreTextView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
//            if (height.getDate().compareTo(maxHeighingDate.getTime()) > 0) {
//                zScoreTextView.setText("");
//            } else {
                double zScore = HeightZScore.calculate(gender, dob, height.getDate(), height.getCm());
                zScore = HeightZScore.roundOff(zScore);
                if(zScore == 0.0 || zScore == 0){
                    zScore = HeightZScore.roundOff(height.getZScore());
                }
                zScoreTextView.setTextColor(context.getResources().getColor(HeightZScore.getZScoreColor(zScore)));
                zScoreTextView.setText(String.valueOf(zScore));
            //}
            curRow.addView(zScoreTextView);
            //tableLayout.addView(curRow);

            Double eachZScoreDouble = HeightZScore.calculate(gender, dob, height.getDate(), height.getCm());
            double eachZScore = (eachZScoreDouble == null) ? 0 : eachZScoreDouble.doubleValue();
            String eachHeightText = getBengaliHeightStatus(HeightZScore.getZScoreText(HeightZScore.roundOff(eachZScore)));
            // double zScore = ZScore.calculate(gender, dob, weight.getDate(), weight.getKg());

            TextView statusTextView = new TextView(tableLayout.getContext());
            statusTextView.setHeight(context.getResources().getDimensionPixelSize(R.dimen.table_contents_text_height));
            statusTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen.weight_table_contents_text_size));
            statusTextView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            statusTextView.setTextColor(context.getResources().getColor(HeightZScore.getZScoreColor(zScore)));
            statusTextView.setText(eachHeightText);

            curRow.addView(statusTextView);
            tableLayout.addView(curRow);
        }


    }
    private static String getBengaliHeightStatus(String zScoreText) {
        switch (zScoreText.toUpperCase()){
            case "SAM":
                return "মারাত্মক খর্ব";
            case "DARK YELLOW":
                return "মাঝারি খর্ব";
            case "MAM":
                return "স্বল্প খর্ব";
            default:
                return "স্বাভাবিক";

        }
    }
}
