package org.smartregister.unicef.mis.risky_patient.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.risky_patient.holder.TelephonicFUpListViewHolder;
import org.smartregister.unicef.mis.risky_patient.model.AncFollowUpModel;

import java.util.ArrayList;
import java.util.Calendar;

public class TelephonicFUpListAdapter extends RecyclerView.Adapter<TelephonicFUpListViewHolder> {
    private ArrayList<AncFollowUpModel> memberArrayList;
    private Context context;
    private OnClickAdapter onClickAdapter;
    private OnClickAdapter addFollowupClickAdapter;
    private OnClickAdapter onItemClickAdapter;

    public TelephonicFUpListAdapter(Context context,
                                    OnClickAdapter onClickAdapter,
                                    OnClickAdapter addFollowupClickAdapter,
                                    OnClickAdapter onItemClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        this.onItemClickAdapter = onItemClickAdapter;
        this.addFollowupClickAdapter = addFollowupClickAdapter;
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
        if(content.telephonyFollowUpDate == 0){
            viewHolder.itemView.setVisibility(View.GONE);
        }else {
            viewHolder.itemView.setVisibility(View.VISIBLE);
        }

        if(content.isCalledTelephonic == 1){
            viewHolder.followupLay.setVisibility(View.VISIBLE);
            viewHolder.dueButton.setVisibility(View.GONE);
            viewHolder.dueLay.setVisibility(View.GONE);
        }else {
            viewHolder.followupLay.setVisibility(View.GONE);
            viewHolder.dueButton.setVisibility(View.GONE);
            viewHolder.dueLay.setVisibility(View.VISIBLE);
        }


        if(content.riskType == 2){
            viewHolder.layout.setBackgroundResource(R.color.due_vaccine_red);
        }else if(content.riskType == 1){
            viewHolder.layout.setBackgroundResource(R.color.yellow);
        }else {
            viewHolder.layout.setBackgroundResource(R.color.green);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(content.telephonyFollowUpDate);
        String date = calendar.get(Calendar.DAY_OF_MONTH)+"/"+(calendar.get(Calendar.MONTH)+1)+"/"+calendar.get(Calendar.YEAR);
        viewHolder.dueDate.setText("Due:"+date);
        viewHolder.phNumber.setText(content.memberPhoneNum);

        viewHolder.nameTv.setText(content.memberName);
        viewHolder.phoneNumber.setText(content.memberPhoneNum);
        viewHolder.dueLay.setOnClickListener(v -> onClickAdapter.onClick(viewHolder.getAdapterPosition(), content));
        viewHolder.itemView.setOnClickListener(v -> onItemClickAdapter.onClick(viewHolder.getAdapterPosition(), content));
        viewHolder.followupButton.setOnClickListener(v -> addFollowupClickAdapter.onClick(viewHolder.getAdapterPosition(), content));
    }


    @Override
    public int getItemCount() {
        return memberArrayList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, AncFollowUpModel content);
    }
}
