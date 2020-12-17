package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.SearchMigrationViewHolder;
import org.smartregister.brac.hnpp.model.Migration;
import org.smartregister.family.util.Utils;

import java.util.ArrayList;

public class SearchMigrationAdapter extends RecyclerView.Adapter<SearchMigrationViewHolder>{
    private ArrayList<Migration> contentList;
    private Context context;
    private SearchMigrationAdapter.OnClickAdapter onClickAdapter;

    public SearchMigrationAdapter(Context context, SearchMigrationAdapter.OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<Migration> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public SearchMigrationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SearchMigrationViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_search_details_item, null));

    }

    @Override
    public void onBindViewHolder(@NonNull final SearchMigrationViewHolder viewHolder, int position) {
        Migration content =  contentList.get(position);
        if(content!=null && content.attributes!=null && content.attributes.SS_Name!=null){
            viewHolder.textViewName.setText(context.getString(R.string.house_hold_head_name,content.firstName));
            viewHolder.textViewAge.setText(context.getString(R.string.ss_name,content.attributes.SS_Name));
            viewHolder.textViewGender.setText(context.getString(R.string.member_count,content.attributes.Number_of_HH_Member));
        }else {
            //final Migration content = contentList.get(position);
            viewHolder.textViewName.setText(content.firstName+"");
            viewHolder.textViewAge.setText(context.getString(R.string.age, Utils.getDuration(content.birthdate+"")));
            viewHolder.textViewGender.setText(context.getString(R.string.gender_postfix,content.gender+""));
        }

        viewHolder.imageViewAppIcon.setImageResource(R.drawable.rowavatar_member);

        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onItemClick(viewHolder,viewHolder.getAdapterPosition(), content));
        viewHolder.imageViewMenu.setOnClickListener(v -> onClickAdapter.onClick(viewHolder,viewHolder.getAdapterPosition(), content));
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(SearchMigrationViewHolder viewHolder, int adapterPosition, Migration content);
        void onItemClick(SearchMigrationViewHolder viewHolder, int adapterPosition, Migration content);
    }
}

