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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.whiteelephant.monthpicker.MonthPickerDialog;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSLocations;
import org.smartregister.brac.hnpp.location.SSModel;

import java.util.ArrayList;
import java.util.Calendar;

public class FilterDialog {
    public interface OnFilterDialogFilter{
         void onDialogPress(String ssName, String villageName, String cluster,int month, int year);
    }
    ArrayAdapter<String> villageSpinnerArrayAdapter;
    String mSelectedVillageName,mSelectedClasterName;
    ArrayAdapter<String> ssSpinnerArrayAdapter;
    ArrayList<SSModel> ssListModel  = new ArrayList<>();
    int day, month, year;
    TextView monthTV,yearTV;
    public  void showDialog(Context context,OnFilterDialogFilter onFilterDialogFilter){
        ArrayList<String> ssSpinnerArray = new ArrayList<>();
        ArrayList<String> skSpinnerArray = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        ArrayList<String> villageSpinnerArray = new ArrayList<>();
        ArrayList<String> clasterList = HnppConstants.getClasterSpinnerArray();
        ssSpinnerArray.add("সকল");
        skSpinnerArray.add("সকল");
        villageSpinnerArray.add("সকল");
        clasterList.add(0,"সকল");

        if(!HnppConstants.isPALogin()){
            ArrayList<SSModel> ssLocationForms = SSLocationHelper.getInstance().getSsModels();

            for (SSModel ssModel : ssLocationForms) {
                ssSpinnerArray.add(ssModel.username);
            }
        }


        ssSpinnerArrayAdapter = new ArrayAdapter<String>
                (context, android.R.layout.simple_spinner_item,
                        ssSpinnerArray){
            @Override
            public android.view.View getDropDownView(int position, @Nullable android.view.View convertView, @NonNull ViewGroup parent) {
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
            public android.view.View getDropDownView(int position, @Nullable android.view.View convertView, @NonNull ViewGroup parent) {
                convertView = super.getDropDownView(position, convertView,
                        parent);

                AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
                appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
                appCompatTextView.setHeight(100);

                return convertView;
            }
        };


        ArrayAdapter<String> clusterSpinnerArrayAdapter = new ArrayAdapter<String>
                (context, android.R.layout.simple_spinner_item,clasterList
                        ){
            @Override
            public android.view.View getDropDownView(int position, @Nullable android.view.View convertView, @NonNull ViewGroup parent) {
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
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(org.smartregister.family.R.color.customAppThemeBlue)));
        dialog.setContentView(R.layout.filter_options_dialog);
        Spinner sk_spinner = dialog.findViewById(R.id.sk_filter_spinner);
        Spinner ss_spinner = dialog.findViewById(R.id.ss_filter_spinner);
        Spinner village_spinner = dialog.findViewById(R.id.village_filter_spinner);
        Spinner cluster_spinner = dialog.findViewById(R.id.klaster_filter_spinner);
        ImageView clearBtn = dialog.findViewById(R.id.clear_filter);
        RelativeLayout monthPicker = dialog.findViewById(R.id.monthDatePicker);
        monthTV = dialog.findViewById(R.id.month_text);
        yearTV =  dialog.findViewById(R.id.year_text);
        monthTV.setText(HnppJsonFormUtils.monthBanglaStr[month-1]);
        yearTV.setText(year+"");
        village_spinner.setAdapter(villageSpinnerArrayAdapter);
        cluster_spinner.setAdapter(clusterSpinnerArrayAdapter);
        monthPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(month == -1) month = calendar.get(Calendar.MONTH)+1;
                if(year == -1) year = calendar.get(Calendar.YEAR);
                MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(context, new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) {
                        month = selectedMonth+1;
                        year = selectedYear;
                        monthTV.setText(HnppJsonFormUtils.monthBanglaStr[month-1]);
                        yearTV.setText(year+"");
                    }
                }, year, month-1);
                builder.setActivatedMonth(month-1)
                        .setMinYear(2010)
                        .setActivatedYear(year)
                        .setMaxYear(calendar.get(Calendar.YEAR))
                        .setTitle("মাস সিলেক্ট করুন")
                        .setOnMonthChangedListener(new MonthPickerDialog.OnMonthChangedListener() {
                            @Override
                            public void onMonthChanged(int selectedMonth) {

                            }
                        })
                        .setOnYearChangedListener(new MonthPickerDialog.OnYearChangedListener() {
                            @Override
                            public void onYearChanged(int selectedYear) {

                            }
                        })
                        .build()
                        .show();
            }
        });
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                monthTV.setText("");
                yearTV.setText("সকল");
                month = -1;
                year = -1;
            }
        });
        if(HnppConstants.isPALogin()){
            dialog.findViewById(R.id.sk_filter_view).setVisibility(View.VISIBLE);
            dialog.findViewById(R.id.claster_view).setVisibility(View.GONE);
            ArrayList<SSModel> skLocationForms = SSLocationHelper.getInstance().getAllSelectedSks();
            for (SSModel ssModel : skLocationForms) {
                skSpinnerArray.add(ssModel.skName+"("+ssModel.skUserName+")");
            }
            ArrayAdapter<String> sKSpinnerArrayAdapter = new ArrayAdapter<String>
                    (context, android.R.layout.simple_spinner_item,
                            skSpinnerArray){
                @Override
                public android.view.View getDropDownView(int position, @Nullable android.view.View convertView, @NonNull ViewGroup parent) {
                    convertView = super.getDropDownView(position, convertView,
                            parent);

                    AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
                    appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
                    appCompatTextView.setHeight(100);

                    return convertView;
                }
            };
            sk_spinner.setAdapter(sKSpinnerArrayAdapter);
            sk_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                    if (position != -1) {
                        if(position==0) return;
                        position = position -1;
                        SSModel ssModel = skLocationForms.get(position);
                        ArrayList<SSModel> ssLocationForms = SSLocationHelper.getInstance().getAllSelectedSS(ssModel.skUserName);
                        ssSpinnerArray.clear();
                        ssSpinnerArray.add("সকল");
                        ssListModel.clear();
                        for (SSModel ssModel1 : ssLocationForms) {
                            ssSpinnerArray.add(ssModel1.username);
                            ssListModel.add(ssModel1);
                        }
                        ssSpinnerArrayAdapter = new ArrayAdapter<String>
                                (context, android.R.layout.simple_spinner_item,
                                        ssSpinnerArray);
                        ss_spinner.setAdapter(ssSpinnerArrayAdapter);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        ss_spinner.setAdapter(ssSpinnerArrayAdapter);
        ss_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position != -1) {
                    if(position==0) return;
                    position = position -1;
                    villageSpinnerArray.clear();
                    villageSpinnerArray.add("সকল");

                    ArrayList<SSLocations> ssLocations;
                    if(HnppConstants.isPALogin()){
                        ssLocations = ssListModel.get(position).locations;

                    }else{
                        ssLocations = SSLocationHelper.getInstance().getSsModels().get(position).locations;

                    }
                    for (SSLocations ssLocations1 : ssLocations) {
                        villageSpinnerArray.add(ssLocations1.village.name);
                    }
                    villageSpinnerArrayAdapter = new ArrayAdapter<String>
                            (context, android.R.layout.simple_spinner_item,
                                    villageSpinnerArray);
                    village_spinner.setAdapter(villageSpinnerArrayAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        village_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position != -1) {
                    if(position ==0) mSelectedVillageName = "";
                    else mSelectedVillageName = villageSpinnerArray.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cluster_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position != -1) {
                    if(position == 0) mSelectedClasterName = "";
                    else mSelectedClasterName = HnppConstants.getClasterNames().get(HnppConstants.getClasterSpinnerArray().get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Button proceed = dialog.findViewById(R.id.filter_apply_button);
        proceed.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                onFilterDialogFilter.onDialogPress(ss_spinner.getSelectedItem().toString(),mSelectedVillageName,mSelectedClasterName,month,year);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
