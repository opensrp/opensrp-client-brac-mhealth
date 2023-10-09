package org.smartregister.unicef.mis.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.holder.SearchViewHolder;
import org.smartregister.unicef.mis.location.SSModel;

import java.util.ArrayList;

public class SkSelectionAdapter extends RecyclerView.Adapter<SearchViewHolder> {
    private ArrayList<SSModel> contentList;
    private Context context;
    private OnClickAdapter onClickAdapter;
    private ArrayList<String> selectedId = new ArrayList<>();

    public SkSelectionAdapter(Context context, OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<SSModel> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }
    public void addItem(SSModel ssModel) {
        this.contentList.add(ssModel);
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SearchViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_forum_member, null));

    }

    @Override
    public void onBindViewHolder(@NonNull final SearchViewHolder viewHolder, int position) {
        final SSModel content = contentList.get(position);
        if(content!=null){
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setImageResource(R.drawable.ic_delete);
            viewHolder.textViewName.setText(context.getString(R.string.nurse_name)+content.username+context.getString(R.string.health_worker_name)+content.skName);
            viewHolder.textViewId.setText(context.getString(R.string.nurse_id)+content.ss_id);
            viewHolder.textViewAge.setVisibility(View.GONE);

            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    contentList.remove(position);
                    onClickAdapter.onRemove(position,content);
                    notifyDataSetChanged();

                }
            });
        }


    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onRemove(int position, SSModel content);
    }
}
