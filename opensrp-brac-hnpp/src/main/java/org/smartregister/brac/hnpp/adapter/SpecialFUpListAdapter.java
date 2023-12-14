package org.smartregister.brac.hnpp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.RoutinFUpListViewHolder;
import org.smartregister.brac.hnpp.holder.SpecialFUpListViewHolder;
import org.smartregister.brac.hnpp.model.AncFollowUpModel;

import java.util.ArrayList;
import java.util.Calendar;

public class SpecialFUpListAdapter extends RecyclerView.Adapter<SpecialFUpListViewHolder> {
    private ArrayList<AncFollowUpModel> memberArrayList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public SpecialFUpListAdapter(Context context, OnClickAdapter onClickAdapter) {
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
    public SpecialFUpListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SpecialFUpListViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(getAdapterLayout(), viewGroup,false));

    }
    public int getAdapterLayout(){
        return R.layout.anc_followup_list_row;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final SpecialFUpListViewHolder viewHolder, int position) {
        final AncFollowUpModel content = memberArrayList.get(position);
       /* if(content.specialFollowUpDate == 0){
            viewHolder.itemView.setVisibility(View.GONE);
        }else {
            viewHolder.itemView.setVisibility(View.VISIBLE);
        }*/

        if(content.riskType == 2){
            viewHolder.layout.setBackgroundResource(R.color.due_vaccine_red);
        }else if(content.riskType == 1){
            viewHolder.layout.setBackgroundResource(R.color.yellow);
        }else {
            viewHolder.layout.setBackgroundResource(R.color.green);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(content.specialFollowUpDate);
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
