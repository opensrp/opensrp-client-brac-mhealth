package org.smartregister.unicef.mis.holder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.smartregister.unicef.mis.R;

/**
 * Created by wizard on 06/08/19.
 */
public class TargetViewHolder extends RecyclerView.ViewHolder {
    public TextView textViewAchievementCount,textViewTargetCount,textViewTitle;
    public ProgressBar progressBar;

    public TargetViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewAchievementCount = itemView.findViewById(R.id.achievement_count_tv);
        textViewTargetCount = itemView.findViewById(R.id.target_count_tv);
        textViewTitle = itemView.findViewById(R.id.title_tv);
        progressBar = itemView.findViewById(R.id.progress_bar);
    }
}
