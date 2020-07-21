package org.smartregister.brac.hnpp.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSLocations;
import org.smartregister.brac.hnpp.location.SSModel;

import java.util.ArrayList;

public class FilterDialog {
    public interface OnFilterDialogFilter{
         void onDialogPress(String ssName, String villageName, String cluster, String gender);
    }
    ArrayAdapter<String> villageSpinnerArrayAdapter;
    String mSelectedVillageName,mSelectedClasterName;

    public  void showDialog(Context context,OnFilterDialogFilter onFilterDialogFilter, boolean isGenderEnable){
        isGenderEnable = false;
        ArrayList<String> ssSpinnerArray = new ArrayList<>();

        ArrayList<String> villageSpinnerArray = new ArrayList<>();

        ArrayList<SSModel> ssLocationForms = SSLocationHelper.getInstance().getSsModels();
        for (SSModel ssModel : ssLocationForms) {
            ssSpinnerArray.add(ssModel.username);
        }

        ArrayAdapter<String> ssSpinnerArrayAdapter = new ArrayAdapter<String>
                (context, android.R.layout.simple_spinner_item,
                        ssSpinnerArray){
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                convertView = super.getDropDownView(position, convertView,
                        parent);

                AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
                appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
                appCompatTextView.setHeight(100);

                return convertView;
            }
        };

         villageSpinnerArrayAdapter = new ArrayAdapter<String>
                (context, android.R.layout.simple_spinner_item,
                        villageSpinnerArray){
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                convertView = super.getDropDownView(position, convertView,
                        parent);
                AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
                appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
                appCompatTextView.setHeight(100);
                return convertView;
            }
        };

        ArrayAdapter<String> clusterSpinnerArrayAdapter = new ArrayAdapter<String>
                (context, android.R.layout.simple_spinner_item,
                        HnppConstants.getClasterSpinnerArray()){
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                convertView = super.getDropDownView(position, convertView,
                        parent);
                AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
                appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
                appCompatTextView.setHeight(100);

                return convertView;
            }
        };

        Dialog dialog = new Dialog(context, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.forum_filter_options_dialog);
        Spinner gender_spinner = dialog.findViewById(R.id.gender_filter_spinner);
        if(isGenderEnable){
            gender_spinner.setVisibility(View.VISIBLE);
            dialog.findViewById(R.id.gender_tv).setVisibility(View.VISIBLE);
        }
        Spinner ss_spinner = dialog.findViewById(R.id.ss_filter_spinner);
        Spinner village_spinner = dialog.findViewById(R.id.village_filter_spinner);
        Spinner cluster_spinner = dialog.findViewById(R.id.klaster_filter_spinner);
        village_spinner.setAdapter(villageSpinnerArrayAdapter);
        cluster_spinner.setAdapter(clusterSpinnerArrayAdapter);
        ss_spinner.setAdapter(ssSpinnerArrayAdapter);
        ss_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != -1) {
                    villageSpinnerArray.clear();
                    ArrayList<SSLocations> ssLocations = SSLocationHelper.getInstance().getSsModels().get(position).locations;
                    for (SSLocations ssLocations1 : ssLocations) {
                        villageSpinnerArray.add(ssLocations1.village.name.trim());
                    }
                    villageSpinnerArrayAdapter = new ArrayAdapter<String>
                            (context, android.R.layout.simple_spinner_item,
                                    villageSpinnerArray);
                    village_spinner.setAdapter(villageSpinnerArrayAdapter);
                    //villageSpinnerArrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        village_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != -1) {
                    mSelectedVillageName = villageSpinnerArray.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cluster_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != -1) {
                    mSelectedClasterName = HnppConstants.getClasterNames().get(HnppConstants.getClasterSpinnerArray().get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Button proceed = dialog.findViewById(R.id.filter_apply_button);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFilterDialogFilter.onDialogPress(ss_spinner.getSelectedItem().toString(),mSelectedVillageName,mSelectedClasterName,gender_spinner.getVisibility()==View.VISIBLE?gender_spinner.getSelectedItem().toString():"");
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
