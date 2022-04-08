package org.smartregister.brac.hnpp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.model.Survey;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;
import java.util.Date;

public class SurveyHistoryAdapter extends RecyclerView.Adapter<SurveyHistoryAdapter.SurveyHistoryViewHolder> {
    private ArrayList<Survey> contentList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public SurveyHistoryAdapter(Context context, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<Survey> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public SurveyHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        @SuppressLint("InflateParams") SurveyHistoryViewHolder viewHolder = new SurveyHistoryViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.survey_tabuler_view_header, null));
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull final SurveyHistoryViewHolder viewHolder, int position) {
        final Survey content = contentList.get(position);
        viewHolder.textViewFormName.setText(content.formName);
        try{
            Date d = new Date(content.dateTime);
            String aa = HnppConstants.DDMMYY.format(d);
            String time = HnppConstants.HHMM.format(d);
            viewHolder.textViewDate.setText(aa);
            viewHolder.textViewTime.setText(time);
        }catch (Exception e){

        }

        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onClick(position, content));
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, Survey content);
    }
    public static class SurveyHistoryViewHolder extends RecyclerView.ViewHolder{
        public TextView textViewDate,textViewTime,textViewFormName;

        public SurveyHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.date);
            textViewTime = itemView.findViewById(R.id.time);
            textViewFormName = itemView.findViewById(R.id.form_name);
        }
    }
}
