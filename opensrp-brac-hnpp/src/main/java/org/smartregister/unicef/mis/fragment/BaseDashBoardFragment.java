package org.smartregister.unicef.mis.fragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.activity.NewDashBoardActivity;
import org.smartregister.unicef.mis.adapter.DashBoardAdapter;
import org.smartregister.unicef.mis.contract.DashBoardContract;
import org.smartregister.unicef.mis.location.HALocationHelper;
import org.smartregister.unicef.mis.location.WardLocation;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppJsonFormUtils;

import java.util.ArrayList;
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
    private ImageView filterBtn, fromFilterBtn, toFilterBtn,fromDateFilterBtn, toDateFilterBtn;
    private  TextView monthTV,yearTV, fromMonthTV, toMonthTV, fromYearTV,toYearTV;
    protected TextView unsyncCountTv;
    protected Button syncBtn;
    protected LinearLayout monthView,dateView,fromDateView,toDateView,fromMonthView, toMonthView,ssView;
    protected RelativeLayout monthPicker, fromMonthPicker, toMonthPicker;
    abstract void filterData();
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
    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.base_fragment_dashboard,null);
        unsyncCountTv = view.findViewById(R.id.unsync_count);
        syncBtn = view.findViewById(R.id.sync_unsync_btn);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
        ssView = view.findViewById(R.id.ss_name_view);
        ssSpinner = view.findViewById(R.id.ss_filter_spinner);
        monthView = view.findViewById(R.id.month_view);
        fromMonthTV = view.findViewById(R.id.from_month_text);
        toMonthTV = view.findViewById(R.id.to_month_text);
        fromYearTV = view.findViewById(R.id.from_year_text);
        toYearTV = view.findViewById(R.id.to_year_text);
        monthTV = view.findViewById(R.id.month_text);
        yearTV  = view.findViewById(R.id.year_text);
        monthPicker = view.findViewById(R.id.monthDatePicker);
        fromMonthPicker = view.findViewById(R.id.fromMonthPicker);
        toMonthPicker = view.findViewById(R.id.toMonthPicker);
        dateView = view.findViewById(R.id.date_view);
        fromMonthView = view.findViewById(R.id.from_month_view);
        toMonthView = view.findViewById(R.id.to_month_view);
        fromDateView = view.findViewById(R.id.from_date_view);
        toDateView = view.findViewById(R.id.to_date_view);
        progressBar = view.findViewById(R.id.progress_bar);
        filterBtn = view.findViewById(R.id.filterBtn);
        fromFilterBtn = view.findViewById(R.id.from_clear_filter);
        toFilterBtn = view.findViewById(R.id.to_clear_filter);
        fromDateFilterBtn = view.findViewById(R.id.from_date_clear_filter);
        toDateFilterBtn = view.findViewById(R.id.to_date_clear_filter);
        dateBtn = view.findViewById(R.id.date_btn);
        fromDateBtn = view.findViewById(R.id.from_date_btn);
        toDateBtn = view.findViewById(R.id.to_date_btn);
        view.findViewById(R.id.clear_filter).setOnClickListener(this);
        dateBtn.setOnClickListener(this);
        fromDateBtn.setOnClickListener(this);
        toDateBtn.setOnClickListener(this);
        filterBtn.setOnClickListener(this);
        fromFilterBtn.setOnClickListener(this);
        toFilterBtn.setOnClickListener(this);
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
        fromDay = day;
        toDay = day;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initilizePresenter();
        updateFilter();
        updateFromFilter();
        updateToFilter();
        loadSSList();
        loadMonthList();
        loadFromMonthList();
        loadToMonthList();
        updateTitle();
        fetchData();
    }

    @SuppressLint("NonConstantResourceId")
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
                fromDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
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
                        filterData();
                    }
                },year,(month-1),day);
                fromDateDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
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
                        filterData();
                    }
                },year,(month-1),day);
                toDateDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                toDateDialog.show();
                break;
            case R.id.filterBtn:
                filterData();
                break;
            case R.id.clear_filter:
                monthTV.setText("");
                yearTV.setText(getString(R.string.all_text));
                month = -1;
                year = -1;
                filterData();
                break;
            case R.id.from_clear_filter:
                fromMonthTV.setText("");
                fromYearTV.setText(R.string.all);
                fromMonth = -1;
                fromYear = -1;
                filterData();
                break;
            case R.id.to_clear_filter:
                toMonthTV.setText("");
                toYearTV.setText(R.string.all);
                toMonth = -1;
                toYear = -1;
                filterData();
                break;
            case R.id.from_date_clear_filter:
                fromDateBtn.setText(R.string.all);
                fromMonth = -1;
                fromYear = -1;
                fromDay = -1;
                filterData();
                break;
            case R.id.to_date_clear_filter:
                toDateBtn.setText(R.string.all);
                toMonth = -1;
                toYear = -1;
                toDay = -1;
                filterData();
                break;
        }
    }
    private void loadSSList(){
        ArrayList<String> ssSpinnerArray = new ArrayList<>();
        ssSpinnerArray.add(getString(R.string.all_text));
        ArrayList<WardLocation> ssLocationForms = HALocationHelper.getInstance().getUnionList();
        for (WardLocation ssModel : ssLocationForms) {
            ssSpinnerArray.add(ssModel.ward.name);
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
                        .setTitle(getString(R.string.select_month))
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
    @SuppressLint("SetTextI18n")
    public void loadFromMonthList() {
        fromMonthPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fromMonth == -1) fromMonth = calendar.get(Calendar.MONTH)+1;
                if(fromYear == -1) fromYear = calendar.get(Calendar.YEAR);
                MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(getContext(), new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) {
                        fromMonth = selectedMonth+1;
                        fromYear = selectedYear;
                        updateFromDatePicker();
                        filterData();

                    }
                }, year, month-1);
                builder.setActivatedMonth(month-1)
                        .setMinYear(2010)
                        .setActivatedYear(year)
                        .setMaxYear(calendar.get(Calendar.YEAR))
                        .setTitle(getActivity().getString(R.string.select_month))
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
        fromMonth = month;
        fromYear = year;
        fromMonthTV.setText(HnppJsonFormUtils.monthBanglaStr[fromMonth-1]);
        fromYearTV.setText(fromYear+"");
    }
    public void loadToMonthList() {
        toMonthPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(toMonth == -1) toMonth = calendar.get(Calendar.MONTH)+1;
                if(toYear == -1) toYear = calendar.get(Calendar.YEAR);
                MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(getContext(), new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) {
                        toMonth = selectedMonth+1;
                        toYear = selectedYear;
                        updateToDatePicker();
                        filterData();
                    }
                }, year, month-1);
                builder.setActivatedMonth(month-1)
                        .setMinYear(2010)
                        .setActivatedYear(year)
                        .setMaxYear(calendar.get(Calendar.YEAR))
                        .setTitle(getActivity().getString(R.string.select_month))
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
        updateToPicker();
    }

    @SuppressLint("SetTextI18n")
    private void updateFromDatePicker() {
        if(fromMonth == -1){
            fromMonthTV.setText("");
            fromYearTV.setText(R.string.all);
        }else{
            Log.v("FROM_MONTH_PICKER","fromMonth:"+fromMonth+":fromYear:"+fromYear);
            int index = fromMonth-1;
            fromMonthTV.setText(HnppJsonFormUtils.monthBanglaStr[index]);
            fromYearTV.setText(fromYear+"");
        }

    }
    @SuppressLint("SetTextI18n")
    private void updateToDatePicker() {
        Log.v("TO_MONTH_PICKER","fromMonth:"+month+":fromYear:"+year);
        int index = toMonth-1;
        toMonthTV.setText(HnppJsonFormUtils.monthBanglaStr[index]);
        toYearTV.setText(toYear+"");
    }
    @SuppressLint("SetTextI18n")
    private void updateToPicker() {
        Log.v("TO_MONTH_PICKER","fromMonth:"+month+":fromYear:"+year);
        toMonth = month;
        toYear = year;
        toMonthTV.setText(HnppJsonFormUtils.monthBanglaStr[toMonth-1]);
        toYearTV.setText(toYear+"");
    }
    @SuppressLint("SetTextI18n")
    private void updateDatePicker() {
        Log.v("MONTH_PICKER","month:"+month+":year:"+year);
        monthTV.setText(HnppJsonFormUtils.monthBanglaStr[month-1]);
        yearTV.setText(year+"");
    }
//    public void refreshData(Runnable runnable){
//        this.runnable = runnable;
//        updateFilter();
//        updateFromFilter();
//        updateToFilter();
//
//    }

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
    @SuppressLint("SetTextI18n")
    private void updateFilter() {
        if (TextUtils.isEmpty(dateBtn.getText().toString())) {
            toDate = currentDate;
            dateBtn.setText(toDate+"");
        }
    }
    @SuppressLint("SetTextI18n")
    private void updateFromFilter() {
        if (TextUtils.isEmpty(fromDateBtn.getText().toString())) {
            fromDay = day;
            fromDate = currentDate;
            fromDateBtn.setText(fromDate+"");
        }
    }
    @SuppressLint("SetTextI18n")
    private void updateToFilter() {
        if (TextUtils.isEmpty(toDateBtn.getText().toString())) {
            toDate = currentDate;
            toDay = day;
            toDateBtn.setText(toDate+"");
        }
    }


}
