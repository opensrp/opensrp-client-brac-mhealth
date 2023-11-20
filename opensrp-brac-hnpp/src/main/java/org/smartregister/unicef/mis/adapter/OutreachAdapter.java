package org.smartregister.unicef.mis.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.repository.MicroPlanRepository;
import org.smartregister.unicef.mis.utils.MicroPlanEpiData;
import org.smartregister.unicef.mis.utils.OutreachContentData;

import java.util.ArrayList;

public class OutreachAdapter extends RecyclerView.Adapter<OutreachAdapter.AddMicroPlanViewHolder> {
    private ArrayList<OutreachContentData> contentList;
    private Context context;
    private OnClickAdapter onClickAdapter;

    public OutreachAdapter(Context context, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<OutreachContentData> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public AddMicroPlanViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        AddMicroPlanViewHolder viewHolder = new AddMicroPlanViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_add_microplan, viewGroup,false));
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull final AddMicroPlanViewHolder viewHolder, int position) {
        final OutreachContentData content = contentList.get(position);
        viewHolder.textViewUnion.setText(content.unionName);
        viewHolder.textViewOldWard.setText(content.oldWardName);
        viewHolder.textViewNewWard.setText(content.newWardName);
        viewHolder.textViewBlock.setText(content.blockName);
        viewHolder.textViewStatus.setBackgroundResource(R.drawable.red_round);
        viewHolder.imageViewEdit.setVisibility(View.VISIBLE);
        viewHolder.imageViewView.setVisibility(View.GONE);

        viewHolder.imageViewView.setOnClickListener(v -> onClickAdapter.onViewClick(position, content));
        viewHolder.imageViewEdit.setOnClickListener(v -> onClickAdapter.onEditClick(position, content));
        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onViewClick(position, content));
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onViewClick(int position, OutreachContentData content);
        void onEditClick(int position, OutreachContentData content);
    }
    public static class AddMicroPlanViewHolder extends RecyclerView.ViewHolder{
        public TextView textViewUnion,textViewOldWard,textViewNewWard,textViewBlock,textViewStatus;
        private ImageView imageViewAdd,imageViewEdit,imageViewView;

        public AddMicroPlanViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUnion = itemView.findViewById(R.id.union_txt);
            textViewOldWard = itemView.findViewById(R.id.old_ward_txt);
            textViewNewWard = itemView.findViewById(R.id.new_ward_txt);
            textViewBlock = itemView.findViewById(R.id.block_txt);
            textViewStatus = itemView.findViewById(R.id.status_txt);
            imageViewAdd = itemView.findViewById(R.id.add_img);
            imageViewAdd.setVisibility(View.GONE);
            textViewStatus.setVisibility(View.GONE);
            imageViewEdit = itemView.findViewById(R.id.edit_img);
            imageViewView = itemView.findViewById(R.id.view_img);
        }
    }
}
