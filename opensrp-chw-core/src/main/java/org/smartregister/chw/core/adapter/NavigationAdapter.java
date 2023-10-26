package org.smartregister.chw.core.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.smartregister.chw.core.R;
import org.smartregister.chw.core.listener.NavigationListener;
import org.smartregister.chw.core.model.NavigationOption;
import org.smartregister.chw.core.utils.CoreConstants;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.MyViewHolder> {

    private List<NavigationOption> navigationOptionList;
    private String selectedView = CoreConstants.DrawerMenu.ALL_FAMILIES;
    private NavigationListener onClickListener;
    private Context context;
    private Map<String, Class> registeredActivities;

    public NavigationAdapter(List<NavigationOption> navigationOptions, Activity context, Map<String, Class> registeredActivities) {
        this.navigationOptionList = navigationOptions;
        this.context = context;
        this.registeredActivities = registeredActivities;
    }
    public void setNavigationListener(NavigationListener navigationListener){

        this.onClickListener = navigationListener;
    }

    public String getSelectedView() {
        if (selectedView == null || selectedView.equals(""))
            setSelectedView(CoreConstants.DrawerMenu.ALL_FAMILIES);

        return selectedView;
    }

    public void setSelectedView(String selectedView) {
        this.selectedView = selectedView;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.navigation_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        NavigationOption model = navigationOptionList.get(position);
        holder.tvName.setText(context.getResources().getText(model.getTitleID()));
        if(model.getRegisterCount()>=0){
            holder.tvCount.setVisibility(View.VISIBLE);
        }else{
            holder.tvCount.setVisibility(View.INVISIBLE);
        }
        holder.tvCount.setText(String.format(Locale.getDefault(), "%d", model.getRegisterCount()));
        holder.ivIcon.setImageResource(model.getResourceID());

        holder.getView().setTag(model.getMenuTitle());


        if (selectedView != null && selectedView.equals(model.getMenuTitle())) {
            holder.tvCount.setTextColor(context.getResources().getColor(R.color.holo_blue));
            holder.tvName.setTextColor(context.getResources().getColor(R.color.holo_blue));
            holder.ivIcon.setImageResource(model.getResourceActiveID());
        } else {
            holder.tvCount.setTextColor(Color.WHITE);
            holder.tvName.setTextColor(Color.WHITE);
            holder.ivIcon.setImageResource(model.getResourceID());
        }
        if(model.isNeedToExpand()){
            holder.expandIcon.setVisibility(View.VISIBLE);
            holder.sTvName.setText(model.getNavigationSubModel().getSubTitle());
            holder.sTvCount.setText(model.getNavigationSubModel().getSubCount()+"");
            boolean expanded = model.isExpanded();

            holder.subLayout.setVisibility(expanded ? View.VISIBLE : View.GONE);
            holder.expandIcon.setImageResource(expanded? R.drawable.ic_less : R.drawable.ic_more);
            holder.expandIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean expanded = model.isExpanded();
                    model.setExpanded(!expanded);
                    notifyItemChanged(holder.getAdapterPosition());

                }
            });
            holder.subLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClickSubMenu(model.getNavigationSubModel().getType());
                }
            });

        }else{
            holder.expandIcon.setVisibility(View.INVISIBLE);
            holder.subLayout.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return navigationOptionList.size();
    }

    public Map<String, Class> getRegisteredActivities() {
        return registeredActivities;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName, tvCount,sTvName, sTvCount;
        public RelativeLayout subLayout;
        public ImageView ivIcon,expandIcon;

        private View myView;

        private MyViewHolder(View view) {
            super(view);
            subLayout = view.findViewById(R.id.sub_item_panel);
            tvName = view.findViewById(R.id.tvName);
            tvCount = view.findViewById(R.id.tvCount);
            ivIcon = view.findViewById(R.id.ivIcon);
            expandIcon = view.findViewById(R.id.expandBtn);
            sTvName = view.findViewById(R.id.stvName);
            sTvCount = view.findViewById(R.id.stvCount);
            myView = view;
            if (onClickListener != null) {
                myView.setOnClickListener(onClickListener);
            }
        }

        public View getView() {
            return myView;
        }
    }

}


