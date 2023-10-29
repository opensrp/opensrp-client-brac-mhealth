package org.smartregister.unicef.mis.fragment;

import static org.smartregister.growthmonitoring.domain.ZScore.getZScoreText;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.growthmonitoring.domain.HeightZScore;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.growthmonitoring.domain.ZScore;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.unicef.mis.adapter.ReportRecyclerViewAdapter;
import org.smartregister.unicef.mis.domain.ChildData;
import org.smartregister.unicef.mis.domain.ReportData;
import org.smartregister.unicef.mis.utils.GrowthUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ReportFragment extends BaseDashBoardFragment {
    private RecyclerView reportRv;
    private ArrayList<ChildData> childDataList;
    private ArrayList<ReportData> reportDataList;
    CommonRepository commonRepository;
    DetailsRepository detailRepository;
    private int gmpChildren=0;
    private int totalChild = 0;
    private int samChild=0;
    private int mamChild=0;
    private int normalChild=0;
    private int edemaChild=0;
    private int overWeightChild=0;
    private int severlyStunted=0;
    private int underWeightChild=0;
    private int muacMeasureChild=0;
    private int weightMeasureChild=0;
    private int heightMeasureChild=0;

    public ReportFragment() {
        commonRepository = HnppApplication.getInstance().getContext().commonrepository("ec_child");
        detailRepository = HnppApplication.getInstance().getContext().detailsRepository();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    void filterData() {

    }

    @Override
    void updateTitle() {
        super.updateTitle(getActivity().getString(R.string.report));
    }

    @Override
    void fetchData() {

    }

    @Override
    void initilizePresenter() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_report, container, false);
        childDataList = new ArrayList<>();
        reportDataList = new ArrayList<>();

        reportRv = view.findViewById(R.id.reportRv);
        reportRv.setLayoutManager(new GridLayoutManager(getActivity(),2));
        populateReportList();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);
        updateTitle();
    }

    private void populateReportList() {
        childDataList.clear();
        reportDataList.clear();
        String query = "select * from ec_child";
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
                        cursor.isNull(child_height)?"0":cursor.getString(child_height),
                        cursor.isNull(child_weight)?"0":cursor.getString(child_weight),
                        cursor.isNull(heightZscore)?0.0:cursor.getDouble(heightZscore),
                        cursor.isNull(weightZscore)?0.0:cursor.getDouble(weightZscore),
                        cursor.isNull(child_muac)?"0":cursor.getString(child_muac),
                        finalStatus,
                        cursor.isNull(has_edema)?"0":cursor.getString(has_edema)

                ));
                cursor.moveToNext();
            }

            totalChild = cursor.getCount();
            reportDataList.add(new ReportData(String.valueOf(totalChild),"Total Registered Children(0-5 years)",R.color.black));

        }

        for(ChildData childData : childDataList){
            if(!childData.getChild_status().isEmpty()){
               gmpChildren++;
            }

            if(!childData.getChild_muac().equalsIgnoreCase("0")) {
                String status = ZScore.getMuacText(Double.parseDouble(childData.getChild_muac()));
                if (status.equalsIgnoreCase("sam")) {
                    samChild++;
                } else if (status.equalsIgnoreCase("mam")) {
                    mamChild++;
                }
            }

            if(childData.getChild_status().equalsIgnoreCase("normal")){
                normalChild++;
            }

            if(childData.getHas_edema().equals("true")){
                edemaChild++;
            }
            if(!childData.getChild_muac().equalsIgnoreCase("0")){
                muacMeasureChild++;
            }
            if(!childData.getChild_weight().equalsIgnoreCase("0")){
                weightMeasureChild++;
            }
            if(!childData.getChild_height().equalsIgnoreCase("0")){
                heightMeasureChild++;
            }
            String overWeight = getZScoreText(childData.getWeightZscore());
            if(overWeight.equalsIgnoreCase("OVER WEIGHT")){
                overWeightChild++;
            }
            String h = HeightZScore.getZScoreText(childData.getHeightZScore());
            Log.v("zscore text","h>>"+h);
            if(h.equalsIgnoreCase("DARK YELLOW") || h.equalsIgnoreCase("SAM")){
                severlyStunted++;
            }
            String w = getZScoreText(childData.getWeightZscore());
            Log.v("zscore text","weightStatus>>"+w);
            if(w.equalsIgnoreCase("DARK YELLOW") || w.equalsIgnoreCase("SAM")){
                underWeightChild++;
            }
        }
        DecimalFormat decimalFormat = new DecimalFormat("##.#");
        reportDataList.add(new ReportData(decimalFormat.format((gmpChildren*100.0)/totalChild).replace("NaN","0"),getString(R.string.no_gmp),R.color.black));
        reportDataList.add(new ReportData(decimalFormat.format((normalChild*100.0)/gmpChildren).replace("NaN","0"),getString(R.string.no_normal_growth),R.color.green));
        reportDataList.add(new ReportData(decimalFormat.format((samChild*100.0)/muacMeasureChild).replace("NaN","0"),getString(R.string.no_sam),R.color.red));
        reportDataList.add(new ReportData(decimalFormat.format((mamChild*100.0)/muacMeasureChild).replace("NaN","0"),getString(R.string.no_mam),R.color.yellow));
        reportDataList.add(new ReportData(decimalFormat.format((edemaChild*100.0)/muacMeasureChild).replace("NaN","0"),getString(R.string.no_edema),R.color.black));

        reportDataList.add(new ReportData(decimalFormat.format((overWeightChild*100.0)/weightMeasureChild).replace("NaN","0"),getString(R.string.no_overweight),R.color.red));
        reportDataList.add(new ReportData(decimalFormat.format((underWeightChild*100.0)/weightMeasureChild).replace("NaN","0"),getString(R.string.no_underweight),R.color.black));
        reportDataList.add(new ReportData(decimalFormat.format((severlyStunted*100.0)/heightMeasureChild).replace("NaN","0"),getString(R.string.no_severly_stunted),R.color.black));

        reportRv.setAdapter(new ReportRecyclerViewAdapter(getActivity(),reportDataList));
    }



/*    public static String getZScoreText(final double absScore) {
        //double absScore = Math.abs(zScore);
        if (absScore <= -3.0) {
            Log.v("ZSCORE", "zscore:" + absScore + ":color:red");
            return "SAM";
        } else if (absScore <= -2.0 && absScore > -3.0) {
            Log.v("ZSCORE", "zscore:" + absScore + ":color:dark_yellow");
            return "DARK YELLOW";
        } else if (absScore <= -1.0 && absScore > -2.0) {
            Log.v("ZSCORE", "zscore:" + absScore + ":color:yellow");
            return "MAM";
        } else if (absScore <= 2) {
            Log.v("ZSCORE", "zscore:" + absScore + ":color:green");
            return "NORMAL";
        } else {
            Log.v("ZSCORE", "zscore:" + absScore + ":color:black");
            return "OVER WEIGHT";
        }
    }*/
}

