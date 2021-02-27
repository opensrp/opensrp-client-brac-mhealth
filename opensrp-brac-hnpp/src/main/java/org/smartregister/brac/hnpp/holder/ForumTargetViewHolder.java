package org.smartregister.brac.hnpp.holder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;

/**
 * Created by wizard on 06/08/19.
 */
public class ForumTargetViewHolder extends RecyclerView.ViewHolder {
    public TextView textViewAchievementCount,textViewTargetCount,textViewTitle;
    public TextView textViewAchievementAvgCount,textViewTargetAvgCount;
    public ProgressBar progressBar,progressBarAvg;

    public ForumTargetViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewAchievementCount = itemView.findViewById(R.id.achievement_count_tv);
        textViewAchievementAvgCount = itemView.findViewById(R.id.achievement_count_avg_tv);
        textViewTargetCount = itemView.findViewById(R.id.target_count_tv);
        textViewTargetAvgCount = itemView.findViewById(R.id.target_count_avg_tv);
        textViewTitle = itemView.findViewById(R.id.title_tv);
        progressBar = itemView.findViewById(R.id.progress_bar);
        progressBarAvg = itemView.findViewById(R.id.progress_bar_avg);
    }
}
