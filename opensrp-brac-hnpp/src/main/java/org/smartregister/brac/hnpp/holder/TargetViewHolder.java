package org.smartregister.brac.hnpp.holder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
    public RelativeLayout itemBg;
    public TextView textViewCount,textViewTitle;
    public ProgressBar progressBar;

    public TargetViewHolder(@NonNull View itemView) {
        super(itemView);
        itemBg = itemView.findViewById(R.id.item_bg);
        textViewCount = itemView.findViewById(R.id.count_tv);
        textViewTitle = itemView.findViewById(R.id.title_tv);
        progressBar = itemView.findViewById(R.id.progress_bar);
    }
}
