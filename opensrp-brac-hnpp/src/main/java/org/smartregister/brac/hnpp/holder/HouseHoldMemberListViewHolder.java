package org.smartregister.brac.hnpp.holder;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;

/**
 * Created by tanvir on 21/09/23.
 */
public class HouseHoldMemberListViewHolder extends RecyclerView.ViewHolder {
    public TextView nameTv;
    public ImageView checkIm;
    public AppCompatButton absentBt;

    public HouseHoldMemberListViewHolder(@NonNull View itemView) {
        super(itemView);
        nameTv = itemView.findViewById(R.id.memberNameTv);
        checkIm = itemView.findViewById(R.id.check_im);
        absentBt = itemView.findViewById(R.id.absentBt);
    }
}
