package org.smartregister.unicef.dghs.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.holder.HnppMemberProfileDueHolder;
import org.smartregister.unicef.dghs.holder.MemberDueViewHolder;
import org.smartregister.unicef.dghs.interactor.HnppMemberProfileInteractor;
import org.smartregister.unicef.dghs.utils.MemberProfileDueData;
import org.smartregister.unicef.dghs.utils.OtherServiceData;

import java.util.ArrayList;

public class HnppMemberProfileDueAdapter extends RecyclerView.Adapter<HnppMemberProfileDueHolder> {
    private ArrayList<MemberProfileDueData> contentList;
    private Context context;
    private HnppMemberProfileDueAdapter.OnClickAdapter onClickAdapter;

    public HnppMemberProfileDueAdapter(Context context, HnppMemberProfileDueAdapter.OnClickAdapter onClickAdapter) {
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
    public HnppMemberProfileDueHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new HnppMemberProfileDueHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_member_due, null));
    }

    @Override
    public void onBindViewHolder(@NonNull HnppMemberProfileDueHolder viewHolder, int position) {
        final MemberProfileDueData content = contentList.get(position);
        viewHolder.imageView.setImageResource(content.getImageSource());
        viewHolder.textViewTitle.setText(content.getTitle());
        if(!TextUtils.isEmpty(content.getSubTitle())){
            viewHolder.textViewLastVisit.setVisibility(View.VISIBLE);
            viewHolder.textViewLastVisit.setText(content.getSubTitle());

        }else{
            viewHolder.textViewLastVisit.setVisibility(View.INVISIBLE);

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
