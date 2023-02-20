package org.smartregister.chw.core.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.smartregister.chw.core.R;


public class RegisterViewHolder extends RecyclerView.ViewHolder {
    public TextView textViewParentName;
    public TextView textViewChildName;
    public TextView textViewAddressGender;
    public TextView textViewWeight;
    public TextView textViewLastVaccine;
    public Button dueButton;
    public View dueButtonLayout;
    public View childColumn;
    public ImageView goToProfileImage;
    public ImageView profileImage;
    public ImageView childStatusImage;
    public View goToProfileLayout;
    public TextView riskView;

    public RegisterViewHolder(View itemView) {
        super(itemView);

        textViewParentName = itemView.findViewById(R.id.textview_parent_name);
        textViewChildName = itemView.findViewById(R.id.text_view_child_name);
        textViewAddressGender = itemView.findViewById(R.id.text_view_address_gender);
        textViewWeight = itemView.findViewById(R.id.weight_tv);
        textViewLastVaccine = itemView.findViewById(R.id.last_vaccine_tv);
        dueButton = itemView.findViewById(R.id.due_button);
        dueButtonLayout = itemView.findViewById(R.id.due_button_wrapper);
        goToProfileImage = itemView.findViewById(R.id.go_to_profile_image_view);
        goToProfileLayout = itemView.findViewById(R.id.go_to_profile_layout);
        profileImage = itemView.findViewById(R.id.profile_img);
        childStatusImage = itemView.findViewById(R.id.childStatusImg);
        profileImage.setVisibility(View.GONE);
        childColumn = itemView.findViewById(R.id.child_column);
        riskView = itemView.findViewById(R.id.risk_view);
    }
}
