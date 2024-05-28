package org.smartregister.brac.hnpp.holder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import org.smartregister.brac.hnpp.R;
import org.smartregister.view.customcontrols.CustomFontTextView;

public class PAHistoryViewHolder extends RecyclerView.ViewHolder{
    public CustomFontTextView textViewForumDate,textViewForumName,textViewNoOfParticipants;
    public LinearLayout details;

    public PAHistoryViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewForumDate = itemView.findViewById(R.id.forum_date);
        textViewForumName = itemView.findViewById(R.id.forum_name);
        textViewNoOfParticipants = itemView.findViewById(R.id.no_of_perticipant);
        details = itemView.findViewById(R.id.details);
    }
}