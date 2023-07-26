package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.DashBoardViewHolder;
import org.smartregister.brac.hnpp.holder.MemberListViewHolder;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.utils.DashBoardData;

import java.util.ArrayList;

public class MemberListAdapter extends RecyclerView.Adapter<MemberListViewHolder> {
    private ArrayList<Member> memberArrayList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public MemberListAdapter(Context context, OnClickAdapter onClickAdapter) {
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
    public MemberListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MemberListViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(getAdapterLayout(), null));

    }
    public int getAdapterLayout(){
        return R.layout.adapter_member_list;
    }

    @Override
    public void onBindViewHolder(@NonNull final MemberListViewHolder viewHolder, int position) {
        final Member content = memberArrayList.get(position);
        viewHolder.nameTv.setText(content.getName());
        Log.v("nameeeeeee",""+content.getName());
      /*  viewHolder.textViewTitle.setText(content.getTitle());
        viewHolder.textViewCount.setText(content.getCount()+"");*/
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
