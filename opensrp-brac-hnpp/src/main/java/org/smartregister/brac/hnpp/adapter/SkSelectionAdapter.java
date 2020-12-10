package org.smartregister.brac.hnpp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.holder.SearchViewHolder;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.model.HHMemberProperty;
import org.smartregister.brac.hnpp.utils.HnppConstants;

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

        viewHolder.checkBox.setVisibility(View.VISIBLE);
        viewHolder.checkBox.setImageResource(R.drawable.ic_delete);
        viewHolder.textViewName.setText("স্বাস্থ্য সেবিকার নাম :"+content.username+" স্বাস্থ্য কর্মীর নাম : "+content.skName);
        viewHolder.textViewId.setText("স্বাস্থ্য সেবিকার আইডি :"+content.ss_id);
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


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onRemove(int position, SSModel content);
    }
}
