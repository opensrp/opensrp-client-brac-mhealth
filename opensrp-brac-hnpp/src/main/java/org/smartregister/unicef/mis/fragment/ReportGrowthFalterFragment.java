package org.smartregister.unicef.mis.fragment;

import static org.smartregister.growthmonitoring.domain.ZScore.getMuacText;
import static org.smartregister.growthmonitoring.domain.ZScore.getZScoreText;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.growthmonitoring.domain.HeightZScore;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.adapter.AppSegmentAdapter;
import org.smartregister.unicef.mis.domain.ChildData;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.utils.GrowthUtil;

import java.util.ArrayList;

import segmented_control.widget.custom.android.com.segmentedcontrol.SegmentedControl;
import segmented_control.widget.custom.android.com.segmentedcontrol.item_row_column.SegmentViewHolder;
import segmented_control.widget.custom.android.com.segmentedcontrol.listeners.OnSegmentClickListener;


public class ReportGrowthFalterFragment extends BaseDashBoardFragment implements SeekBar.OnSeekBarChangeListener, OnChartValueSelectedListener {
    private CommonRepository commonRepository;
    private ArrayList<ChildData> childDataList;
    private int totalGmpChild=0;
    private BarChart chart;


    public ReportGrowthFalterFragment(){
        commonRepository = HnppApplication.getInstance().getContext().commonrepository("ec_child");
    }
    private SegmentedControl segmentedControl;

    @Override
    void filterData() {

    }

    @Override
    void updateTitle() {
        super.updateTitle(getActivity().getString(R.string.grow_filter));
    }

    @Override
    void fetchData() {

    }

