package org.smartregister.unicef.dghs.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.vision.text.Text;

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.holder.MemberDueViewHolder;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.MemberHistoryData;
import org.smartregister.chw.core.utils.CoreConstants;

import java.util.ArrayList;

public class MemberHistoryAdapter extends RecyclerView.Adapter<MemberDueViewHolder> {
    private ArrayList<MemberHistoryData> contentList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public MemberHistoryAdapter(Context context, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<MemberHistoryData> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public MemberDueViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        MemberDueViewHolder viewHolder = new MemberDueViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_member_due, null));
        viewHolder.statusImage.setVisibility(View.INVISIBLE);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull final MemberDueViewHolder viewHolder, int position) {
        final MemberHistoryData content = contentList.get(position);
        viewHolder.imageView.setImageResource(content.getImageSource());
        if(!TextUtils.isEmpty(content.getMemberName())){
            viewHolder.textViewTitle.setText(content.getMemberName()+"\n সেবা:"+content.getTitle());
        }else{
            viewHolder.textViewTitle.setText("সেবা: "+content.getTitle());
        }
        viewHolder.textViewLastVisit.setVisibility(View.VISIBLE);
        if(!TextUtils.isEmpty(content.getVisitDay())){
            viewHolder.textViewLastVisit.setText("তারিখ: "+content.getVisitDay());

        }else{
            viewHolder.textViewLastVisit.setText("তারিখ: "+HnppConstants.DDMMYY.format(content.getVisitDate()));

        }
        if(!TextUtils.isEmpty(content.getScheduleDate())){
            viewHolder.textViewScheduleDate.setVisibility(View.VISIBLE);
            viewHolder.textViewScheduleDate.setText("নির্ধারিত তারিখ: "+content.getScheduleDate());

        }
        if(!TextUtils.isEmpty(content.getServiceTakenDate())){
            viewHolder.textViewLastVisit.setText("সেবা গ্রহণের তারিখ:"+content.getServiceTakenDate());
        }
        if(content.isDelay()){
            viewHolder.statusImage.setVisibility(View.VISIBLE);
            viewHolder.statusImage.setImageResource(R.color.alert_urgent_red);
        }else{
            viewHolder.statusImage.setVisibility(View.INVISIBLE);
        }
        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onClick(position, content));
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, MemberHistoryData content);
    }
}
