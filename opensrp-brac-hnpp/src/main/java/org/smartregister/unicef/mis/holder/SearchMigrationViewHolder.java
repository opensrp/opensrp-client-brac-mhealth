package org.smartregister.unicef.mis.holder;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import org.smartregister.unicef.mis.R;
import org.smartregister.view.customcontrols.CustomFontTextView;

public class SearchMigrationViewHolder extends RecyclerView.ViewHolder{

    public CustomFontTextView textViewName;
    public CustomFontTextView textViewAge;
    public CustomFontTextView textViewGender;
    public ImageView imageViewAppIcon;
    public ImageView imageViewMenu;
    public CardView background_row;

    public SearchMigrationViewHolder(@NonNull View itemView) {
        super(itemView);
        background_row = itemView.findViewById(R.id.background_row);
        textViewName = itemView.findViewById(R.id.name_TV);
        textViewAge = itemView.findViewById(R.id.age_TV);
        textViewGender = itemView.findViewById(R.id.gender_TV);
        imageViewAppIcon = itemView.findViewById(R.id.image);
        imageViewMenu = itemView.findViewById(R.id.migration_option_menu);
    }
}