    @Override
    void initilizePresenter() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.report_growth_fragment, container, false);
        childDataList = new ArrayList<>();

        initUi(view);
        getChildData();
        setupGraph(0,view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);
    }

    /**
     * getting child data from db
     */
    private void getChildData() {
        childDataList.clear();
        String query = "select * from ec_child where muac_status is not NULL or weight_status is not null or height_status is not null";
        Cursor cursor = commonRepository.rawCustomQueryForAdapter(query);

        if(cursor!=null && cursor.getCount()>0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                int heightZscore = cursor.getColumnIndex("height_zscore");
                int weightZscore = cursor.getColumnIndex("weight_zscore");
                int child_height = cursor.getColumnIndex("child_height");
                int child_weight = cursor.getColumnIndex("child_weight");
                int child_muac = cursor.getColumnIndex("child_muac");
                int has_edema = cursor.getColumnIndex("has_edema");
                String muac_status = cursor.getString(cursor.getColumnIndex("muac_status"));
                String weight_status = cursor.getString(cursor.getColumnIndex("weight_status"));
                String height_status = cursor.getString(cursor.getColumnIndex("height_status"));
                String finalStatus = GrowthUtil.getOverallChildStatus(muac_status,weight_status,height_status);

                childDataList.add(new ChildData(
                        cursor.isNull(child_height)?"":cursor.getString(child_height),
                        cursor.isNull(child_weight)?"":cursor.getString(child_weight),
                        cursor.isNull(heightZscore)?0.0:cursor.getDouble(heightZscore),
                        cursor.isNull(weightZscore)?0.0:cursor.getDouble(weightZscore),
                        cursor.isNull(child_muac)?"":cursor.getString(child_muac),
                        finalStatus,
                        cursor.isNull(has_edema)?"":cursor.getString(has_edema)

                ));
                cursor.moveToNext();
            }

            totalGmpChild = cursor.getCount();

        }
    }

    /**
     * graphview setup for each positions of segmented tab
     * @param pos
     * @param view
     */
    private void setupGraph(int pos, View view) {
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.getDescription().setEnabled(false);
        chart.setMaxVisibleValueCount(60);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        chart.setClickable(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setHighlightFullBarEnabled(false);
        chart.setDrawBarShadow(false);
        chart.setEnabled(false);



        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);


        if(pos == 0){
            final ArrayList<String> axiss = new ArrayList<>();
            axiss.add("Normal Growth");
            axiss.add("Normal Growth");
            axiss.add("MAM");
            axiss.add("SAM");
            axiss.add("Edema");
            xAxis.setValueFormatter(new IndexAxisValueFormatter(axiss));
        }else if(pos == 1){
            final ArrayList<String> axiss = new ArrayList<>();
            axiss.add("Healthy");
            axiss.add("Healthy");
            axiss.add("Overweight");
            axiss.add("Moderately Underweight");
            axiss.add("Severly Underweight");
            xAxis.setValueFormatter(new IndexAxisValueFormatter(axiss));
        }else if(pos == 2){
            final ArrayList<String> axiss = new ArrayList<>();
            axiss.add("Normal");
            axiss.add("Normal");
            axiss.add("Moderately Stunted");
            axiss.add("Severly Stunted");
            xAxis.setValueFormatter(new IndexAxisValueFormatter(axiss));
        }



        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setLabelCount(5, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(5f);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.resetAxisMaximum();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(0, true);
        rightAxis.setSpaceTop(0);
        rightAxis.setAxisMinimum(0f);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        setData(4, 100,pos,chart);
    }

    @Override
    public void onValueSelected(Entry entry, Highlight highlight) {

    }

    @Override
    public void onNothingSelected() {

    }


    /**
     * data setting to chart
     * @param count
     * @param range
     * @param pos
     * @param chart
     */
    private void setData(int count, float range,int pos,BarChart chart) {
         int normalChildMuac=0;
         int samChildMuac=0;
         int mamChildMuac=0;
         int hasEdemaMuac=0;

         int healthyWeight = 0;
         int overWeight = 0;
         int moderatelyWeight = 0;
         int severlyWeight = 0;

        int normalStunting = 0;
        int moderatelyStunting = 0;
        int severlyStunting = 0;


        ArrayList<BarEntry> values = new ArrayList<>();

        BarDataSet barDataSet;
        int[] colors = null;

        int totalGmpChild = childDataList.size();
        for(ChildData childData : childDataList){
           if(pos == 0){
               if(!childData.getChild_muac().isEmpty()){
                   if(getMuacText(Double.valueOf(childData.getChild_muac())).equalsIgnoreCase("normal")){
                       normalChildMuac++;
                   }else if(getMuacText(Double.valueOf(childData.getChild_muac())).equalsIgnoreCase("sam")){
                       samChildMuac++;
                   }else if(getMuacText(Double.valueOf(childData.getChild_muac())).equalsIgnoreCase("mam")){
                       mamChildMuac++;
                   }
               }

               if(childData.getHas_edema().equals("true")){
                   hasEdemaMuac++;
               }
           }
           else if(pos == 1){
               if(!childData.getChild_weight().isEmpty()){
                   if(getZScoreText(childData.getWeightZscore()).equalsIgnoreCase("normal")){
                       healthyWeight++;
                   }else if(getZScoreText(childData.getWeightZscore()).equalsIgnoreCase("OVER WEIGHT")){
                       overWeight++;
                   }else if(getZScoreText(childData.getWeightZscore()).equalsIgnoreCase("MAM")
                    || getZScoreText(childData.getWeightZscore()).equalsIgnoreCase("SAM")){
                       moderatelyWeight++;
                   }else if(getZScoreText(childData.getWeightZscore()).equalsIgnoreCase("MAM")){
                       severlyWeight++;
                   }
               }
           }
           else if(pos == 2){
               if(!childData.getChild_height().isEmpty()){
                   Log.d("ttttHeiZSc",getZScoreText(Double.valueOf(childData.getChild_height())));
                   if(HeightZScore.getZScoreText(childData.getHeightZScore()).equalsIgnoreCase("normal")){
                       normalStunting++;
                   }else if(HeightZScore.getZScoreText(childData.getHeightZScore()).equalsIgnoreCase("MAM")
                    || HeightZScore.getZScoreText(childData.getHeightZScore()).equalsIgnoreCase("SAM")){
                       moderatelyStunting++;
                   }else if(HeightZScore.getZScoreText(childData.getHeightZScore()).equalsIgnoreCase("MAM")){
                       severlyStunting++;
                   }
               }
           }
        }
        if(totalGmpChild==0) totalGmpChild = 1;


        if(pos==0){
            values.add(new BarEntry(1, (normalChildMuac*100)/totalGmpChild));
            values.add(new BarEntry(2, (mamChildMuac*100)/totalGmpChild));
            values.add(new BarEntry(3, (samChildMuac*100)/totalGmpChild));
            values.add(new BarEntry(4, (hasEdemaMuac*100)/totalGmpChild));

            colors = new int[]{
                    ContextCompat.getColor(getActivity(), android.R.color.holo_green_dark),
                    ContextCompat.getColor(getActivity(), R.color.yellow),
                    ContextCompat.getColor(getActivity(), android.R.color.holo_red_dark),
                    ContextCompat.getColor(getActivity(), R.color.quick_check_red)};

        }
        else if(pos==1){
            values.add(new BarEntry(1, (healthyWeight*100)/totalGmpChild));
            values.add(new BarEntry(2, (overWeight*100)/totalGmpChild));
            values.add(new BarEntry(3, (moderatelyWeight*100)/totalGmpChild));
            values.add(new BarEntry(4, (severlyWeight*100)/totalGmpChild));

            colors = new int[]{
                    ContextCompat.getColor(getActivity(), android.R.color.holo_green_dark),
                    ContextCompat.getColor(getActivity(), R.color.red),
                    ContextCompat.getColor(getActivity(), R.color.dark_yellow),
                    ContextCompat.getColor(getActivity(), R.color.quick_check_red)};
        }
        else if(pos==2){
            values.add(new BarEntry(1, (normalStunting*100)/totalGmpChild));
            values.add(new BarEntry(2, (moderatelyStunting*100)/totalGmpChild));
            values.add(new BarEntry(3, (severlyStunting*100)/totalGmpChild));

            colors = new int[]{
                    ContextCompat.getColor(getActivity(), android.R.color.holo_green_dark),
                    ContextCompat.getColor(getActivity(), R.color.quick_check_red),
                    ContextCompat.getColor(getActivity(), R.color.vaccine_red_bg_end)};
        }

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            barDataSet = (BarDataSet) chart.getData().getDataSetByIndex(0);
            barDataSet.setColors(colors);
            barDataSet.setValues(values);
            barDataSet.setHighlightEnabled(false);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.invalidate();

        } else {
            barDataSet = new BarDataSet(values,"");

            barDataSet.setDrawIcons(false);
            barDataSet.setColors(colors);
            barDataSet.setHighlightEnabled(false);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(barDataSet);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(0.9f);

            chart.setData(data);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.invalidate();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
       // tvX.setText(String.valueOf(seekBarX.getProgress()));
        //tvY.setText(String.valueOf(seekBarY.getProgress()));

        //setData(10, 5,0);
       // chart.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    /**
     * view initialization
     * @param view
     */
    private void initUi(View view) {
        chart = view.findViewById(R.id.idBarChart);

        segmentedControl = (SegmentedControl) view.findViewById(R.id.segmented_control);
        segmentedControl.setAdapter(new AppSegmentAdapter());
        segmentedControl.setSelectedSegment(0);

        segmentedControl.addOnSegmentClickListener(new OnSegmentClickListener() {
            @Override
            public void onSegmentClick(SegmentViewHolder segmentViewHolder) {
                setupGraph(segmentViewHolder.getAbsolutePosition(),view);
            }
        });
    }
}
