package org.smartregister.unicef.dghs.fragment;

import static org.smartregister.unicef.dghs.utils.HnppJsonFormUtils.REFEREL_EVENT_TYPE;
import static org.smartregister.util.Utils.dobToDateTime;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.opensrp.api.constants.Gender;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.DBConstants;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.domain.Height;
import org.smartregister.growthmonitoring.domain.HeightWrapper;
import org.smartregister.growthmonitoring.domain.HeightZScore;
import org.smartregister.growthmonitoring.domain.MUAC;
import org.smartregister.growthmonitoring.domain.MUACWrapper;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.domain.WeightWrapper;
import org.smartregister.growthmonitoring.domain.ZScore;
import org.smartregister.growthmonitoring.fragment.GrowthDialogFragment;
import org.smartregister.growthmonitoring.fragment.HeightMonitoringFragment;
import org.smartregister.growthmonitoring.fragment.MUACMonitoringFragment;
import org.smartregister.growthmonitoring.listener.HeightActionListener;
import org.smartregister.growthmonitoring.listener.MUACActionListener;
import org.smartregister.growthmonitoring.listener.WeightActionListener;
import org.smartregister.growthmonitoring.repository.HeightRepository;
import org.smartregister.growthmonitoring.repository.MUACRepository;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.util.HeightUtils;
import org.smartregister.growthmonitoring.util.MUACUtils;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.activity.HnppChildProfileActivity;
import org.smartregister.unicef.dghs.job.HeightIntentServiceJob;
import org.smartregister.unicef.dghs.job.MuactIntentServiceJob;
import org.smartregister.unicef.dghs.job.WeightIntentServiceJob;
import org.smartregister.unicef.dghs.utils.GrowthUtil;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppDBConstants;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.util.DateUtil;
import org.smartregister.util.Utils;
import org.smartregister.view.fragment.BaseProfileFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GMPFragment extends BaseProfileFragment implements WeightActionListener, HeightActionListener, MUACActionListener {
    public static final String DIALOG_TAG = "GMPFragment_DIALOG_TAG";
    View fragmentView;
    Activity mActivity;
    public CommonPersonObjectClient childDetails;
    public static GMPFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        GMPFragment fragment = new GMPFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity)context;
    }

    @Override
    protected void onCreation() {

    }

    @Override
    protected void onResumption() {

    }
    public void setChildDetails(CommonPersonObjectClient childDetails)
    {
        this.childDetails = childDetails;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.gmp_layout, container, false);
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String dobString = Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.DOB, false);

        if(TextUtils.isEmpty(dobString)){
            Toast.makeText(mActivity,"DOB invalid formate",Toast.LENGTH_SHORT).show();
            return;
        }
        initViews();
        updateGenderInChildDetails();
        refreshEditWeightLayout(false);
        refreshEditHeightLayout(false);
        refreshEditMuacLayout(false);
        updateProfileColor();
    }

