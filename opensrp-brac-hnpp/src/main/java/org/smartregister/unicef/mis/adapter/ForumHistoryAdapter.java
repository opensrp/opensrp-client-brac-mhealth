package org.smartregister.unicef.mis.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.holder.ForumHistoryViewHolder;
import org.smartregister.unicef.mis.model.ForumDetails;
import org.smartregister.unicef.mis.utils.HnppConstants;

import java.util.ArrayList;
import java.util.Date;

public class ForumHistoryAdapter extends RecyclerView.Adapter<ForumHistoryViewHolder> {
    private ArrayList<ForumDetails> contentList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public ForumHistoryAdapter(Context context, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<ForumDetails> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public ForumHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ForumHistoryViewHolder viewHolder = new ForumHistoryViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tabuler_view_content, null));
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull final ForumHistoryViewHolder viewHolder, int position) {
        final ForumDetails content = contentList.get(position);
        viewHolder.textViewNoOfParticipants.setText(content.noOfParticipant);
        viewHolder.textViewForumName.setText(content.forumName);
        Date d = new Date(content.forumDate);
        String aa = HnppConstants.DDMMYY.format(d);
        viewHolder.textViewForumDate.setText(aa);


        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onClick(position, content));
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, ForumDetails content);
    }
}
