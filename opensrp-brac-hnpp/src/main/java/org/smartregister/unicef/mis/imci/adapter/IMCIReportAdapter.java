package org.smartregister.unicef.mis.imci.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.imci.Utility;
import org.smartregister.unicef.mis.imci.model.IMCIReport;
import org.smartregister.unicef.mis.model.PaymentHistory;
import org.smartregister.unicef.mis.utils.HnppConstants;

import java.util.ArrayList;

public class IMCIReportAdapter extends RecyclerView.Adapter<IMCIReportViewHolder> {
    private ArrayList<IMCIReport> contentList;
    private Context context;

    public IMCIReportAdapter(Context context) {
        this.context = context;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<IMCIReport> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public IMCIReportViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new IMCIReportViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_dialog_assessment_report, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull IMCIReportViewHolder imciReportViewHolder, @SuppressLint("RecyclerView") int i) {
        IMCIReport content = contentList.get(i);
        imciReportViewHolder.imciTypeTv.setText(content.getImciType());
        imciReportViewHolder.assessmentResultText.setText(content.getAssessmentResultType());
        imciReportViewHolder.assessmentResultTv.setText(Html.fromHtml(content.getAssessmentResult()));
        imciReportViewHolder.assessmentResultText.setBackgroundColor(getColorFromAssessment(content.getAssessmentResultType()));

    }

    private int getColorFromAssessment(String assessmentResultTypeId) {
        if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.SIX.getValue())
                || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.FIVE.getValue())
                || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_ANEMIA.THREE.getValue())
                ||assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_MALNUTRITION.FOUR.getValue())
                ||assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_FEVER.FIVE.getValue())
                ||assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_PNEUMONIA.FOUR.getValue())
                ||assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DANGER_SIGN.TWO.getValue())
                ||assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_FEEDING.THREE.getValue())
                ||assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA.THREE.getValue())
                ||assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.TWO.getValue())
                ||assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.THREE.getValue())
                ||assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.FOUR.getValue())
                ||assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.FIVE.getValue())
        ){
            return context.getResources().getColor(R.color.imci_red);
        }else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.SIX.getValue())
                || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA.TWO.getValue())
                || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_FEEDING.TWO.getValue())
                || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_PNEUMONIA.THREE.getValue())
                || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_FEVER.FOUR.getValue())
                || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_MALNUTRITION.THREE.getValue())
                || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_MALNUTRITION.TWO.getValue())
                || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_ANEMIA.TWO.getValue())
                || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.FOUR.getValue())
                || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.THREE.getValue())
                || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.TWO.getValue())
        ){
            return context.getResources().getColor(R.color.imci_yello);
        }else{
            return context.getResources().getColor(R.color.imci_green);
        }
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }
}
