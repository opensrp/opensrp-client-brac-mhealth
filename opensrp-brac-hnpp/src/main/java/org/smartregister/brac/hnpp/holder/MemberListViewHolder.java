package org.smartregister.brac.hnpp.holder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.view.customcontrols.CustomFontTextView;

/**
 * Created by tanvir on 26/07/23.
 */
public class MemberListViewHolder extends RecyclerView.ViewHolder {
    public TextView nameTv;

    public MemberListViewHolder(@NonNull View itemView) {
        super(itemView);
        nameTv = itemView.findViewById(R.id.memberNameTv);
    }
}
