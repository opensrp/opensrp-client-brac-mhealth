package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.TargetViewHolder;

public class ServiceTargetAchievementAdapter extends TargetAchievementAdapter {
    public ServiceTargetAchievementAdapter(Context context, OnClickAdapter onClickAdapter) {
        super(context, onClickAdapter);
    }

    @NonNull
    @Override
    public TargetViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new TargetViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.service_target_adapter_item, null));

    }
}
