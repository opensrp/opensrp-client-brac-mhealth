package org.smartregister.unicef.mis.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.Context;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;

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
    String baseEntityId;

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
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
                if(position==1){
                    isHighTemparature = true;
                    updateChildBodyInfo(isHighTemparature);
                }else if(position == 2){
                    isLowTemparature = true;
                    updateChildBodyInfo(isLowTemparature);
                }

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
            }
            else if(label.equalsIgnoreCase("তাপমাত্রা কম - ৩৬.৫o সে বা ৯৭.৭ ফারেনহাইট এর নিচে") && isLowTemparature){
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
            if(v instanceof MaterialEditText) {
                if (((MaterialEditText) v).getFloatingLabelText() != null && (((MaterialEditText) v).getFloatingLabelText().toString()).equals("কেএমসি স্বাস্থ্যকেন্দ্রে")) {
                    ((MaterialEditText) v).addTextChangedListener(textWatcherKMCHospital);
                }
            }
        }


    }
    boolean isKMCHospital = false;
    TextWatcher textWatcherKMCHospital = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            isKMCHospital = false;
            if(!StringUtils.isEmpty(s)){
                Log.v("KMC_SERVICE","isKMCHospital>>>"+s+":baseEntityId:"+baseEntityId);
                if(s.toString().equalsIgnoreCase("1")){
                    isKMCHospital = true;
                }
            }
           if(isKMCHospital){
               showReferToHospitalPopup();
           }

        }
    };
    private void showReferToHospitalPopup(){
        new AlertDialog.Builder(getActivity()).setMessage("নবজাতককে স্বাস্থ্যকেন্দ্রে কেএমসি সেবার ফলোআপ ক়রুন ")
                .setTitle("স্বাস্থ্যকেন্দ্রে কেএমসি সেবার ফলোআপ").setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        //SQLiteDatabase database = HnppApplication.getInstance().getRepository().getReadableDatabase();
                        //database.execSQL("UPDATE ec_child set kmc_status='"+ KMC_SERVICE_HOSPITAL+"',identified_date ='"+System.currentTimeMillis()+"' where base_entity_id='"+log.getBaseEntityId()+"'");

                        getActivity().finish();

                    }
                }).show();
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
