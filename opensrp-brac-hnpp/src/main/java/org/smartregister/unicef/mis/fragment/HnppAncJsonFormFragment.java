package org.smartregister.unicef.mis.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;
import org.smartregister.Context;
import org.smartregister.unicef.mis.HnppApplication;

import java.util.ArrayList;
import java.util.Collection;

public class HnppAncJsonFormFragment extends JsonWizardFormFragment {
    ArrayList<ViewObject> viewList = new ArrayList<>();
    public static HnppAncJsonFormFragment getFormFragment(String stepName, boolean isNeedToShowSaveHeader) {
        HnppAncJsonFormFragment jsonFormFragment = new HnppAncJsonFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString("stepName", stepName);
        bundle.putBoolean("saveButton", isNeedToShowSaveHeader);
        jsonFormFragment.setArguments(bundle);
        return jsonFormFragment;
    }
    public Context context() {
        return HnppApplication.getInstance().getContext();
    }
    boolean isHighTemparature,isLowTemparature = false,breathing = false;
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        super.onItemSelected(parent, view, position, id);
        if(parent instanceof MaterialSpinner){
            if (((MaterialSpinner) parent).getHint() != null && (((MaterialSpinner) parent).getHint().toString()).equals("শরীরের তাপমাত্রা")) {
                isHighTemparature = position == 1;
                updateChildBodyInfo(isHighTemparature);

            }else if (((MaterialSpinner) parent).getHint() != null && (((MaterialSpinner) parent).getHint().toString()).equals("শরীরের তাপমাত্রা")) {
                isLowTemparature = position == 2;
                updateChildBodyInfo(isLowTemparature);

            }else if (((MaterialSpinner) parent).getHint() != null && (((MaterialSpinner) parent).getHint().toString()).equals("শ্বাসের হার (প্রতি মিনিটে)")) {
                breathing = position == 1;
                updateChildBodyInfo(breathing);

            }
        }
    }
    public void updateChildBodyInfo(boolean isChecked) {
        for (int i = 0; i < viewList.size(); i++) {
            CompoundButton buttonView = viewList.get(i).view;
            String label = viewList.get(i).label;
            if (label.equalsIgnoreCase("তাপমাত্রা বেশি/জ্বর -৩৭.৫oসে বা ৯৯.৫ ফারেনহাইট এর উপরে") && isHighTemparature){
                buttonView.setChecked(isChecked);
                break;
            }else if(label.equalsIgnoreCase("তাপমাত্রা ক়ম - ৩৬.৫o সে বা ৯৭.৭ ফারেনহাইট এর নিচে") && isLowTemparature){
                buttonView.setChecked(isChecked);
                break;
            }
            else if(label.equalsIgnoreCase("স্বাভাবিক এর থেকে দ্রুত নিঃশ্বাস নিচ্ছে (প্রতি মিনিটে ৬০ অথবা তার অধিক)") && breathing){
                buttonView.setChecked(isChecked);
                break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Collection<View> formDataViews = getJsonApi().getFormDataViews();
        for (View v : formDataViews) {
            if (v instanceof LinearLayout) {
                LinearLayout viewGroup = (LinearLayout) v;
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childView = viewGroup.getChildAt(i);
                    if (childView instanceof LinearLayout) {
                        LinearLayout childGroup = (LinearLayout) childView;
                        int childGroupCount = childGroup.getChildCount();
                        for (int k = 0; k < childGroupCount; k++) {
                            View checkboxView = childGroup.getChildAt(k);
                            if (checkboxView instanceof AppCompatCheckBox) {
                                viewList.add(new ViewObject(((AppCompatCheckBox) checkboxView).getText().toString(), (AppCompatCheckBox) checkboxView));
                            }
                        }

                    }
                }
            }
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
       boolean  isNeedToShowSaveHeader = getArguments().getBoolean("saveButton");
        menu.findItem(com.vijay.jsonwizard.R.id.action_save).setVisible(false);

    }
    class ViewObject {
        String label;
        AppCompatCheckBox view;
        boolean value = false;
        ViewObject(String label, AppCompatCheckBox view) {
            this.label = label;
            this.view = view;
        }
        public boolean getValue(){
            if(view!=null)return view.isChecked()||value;
            else return value;
        }
    }
}
