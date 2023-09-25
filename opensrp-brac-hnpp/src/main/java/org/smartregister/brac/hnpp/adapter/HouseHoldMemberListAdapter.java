package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.HouseHoldMemberListViewHolder;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.utils.FormApplicability;

import java.util.ArrayList;
import java.util.Objects;

public class HouseHoldMemberListAdapter extends RecyclerView.Adapter<HouseHoldMemberListViewHolder> {
    private ArrayList<Member> memberArrayList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public HouseHoldMemberListAdapter(Context context, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        memberArrayList = new ArrayList<>();
    }

    public void setData(ArrayList<Member> memberArrayList) {
        this.memberArrayList.clear();
        this.memberArrayList.addAll(memberArrayList);
    }

    @NonNull
    @Override
    public HouseHoldMemberListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new HouseHoldMemberListViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(getAdapterLayout(), null));

    }
    public int getAdapterLayout(){
        return R.layout.adapter_hh_member_list;
    }

    @Override
    public void onBindViewHolder(@NonNull final HouseHoldMemberListViewHolder viewHolder, int position) {
        final Member content = memberArrayList.get(position);
        viewHolder.nameTv.setText(content.getName());
        String gender = Objects.equals(content.getGender(), "F") ?"Female":"Male";
        int age = FormApplicability.getAge(content.getDob());
        if(age <= 5){
            viewHolder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.rowavatar_child));
        }else {
            viewHolder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.rowavatar_member));
        }
        viewHolder.nameTv.setText(content.getName());
        viewHolder.ageGenderTv.setText(context.getString(R.string.age,String.valueOf(age))+", "+gender);
        if(content.getStatus() == 1){//success
            viewHolder.checkIm.setImageResource(R.drawable.success);
            viewHolder.checkIm.setColorFilter(ContextCompat.getColor(context, R.color.others));
            viewHolder.itemView.setClickable(false);
            viewHolder.itemView.setEnabled(false);
            viewHolder.absentBt.setEnabled(false);
        }else if(content.getStatus() == 2){//failed
            viewHolder.checkIm.setImageResource(R.drawable.success);
            viewHolder.checkIm.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
        }
        else {//no data found
            viewHolder.checkIm.setImageResource(R.drawable.circle_background);
            viewHolder.checkIm.clearColorFilter();
        }
        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onClick(viewHolder.getAdapterPosition(), content));
        viewHolder.absentBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                content.setStatus(2);
                notifyDataSetChanged();
            }
        });
    }


    @Override
    public int getItemCount() {
        return memberArrayList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, Member content);
    }
}
