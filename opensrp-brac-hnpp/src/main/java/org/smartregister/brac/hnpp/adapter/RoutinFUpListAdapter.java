package org.smartregister.brac.hnpp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.MemberListViewHolder;
import org.smartregister.brac.hnpp.holder.RoutinFUpListViewHolder;
import org.smartregister.brac.hnpp.model.FollowUpModel;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.utils.FormApplicability;

import java.util.ArrayList;
import java.util.Objects;

public class RoutinFUpListAdapter extends RecyclerView.Adapter<RoutinFUpListViewHolder> {
    private ArrayList<FollowUpModel> memberArrayList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public RoutinFUpListAdapter(Context context, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        memberArrayList = new ArrayList<>();
    }

    public void setData(ArrayList<FollowUpModel> memberArrayList) {
        this.memberArrayList.clear();
        this.memberArrayList.addAll(memberArrayList);
    }

    @NonNull
    @Override
    public RoutinFUpListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new RoutinFUpListViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(getAdapterLayout(), null));

    }
    public int getAdapterLayout(){
        return R.layout.anc_followup_list_row;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final RoutinFUpListViewHolder viewHolder, int position) {
        final FollowUpModel content = memberArrayList.get(position);
        viewHolder.nameTv.setText(content.getMemberName());
        viewHolder.phoneNumber.setText(content.getMemberMobileNo());
        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onClick(viewHolder.getAdapterPosition(), content));
    }


    @Override
    public int getItemCount() {
        return memberArrayList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, FollowUpModel content);
    }
}
