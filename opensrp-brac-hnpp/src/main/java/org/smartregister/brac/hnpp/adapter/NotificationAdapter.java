package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.NotificationViewHolder;
import org.smartregister.brac.hnpp.model.Notification;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder>{
    private ArrayList<Notification> contentList;
    private Context context;
    private NotificationAdapter.OnClickAdapter onClickAdapter;

    public NotificationAdapter(Context context, NotificationAdapter.OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<Notification> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new NotificationViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_notification, null));

    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationViewHolder viewHolder, int position) {
        final Notification content = contentList.get(position);
        viewHolder.textViewDate.setText(content.getSendDate());
        viewHolder.textViewTitle.setText(content.getTitle()+"");
        viewHolder.textViewHour.setText(content.getHour()+"");
        viewHolder.textViewMin.setText(content.getMinute()+"");
        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onClick(viewHolder.getAdapterPosition(), content));
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, Notification content);
    }
}
