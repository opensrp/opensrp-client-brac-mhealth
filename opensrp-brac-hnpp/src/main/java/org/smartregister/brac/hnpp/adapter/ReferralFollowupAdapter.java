package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.DashBoardViewHolder;
import org.smartregister.brac.hnpp.holder.ReferralFollowupViewHolder;
import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;
import org.smartregister.brac.hnpp.model.ReferralFollowupJsonModel;
import org.smartregister.brac.hnpp.utils.DashBoardData;

import java.util.ArrayList;

public class ReferralFollowupAdapter extends RecyclerView.Adapter<ReferralFollowupViewHolder> {
    private ArrayList<ReferralFollowupJsonModel> contentList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public ReferralFollowupAdapter(Context context, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<ReferralFollowupJsonModel> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public ReferralFollowupViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ReferralFollowupViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(getAdapterLayout(), viewGroup,false));

    }
    public int getAdapterLayout(){
        return R.layout.view_referral_followup;
    }

    @Override
    public void onBindViewHolder(@NonNull final ReferralFollowupViewHolder viewHolder, int position) {
        final ReferralFollowupJsonModel content = contentList.get(position);
        viewHolder.titleTv.setText(context.getText(R.string.referral_followup));
        viewHolder.referralCauseTv.setText(content.getReferralFollowUpModel().getReferralReason());
         if(content.getJson().length()>0) {
            viewHolder.referralCheckIm.setImageResource(R.drawable.success);
            viewHolder.referralCheckIm.setColorFilter(ContextCompat.getColor(context, R.color.others));
        }
        viewHolder.item.setOnClickListener(v -> onClickAdapter.onClick(viewHolder.getAdapterPosition(), content));
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, ReferralFollowupJsonModel content);
    }
}
