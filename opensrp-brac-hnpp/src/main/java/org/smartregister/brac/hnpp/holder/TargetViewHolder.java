package org.smartregister.brac.hnpp.holder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.view.customcontrols.CustomFontTextView;

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
