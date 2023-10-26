package org.smartregister.brac.hnpp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.TelephonicFUpListViewHolder;
import org.smartregister.brac.hnpp.model.AncFollowUpModel;

import java.util.ArrayList;

public class TelephonicFUpListAdapter extends RecyclerView.Adapter<TelephonicFUpListViewHolder> {
    private ArrayList<AncFollowUpModel> memberArrayList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public TelephonicFUpListAdapter(Context context, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        memberArrayList = new ArrayList<>();
    }

    public void setData(ArrayList<AncFollowUpModel> memberArrayList) {
        this.memberArrayList.clear();
        this.memberArrayList.addAll(memberArrayList);
    }

    @NonNull
    @Override
    public TelephonicFUpListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new TelephonicFUpListViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(getAdapterLayout(), viewGroup,false));

    }
    public int getAdapterLayout(){
        return R.layout.anc_followup_list_row;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final TelephonicFUpListViewHolder viewHolder, int position) {
        final AncFollowUpModel content = memberArrayList.get(position);
        viewHolder.dueButton.setVisibility(View.GONE);
        viewHolder.dueLay.setVisibility(View.VISIBLE);

        if(content.riskType == 2){
            viewHolder.layout.setBackgroundResource(R.color.red);
        }else if(content.riskType == 1){
            viewHolder.layout.setBackgroundResource(R.color.yellow);
        }else {
            viewHolder.layout.setBackgroundResource(R.color.green);
        }
        viewHolder.nameTv.setText(content.memberName+" "+content.riskType);
        viewHolder.phoneNumber.setText(content.memberPhoneNum);
        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onClick(viewHolder.getAdapterPosition(), content));
    }


    @Override
    public int getItemCount() {
        return memberArrayList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, AncFollowUpModel content);
    }
}
