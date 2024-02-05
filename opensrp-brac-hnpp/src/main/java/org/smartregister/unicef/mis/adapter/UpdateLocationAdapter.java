package org.smartregister.unicef.mis.adapter;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.location.UpdateLocationModel;

import java.util.ArrayList;

public class UpdateLocationAdapter extends RecyclerView.Adapter<UpdateLocationAdapter.FilterTypeViewHolder> {
    private final ArrayList<UpdateLocationModel> filterTypeList;
    private final OnClickAdapter onClickAdapter;
    int selectedPosition = -1;
    ArrayList<Integer> selectedList = new ArrayList<Integer>();
    ArrayList<String> locationNameList = new ArrayList<String>();
    public UpdateLocationAdapter(OnClickAdapter onClickAdapter) {
        this.onClickAdapter = onClickAdapter;
        filterTypeList = new ArrayList<>();
    }

    public void setData(ArrayList<UpdateLocationModel> contentList) {
        this.filterTypeList.clear();
        this.filterTypeList.addAll(contentList);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public FilterTypeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new FilterTypeViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.location_adapter, null));

    }

    @Override
    public void onBindViewHolder(@NonNull final FilterTypeViewHolder viewHolder, @SuppressLint("RecyclerView") int position) {
        final UpdateLocationModel content = filterTypeList.get(position);
        viewHolder.filterTypeCheck.setText(content.name);

        Log.d("POSSSS",""+position +"  "+selectedPosition);

        viewHolder.filterTypeCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    selectedPosition = viewHolder.getAdapterPosition();
                    selectedList.add(content.id);
                    locationNameList.add(content.name);
                    onClickAdapter.onClick(viewHolder.getAdapterPosition(), content);
                }else{
                    selectedList.remove(Integer.valueOf(content.id));
                    locationNameList.remove(content.name);
                    onClickAdapter.unChecked(viewHolder.getAdapterPosition(), content);
//                    Iterator itr = selectedList.iterator();
//                    while (itr.hasNext()) {
//                        int x = (Integer)itr.next();
//                        if (x==content.id)
//                            itr.remove();
//                    }
                }
            }
        });
        viewHolder.filterTypeCheck.setChecked(selectedList.contains(content.id));
    }
    public ArrayList<Integer> getSelectedList(){
        return selectedList;
    }

    public ArrayList<String> getLocationNameList() {
        return locationNameList;
    }

    @Override
    public int getItemCount() {
        return filterTypeList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, UpdateLocationModel content);
        void unChecked(int position, UpdateLocationModel content);
    }
    public static class FilterTypeViewHolder extends RecyclerView.ViewHolder {
        public CheckBox filterTypeCheck;

        public FilterTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            filterTypeCheck = itemView.findViewById(R.id.filter_type_checkbox);
        }
    }
}