//    String muacText;

    private void initViews() {

        ImageButton growthChartButton = (ImageButton) fragmentView.findViewById(R.id.growth_chart_button);
        growthChartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startAsyncTask(new ShowGrowthChartTask(), null);
            }
        });
        fragmentView.findViewById(R.id.record_height).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GrowthUtil.showHeightRecordDialog(mActivity, childDetails, 1, DIALOG_TAG);
            }
        });
        fragmentView.findViewById(R.id.record_weight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GrowthUtil.showWeightRecordDialog(mActivity, childDetails, 1, DIALOG_TAG);
            }
        });
        fragmentView.findViewById(R.id.height_chart_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startAsyncTask(new ShowHeightChartTask(), null);
            }
        });
        fragmentView.findViewById(R.id.refer_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isRefered = HnppJsonFormUtils.updateClientStatusAsEvent(mActivity,childDetails.entityId(),"","","ec_referel",REFEREL_EVENT_TYPE);
                if(isRefered){
                    GrowthUtil.updateIsRefered(childDetails.entityId(),"true");
                    Toast.makeText(mActivity,"Successfully refered to clinic",Toast.LENGTH_SHORT).show();
                    fragmentView.findViewById(R.id.refer_btn).setVisibility(View.GONE);
                }
            }
        });

        View recordMUAC = fragmentView.findViewById(R.id.recordMUAC);
        recordMUAC.setClickable(true);
        recordMUAC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GrowthUtil.showMuacRecordDialog(mActivity, childDetails, DIALOG_TAG);
            }
        });
        fragmentView.findViewById(R.id.muac_chart_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startAsyncTask(new ShowMuacChartTask(), null);
            }
        });

    }
    String heightText = "";
    String weightText = "";
    @SuppressLint("InflateParams")
    private String refreshEditWeightLayout(boolean isNeedToUpdateDb){
        LinearLayout fragmentContainer = (LinearLayout) fragmentView.findViewById(R.id.weight_group_canvas_ll);
        fragmentContainer.removeAllViews();
        fragmentContainer.addView(getLayoutInflater().inflate(R.layout.previous_weightview, null));
        TableLayout weightTable = fragmentView.findViewById(R.id.weights_table);

        WeightRepository wp = GrowthMonitoringLibrary.getInstance().weightRepository();
        List<Weight> weightlist = wp.getMaximum12(childDetails.entityId());
        /////////////////////////////////////////////////
        String dobString = Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.DOB, false);
        DateTime dateTime = new DateTime(dobString);
        Date dob  = dateTime.toDate();
        if (!TextUtils.isEmpty(Utils.getValue(childDetails.getColumnmaps(), HnppConstants.KEY.BIRTH_WEIGHT, false))
                && !TextUtils.isEmpty(dobString)) {

            Double birthWeight = Double.valueOf(Utils.getValue(childDetails.getColumnmaps(), HnppConstants.KEY.BIRTH_WEIGHT, false));

            Weight weight = new Weight(-1l, null, (float) birthWeight.doubleValue(), dateTime.toDate(), null, null, null, Calendar.getInstance().getTimeInMillis(), null, null, 0);
            weightlist.add(weight);
        }
        Gender gender = getGender();

        weightText = GrowthUtil.refreshPreviousWeightsTable(mActivity,weightTable, gender, dob, weightlist,isNeedToUpdateDb);
        return weightText;
    }
    @SuppressLint("InflateParams")
    private String refreshEditHeightLayout(boolean isNeedToUpdateDB) {
        LinearLayout fragmentContainer = (LinearLayout) fragmentView.findViewById(R.id.height_group_canvas_ll);
        fragmentContainer.removeAllViews();
        fragmentContainer.addView(getLayoutInflater().inflate(R.layout.previous_height_view, null));
        TableLayout heightTable = fragmentView.findViewById(R.id.heights_table);
        HeightRepository wp = GrowthMonitoringLibrary.getInstance().getHeightRepository();
        List<Height> heightList = wp.getMaximum12(childDetails.entityId());
        if (heightList.size() > 0) {
            try {
                HeightUtils.refreshPreviousHeightsTable(mActivity, heightTable, getGender(), dobToDateTime(childDetails).toDate(), heightList, Calendar.getInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Height height = heightList.get(0);
            heightText = HeightZScore.getZScoreText(height.getZScore());
            Log.v("ZSCORE","heightText>>"+heightText);
            if(isNeedToUpdateDB) GrowthUtil.updateLastHeight(height.getCm(),height.getZScore(),height.getBaseEntityId(),heightText);
        }
        return heightText;
    }

    int muakColor = 0;
    String muakText = "";

    @SuppressLint("InflateParams")
    private String refreshEditMuacLayout(boolean isNeedToUpdateDB) {
        LinearLayout fragmentContainer = (LinearLayout) fragmentView.findViewById(R.id.muac_group_canvas_ll);
        fragmentContainer.removeAllViews();
        fragmentContainer.addView(getLayoutInflater().inflate(R.layout.previous_muac_view, null));
        TableLayout muacTable = fragmentView.findViewById(R.id.muac_table);
        MUACRepository wp = GrowthMonitoringLibrary.getInstance().getMuacRepository();
        List<MUAC> heightList = wp.getMaximum12(childDetails.entityId());
        if (heightList.size() > 0) {
            MUACUtils.refreshPreviousMuacTable(mActivity, muacTable, getGender(), dobToDateTime(childDetails).toDate(), heightList);
            MUAC latestMuac = heightList.get(0);
            muakColor = ZScore.getMuacColor(latestMuac.getCm());
            muakText = ZScore.getMuacText(latestMuac.getCm());
            if(isNeedToUpdateDB)GrowthUtil.updateLastMuac(latestMuac.getCm(),childDetails.entityId(),muakText,latestMuac.getEdemaValue());
        }
        return muakText;

    }
    private void updateProfileColor() {
        String resultText = getOverallStatus();
        Log.v("CHILD_STATUS", " resultText>>>"+resultText);
        if(!resultText.isEmpty()){
            int resultColor = ZScore.getZscoreColorByText(resultText);
            updateChildProfileColor(resultColor,resultText);
        }

    }
    private String getOverallStatus(){
        return GrowthUtil.getOverallChildStatus(muakText,weightText,heightText);

    }
    private void updateChildProfileColor(int resultColor,String text){
        if(mActivity!=null && !mActivity.isFinishing()){
            HnppChildProfileActivity profileActivity = (HnppChildProfileActivity) mActivity;
            profileActivity.updateProfileIconColor(resultColor,text);

        }
        showReferedBtn();
    }
    private void updateGenderInChildDetails() {
        if (childDetails != null) {
            String genderString = Utils.getValue(childDetails, DBConstants.KEY.GENDER, false);
            if (genderString.equalsIgnoreCase("ছেলে") || genderString.equalsIgnoreCase("male")) {
                childDetails.getDetails().put("gender", "male");
            } else if (genderString.equalsIgnoreCase("মেয়ে") || genderString.equalsIgnoreCase("female")) {
                childDetails.getDetails().put("gender", "female");
            } else {
                childDetails.getDetails().put("gender", "male");
            }
        }
    }

    @Override
    public void onHeightTaken(HeightWrapper heightWrapper) {
        if (heightWrapper != null) {
            final HeightRepository heightRepository = GrowthMonitoringLibrary.getInstance().getHeightRepository();
            Height height = new Height();
//            if (heightWrapper.getDbKey() != null) {
//                height = heightRepository.find(heightWrapper.getDbKey());
//            }
            height.setBaseEntityId(childDetails.entityId());
            height.setCm(heightWrapper.getHeight());
            height.setDate(heightWrapper.getUpdatedHeightDate().toDate());
            String anm = FamilyLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
            height.setAnmId(FamilyLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM());
            height.setLocationId(FamilyLibrary.getInstance().context().allSharedPreferences().fetchDefaultLocalityId(anm));
            height.setTeam(FamilyLibrary.getInstance().context().allSharedPreferences().fetchDefaultTeam(anm));
            height.setTeamId(FamilyLibrary.getInstance().context().allSharedPreferences().fetchDefaultTeamId(anm));


            String g = childDetails.getColumnmaps().get("gender");
            String dobstring = childDetails.getColumnmaps().get("dob");
            GrowthUtil.DOB_STRING = dobstring;
            Gender gender = getGender();

            Date dob = null;
            if (!TextUtils.isEmpty(GrowthUtil.DOB_STRING)) {
                DateTime dateTime = new DateTime(GrowthUtil.DOB_STRING);
                dob = dateTime.toDate();
            }

            if (dob != null && gender != Gender.UNKNOWN) {
                heightRepository.add(dob, gender, height);
            } else {
                heightRepository.add(height);
            }

            heightWrapper.setDbKey(height.getId());

        }
        HeightIntentServiceJob.scheduleJobImmediately(HeightIntentServiceJob.TAG);

        String text = refreshEditHeightLayout(true);
        updateProfileColor();
        showGMPDialog(text,2);
        HnppConstants.isViewRefresh = true;
    }

    @Override
    public void onMUACTaken(MUACWrapper muacWrapper) {
        if (muacWrapper != null) {
            final MUACRepository muacRepository = GrowthMonitoringLibrary.getInstance().getMuacRepository();
            MUAC muac = new MUAC();
//            if (muacWrapper.getDbKey() != null) {
//                height = heightRepository.find(muacWrapper.getDbKey());
//            }
            muac.setBaseEntityId(childDetails.entityId());
            muac.setCm(muacWrapper.getHeight());
            muac.setDate(muacWrapper.getUpdatedHeightDate().toDate());
            String anm = FamilyLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
            muac.setAnmId(FamilyLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM());
            muac.setLocationId(FamilyLibrary.getInstance().context().allSharedPreferences().fetchDefaultLocalityId(anm));
            muac.setTeam(FamilyLibrary.getInstance().context().allSharedPreferences().fetchDefaultTeam(anm));
            muac.setTeamId(FamilyLibrary.getInstance().context().allSharedPreferences().fetchDefaultTeamId(anm));
            muac.setEdemaValue(muacWrapper.getEdemaValue());

            String dobstring = childDetails.getColumnmaps().get("dob");
            GrowthUtil.DOB_STRING = dobstring;


//            Date dob = null;
//            if (!TextUtils.isEmpty(GrowthUtil.DOB_STRING)) {
//                DateTime dateTime = new DateTime(GrowthUtil.DOB_STRING);
//                dob = dateTime.toDate();
//            }
//            Gender gender = getGender();
            muacRepository.add(muac);
            muacWrapper.setDbKey(muac.getId());

        }
        MuactIntentServiceJob.scheduleJobImmediately(MuactIntentServiceJob.TAG);
        String text = refreshEditMuacLayout(true);
        updateProfileColor();
        showGMPDialog(text,3);
        HnppConstants.isViewRefresh = true;
    }

    @Override
    public void onWeightTaken(WeightWrapper tag) {
        if (tag != null) {
            final WeightRepository weightRepository = GrowthMonitoringLibrary.getInstance().weightRepository();
            Weight weight = new Weight();
//            if (tag.getDbKey() != null) {
//                weight = weightRepository.find(tag.getDbKey());
//            }
            weight.setBaseEntityId(childDetails.entityId());
            weight.setKg(tag.getWeight());
            weight.setDate(tag.getUpdatedWeightDate().toDate());
            weight.setAnmId(FamilyLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM());
            try {
                String lastLocationId = LocationHelper.getInstance().getChildLocationId();

                weight.setLocationId(lastLocationId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Gender gender = getGender();

            Date dob = null;

            String formattedAge = "";
            if (isDataOk()) {
                String dobString = Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.DOB, false);
                if (!TextUtils.isEmpty(dobString)) {
                    DateTime dateTime = new DateTime(dobString);
                    dob = dateTime.toDate();
                    long timeDiff = Calendar.getInstance().getTimeInMillis() - dob.getTime();
                    if (timeDiff >= 0) {
                        formattedAge = DateUtil.getDuration(timeDiff);
                    }
                }
            }

            if (dob != null && gender != Gender.UNKNOWN) {
                weightRepository.add(dob, gender, weight);
            } else {
                weightRepository.add(weight);
            }

            tag.setDbKey(weight.getId());
            tag.setPatientAge(formattedAge);
            WeightIntentServiceJob.scheduleJobImmediately(WeightIntentServiceJob.TAG);
            String text = refreshEditWeightLayout(true);
            showGMPDialog(text,1);
            updateProfileColor();
            HnppConstants.isViewRefresh = true;
        }
    }
    private void showGMPDialog(String text, int type){
        int resultColor = ZScore.getZscoreColorByText(text);
        if(text.equalsIgnoreCase("OVER WEIGHT")) text = GMP_STATUS.OVER_WEIGHT.toString();
        if(text.equalsIgnoreCase("DARK YELLOW")) text = GMP_STATUS.MAM.toString();
        Log.v("SHOW_GMP","text>>"+text);
        String dialogMessage = getDialogMessageByType(text,type);
        Dialog dialog = new Dialog(mActivity);
        dialog.setCancelable(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        titleTv.setText(dialogMessage);

        titleTv.setTextColor(mActivity.getResources().getColor(resultColor));
        Button ok_btn = dialog.findViewById(R.id.ok_btn);
        showReferedBtn();

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        if(!dialogMessage.isEmpty())dialog.show();

    }
    private String getDialogMessageByType(String text, int type){
        String dialogMsg = "";
        switch (type){
            case 1://weight
                if(text.equalsIgnoreCase(GMP_STATUS.SAM.toString())){
                    int month = getMonthDifferenceByDOB();
                    if(month<6) return getString(R.string.sam_6m_weight);
                    if(month <=8) return getString(R.string.sam_6m_8m_weight);
                    if(month <=11) return getString(R.string.sam_9m_11m_weight);
                    if(month <=23) return getString(R.string.sam_12m_23m_weight);
                }
                else if(text.equalsIgnoreCase(GMP_STATUS.MAM.toString()) ){
                    int month = getMonthDifferenceByDOB();
                    if(month<6) return getString(R.string.mam_6m_weight);
                    if(month <=8) return getString(R.string.mam_6m_8m_weight);
                    if(month <=11) return getString(R.string.mam_9m_11m_weight);
                    if(month <=23) return getString(R.string.mam_12m_23m_weight);
                }
                else if(text.equalsIgnoreCase(GMP_STATUS.OVER_WEIGHT.toString())){
                    int month = getMonthDifferenceByDOB();
                    if(month<6) return getString(R.string.over_6m);
                    if(month <=8) return getString(R.string.over_6m_8m);
                    if(month <=11) return getString(R.string.over_9m_11m);
                    if(month <=23) return getString(R.string.over_12m_23m);
                }
                else {
                    int month = getMonthDifferenceByDOB();
                    if(month<6) return getString(R.string.normal_6m_weight);
                    if(month <=8) return getString(R.string.normal_6m_8m_weight);
                    if(month <=11) return getString(R.string.normal_9m_11m_weight);
                    if(month <=23) return getString(R.string.normal_12m_23m_weight);
                }
                break;
            case 2://height
                if(text.equalsIgnoreCase(GMP_STATUS.SAM.toString())){
                    int month = getMonthDifferenceByDOB();
                    if(month<6) return getString(R.string.sam_6m_height);
                    if(month <=8) return getString(R.string.sam_6m_8m_height);
                    if(month <=11) return getString(R.string.sam_9m_11m_height);
                    if(month <=23) return getString(R.string.sam_12m_23m_height);
                }
                else if(text.equalsIgnoreCase(GMP_STATUS.MAM.toString())){
                    int month = getMonthDifferenceByDOB();
                    if(month<6) return getString(R.string.mam_6m_height);
                    if(month <=8) return getString(R.string.mam_6m_8m_height);
                    if(month <=11) return getString(R.string.mam_9m_11m_height);
                    if(month <=23) return getString(R.string.mam_12m_23m_height);
                }
                else {
                    int month = getMonthDifferenceByDOB();
                    if(month<6) return getString(R.string.normal_6m_height);
                    if(month <=8) return getString(R.string.normal_6m_8m_height);
                    if(month <=11) return getString(R.string.normal_9m_11m_height);
                    if(month <=23) return getString(R.string.normal_12m_23m_height);
                }
                break;
            case 3://muac
                if(text.equalsIgnoreCase(GMP_STATUS.SAM.toString())){
                    int month = getMonthDifferenceByDOB();
                    if(month<6) return "";
                    if(month <=8) return getString(R.string.sam_6m_8m_muac);
                    if(month <=11) return getString(R.string.sam_9m_11m_muac);
                    if(month <=23) return getString(R.string.sam_12m_23m_muac);
                }
                else if(text.equalsIgnoreCase(GMP_STATUS.MAM.toString())){
                    int month = getMonthDifferenceByDOB();
                    if(month<6) return "";
                    if(month <=8) return getString(R.string.mam_6m_8m_muac);
                    if(month <=11) return getString(R.string.mam_9m_11m_muac);
                    if(month <=23) return getString(R.string.mam_12m_23m_muac);
                }
                else {
                    int month = getMonthDifferenceByDOB();
                    if(month<6) return "";
                    if(month <=8) return getString(R.string.normal_6m_8m_muac);
                    if(month <=11) return getString(R.string.normal_9m_11m_muac);
                    if(month <=23) return getString(R.string.normal_12m_23m_muac);
                }
                break;
        }
        return dialogMsg;
    }
    private void showReferedBtn(){
        String isReferedValue = Utils.getValue(childDetails, "is_refered", false);
        boolean isAlreadyRefered = !TextUtils.isEmpty(isReferedValue)&&isReferedValue.equalsIgnoreCase("true");
        if(isAlreadyRefered) {
            fragmentView.findViewById(R.id.refer_btn).setVisibility(View.GONE);
            return;
        }
        if(muakText.equalsIgnoreCase("sam")||heightText.equalsIgnoreCase("sam")
           || weightText.equalsIgnoreCase("sam")){
            fragmentView.findViewById(R.id.refer_btn).setVisibility(View.GONE);
        }
    }
    private boolean isDataOk() {
        return childDetails != null && childDetails.getDetails() != null;
    }
    @SuppressLint("StaticFieldLeak")
    private class ShowGrowthChartTask extends AsyncTask<Void, Void, List<Weight>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Weight> doInBackground(Void... params) {
            WeightRepository weightRepository = GrowthMonitoringLibrary.getInstance().weightRepository();
            List<Weight> allWeights = weightRepository.findByEntityId(childDetails.entityId());
            try {
                String dobString = Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.DOB, false);
                if (!TextUtils.isEmpty(Utils.getValue(childDetails.getColumnmaps(), HnppConstants.KEY.BIRTH_WEIGHT, false))
                        && !TextUtils.isEmpty(dobString)) {
                    DateTime dateTime = new DateTime(dobString);
                    Double birthWeight = Double.valueOf(Utils.getValue(childDetails.getColumnmaps(), HnppConstants.KEY.BIRTH_WEIGHT, false));

                    Weight weight = new Weight(-1l, null, (float) birthWeight.doubleValue(), dateTime.toDate(), null, null, null, Calendar.getInstance().getTimeInMillis(), null, null, 0);
                    allWeights.add(weight);
                }
            } catch (Exception e) {
            }

            return allWeights;
        }

        @Override
        protected void onPostExecute(List<Weight> allWeights) {
            super.onPostExecute(allWeights);
            FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
            Fragment prev = mActivity.getFragmentManager().findFragmentByTag(DIALOG_TAG);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);


            GrowthDialogFragment growthDialogFragment = GrowthDialogFragment.newInstance(childDetails, allWeights);
            growthDialogFragment.show(ft, DIALOG_TAG);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ShowHeightChartTask extends AsyncTask<Void, Void, List<Height>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Height> doInBackground(Void... params) {
            HeightRepository heightRepository = GrowthMonitoringLibrary.getInstance().getHeightRepository();
            List<Height> allHeight = heightRepository.findByEntityId(childDetails.entityId());
            return allHeight;
        }

        @Override
        protected void onPostExecute(List<Height> allHeight) {
            super.onPostExecute(allHeight);
            FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
            Fragment prev = mActivity.getFragmentManager().findFragmentByTag(DIALOG_TAG);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            String dobString = Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.DOB, false);

            HeightMonitoringFragment growthDialogFragment = HeightMonitoringFragment.createInstance(dobString, getGender(), allHeight);
            growthDialogFragment.show(ft, DIALOG_TAG);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ShowMuacChartTask extends AsyncTask<Void, Void, List<MUAC>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<MUAC> doInBackground(Void... params) {
            MUACRepository heightRepository = GrowthMonitoringLibrary.getInstance().getMuacRepository();
            List<MUAC> allHeight = heightRepository.findByEntityId(childDetails.entityId());
            return allHeight;
        }

        @Override
        protected void onPostExecute(List<MUAC> allHeight) {
            super.onPostExecute(allHeight);
            FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
            Fragment prev = mActivity.getFragmentManager().findFragmentByTag(DIALOG_TAG);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            String dobString = Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.DOB, false);

            MUACMonitoringFragment growthDialogFragment = MUACMonitoringFragment.createInstance(dobString, getGender(), allHeight);
            growthDialogFragment.show(ft, DIALOG_TAG);
        }
    }

    private Gender getGender() {
        Gender gender = Gender.UNKNOWN;
        String genderString = Utils.getValue(childDetails, DBConstants.KEY.GENDER, false);

        if (genderString != null && genderString.equalsIgnoreCase("female")) {
            gender = Gender.FEMALE;
        } else if (genderString != null && genderString.equalsIgnoreCase("male")) {
            gender = Gender.MALE;
        }
        return gender;
    }
    private int getMonthDifferenceByDOB(){
        String dobString = Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.DOB, false);
        DateTime dateTime = new DateTime(dobString);
        double month = HeightZScore.getAgeInMonths(dateTime.toDate(),new Date());
        int m = (int) Math.round(month);
        Log.v("MONTH_DIFF","m:"+m);

        return m;
    }
    public enum GMP_STATUS {
        SAM,
        MAM,
        OVER_WEIGHT,
        NORMAL;

        private GMP_STATUS() {
        }
    }
}