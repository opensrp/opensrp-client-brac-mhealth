package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.model.ForumDetails;
import org.smartregister.brac.hnpp.model.InvalidDataModel;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;
import java.util.Date;

public class InvalidDataAdapter extends RecyclerView.Adapter<InvalidDataAdapter.InvalidDataViewHolder> {
    private ArrayList<InvalidDataModel> contentList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public InvalidDataAdapter(Context context, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<InvalidDataModel> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public InvalidDataViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new InvalidDataViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.invalid_client_view_content, null));

    }

    @Override
    public void onBindViewHolder(@NonNull final InvalidDataViewHolder viewHolder, int position) {
        final InvalidDataModel content = contentList.get(position);
        try{
            viewHolder.textViewDate.setText(content.date.getYear()+"-"+content.date.getMonthOfYear()+"-"+content.date.getDayOfMonth());
        }catch (Exception e){

        }
        viewHolder.textViewName.setText(content.firstName);
        viewHolder.textViewEventName.setText(content.eventType);
        viewHolder.textViewErrorCause.setText(content.errorCause);
        viewHolder.textViewUniqueId.setText(content.unique_id);
        viewHolder.textViewAddress.setText(content.address);
        viewHolder.textViewBaseEntityId.setText(content.baseEntityId);
        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onClick(position, content));
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, InvalidDataModel content);
    }
    public static class InvalidDataViewHolder extends RecyclerView.ViewHolder{
        public CustomFontTextView textViewDate,textViewName,textViewEventName,textViewErrorCause,textViewUniqueId,textViewAddress,textViewBaseEntityId;

        public InvalidDataViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.date_tv);
            textViewName = itemView.findViewById(R.id.first_name);
            textViewEventName = itemView.findViewById(R.id.event_type);
            textViewErrorCause = itemView.findViewById(R.id.error_cause);
            textViewUniqueId = itemView.findViewById(R.id.unique_id_tv);
            textViewAddress = itemView.findViewById(R.id.address_tv);
            textViewBaseEntityId = itemView.findViewById(R.id.base_entity_id);
        }
    }
}
