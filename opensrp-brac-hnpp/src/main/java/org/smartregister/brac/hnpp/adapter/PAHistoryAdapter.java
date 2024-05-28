package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.ForumHistoryViewHolder;
import org.smartregister.brac.hnpp.holder.PAHistoryViewHolder;
import org.smartregister.brac.hnpp.model.ForumDetails;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.anc.domain.Visit;

import java.util.ArrayList;
import java.util.Date;

public class PAHistoryAdapter extends RecyclerView.Adapter<PAHistoryViewHolder> {
    private ArrayList<Visit> contentList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public PAHistoryAdapter(Context context, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<Visit> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public PAHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        PAHistoryViewHolder viewHolder = new PAHistoryViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tabuler_view_pa_content, null));
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull final PAHistoryViewHolder viewHolder, int position) {
        final Visit content = contentList.get(position);
        viewHolder.textViewNoOfParticipants.setText("");
        viewHolder.textViewForumName.setText(HnppConstants.targetTypeMapping.get(content.getVisitType()));
        String aa = HnppConstants.DDMMYY.format(content.getDate());
        viewHolder.textViewForumDate.setText(aa);


        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onClick(position, content));
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, Visit content);
    }
}
