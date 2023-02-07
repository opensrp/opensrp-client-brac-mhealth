package org.smartregister.unicef.dghs.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.model.Survey;
import org.smartregister.unicef.dghs.utils.HnppConstants;
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
       SurveyHistoryViewHolder viewHolder = new SurveyHistoryViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.survey_tabuler_view_content, viewGroup,false));
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull final SurveyHistoryViewHolder viewHolder, int position) {
        final Survey content = contentList.get(position);
        viewHolder.textViewFormName.setText(content.formName);
        viewHolder.textViewDate.setText("-");
        viewHolder.textViewTime.setText("-");
        try{
            String[] dt = content.dateTime.split(" ");
//            DateTime d = new DateTime(content.dateTime);
//            String aa = HnppConstants.YYMMDD.format(d);
//            String time = HnppConstants.HHMM.format(d);
            viewHolder.textViewDate.setText(dt[0]);
            viewHolder.textViewTime.setText(dt[1]);
        }catch (Exception e){
            e.printStackTrace();
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
