package org.smartregister.unicef.mis.holder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.smartregister.unicef.mis.R;
import org.smartregister.view.customcontrols.CustomFontTextView;

public class GuestMemberViewHolder extends RecyclerView.ViewHolder{
    public CustomFontTextView textViewForumDate,textViewName,textViewAge,textViewGender;

    public GuestMemberViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewForumDate = itemView.findViewById(R.id.last_sumission_date_tv);
        textViewForumDate.setVisibility(View.GONE);
        textViewName = itemView.findViewById(R.id.name_tv);
        textViewAge = itemView.findViewById(R.id.age_tv);
        textViewGender = itemView.findViewById(R.id.gender_id);
    }
}