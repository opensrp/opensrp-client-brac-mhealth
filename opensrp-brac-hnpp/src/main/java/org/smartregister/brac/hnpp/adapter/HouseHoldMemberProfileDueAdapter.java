package org.smartregister.brac.hnpp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.fragment.HouseHoldMemberDueFragment;
import org.smartregister.brac.hnpp.holder.HnppMemberProfileDueHolder;
import org.smartregister.brac.hnpp.holder.HouseHoldMemberProfileDueHolder;
import org.smartregister.brac.hnpp.interactor.HnppMemberProfileInteractor;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.MemberProfileDueData;

import java.util.ArrayList;
import java.util.Objects;

public class HouseHoldMemberProfileDueAdapter extends RecyclerView.Adapter<HouseHoldMemberProfileDueHolder> {
    private ArrayList<MemberProfileDueData> contentList;
    private Context context;
    private HouseHoldMemberProfileDueAdapter.OnClickAdapter onClickAdapter;
    private HouseHoldMemberProfileDueAdapter.OnClickAdapter noNeedClick;

    public HouseHoldMemberProfileDueAdapter(Context context,
                                            HouseHoldMemberProfileDueAdapter.OnClickAdapter onClickAdapter,
                                            HouseHoldMemberProfileDueAdapter.OnClickAdapter noNeedClick) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        this.noNeedClick = noNeedClick;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<MemberProfileDueData> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }


    @SuppressLint("InflateParams")
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
        if(Objects.equals(content.getEventType(), HnppConstants.EVENT_TYPE.ANC1_REGISTRATION) ||
                Objects.equals(content.getEventType(), HnppConstants.EVENT_TYPE.ANC2_REGISTRATION) ||
                Objects.equals(content.getEventType(), HnppConstants.EVENT_TYPE.ANC3_REGISTRATION) ||
                Objects.equals(content.getEventType(), HnppConstants.EVENT_TYPE.EYE_TEST) ||
                Objects.equals(content.getEventType(), HnppConstants.EVENT_TYPE.NCD_PACKAGE)){
            viewHolder.noNeedBt.setVisibility(View.VISIBLE);
        }else {
            viewHolder.noNeedBt.setVisibility(View.GONE);
        }

        if(!TextUtils.isEmpty(content.getSubTitle())){
            viewHolder.textViewLastVisit.setVisibility(View.VISIBLE);
            viewHolder.textViewLastVisit.setText(content.getSubTitle());
        }else{
            viewHolder.textViewLastVisit.setVisibility(View.INVISIBLE);
        }

        if(content.getStatus() == 1) {
            viewHolder.checkIm.setImageResource(R.drawable.success);
            viewHolder.checkIm.setColorFilter(ContextCompat.getColor(context, R.color.others));
            viewHolder.itemView.setClickable(false);
            viewHolder.itemView.setEnabled(false);
            viewHolder.noNeedBt.setEnabled(false);
        }else if(content.getStatus() == 2){
            viewHolder.checkIm.setImageResource(R.drawable.success);
            viewHolder.checkIm.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
        }else {
            viewHolder.checkIm.clearColorFilter();
            viewHolder.checkIm.setImageResource(R.drawable.circle_background);
        }

        if(content.getType() == HnppMemberProfileInteractor.TAG_OPEN_FAMILY || content.getType() == HnppMemberProfileInteractor.TAG_OPEN_REFEREAL){
            viewHolder.statusImage.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.statusImage.setVisibility(View.VISIBLE);
        }

        viewHolder.noNeedBt.setOnClickListener(v -> noNeedClick.onClick(viewHolder.getAdapterPosition(), content));

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
