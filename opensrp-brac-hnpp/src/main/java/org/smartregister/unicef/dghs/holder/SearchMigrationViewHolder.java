package org.smartregister.unicef.dghs.holder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import org.smartregister.unicef.dghs.R;
import org.smartregister.view.customcontrols.CustomFontTextView;

public class SearchMigrationViewHolder extends RecyclerView.ViewHolder{

    public CustomFontTextView textViewName;
    public CustomFontTextView textViewAge;
    public CustomFontTextView textViewGender;
    public ImageView imageViewAppIcon;
    public ImageView imageViewMenu;

    public SearchMigrationViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewName = itemView.findViewById(R.id.name_TV);
        textViewAge = itemView.findViewById(R.id.age_TV);
        textViewGender = itemView.findViewById(R.id.gender_TV);
        imageViewAppIcon = itemView.findViewById(R.id.image);
        imageViewMenu = itemView.findViewById(R.id.migration_option_menu);
    }
}