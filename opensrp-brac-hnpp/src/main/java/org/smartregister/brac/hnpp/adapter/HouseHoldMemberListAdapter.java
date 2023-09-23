package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.HouseHoldMemberListViewHolder;
import org.smartregister.brac.hnpp.holder.MemberListViewHolder;
import org.smartregister.brac.hnpp.model.Member;

import java.util.ArrayList;

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
      /*  viewHolder.textViewTitle.setText(content.getTitle());
        viewHolder.textViewCount.setText(content.getCount()+"");*/
        if(content.getStatus()){
            viewHolder.checkIm.setImageResource(R.drawable.success);
            viewHolder.itemView.setClickable(false);
        }else {
            viewHolder.checkIm.setImageResource(R.drawable.circle_background);
        }
        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onClick(viewHolder.getAdapterPosition(), content));
    }


    @Override
    public int getItemCount() {
        return memberArrayList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, Member content);
    }
}
