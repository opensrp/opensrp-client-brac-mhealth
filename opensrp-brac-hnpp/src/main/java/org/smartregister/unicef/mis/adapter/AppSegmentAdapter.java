package org.smartregister.unicef.mis.adapter;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.smartregister.unicef.mis.R;

import segmented_control.widget.custom.android.com.segmentedcontrol.item_row_column.SegmentAdapter;
import segmented_control.widget.custom.android.com.segmentedcontrol.item_row_column.SegmentViewHolder;

public class AppSegmentAdapter extends SegmentAdapter<String, AppSegmentAdapter.AppSegmentViewHolder>{


    @SuppressLint("InflateParams")
    @Override
    protected AppSegmentViewHolder onCreateViewHolder(LayoutInflater layoutInflater, ViewGroup viewGroup, int i) {
        return new AppSegmentViewHolder(layoutInflater.inflate(R.layout.item_segment, null));
    }


    public class AppSegmentViewHolder extends SegmentViewHolder<String> {
        TextView textView;

        public AppSegmentViewHolder(@NonNull View sectionView) {
            super(sectionView);
            textView = (TextView) sectionView.findViewById(R.id.text_view);
        }

        @Override
        protected void onSegmentBind(String segmentData) {
            textView.setText(segmentData);
        }
    }
}
