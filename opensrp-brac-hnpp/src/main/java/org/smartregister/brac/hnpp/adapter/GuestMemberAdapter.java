package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.ForumHistoryViewHolder;
import org.smartregister.brac.hnpp.holder.GuestMemberViewHolder;
import org.smartregister.brac.hnpp.model.ForumDetails;
import org.smartregister.brac.hnpp.utils.GuestMemberData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.family.util.Utils;

import java.util.ArrayList;
import java.util.Date;

public class GuestMemberAdapter extends RecyclerView.Adapter<GuestMemberViewHolder> {
    private ArrayList<GuestMemberData> contentList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public GuestMemberAdapter(Context context, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<GuestMemberData> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public GuestMemberViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        GuestMemberViewHolder viewHolder = new GuestMemberViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_guest_member, null));
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull final GuestMemberViewHolder viewHolder, int position) {
        final GuestMemberData content = contentList.get(position);
        viewHolder.textViewName.setText(context.getString(R.string.name,content.getName()));
        viewHolder.textViewGender.setText(context.getString(R.string.gender_postfix,HnppConstants.getGender(content.getGender())));
        Date d = new Date(content.getLastSubmissionDate());
        String aa = HnppConstants.DDMMYY.format(d);
        viewHolder.textViewForumDate.setText(context.getString(R.string.last_submission_date,aa));
        String dobString = Utils.getDuration(content.getDob());
        viewHolder.textViewAge.setText(context.getString(R.string.age,dobString) );


        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onClick(position, content));
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, GuestMemberData content);
    }
}