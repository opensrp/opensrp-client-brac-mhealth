package org.smartregister.brac.hnpp.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.whiteelephant.monthpicker.MonthPickerDialog;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.NewDashBoardActivity;
import org.smartregister.brac.hnpp.adapter.DashBoardAdapter;
import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public abstract class BaseDashBoardFragment extends Fragment implements View.OnClickListener, DashBoardContract.View {

    private Button dateBtn,fromDateBtn,toDateBtn;
    protected RecyclerView recyclerView;
    protected int day, month, year, fromDay, fromMonth, fromYear, toDay, toMonth, toYear;
    private String fromDate, toDate, currentDate;
    private Runnable runnable;
    protected Spinner ssSpinner;
    protected ProgressBar progressBar;
    protected String ssName;
    private ImageView filterBtn;
    private  TextView monthTV,yearTV;
    protected LinearLayout monthView,dateView,fromDateView,toDateView;
    protected RelativeLayout monthPicker;
    abstract void filterData();
    abstract void filterByFromToDate();
    abstract void updateTitle();
    abstract void fetchData();
    abstract void initilizePresenter();
    protected NewDashBoardActivity mActivity;
    protected DashBoardAdapter adapter;
    Calendar calendar;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (NewDashBoardActivity)context;
    }
    public void updateTitle(String title){
        if(mActivity != null) mActivity.updateTitle(title);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.base_fragment_dashboard,null);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
        ssSpinner = view.findViewById(R.id.ss_filter_spinner);
        monthView = view.findViewById(R.id.month_view);
        monthTV = view.findViewById(R.id.month_text);
        yearTV  = view.findViewById(R.id.year_text);
        monthPicker = view.findViewById(R.id.monthDatePicker);
        dateView = view.findViewById(R.id.date_view);
        fromDateView = view.findViewById(R.id.from_date_view);
        toDateView = view.findViewById(R.id.to_date_view);
        progressBar = view.findViewById(R.id.progress_bar);
        filterBtn = view.findViewById(R.id.filterBtn);
        dateBtn = view.findViewById(R.id.date_btn);
        fromDateBtn = view.findViewById(R.id.from_date_btn);
        toDateBtn = view.findViewById(R.id.to_date_btn);
        view.findViewById(R.id.clear_filter).setOnClickListener(this);
        dateBtn.setOnClickListener(this);
        fromDateBtn.setOnClickListener(this);
        toDateBtn.setOnClickListener(this);
        filterBtn.setOnClickListener(this);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        currentDate   = year+"-"+ HnppConstants.addZeroForMonth(month+"")+"-"+HnppConstants.addZeroForMonth(day+"");
        fromDate = currentDate;
        toDate = currentDate;
        dateBtn.setText(currentDate);
        toDateBtn.setText(currentDate);
        fromDateBtn.setText(currentDate);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initilizePresenter();
        updateFilter();
        loadSSList();
        loadMonthList();
        updateTitle();
        fetchData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.date_btn:
                if(month == -1) month = calendar.get(Calendar.MONTH)+1;
                if(year == -1) year = calendar.get(Calendar.YEAR);

                DatePickerDialog fromDialog = new DatePickerDialog(getActivity(), R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int yr, int mnt, int dayOfMonth) {

                        day = dayOfMonth;
                        month = mnt +1;
                        year = yr;

                        fromDate = year + "-" + HnppConstants.addZeroForMonth((mnt+1)+"")+"-"+HnppConstants.addZeroForMonth(dayOfMonth+"");

                        dateBtn.setText(fromDate);
                        updateFilter();
                        filterData();
                    }
                },year,(month-1),day);
                //fromDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                fromDialog.show();
                break;
            case R.id.from_date_btn:
                if(fromMonth == -1) fromMonth = calendar.get(Calendar.MONTH)+1;
                if(fromYear == -1) fromYear = calendar.get(Calendar.YEAR);

                DatePickerDialog fromDateDialog = new DatePickerDialog(getActivity(), R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int yr, int mnt, int dayOfMonth) {

                        fromDay = dayOfMonth;
                        fromMonth = mnt +1;
                        fromYear = yr;

                        fromDate = fromYear + "-" + HnppConstants.addZeroForMonth((mnt+1)+"")+"-"+HnppConstants.addZeroForMonth(dayOfMonth+"");

                        fromDateBtn.setText(fromDate);
                        updateFromFilter();
                    }
                },year,(month-1),day);
                //fromDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                fromDateDialog.show();
                break;
            case R.id.to_date_btn:
                if(toMonth == -1) toMonth = calendar.get(Calendar.MONTH)+1;
                if(toYear == -1) toYear = calendar.get(Calendar.YEAR);

                DatePickerDialog toDateDialog = new DatePickerDialog(getActivity(), R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int yr, int mnt, int dayOfMonth) {

                        toDay = dayOfMonth;
                        toMonth = mnt +1;
                        toYear = yr;

                        toDate = toYear + "-" + HnppConstants.addZeroForMonth((mnt+1)+"")+"-"+HnppConstants.addZeroForMonth(dayOfMonth+"");

                        toDateBtn.setText(toDate);
                        updateToFilter();
                        filterByFromToDate();
                    }
                },year,(month-1),day);
                //fromDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                toDateDialog.show();
                break;
            case R.id.filterBtn:
                filterData();
                break;
            case R.id.clear_filter:
                monthTV.setText("");
                yearTV.setText("সকল");
                month = -1;
                year = -1;
                filterData();
                break;
        }
    }
    private void loadSSList(){
        ArrayList<String> ssSpinnerArray = new ArrayList<>();
        ssSpinnerArray.add("সকল");
        ArrayList<SSModel> ssLocationForms = SSLocationHelper.getInstance().getSsModels();
        for (SSModel ssModel : ssLocationForms) {
            ssSpinnerArray.add(ssModel.username);
        }
        ArrayAdapter<String> ssSpinnerArrayAdapter = new ArrayAdapter<String>
                (getActivity(), android.R.layout.simple_spinner_item,
                        ssSpinnerArray){
            @Override
            public android.view.View getDropDownView(int position, @Nullable android.view.View convertView, @NonNull ViewGroup parent) {
                convertView = super.getDropDownView(position, convertView,
                        parent);

                AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
                appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
                appCompatTextView.setHeight(50);
                //appCompatTextView.setTextColor(Color.WHITE);
                return convertView;
            }
        };
        ssSpinner.setAdapter(ssSpinnerArrayAdapter);
        ssSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                if (position != -1) {
                    if(position == 0) ssName = "";
                    else ssName = ssSpinner.getSelectedItem().toString();
                    filterData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void loadMonthList(){
        monthPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(month == -1) month = calendar.get(Calendar.MONTH)+1;
                if(year == -1) year = calendar.get(Calendar.YEAR);
                MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(getContext(), new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) {
                        month = selectedMonth+1;
                        year = selectedYear;
                        updateDatePicker();
                        filterData();
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
        updateDatePicker();
//        ArrayList<String> monthSpinnerArray = new ArrayList<>();
//        monthSpinnerArray.add("সিলেক্ট করুন");
//        monthSpinnerArray.addAll(Arrays.asList(HnppJsonFormUtils.monthBanglaStr));
//        ArrayAdapter<String> monthSpinnerArrayAdapter = new ArrayAdapter<String>
//                (getActivity(), android.R.layout.simple_spinner_item,
//                        monthSpinnerArray){
//            @Override
//            public android.view.View getDropDownView(int position, @Nullable android.view.View convertView, @NonNull ViewGroup parent) {
//                convertView = super.getDropDownView(position, convertView,
//                        parent);
//
//                AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
//                appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
//                appCompatTextView.setHeight(50);
//
//                return convertView;
//            }
//        };
//        monthSpinner.setAdapter(monthSpinnerArrayAdapter);
//        monthSpinner.setSelection(month);
//        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
//                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
//                if (position != -1) {
//                    if(position == 0) month = -1;
//                    else month = position;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
    }
    private void updateDatePicker() {
        Log.v("MONTH_PICKER","month:"+month+":year:"+year);
        monthTV.setText(HnppJsonFormUtils.monthBanglaStr[month-1]);
        yearTV.setText(year+"");
    }
    public void refreshData(Runnable runnable){
        this.runnable = runnable;
        updateFilter();
        updateFromFilter();
        updateToFilter();

    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);

    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);

    }

    @Nullable
    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void updateAdapter() {
        if(runnable != null) runnable.run();



    }
    private void updateFilter() {
        if (TextUtils.isEmpty(dateBtn.getText().toString())) {
            toDate = currentDate;
            dateBtn.setText(toDate+"");
        }
    }
    private void updateFromFilter() {
        if (TextUtils.isEmpty(fromDateBtn.getText().toString())) {
            toDate = currentDate;
            fromDateBtn.setText(toDate+"");
        }
    }
    private void updateToFilter() {
        if (TextUtils.isEmpty(toDateBtn.getText().toString())) {
            toDate = currentDate;
            toDateBtn.setText(toDate+"");
        }
    }


}
