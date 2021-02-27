package org.smartregister.brac.hnpp.holder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import org.smartregister.brac.hnpp.R;
import org.smartregister.view.customcontrols.CustomFontTextView;

public class GuestMemberViewHolder extends RecyclerView.ViewHolder{
    public CustomFontTextView textViewForumDate,textViewName,textViewAge,textViewGender;

    public GuestMemberViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewForumDate = itemView.findViewById(R.id.last_sumission_date_tv);
        textViewName = itemView.findViewById(R.id.name_tv);
        textViewAge = itemView.findViewById(R.id.age_tv);
        textViewGender = itemView.findViewById(R.id.gender_id);
    }
}