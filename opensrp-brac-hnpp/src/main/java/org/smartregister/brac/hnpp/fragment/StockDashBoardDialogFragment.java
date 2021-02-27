package org.smartregister.brac.hnpp.fragment;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.whiteelephant.monthpicker.MonthPickerDialog;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.StockDetailsContract;
import org.smartregister.brac.hnpp.presenter.StockDetailsPresenter;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.StockDetailsData;

import java.util.ArrayList;
import java.util.Calendar;

public class StockDashBoardDialogFragment extends DialogFragment implements StockDetailsContract.View{
    String month, year;
    DashBoardData content;
    TextView titleTV,monthStartBalanceTV,newPackageTV,sellTV,endBalanceTV, monthTV,yearTV;
    Calendar calendar = Calendar.getInstance();
    StockDetailsPresenter presenter;
    public static StockDashBoardDialogFragment getInstance(){
        return new StockDashBoardDialogFragment();
    }

    public void setContent(DashBoardData content){
        this.content = content;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stock_details, container, false);
        LinearLayout monthPicker = view.findViewById(R.id.monthDatePicker);
        titleTV= view.findViewById(R.id.title_text);
        monthStartBalanceTV = view.findViewById(R.id.month_start_blnc_text);
        newPackageTV = view.findViewById(R.id.new_pkg_text);
        sellTV = view.findViewById(R.id.sell_text);
        endBalanceTV = view.findViewById(R.id.end_blnc_text);
        monthTV = view.findViewById(R.id.month_text);
        yearTV  = view.findViewById(R.id.year_text);
        monthPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(getContext(), new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) {
                        calendar.set(Calendar.YEAR,selectedYear);
                        calendar.set(Calendar.MONTH,selectedMonth);
                        updateDatePicker();
                        filterData();
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
                builder.setActivatedMonth(calendar.get(Calendar.MONTH))
                        .setMinYear(1990)
                        .setActivatedYear(calendar.get(Calendar.YEAR))
                        .setMaxYear(Calendar.getInstance().get(Calendar.YEAR))
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
        view.findViewById(R.id.cross_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = getDialog();
                dialog.dismiss();
            }
        });

        return view;
    }

    private void updateDatePicker() {
        month = calendar.get(Calendar.MONTH)+"";
        year = calendar.get(Calendar.YEAR)+"";
        monthTV.setText(HnppJsonFormUtils.monthBanglaStr[calendar.get(Calendar.MONTH)]);
        yearTV.setText(year);
    }
    private void filterData(){
        int months = Integer.parseInt(month)+1;
        presenter.filterData(content.getEventType(),Integer.toString(months),year);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        titleTV.setText(content.getTitle());
        presenter = new StockDetailsPresenter(this);
        updateDatePicker();
        filterData();

    }

    @Override
    public void showProgressBar() {

    }

    @Override
    public void hideProgressBar() {

    }

    @Override
    public void updateView() {
        ArrayList<StockDetailsData> stockDetailsDataList =  presenter.getStockDetailsData();
        if(stockDetailsDataList.size()>0){
            StockDetailsData stockDetailsData = stockDetailsDataList.get(0);
            monthStartBalanceTV.setText(stockDetailsData.getMonthStartBalance()+"");
            newPackageTV.setText(stockDetailsData.getNewPackage()+"");
            sellTV.setText(stockDetailsData.getSell()+"");
            endBalanceTV.setText(stockDetailsData.getEndBalance()+"");

        }

    }
}