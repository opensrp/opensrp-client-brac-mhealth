package org.smartregister.unicef.mis.adapter;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import org.smartregister.unicef.mis.R;

import java.util.ArrayList;

public class ChildFilterTypeAdapter extends RecyclerView.Adapter<ChildFilterTypeAdapter.FilterTypeViewHolder> {
    private final ArrayList<String> filterTypeList;
    private final OnClickAdapter onClickAdapter;
    int selectedPosition = -1;
    ArrayList<Integer> selectedList = new ArrayList<Integer>();

    public ChildFilterTypeAdapter(OnClickAdapter onClickAdapter) {
        this.onClickAdapter = onClickAdapter;
        filterTypeList = new ArrayList<>();
    }

    public void setData(ArrayList<String> contentList) {
        this.filterTypeList.clear();
        this.filterTypeList.addAll(contentList);
    }

    @NonNull
    @Override
    public FilterTypeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new FilterTypeViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.filter_type_item, null));

    }

    @Override
    public void onBindViewHolder(@NonNull final FilterTypeViewHolder viewHolder, @SuppressLint("RecyclerView") int position) {
        final String content = filterTypeList.get(position);
        viewHolder.filterTypeRadio.setText(content);

        Log.d("POSSSS",""+position +"  "+selectedPosition);

        viewHolder.filterTypeRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    selectedPosition = viewHolder.getAdapterPosition();
                    selectedList.add(selectedPosition);
                    onClickAdapter.onClick(viewHolder.getAdapterPosition(), content);
                }
            }
        });
        viewHolder.filterTypeRadio.setChecked(position == selectedPosition);
    }


    @Override
    public int getItemCount() {
        return filterTypeList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, String content);
    }
    public static class FilterTypeViewHolder extends RecyclerView.ViewHolder {
        public RadioButton filterTypeRadio;

        public FilterTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            filterTypeRadio = itemView.findViewById(R.id.filter_type_radio);
        }
    }
}
