package org.smartregister.brac.hnpp.adapter;

import android.annotation.SuppressLint;
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
import org.smartregister.brac.hnpp.utils.FormApplicability;

import java.util.ArrayList;
import java.util.Objects;

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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MemberListViewHolder viewHolder, int position) {
        final Member content = memberArrayList.get(position);
        String gender = Objects.equals(content.getGender(), "F") ?"Female":"Male";
        int age = FormApplicability.getAge(content.getDob());
        if(age <= 5){
            viewHolder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.rowavatar_child));
        }else {
            viewHolder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.rowavatar_member));
        }
        viewHolder.nameTv.setText(content.getName());
        viewHolder.ageGenderTv.setText(context.getString(R.string.age,String.valueOf(age))+", "+gender);
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
