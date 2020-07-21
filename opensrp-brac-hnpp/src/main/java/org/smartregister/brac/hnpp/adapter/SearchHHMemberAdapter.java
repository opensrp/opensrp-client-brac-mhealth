package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.DashBoardViewHolder;
import org.smartregister.brac.hnpp.holder.SearchViewHolder;
import org.smartregister.brac.hnpp.model.HHMemberProperty;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.brac.hnpp.utils.HnppConstants;

import java.util.ArrayList;

public class SearchHHMemberAdapter extends RecyclerView.Adapter<SearchViewHolder> {
    private ArrayList<HHMemberProperty> contentList;
    private Context context;
    private OnClickAdapter onClickAdapter;
    private ArrayList<String> selectedId = new ArrayList<>();
    private String searchType;

    public SearchHHMemberAdapter(Context context, OnClickAdapter onClickAdapter, String searchType) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        this.searchType = searchType;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<HHMemberProperty> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SearchViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_forum_member, null));

    }

    @Override
    public void onBindViewHolder(@NonNull final SearchViewHolder viewHolder, int position) {
        final HHMemberProperty content = contentList.get(position);
        if(TextUtils.isEmpty(searchType) || searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.HH.toString())){
            viewHolder.checkBox.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            if(selectedId.contains(content.getId())){
                viewHolder.checkBox.setImageResource(R.drawable.ic_checked);
            }else{
                viewHolder.checkBox.setImageResource(R.drawable.ic_unchecked);
            }
        }

        viewHolder.textViewName.setText(content.getName());
        viewHolder.textViewId.setText(content.getId());
        String age = content.getAge();
        if(TextUtils.isEmpty(age)){
            viewHolder.textViewAge.setVisibility(View.GONE);
        }else{
            viewHolder.textViewAge.setVisibility(View.VISIBLE);
            viewHolder.textViewAge.setText(context.getString(R.string.age,content.getAge()));
        }
        viewHolder.itemView.setOnClickListener(v -> {
            if(TextUtils.isEmpty(searchType)){
                return;
            }
            if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.HH.toString())){
                onClickAdapter.onClickHH(viewHolder.getAdapterPosition(), content);
            }else{
                if(selectedId.contains(content.getId())){
                    selectedId.remove(content.getId());
                }else{
                    selectedId.add(content.getId());
                }
                onClickAdapter.onClick(viewHolder.getAdapterPosition(), content);
            }

        });
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, HHMemberProperty content);
        void onClickHH(int position, HHMemberProperty content);
    }
}
