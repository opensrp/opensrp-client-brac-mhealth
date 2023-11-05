package org.smartregister.brac.hnpp.holder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.view.customcontrols.CustomFontTextView;

public class NotificationViewHolder extends RecyclerView.ViewHolder{

    public CustomFontTextView textViewDate;
    public CustomFontTextView textViewTitle;
    public CustomFontTextView textViewMin;
    public ImageView imageViewAppIcon;

    public NotificationViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewDate = itemView.findViewById(R.id.date_tv);
        textViewTitle = itemView.findViewById(R.id.title_tv);
        textViewMin = itemView.findViewById(R.id.min_tv);
        imageViewAppIcon = itemView.findViewById(R.id.app_icon);
    }
}
