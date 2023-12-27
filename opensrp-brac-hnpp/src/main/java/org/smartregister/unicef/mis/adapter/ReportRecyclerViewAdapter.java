package org.smartregister.unicef.mis.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.smartregister.unicef.mis.domain.ReportData;
import org.smartregister.unicef.mis.R;
import org.smartregister.view.customcontrols.CustomFontTextView;
import org.smartregister.view.customcontrols.FontVariant;

import java.util.ArrayList;


public class ReportRecyclerViewAdapter extends RecyclerView.Adapter<ReportRecyclerViewAdapter.Holder> {
    Context context;
    ArrayList<ReportData> arrayList;

    public ReportRecyclerViewAdapter(Context context, ArrayList<ReportData> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Holder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_dashboard_item,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        ReportData reportData = arrayList.get(i);

        holder.imageView.setColorFilter(ContextCompat.getColor(context, reportData.getColor()));

        holder.textViewTitle.setText(reportData.getSubTitle());
        Log.v("DASHBOARD_DATA","reportData>>>"+reportData.getSubTitle()+":reportData.getTitle():"+reportData.getTitle());
        holder.textViewCount.setText(reportData.getTitle());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class Holder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public CustomFontTextView textViewTitle;
        public CustomFontTextView textViewCount;

        public Holder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            imageView.setImageResource(R.drawable.child_boy_infant);
            textViewTitle = itemView.findViewById(R.id.patient_name_age);
            textViewCount = itemView.findViewById(R.id.count_txt);
            textViewTitle.setFontVariant(FontVariant.REGULAR);
            textViewTitle.setTypeface(textViewTitle.getTypeface(), Typeface.NORMAL);
            textViewCount.setFontVariant(FontVariant.REGULAR);
            textViewCount.setTypeface(textViewCount.getTypeface(), Typeface.NORMAL);
        }
    }
}
