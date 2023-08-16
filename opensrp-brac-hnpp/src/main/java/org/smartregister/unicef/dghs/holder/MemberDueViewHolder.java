package org.smartregister.unicef.dghs.holder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.smartregister.unicef.dghs.R;
import org.smartregister.view.customcontrols.CustomFontTextView;

/**
 * Created by wizard on 06/08/19.
 */
public class MemberDueViewHolder extends RecyclerView.ViewHolder {
    public RelativeLayout itemBg;
    public ImageView statusImage,imageView;
    public CustomFontTextView textViewTitle;
    public CustomFontTextView textViewLastVisit;
    public CustomFontTextView textViewScheduleDate;
    public LinearLayout nextArrowBtn;

    public MemberDueViewHolder(@NonNull View itemView) {
        super(itemView);
        itemBg = itemView.findViewById(R.id.register_columns);
        itemBg.setBackgroundColor(itemView.getResources().getColor(R.color.light_gray));
        statusImage = itemView.findViewById(R.id.status);
        imageView = itemView.findViewById(R.id.image_view);
        nextArrowBtn = itemView.findViewById(R.id.next_arrow_column);
        textViewTitle = itemView.findViewById(R.id.patient_name_age);
        textViewLastVisit = itemView.findViewById(R.id.last_visit);
        textViewScheduleDate = itemView.findViewById(R.id.schedule_date_visit);
    }
}
