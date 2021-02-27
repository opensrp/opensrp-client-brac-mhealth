package org.smartregister.brac.hnpp.holder;

import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.view.customcontrols.CustomFontTextView;

public class ForumHistoryViewHolder extends RecyclerView.ViewHolder{
    public CustomFontTextView textViewForumDate,textViewForumName,textViewNoOfParticipants;
    public LinearLayout details;

    public ForumHistoryViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewForumDate = itemView.findViewById(R.id.forum_date);
        textViewForumName = itemView.findViewById(R.id.forum_name);
        textViewNoOfParticipants = itemView.findViewById(R.id.no_of_perticipant);
        details = itemView.findViewById(R.id.details);
    }
}