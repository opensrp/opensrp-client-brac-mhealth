package org.smartregister.brac.hnpp.holder;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.view.customcontrols.CustomFontTextView;

/**
 * Created by tanvir on 17/09/23.
 */
public class ReferralFollowupViewHolder extends RecyclerView.ViewHolder {
    public TextView titleTv;
    public TextView referralCauseTv;

    public ImageView referralCheckIm;

    public ConstraintLayout item;

    public ReferralFollowupViewHolder(@NonNull View itemView) {
        super(itemView);
        item = itemView.findViewById(R.id.item);
        titleTv = itemView.findViewById(R.id.title_tv);
        referralCauseTv = itemView.findViewById(R.id.referral_followup_cause_tv);
        referralCheckIm = itemView.findViewById(R.id.referral_followup_check_im);
    }
}
