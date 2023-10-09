package org.smartregister.unicef.mis.holder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import org.smartregister.unicef.mis.R;
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
