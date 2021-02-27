package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.ForumTargetViewHolder;
import org.smartregister.brac.hnpp.holder.TargetViewHolder;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.brac.hnpp.utils.TargetVsAchievementData;

import java.util.ArrayList;

public class ForumTargetAchievementAdapter extends RecyclerView.Adapter<ForumTargetViewHolder> {
    private ArrayList<TargetVsAchievementData> contentList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public ForumTargetAchievementAdapter(Context context, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<TargetVsAchievementData> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public ForumTargetViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ForumTargetViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.forum_target_adapter_item, null));

    }

    @Override
    public void onBindViewHolder(@NonNull final ForumTargetViewHolder viewHolder, int position) {
        final TargetVsAchievementData content = contentList.get(position);
        viewHolder.textViewTitle.setText(content.getTitle());
        viewHolder.textViewAchievementCount.setText(content.getAchievementCount()+"");
        viewHolder.textViewAchievementAvgCount.setText(content.getAvgAchievmentCount()+"");
        viewHolder.textViewTargetAvgCount.setText(content.getAvgTargetCount()+"");
        viewHolder.textViewTargetCount.setText(content.getTargetCount()+"");
        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onClick(viewHolder.getAdapterPosition(), content));
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, DashBoardData content);
    }
}
