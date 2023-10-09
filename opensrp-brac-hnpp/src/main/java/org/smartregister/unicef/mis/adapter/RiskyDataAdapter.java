package org.smartregister.unicef.mis.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.model.InvalidDataModel;
import org.smartregister.unicef.mis.utils.RiskyModel;

import java.util.ArrayList;

public class RiskyDataAdapter extends RecyclerView.Adapter<RiskyDataAdapter.RiskyDataViewHolder> {
    private ArrayList<RiskyModel> contentList;
    private Context context;

    public RiskyDataAdapter(Context context) {
        this.context = context;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<RiskyModel> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public RiskyDataViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new RiskyDataViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.risky_details_content, null));

    }

    @Override
    public void onBindViewHolder(@NonNull final RiskyDataViewHolder viewHolder, int position) {
        RiskyModel model = contentList.get(position);
        viewHolder.textViewDate.setText(model.date);
        viewHolder.textViewRiskyKey.setText(model.riskyKey);
        viewHolder.textViewRiskyValue.setText(model.riskyValue);
        viewHolder.textViewEventName.setText(model.eventType);

    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, InvalidDataModel content);
        void onDelete(int position, InvalidDataModel content);
    }
    public static class RiskyDataViewHolder extends RecyclerView.ViewHolder{
        public TextView textViewDate,textViewName,textViewEventName,textViewRiskyKey,textViewRiskyValue,
                textViewRiskyType;

        public RiskyDataViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.date_tv);
            textViewEventName = itemView.findViewById(R.id.event_type);
            textViewRiskyKey = itemView.findViewById(R.id.risk_key);
            textViewRiskyValue = itemView.findViewById(R.id.risk_value);
            textViewRiskyType = itemView.findViewById(R.id.risk_type);
        }
    }
}
