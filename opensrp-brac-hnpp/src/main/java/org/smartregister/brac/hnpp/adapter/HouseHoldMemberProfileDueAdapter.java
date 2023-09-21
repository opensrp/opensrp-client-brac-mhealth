package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.HnppMemberProfileDueHolder;
import org.smartregister.brac.hnpp.holder.HouseHoldMemberProfileDueHolder;
import org.smartregister.brac.hnpp.interactor.HnppMemberProfileInteractor;
import org.smartregister.brac.hnpp.utils.MemberProfileDueData;

import java.util.ArrayList;

public class HouseHoldMemberProfileDueAdapter extends RecyclerView.Adapter<HouseHoldMemberProfileDueHolder> {
    private ArrayList<MemberProfileDueData> contentList;
    private Context context;
    private HouseHoldMemberProfileDueAdapter.OnClickAdapter onClickAdapter;

    public HouseHoldMemberProfileDueAdapter(Context context, HouseHoldMemberProfileDueAdapter.OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<MemberProfileDueData> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }


    @NonNull
    @Override
    public HouseHoldMemberProfileDueHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new HouseHoldMemberProfileDueHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_hh_member_due, null));
    }

    @Override
    public void onBindViewHolder(@NonNull HouseHoldMemberProfileDueHolder viewHolder, int position) {
        final MemberProfileDueData content = contentList.get(position);
        viewHolder.imageView.setImageResource(content.getImageSource());
        viewHolder.textViewTitle.setText(content.getTitle());
        if(!TextUtils.isEmpty(content.getSubTitle())){
            viewHolder.textViewLastVisit.setVisibility(View.VISIBLE);
            viewHolder.textViewLastVisit.setText(content.getSubTitle());

        }else{
            viewHolder.textViewLastVisit.setVisibility(View.INVISIBLE);

        }
        if(content.getStatus()) {
            viewHolder.checkIm.setImageResource(R.drawable.success);
            viewHolder.checkIm.setColorFilter(ContextCompat.getColor(context, R.color.others));
        }
        if(content.getType() == HnppMemberProfileInteractor.TAG_OPEN_FAMILY || content.getType() == HnppMemberProfileInteractor.TAG_OPEN_REFEREAL){
            viewHolder.statusImage.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.statusImage.setVisibility(View.VISIBLE);
        }
        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onClick(viewHolder.getAdapterPosition(), content));
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, MemberProfileDueData content);
    }
}
