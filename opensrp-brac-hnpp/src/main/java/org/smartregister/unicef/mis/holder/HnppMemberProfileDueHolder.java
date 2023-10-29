package org.smartregister.unicef.mis.holder;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.smartregister.unicef.mis.R;
import org.smartregister.view.customcontrols.CustomFontTextView;
import org.smartregister.view.customcontrols.FontVariant;

public class HnppMemberProfileDueHolder extends RecyclerView.ViewHolder {
    public RelativeLayout itemBg;
    public ImageView statusImage,imageView;
    public CustomFontTextView textViewTitle;
    public CustomFontTextView textViewLastVisit;
    public LinearLayout nextArrowBtn;
    public HnppMemberProfileDueHolder(@NonNull View itemView) {
        super(itemView);
        itemBg = itemView.findViewById(R.id.register_columns);
        statusImage = itemView.findViewById(R.id.status);
        imageView = itemView.findViewById(R.id.image_view);
        nextArrowBtn = itemView.findViewById(R.id.next_arrow_column);
        textViewTitle = itemView.findViewById(R.id.patient_name_age);
        textViewTitle.setFontVariant(FontVariant.REGULAR);
        textViewTitle.setTypeface(textViewTitle.getTypeface(), Typeface.NORMAL);

        textViewLastVisit = itemView.findViewById(R.id.last_visit);
        textViewLastVisit.setFontVariant(FontVariant.REGULAR);
        textViewLastVisit.setTypeface(textViewLastVisit.getTypeface(), Typeface.NORMAL);
    }
}
