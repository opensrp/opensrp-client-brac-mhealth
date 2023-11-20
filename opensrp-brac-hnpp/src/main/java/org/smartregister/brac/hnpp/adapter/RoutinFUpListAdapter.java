package org.smartregister.brac.hnpp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.MemberListViewHolder;
import org.smartregister.brac.hnpp.holder.RoutinFUpListViewHolder;
import org.smartregister.brac.hnpp.model.AncFollowUpModel;
import org.smartregister.brac.hnpp.model.FollowUpModel;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.utils.FormApplicability;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class RoutinFUpListAdapter extends RecyclerView.Adapter<RoutinFUpListViewHolder> {
    private ArrayList<AncFollowUpModel> memberArrayList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public RoutinFUpListAdapter(Context context, OnClickAdapter onClickAdapter) {
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
    public RoutinFUpListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new RoutinFUpListViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(getAdapterLayout(), viewGroup,false));

    }
    public int getAdapterLayout(){
        return R.layout.anc_followup_list_row;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final RoutinFUpListViewHolder viewHolder, int position) {
        final AncFollowUpModel content = memberArrayList.get(position);
        if(content.followUpDate == 0){
            viewHolder.itemView.setVisibility(View.GONE);
        }else {
            viewHolder.itemView.setVisibility(View.VISIBLE);
        }
        if(content.riskType == 2){
            viewHolder.layout.setBackgroundResource(R.color.red);
        }else if(content.riskType == 1){
            viewHolder.layout.setBackgroundResource(R.color.yellow);
        }else {
            viewHolder.layout.setBackgroundResource(R.color.green);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(content.nextFollowUpDate);
        String date = calendar.get(Calendar.DAY_OF_MONTH)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.YEAR);
        viewHolder.dueButton.setText(date);

        viewHolder.nameTv.setText(content.memberName);
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
