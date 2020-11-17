package org.smartregister.brac.hnpp.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.NewDashBoardActivity;
import org.smartregister.brac.hnpp.adapter.DashBoardAdapter;
import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public abstract class BaseDashBoardFragment extends Fragment implements View.OnClickListener, DashBoardContract.View {

    private Button dateBtn;
    protected RecyclerView recyclerView;
    private int date, month, year;
    private String fromDate, toDate, currentDate;
    private Runnable runnable;
    protected Spinner ssSpinner,monthSpinner;
    protected ProgressBar progressBar;
    protected String ssName;
    private ImageView filterBtn;
    abstract void filterData();
    abstract void updateTitle();
    abstract void fetchData();
    abstract void initilizePresenter();
    protected NewDashBoardActivity mActivity;
    protected DashBoardAdapter adapter;

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
        monthSpinner = view.findViewById(R.id.month_filter_spinner);
        progressBar = view.findViewById(R.id.progress_bar);
        filterBtn = view.findViewById(R.id.filterBtn);
        dateBtn = view.findViewById(R.id.date_btn);
        dateBtn.setOnClickListener(this);
        filterBtn.setOnClickListener(this);
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        date = calendar.get(Calendar.DAY_OF_MONTH);
        currentDate   = year+"-"+addZeroForMonth(month+"")+"-"+addZeroForMonth(date+"");
        fromDate = currentDate;
        toDate = currentDate;
        dateBtn.setText(currentDate);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initilizePresenter();
        updateFilter();
        loadSSList();
        loadMonthList();
        fetchData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.date_btn:
                DatePickerDialog fromDialog = new DatePickerDialog(getActivity(), R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        fromDate = year + "-" + addZeroForMonth((month+1)+"")+"-"+addZeroForMonth(dayOfMonth+"");

                        dateBtn.setText(fromDate);
                        updateFilter();
                    }
                },year,month,date);
                fromDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                fromDialog.show();
                break;
            case R.id.filterBtn:
                filterData();
                break;
        }
    }
    private void loadSSList(){
        ArrayList<String> ssSpinnerArray = new ArrayList<>();
        ssSpinnerArray.add("সিলেক্ট করুন");
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
                appCompatTextView.setHeight(100);

                return convertView;
            }
        };
        ssSpinner.setAdapter(ssSpinnerArrayAdapter);
        ssSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position != -1) {
                    if(position == 0) ssName = "";
                    else ssName = ssSpinner.getSelectedItem().toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void loadMonthList(){
        ArrayList<String> monthSpinnerArray = new ArrayList<>();
        monthSpinnerArray.add("সিলেক্ট করুন");
        monthSpinnerArray.addAll(Arrays.asList(HnppJsonFormUtils.monthBanglaStr));
        ArrayAdapter<String> monthSpinnerArrayAdapter = new ArrayAdapter<String>
                (getActivity(), android.R.layout.simple_spinner_item,
                        monthSpinnerArray){
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
        monthSpinner.setAdapter(monthSpinnerArrayAdapter);
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position != -1) {
                    if(position == 0) month = -1;
                    else month = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    public void refreshData(Runnable runnable){
        this.runnable = runnable;
        updateFilter();
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

    public String addZeroForMonth(String month){
        if(month.length()==1) return "0"+month;
        return month;
    }
}
