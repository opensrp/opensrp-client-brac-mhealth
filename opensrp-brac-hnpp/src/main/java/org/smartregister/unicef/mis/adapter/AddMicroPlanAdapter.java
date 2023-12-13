package org.smartregister.unicef.mis.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.model.Survey;
import org.smartregister.unicef.mis.repository.MicroPlanRepository;
import org.smartregister.unicef.mis.utils.MicroPlanEpiData;
import org.smartregister.unicef.mis.utils.SessionPlanData;

import java.util.ArrayList;

public class AddMicroPlanAdapter extends RecyclerView.Adapter<AddMicroPlanAdapter.AddMicroPlanViewHolder> {
    private ArrayList<MicroPlanEpiData> contentList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public AddMicroPlanAdapter(Context context, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<MicroPlanEpiData> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public AddMicroPlanViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        AddMicroPlanViewHolder viewHolder = new AddMicroPlanViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_add_microplan, viewGroup,false));
        return viewHolder;

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final AddMicroPlanViewHolder viewHolder, int position) {
        final MicroPlanEpiData content = contentList.get(position);
        viewHolder.textViewUnion.setText(content.unionName);
        viewHolder.textViewOldWard.setText(content.oldWardName);
        viewHolder.textViewNewWard.setText(content.newWardName);
        viewHolder.textViewBlock.setText(content.blockName);
        String status = content.microPlanStatus;
        viewHolder.textViewStatus.setText(status);
         if(status.equalsIgnoreCase(MicroPlanRepository.MICROPLAN_STATUS_TAG.PENDING.getValue())){
            viewHolder.textViewStatus.setBackgroundResource(R.drawable.blue_round);
             viewHolder.imageViewAdd.setVisibility(View.GONE);
             viewHolder.imageViewEdit.setVisibility(View.VISIBLE);
             viewHolder.imageViewView.setVisibility(View.VISIBLE);
        }else if(status.equalsIgnoreCase(MicroPlanRepository.MICROPLAN_STATUS_TAG.APPROVED.getValue())){
            viewHolder.textViewStatus.setBackgroundResource(R.drawable.green_round);
             viewHolder.imageViewAdd.setVisibility(View.GONE);
             viewHolder.imageViewEdit.setVisibility(View.GONE);
             viewHolder.imageViewView.setVisibility(View.VISIBLE);
        }else if(status.equalsIgnoreCase(MicroPlanRepository.MICROPLAN_STATUS_TAG.RETAKE.getValue())){
            viewHolder.textViewStatus.setBackgroundResource(R.drawable.yellow_round);
             viewHolder.imageViewAdd.setVisibility(View.GONE);
             viewHolder.imageViewEdit.setVisibility(View.VISIBLE);
             viewHolder.imageViewView.setVisibility(View.VISIBLE);
        }else{
            viewHolder.textViewStatus.setBackgroundResource(R.drawable.red_round);
            viewHolder.imageViewAdd.setVisibility(View.VISIBLE);
            viewHolder.imageViewEdit.setVisibility(View.GONE);
            viewHolder.imageViewView.setVisibility(View.GONE);
        }
         if(content.sessionPlanData!=null){
             int count = Integer.parseInt(content.sessionPlanData.yearlyCount+"");
             viewHolder.textViewYearly.setText(count+"");

             viewHolder.textViewMonthly.setText(count/12+"");
             viewHolder.textViewDays.setText(getDaysName(content.sessionPlanData));
         }
        viewHolder.imageViewView.setOnClickListener(v -> onClickAdapter.onViewClick(position, content));
        viewHolder.imageViewEdit.setOnClickListener(v -> onClickAdapter.onEditClick(position, content));
        viewHolder.imageViewAdd.setOnClickListener(v -> onClickAdapter.onAddClick(position, content));
    }

    private String getDaysName(SessionPlanData sessionPlanData) {
        StringBuilder builder = new StringBuilder();
        if(sessionPlanData.saturday){
            builder.append(context.getResources().getString(R.string.every_saterday));
        }
        if(sessionPlanData.sunday){
            if(builder.length()>0)builder.append(",");
            builder.append(context.getResources().getString(R.string.every_sunday));
        }
        if(sessionPlanData.monday){
            if(builder.length()>0)builder.append(",");
            builder.append(context.getResources().getString(R.string.every_monday));
        }
        if(sessionPlanData.tuesday){
            if(builder.length()>0)builder.append(",");
            builder.append(context.getResources().getString(R.string.every_tuesday));
        }
        if(sessionPlanData.wednesday){
            if(builder.length()>0)builder.append(",");
            builder.append(context.getResources().getString(R.string.wednesday));
        }
        if(sessionPlanData.wednesday){
            if(builder.length()>0)builder.append(",");
            builder.append(context.getResources().getString(R.string.thursday));
        }
        return builder.toString();
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onViewClick(int position, MicroPlanEpiData content);
        void onEditClick(int position, MicroPlanEpiData content);
        void onAddClick(int position, MicroPlanEpiData content);
    }
    public static class AddMicroPlanViewHolder extends RecyclerView.ViewHolder{
        public TextView textViewUnion,textViewOldWard,textViewNewWard,textViewBlock,textViewStatus,textViewYearly,textViewMonthly,textViewDays;
        private ImageView imageViewAdd,imageViewEdit,imageViewView;

        public AddMicroPlanViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUnion = itemView.findViewById(R.id.union_txt);
            textViewOldWard = itemView.findViewById(R.id.old_ward_txt);
            textViewNewWard = itemView.findViewById(R.id.new_ward_txt);
            textViewBlock = itemView.findViewById(R.id.block_txt);
            textViewStatus = itemView.findViewById(R.id.status_txt);
            imageViewAdd = itemView.findViewById(R.id.add_img);
            imageViewEdit = itemView.findViewById(R.id.edit_img);
            imageViewView = itemView.findViewById(R.id.view_img);
            textViewYearly = itemView.findViewById(R.id.yearly_txt);
            textViewMonthly = itemView.findViewById(R.id.monthly_txt);
            textViewDays = itemView.findViewById(R.id.weekly_txt);
        }
    }
}
