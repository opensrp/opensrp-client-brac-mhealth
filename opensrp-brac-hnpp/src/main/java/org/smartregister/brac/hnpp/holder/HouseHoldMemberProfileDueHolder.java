package org.smartregister.brac.hnpp.holder;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.smartregister.brac.hnpp.R;
import org.smartregister.view.customcontrols.CustomFontTextView;

public class HouseHoldMemberProfileDueHolder extends RecyclerView.ViewHolder {
    public RelativeLayout itemBg;
    public ImageView statusImage,imageView,checkIm;
    public CustomFontTextView textViewTitle;
    public CustomFontTextView textViewLastVisit;

    public AppCompatButton noNeedBt;

    public HouseHoldMemberProfileDueHolder(@NonNull View itemView) {
        super(itemView);
        itemBg = itemView.findViewById(R.id.register_columns);
        statusImage = itemView.findViewById(R.id.status);
        imageView = itemView.findViewById(R.id.image_view);
        textViewTitle = itemView.findViewById(R.id.patient_name_age);
        textViewLastVisit = itemView.findViewById(R.id.last_visit);
        checkIm = itemView.findViewById(R.id.check_im);
        noNeedBt = itemView.findViewById(R.id.noNeedBt);
    }
}
