package org.smartregister.unicef.dghs.holder;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.smartregister.unicef.dghs.R;
import org.smartregister.view.customcontrols.CustomFontTextView;
import org.smartregister.view.customcontrols.FontVariant;

/**
 * Created by wizard on 06/08/19.
 */
public class DashBoardViewHolder extends RecyclerView.ViewHolder {
    public ImageView imageView;
    public CustomFontTextView textViewTitle;
    public CustomFontTextView textViewCount;

    public DashBoardViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.image_view);
        textViewTitle = itemView.findViewById(R.id.patient_name_age);
        textViewCount = itemView.findViewById(R.id.count_txt);
        textViewTitle.setFontVariant(FontVariant.REGULAR);
        textViewTitle.setTypeface(textViewTitle.getTypeface(), Typeface.NORMAL);
        textViewCount.setFontVariant(FontVariant.REGULAR);
        textViewCount.setTypeface(textViewCount.getTypeface(), Typeface.NORMAL);
    }
}
