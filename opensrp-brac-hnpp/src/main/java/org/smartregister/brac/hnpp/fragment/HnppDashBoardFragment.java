package org.smartregister.brac.hnpp.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import org.joda.time.LocalDate;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.DashBoardAdapter;
import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.presenter.DashBoardPresenter;

import java.util.Calendar;

public class HnppDashBoardFragment extends Fragment implements View.OnClickListener, DashBoardContract.View {

    private Button textViewFromDate,textViewToDate;
    private RecyclerView recyclerView;
    private int date, month, year;
    private DashBoardPresenter presenter;
    private String fromDate, toDate, currentDate;
    private DashBoardAdapter adapter;
    private Runnable runnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard,null);
        recyclerView = view.findViewById(R.id.recycler_view);
        textViewFromDate = view.findViewById(R.id.from);
        textViewToDate = view.findViewById(R.id.to);
        textViewFromDate.setOnClickListener(this);
        textViewToDate.setOnClickListener(this);
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        date = calendar.get(Calendar.DAY_OF_MONTH);
        currentDate   = year+"-"+addZeroForMonth(month+"")+"-"+addZeroForMonth(date+"");
        fromDate = currentDate;
        toDate = currentDate;
        textViewFromDate.setText(currentDate);
        textViewToDate.setText(currentDate);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new DashBoardPresenter(this);
        updateFilter();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.from:
                DatePickerDialog fromDialog = new DatePickerDialog(getActivity(), R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        fromDate = year + "-" + addZeroForMonth((month+1)+"")+"-"+addZeroForMonth(dayOfMonth+"");

                        textViewFromDate.setText(fromDate);
                        updateFilter();
                    }
                },year,month,date);
                fromDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                fromDialog.show();
                break;
            case R.id.to:
                DatePickerDialog toDialog = new DatePickerDialog(getActivity(), R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        toDate = year + "-" + addZeroForMonth((month+1)+"")+"-"+addZeroForMonth(dayOfMonth+"");

                        textViewToDate.setText(toDate);
                        updateFilter();
                    }
                },year,month,date);
                toDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                toDialog.show();
                break;
        }
    }
    public void refreshData(Runnable runnable){
        this.runnable = runnable;
        updateFilter();
    }

    @Override
    public void showProgressBar() {

    }

    @Override
    public void hideProgressBar() {

    }

    @Nullable
    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void updateAdapter() {
        if(runnable != null) runnable.run();
        if(adapter == null){
            adapter = new DashBoardAdapter(getActivity(), (position, content) -> {

            });
            adapter.setData(presenter.getDashBoardDataArrayList());
            recyclerView.setAdapter(adapter);
        }else{
            adapter.setData(presenter.getDashBoardDataArrayList());
            adapter.notifyDataSetChanged();
        }

    }
    private void updateFilter() {
        if (TextUtils.isEmpty(textViewToDate.getText().toString())) {
            toDate = currentDate;
            textViewToDate.setText(toDate+"");
        }

        if (TextUtils.isEmpty(textViewFromDate.getText().toString())) {
            fromDate = currentDate;
            textViewFromDate.setText(fromDate+"");
        }
        LocalDate tolocalDate = new LocalDate(toDate);
        LocalDate fromLocalDate = new LocalDate(fromDate);

        long t = tolocalDate.toDate().getTime();
        long f = fromLocalDate.toDate().getTime();

        if(f>t){
            Toast.makeText(getActivity(),"সঠিক তারিখ নির্বাচন করুন !!!",Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(textViewFromDate.getText().toString())
                && !TextUtils.isEmpty(textViewToDate.getText().toString())) {
            presenter.fetchDashBoardData(fromDate, toDate);
        }
    }

    public String addZeroForMonth(String month){
        if(month.length()==1) return "0"+month;
        return month;
    }
}
