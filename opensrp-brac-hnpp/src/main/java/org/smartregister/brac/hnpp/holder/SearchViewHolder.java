package org.smartregister.brac.hnpp.holder;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.view.customcontrols.CustomFontTextView;

/**
 * Created by wizard on 06/08/19.
 */
public class SearchViewHolder extends RecyclerView.ViewHolder {
    public ImageView checkBox;
    public ImageView imageView;
    public CustomFontTextView textViewName;
    public CustomFontTextView textViewId;
    public CustomFontTextView textViewAge;

    public SearchViewHolder(@NonNull View itemView) {
        super(itemView);
        checkBox = itemView.findViewById(R.id.checked_image);
        imageView = itemView.findViewById(R.id.image_view);
        textViewName = itemView.findViewById(R.id.name_tv);
        textViewId = itemView.findViewById(R.id.id_tv);
        textViewAge = itemView.findViewById(R.id.age_tv);
    }
}
