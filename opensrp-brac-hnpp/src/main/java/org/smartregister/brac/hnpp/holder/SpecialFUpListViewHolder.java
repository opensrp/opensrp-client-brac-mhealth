package org.smartregister.brac.hnpp.holder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;

/**
 * Created by tanvir on 25/10/23.
 */
public class SpecialFUpListViewHolder extends RecyclerView.ViewHolder {
    public TextView nameTv;
    public TextView phoneNumber;
    public ImageView imageView;
    public RelativeLayout layout;

    public SpecialFUpListViewHolder(@NonNull View itemView) {
        super(itemView);
        nameTv = itemView.findViewById(R.id.woman_name);
        phoneNumber = itemView.findViewById(R.id.phoneNumber);
        imageView = itemView.findViewById(R.id.imageView);
        layout = itemView.findViewById(R.id.item);
    }
}
