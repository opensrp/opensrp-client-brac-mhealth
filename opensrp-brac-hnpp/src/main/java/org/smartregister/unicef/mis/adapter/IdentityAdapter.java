package org.smartregister.unicef.mis.adapter;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.utils.IdentityModel;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;

public class IdentityAdapter extends RecyclerView.Adapter<IdentityAdapter.IdentityViewHolder> {
    private ArrayList<IdentityModel> contentList;
    private OnClickAdapter onClickAdapter;

    public IdentityAdapter(OnClickAdapter onClickAdapter) {
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<IdentityModel> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @NonNull
    @Override
    public IdentityViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new IdentityViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_simprints_identify, null));

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final IdentityViewHolder viewHolder, int position) {
        final IdentityModel content = contentList.get(position);
        viewHolder.textViewName.setText(content.getName());
        viewHolder.textViewName.setText(viewHolder.textViewName.getText()+HnppApplication.appContext.getString(R.string.age_and_comma)+content.getAge());
        viewHolder.textViewGuid.setText(content.getId());
        viewHolder.textViewTier.setText(content.getTier());
        viewHolder.textViewFamilyName.setText(HnppApplication.appContext.getString(R.string.house_hold)+content.getFamilyHead());
        if(!TextUtils.isEmpty(content.getHusband())){
            viewHolder.textViewFamilyName.setText(viewHolder.textViewFamilyName.getText()+", "+HnppApplication.appContext.getString(R.string.husband)+content.getHusband());
        }
        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onClick(viewHolder.getAdapterPosition(), content));
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(int position, IdentityModel content);
    }
    public class IdentityViewHolder extends RecyclerView.ViewHolder {
        public CustomFontTextView textViewName;
        public CustomFontTextView textViewFamilyName;
        public CustomFontTextView textViewGuid;
        public TextView textViewTier;

        public IdentityViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.name_tv);
            textViewGuid = itemView.findViewById(R.id.gu_id);
            textViewTier = itemView.findViewById(R.id.tier_count);
            textViewFamilyName = itemView.findViewById(R.id.family_name_tv);
        }
    }
}