package org.smartregister.unicef.mis.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.smartregister.clientandeventmodel.Client;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.holder.SearchMigrationViewHolder;
import org.smartregister.unicef.mis.utils.HnppConstants;

import java.util.ArrayList;

public class SearchMigrationAdapter extends RecyclerView.Adapter<SearchMigrationViewHolder>{
    private ArrayList<Client> contentList;
    private Context context;
    private SearchMigrationAdapter.OnClickAdapter onClickAdapter;

    public SearchMigrationAdapter(Context context, SearchMigrationAdapter.OnClickAdapter onClickAdapter) {
        this.context = context;
        this.onClickAdapter = onClickAdapter;
        contentList = new ArrayList<>();
    }

    public void setData(ArrayList<Client> contentList) {
        this.contentList.clear();
        this.contentList.addAll(contentList);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public SearchMigrationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SearchMigrationViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_search_details_item, null));

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final SearchMigrationViewHolder viewHolder, int position) {
        Client content =  contentList.get(position);
        String fromSHR = content.getIdentifier("is_shr");
        if(!TextUtils.isEmpty(fromSHR)){
            viewHolder.background_row.setBackgroundColor(context.getResources().getColor(R.color.tika_card_yellow_bg));
        }else{
            viewHolder.background_row.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        }
            //final Migration content = contentList.get(position);
        viewHolder.textViewName.setText(content.getFirstName()+" "+content.getLastName());
        viewHolder.textViewAge.setText(context.getString(R.string.dob, HnppConstants.DDMMYY.format(content.getBirthdate())));
        viewHolder.textViewGender.setText(context.getString(R.string.gender_postfix,content.getGender()+""));
        viewHolder.textViewGender.append(" ");
        viewHolder.textViewGender.append(context.getString(R.string.village,content.getAddresses().get(0).getCityVillage()));
        viewHolder.imageViewAppIcon.setImageResource(R.drawable.rowavatar_member);

        viewHolder.itemView.setOnClickListener(v -> onClickAdapter.onItemClick(viewHolder,viewHolder.getAdapterPosition(), content));
        viewHolder.imageViewMenu.setOnClickListener(v -> onClickAdapter.onClick(viewHolder,viewHolder.getAdapterPosition(), content));
    }


    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public interface OnClickAdapter {
        void onClick(SearchMigrationViewHolder viewHolder, int adapterPosition, Client content);
        void onItemClick(SearchMigrationViewHolder viewHolder, int adapterPosition, Client content);
    }
}

