package org.smartregister.unicef.mis.fragment;

import static org.smartregister.util.Utils.dobToDateTime;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.text.HtmlCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONObject;
import org.opensrp.api.constants.Gender;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.Constants;
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
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.activity.FamilyRegisterActivity;
import org.smartregister.unicef.mis.activity.HnppAncJsonFormActivity;
import org.smartregister.unicef.mis.activity.HnppChildProfileActivity;
import org.smartregister.unicef.mis.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.unicef.mis.activity.WebViewActivity;
import org.smartregister.unicef.mis.job.HeightIntentServiceJob;
import org.smartregister.unicef.mis.job.MuactIntentServiceJob;
import org.smartregister.unicef.mis.job.WeightIntentServiceJob;
import org.smartregister.unicef.mis.sync.FormParser;
import org.smartregister.unicef.mis.utils.GrowthUtil;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppJsonFormUtils;
import org.smartregister.util.DateUtil;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.util.Utils;
import org.smartregister.view.fragment.BaseProfileFragment;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class GMPFragment extends BaseProfileFragment implements WeightActionListener, HeightActionListener, MUACActionListener {
    public static final String DIALOG_TAG = "GMPFragment_DIALOG_TAG";
    View fragmentView;
    Activity mActivity;
    public CommonPersonObjectClient childDetails;
    String baseEntityId = "";
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
        baseEntityId = Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);
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
                Utils.startAsyncTask(new ShowGrowthChartNew(), null);

//                Utils.startAsyncTask(new ShowGrowthChartTask(), null);
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
        fragmentView.findViewById(R.id.gmpCounceling).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIYCFDialog();
                HnppJsonFormUtils.updateReferralAsEvent(mActivity,childDetails.entityId(),"","","ec_counseling",HnppConstants.EVENT_TYPE.GMP_COUNSELING);

            }
        });
        fragmentView.findViewById(R.id.height_chart_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Utils.startAsyncTask(new ShowHeightChartTask(), null);
                Utils.startAsyncTask(new ShowHeightChartNew(), null);
            }
        });
        fragmentView.findViewById(R.id.refer_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isRefered = HnppJsonFormUtils.updateReferralAsEvent(mActivity,childDetails.entityId(),"","","ec_referel",HnppConstants.EVENT_TYPE.GMP_REFERRAL);
                if(isRefered){

                    GrowthUtil.updateIsRefered(childDetails.entityId(),"true","");
                    HnppConstants.showOneButtonDialog(mActivity, getString(R.string.referrel_hospital), "", new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                    fragmentView.findViewById(R.id.refer_btn).setVisibility(View.GONE);
                    fragmentView.findViewById(R.id.refer_followup_btn).setVisibility(View.VISIBLE);
                    FormParser.makeVisitLog();
                }
            }
        });
        fragmentView.findViewById(R.id.refer_followup_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                    HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                    activity.openGMPRefereal();
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
    public void updateProfileColor() {
       if(fragmentView!=null) fragmentView.findViewById(R.id.refer_followup_btn).setVisibility(View.GONE);
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
            if(mActivity instanceof HnppChildProfileActivity){
                HnppChildProfileActivity profileActivity = (HnppChildProfileActivity) mActivity;
                profileActivity.updateProfileIconColor(resultColor,text);
            }


        }
        showReferedBtn(text);
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
        final HeightRepository heightRepository = GrowthMonitoringLibrary.getInstance().getHeightRepository();
        float previousHeight =-1;
        if(heightRepository.getMaximum(childDetails.entityId())!=null){
            previousHeight = heightRepository.getMaximum(childDetails.entityId()).getCm();
            if(heightWrapper.getHeight()<previousHeight){
                String text = getString(R.string.old_height)+previousHeight+"\n"+getString(R.string.current_height)+heightWrapper.getHeight();
                showDialogWithAction(getActivity(), getString(R.string.want_to_add_height), text, new Runnable() {
                    @Override
                    public void run() {
                        addHeight(heightWrapper);
                    }
                });
                return;
            }
        }
        addHeight(heightWrapper);
    }

    @Override
    public void onMUACTaken(MUACWrapper muacWrapper) {
        final MUACRepository muacRepository = GrowthMonitoringLibrary.getInstance().getMuacRepository();
        float previousHeight =-1;
        if(muacRepository.getMaximum(childDetails.entityId())!=null){
            previousHeight = muacRepository.getMaximum(childDetails.entityId()).getCm();
            if(muacWrapper.getHeight()<previousHeight){
                String text = getString(R.string.old_muac)+previousHeight+"\n"+getString(R.string.current_muac)+muacWrapper.getHeight();
                showDialogWithAction(getActivity(), getString(R.string.want_to_add_muac), text, new Runnable() {
                    @Override
                    public void run() {
                        addMUAC(muacWrapper);
                    }
                });
                return;
            }
        }
        addMUAC(muacWrapper);
    }

    @Override
    public void onWeightTaken(WeightWrapper tag) {
        if (tag != null) {
            final WeightRepository weightRepository = GrowthMonitoringLibrary.getInstance().weightRepository();
            float previousWeight =-1;
            if(weightRepository.getMaximum(childDetails.entityId())!=null){
                previousWeight = weightRepository.getMaximum(childDetails.entityId()).getKg();
                if(tag.getWeight()<previousWeight){
                    String text = getString(R.string.old_weight)+previousWeight+"\n"+getString(R.string.current_weight)+tag.getWeight();
                    showDialogWithAction(getActivity(), getString(R.string.want_to_add_weight), text, new Runnable() {
                        @Override
                        public void run() {
                            addWeight(tag);
                        }
                    });
                    return;
                }
            }
             addWeight(tag);


        }
    }
    private void showIYCFDialog(){
//        showDialogWithAction(getActivity(), getString(R.string.gmp_taken), "", new Runnable() {
//            @Override
//            public void run() {
                int month = getMonthDifferenceByDOB();
                showGenericDialog(getCountByMonth(month),1,month);
//            }
//        });
    }
    private void addWeight(WeightWrapper tag){
        final WeightRepository weightRepository = GrowthMonitoringLibrary.getInstance().weightRepository();
        Weight weight = new Weight();
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
        //showIYCFDialog();
        showGMPDialog(text,1);
        new Handler().postDelayed(() -> showGrowthChart(),1000);


//        int month = getMonthDifferenceByDOB();
//        showGenericDialog(getCountByMonth(month),1,month);
        updateProfileColor();
        HnppConstants.isViewRefresh = true;
    }
    private void addHeight(HeightWrapper heightWrapper){
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

        //showIYCFDialog();
        showGMPDialog(text,2);
        HnppConstants.isViewRefresh = true;
        new Handler().postDelayed(() -> showHeightChart(),1000);

//        int month = getMonthDifferenceByDOB();
//        showGenericDialog(getCountByMonth(month),1,month);
    }
    private void addMUAC(MUACWrapper muacWrapper){
        if (muacWrapper != null) {
            final MUACRepository muacRepository = GrowthMonitoringLibrary.getInstance().getMuacRepository();
            MUAC muac = new MUAC();
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

            muacRepository.add(muac);
            muacWrapper.setDbKey(muac.getId());

        }
        MuactIntentServiceJob.scheduleJobImmediately(MuactIntentServiceJob.TAG);
        String text = refreshEditMuacLayout(true);
        updateProfileColor();

        //showIYCFDialog();
        showGMPDialog(text,3);
        HnppConstants.isViewRefresh = true;
        showMuacChart();
//        int month = getMonthDifferenceByDOB();
//        showGenericDialog(getCountByMonth(month),1,month);
    }
    private int getCountByMonth(int month){
        if(month>=12 && month <=23) return 3;
        if(month>=9 && month <=11) return 3;
        if(month>6 && month <=8) return 3;
        return 2;
    }
    private void showGrowthChart(){
//        Utils.startAsyncTask(new ShowGrowthChartTask(), null);
        Utils.startAsyncTask(new ShowGrowthChartNew(), null);

    }
    private void showHeightChart(){
        Utils.startAsyncTask(new ShowHeightChartNew(), null);
    }
    private void showMuacChart(){
        Utils.startAsyncTask(new ShowMuacChartTask(), null);
    }
    public static void showDialogWithAction(Context context,String title, String text,Runnable runnable){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_two_button);
        TextView textViewTitle = dialog.findViewById(R.id.text_tv);
        TextView titleTxt = dialog.findViewById(R.id.title_tv);
        titleTxt.setText(title);
        textViewTitle.setText(text);
        dialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                runnable.run();
            }
        });
        dialog.show();
    }
    int globalCount = 1;
    private void showGenericDialog(int totalCount,int count, int month){
        Log.v("CLICK_COUNT","showGenericDialog totalCount>>"+totalCount+":count :"+count );
//        if(Boolean.TRUE.equals(HnppConstants.GMPMessage.get(childDetails.entityId()))){
//            return;
//        }else{
//            HnppConstants.GMPMessage.put(childDetails.entityId(),true);
//        }
        String dialogMessage = getGenericMessage(count,month);
        Dialog dialog = new Dialog(mActivity,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setCancelable(false);
       // dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_gmp_message);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        titleTv.setText(HtmlCompat.fromHtml(dialogMessage,HtmlCompat.FROM_HTML_MODE_COMPACT));
        LinearLayout imageView = dialog.findViewById(R.id.image_view);
        ImageView imageView1 = dialog.findViewById(R.id.image_view_1);
        ImageView imageView2 = dialog.findViewById(R.id.image_view_2);
        if(month>=6 && month <=8){
            imageView1.setImageResource(R.drawable.gmp_8_1);
            imageView2.setImageResource(R.drawable.gmp_8_4);
        }else if(month>=9 && month <=11){
            imageView1.setImageResource(R.drawable.gmp_8_2);
            imageView2.setImageResource(R.drawable.gmp_8_5);
        }else if(month>=12){
            imageView1.setImageResource(R.drawable.gmp_8_3);
            imageView2.setImageResource(R.drawable.gmp_8_6);
        }else{
            imageView1.setImageResource(R.drawable.gmp_7_2);
            imageView2.setImageResource(R.drawable.gmp_7_3);
        }
        Button ok_btn = dialog.findViewById(R.id.ok_btn);
        Button previous_btn = dialog.findViewById(R.id.previous_btn);
        previous_btn.setVisibility(View.INVISIBLE);
        if(count<totalCount){
            ok_btn.setText(getString(R.string.next));
        }else{
            previous_btn.setVisibility(View.INVISIBLE);
            ok_btn.setText(getString(R.string.ok));
        }
        previous_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.v("CLICK_COUNT","previous_btn totalCount>>"+totalCount+":click[0] :"+globalCount );

                    globalCount--;
                    String dialogMessage = getGenericMessage(globalCount,month);
                    titleTv.setText(HtmlCompat.fromHtml(dialogMessage,HtmlCompat.FROM_HTML_MODE_COMPACT));
                    if(globalCount<totalCount){
                        ok_btn.setText(getString(R.string.next));
                        //previous_btn.setVisibility(View.VISIBLE);
                    }else{
                        ok_btn.setText(getString(R.string.ok));
                        previous_btn.setVisibility(View.INVISIBLE);
                    }


            }
        });
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.v("CLICK_COUNT","ok_btn totalCount>>"+totalCount+":click[0] :"+globalCount );
                if(totalCount==globalCount){
                    dialog.dismiss();
                    globalCount=0;
                }else {
                    globalCount++;
                    String dialogMessage = getGenericMessage(globalCount,month);
                    titleTv.setText(HtmlCompat.fromHtml(dialogMessage,HtmlCompat.FROM_HTML_MODE_COMPACT));
                    if(globalCount<totalCount){
                        ok_btn.setText(getString(R.string.next));
                       // previous_btn.setVisibility(View.VISIBLE);
                    }else{
                        if(month>6){
                            imageView1.setImageResource(R.drawable.gmp_8_7);
                            imageView2.setImageResource(R.drawable.gmp_8_8);
                        }
                        ok_btn.setText(getString(R.string.ok));
                        previous_btn.setVisibility(View.INVISIBLE);
                    }
                }

            }
        });
       dialog.show();

    }
    private void showGMPDialog(String text, int type){
        int resultColor = ZScore.getZscoreColorByText(text);
        if(text.equalsIgnoreCase("OVER WEIGHT")) text = GMP_STATUS.OVER_WEIGHT.toString();
        if(text.equalsIgnoreCase("MAM")) text = GMP_STATUS.MAM.toString();
        if(text.equalsIgnoreCase("LMAL")) text = GMP_STATUS.LMAL.toString();
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

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        if(!dialogMessage.isEmpty())dialog.show();

    }
    private String getGenericMessage(int count, int month){

        switch (count){

            case 1:
                if(month<3) return getString(R.string.gmp_msg_0_3m_1);
                if(month>=24 && month <=36) return getString(R.string.gmp_msg_24m_36m_1);
                if(month>=12 && month <=23) return getString(R.string.gmp_msg_12m_23m_1);
                if(month>=9 && month <=11) return getString(R.string.gmp_msg_9m_11m_1);
                if(month>6 && month <=8) return getString(R.string.gmp_msg_6m_8m_1);
                if(month<=6) return getString(R.string.gmp_msg_3m_6m_1);

            case 2:
                if(month<3) return getString(R.string.gmp_msg_0_3m_2);
                if(month>=24 && month <=36) return getString(R.string.gmp_msg_24m_36m_2);
                if(month>=12 && month <=23) return getString(R.string.gmp_msg_12m_23m_2);
                if(month>=9 && month <=11) return getString(R.string.gmp_msg_9m_11m_2);
                if(month>6 && month <=8) return getString(R.string.gmp_msg_6m_8m_2);
                if(month<=6) return getString(R.string.gmp_msg_3m_6m_2);
            case 3:
                if(month>=12 && month <=23) return getString(R.string.gmp_msg_12m_23m_3);
                if(month>=9 && month <=11) return getString(R.string.gmp_msg_9m_11m_3);
                if(month>6 && month <=8) return getString(R.string.gmp_msg_6m_8m_3);
        }
         return "";
    }
    private String getDialogMessageByType(String text, int type){
        String dialogMsg = "";
        switch (type){
            case 1://weight
                if(text.equalsIgnoreCase(GMP_STATUS.SAM.toString())){
                    int month = getMonthDifferenceByDOB();
                    if(month<6) return getString(R.string.sam_6m_weight)+" \n "+getString(R.string.gmp_message);
                    if(month <=8) return getString(R.string.sam_6m_8m_weight)+" \n "+getString(R.string.gmp_message);
                    if(month <=11) return getString(R.string.sam_9m_11m_weight)+" \n "+getString(R.string.gmp_message);
                    if(month <=23) return getString(R.string.sam_12m_23m_weight)+" \n "+getString(R.string.gmp_message);
                }
                else if(text.equalsIgnoreCase(GMP_STATUS.MAM.toString()) ){
                    int month = getMonthDifferenceByDOB();
                    if(month<6) return getString(R.string.mam_6m_weight)+" \n "+getString(R.string.gmp_message);
                    if(month <=8) return getString(R.string.mam_6m_8m_weight)+" \n "+getString(R.string.gmp_message);
                    if(month <=11) return getString(R.string.mam_9m_11m_weight)+" \n "+getString(R.string.gmp_message);
                    if(month <=23) return getString(R.string.mam_12m_23m_weight)+" \n "+getString(R.string.gmp_message);
                }
                else if(text.equalsIgnoreCase(GMP_STATUS.OVER_WEIGHT.toString())){
                    int month = getMonthDifferenceByDOB();
                    if(month<6) return getString(R.string.over_6m)+" \n "+getString(R.string.gmp_message);
                    if(month <=8) return getString(R.string.over_6m_8m)+" \n "+getString(R.string.gmp_message);
                    if(month <=11) return getString(R.string.over_9m_11m)+" \n "+getString(R.string.gmp_message);
                    if(month <=23) return getString(R.string.over_12m_23m)+" \n "+getString(R.string.gmp_message);
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
                    if(month<6) return getString(R.string.sam_6m_height)+" \n "+getString(R.string.gmp_message);
                    if(month <=8) return getString(R.string.sam_6m_8m_height)+" \n "+getString(R.string.gmp_message);
                    if(month <=11) return getString(R.string.sam_9m_11m_height)+" \n "+getString(R.string.gmp_message);
                    if(month <=23) return getString(R.string.sam_12m_23m_height)+" \n "+getString(R.string.gmp_message);
                }
                else if(text.equalsIgnoreCase(GMP_STATUS.MAM.toString())){
                    int month = getMonthDifferenceByDOB();
                    if(month<6) return getString(R.string.mam_6m_height)+" \n "+getString(R.string.gmp_message);
                    if(month <=8) return getString(R.string.mam_6m_8m_height)+" \n "+getString(R.string.gmp_message);
                    if(month <=11) return getString(R.string.mam_9m_11m_height)+" \n "+getString(R.string.gmp_message);
                    if(month <=23) return getString(R.string.mam_12m_23m_height)+" \n "+getString(R.string.gmp_message);
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
                    if(month<6) return getString(R.string.gmp_message);
                    if(month <=8) return getString(R.string.sam_6m_8m_muac)+" \n "+getString(R.string.gmp_message);
                    if(month <=11) return getString(R.string.sam_9m_11m_muac)+" \n "+getString(R.string.gmp_message);
                    if(month <=23) return getString(R.string.sam_12m_23m_muac)+" \n "+getString(R.string.gmp_message);
                }
                else if(text.equalsIgnoreCase(GMP_STATUS.MAM.toString())){
                    int month = getMonthDifferenceByDOB();
                    if(month<6) return getString(R.string.gmp_message);
                    if(month <=8) return getString(R.string.mam_6m_8m_muac)+" \n "+getString(R.string.gmp_message);
                    if(month <=11) return getString(R.string.mam_9m_11m_muac)+" \n "+getString(R.string.gmp_message);
                    if(month <=23) return getString(R.string.mam_12m_23m_muac)+" \n "+getString(R.string.gmp_message);
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
    private void showReferedBtn(String text){
        String isReferedValue = GrowthUtil.getIsRefferedValue(baseEntityId);//Utils.getValue(childDetails, "is_refered", false);
        boolean isAlreadyRefered = !TextUtils.isEmpty(isReferedValue)&&isReferedValue.equalsIgnoreCase("true");
        if(isAlreadyRefered) {
            fragmentView.findViewById(R.id.refer_btn).setVisibility(View.GONE);
            fragmentView.findViewById(R.id.refer_followup_btn).setVisibility(View.VISIBLE);
        }else{
            if(text.equalsIgnoreCase("sam")){
                fragmentView.findViewById(R.id.refer_btn).setVisibility(View.VISIBLE);
                fragmentView.findViewById(R.id.refer_followup_btn).setVisibility(View.GONE);
            }

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
            HashMap<Integer,Float> ageWeight = new HashMap<>();
            for (Weight weight:allWeights){
                String wd = HnppConstants.DDMMYY.format(weight.getDate());
                Log.v("GMP_WEIGHT","wd:"+wd);

                int month = getMonthDifferenceByDate(wd);
                ageWeight.put(month,weight.getKg());
            }
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
    private class ShowGrowthChartNew extends AsyncTask<Void, Void, HashMap<Integer,Float>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected HashMap<Integer,Float> doInBackground(Void... params) {
            WeightRepository weightRepository = GrowthMonitoringLibrary.getInstance().weightRepository();
            List<Weight> allWeights = weightRepository.findByEntityId(childDetails.entityId());
            Collections.sort(allWeights,new Comparator<Weight>() {
                @Override
                public int compare(Weight t1, Weight t2) {
                    return (int)t1.getDate().getTime()-(int)t2.getDate().getTime();
                }
            });
            HashMap<Integer,Float> ageWeight = new HashMap<>();
            for (Weight weight:allWeights){
                String wd = HnppConstants.YYMMDD.format(weight.getDate());
                Log.v("GMP_WEIGHT","wd:"+wd+":"+weight.getKg());

                int month = getMonthDifferenceByDate(wd);
                Log.v("GMP_WEIGHT","month:"+month+":"+weight.getKg());
                ageWeight.put(month,weight.getKg());
            }
            try {
                String dobString = Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.DOB, false);
                if (!TextUtils.isEmpty(Utils.getValue(childDetails.getColumnmaps(), HnppConstants.KEY.BIRTH_WEIGHT, false))
                        && !TextUtils.isEmpty(dobString)) {
                    DateTime dateTime = new DateTime(dobString);
                    Double birthWeight = Double.valueOf(Utils.getValue(childDetails.getColumnmaps(), HnppConstants.KEY.BIRTH_WEIGHT, false));

                    Weight weight = new Weight(-1l, null, (float) birthWeight.doubleValue(), dateTime.toDate(), null, null, null, Calendar.getInstance().getTimeInMillis(), null, null, 0);
                    allWeights.add(weight);
                    String wd = HnppConstants.DDMMYY.format(weight.getDate());
                    int month = getMonthDifferenceByDate(wd);
                    Log.v("GMP_WEIGHT","else month:"+month+":"+weight.getKg());

                    ageWeight.put(month,weight.getKg());

                }
            } catch (Exception e) {
            }

            return ageWeight;
        }

        @Override
        protected void onPostExecute(HashMap<Integer,Float> allWeights) {
            super.onPostExecute(allWeights);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.INTENT_KEY.BASE_ENTITY_ID,baseEntityId);
            GMPWeightDialogFragment weightDialogFragment = GMPWeightDialogFragment.getInstance(mActivity,bundle);
            int currentAge = getMonthDifferenceByDOB();
            weightDialogFragment.setWeightValues(allWeights,currentAge,mActivity);
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class ShowHeightChartNew extends AsyncTask<Void, Void, HashMap<Integer,Float>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected HashMap<Integer,Float> doInBackground(Void... params) {
            HeightRepository heightRepository = GrowthMonitoringLibrary.getInstance().getHeightRepository();
            List<Height> allHeight = heightRepository.findByEntityId(childDetails.entityId());
            Collections.sort(allHeight,new Comparator<Height>() {
                @Override
                public int compare(Height t1, Height t2) {
                    return (int)t1.getDate().getTime()-(int)t2.getDate().getTime();
                }
            });
            HashMap<Integer,Float> ageWeight = new HashMap<>();
            for (Height height:allHeight){
                String wd = HnppConstants.YYMMDD.format(height.getDate());
                int month = getMonthDifferenceByDate(wd);
                Log.v("GMP_WEIGHT","wd:"+wd+":month:"+month+":weight:"+height.getCm());
                ageWeight.put(month,height.getCm());
            }


            return ageWeight;
        }

        @Override
        protected void onPostExecute(HashMap<Integer,Float> allWeights) {
            super.onPostExecute(allWeights);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.INTENT_KEY.BASE_ENTITY_ID,baseEntityId);
            GMPHeightDialogFragment weightDialogFragment = GMPHeightDialogFragment.getInstance(mActivity,bundle);
            int currentAge = getMonthDifferenceByDOB();
            weightDialogFragment.setHeightValues(allWeights,currentAge);
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
    private int getMonthDifferenceByDate(String dateStr){
        LocalDate dateTime = new LocalDate(dateStr);
        String dobString = Utils.getValue(childDetails.getColumnmaps(), DBConstants.KEY.DOB, false);
        LocalDate dobDate = new LocalDate(dobString.substring(0,dobString.indexOf("T")));
        Log.v("GMP_WEIGHT","getMonthDifferenceByDate>>>dateStr:"+dateStr+":dobString:"+dobString);
//        double month = HeightZScore.getAgeInMonths(dateTime.toDate(),dobTime.toDate());
//        int m = (int) Math.round(month);
        int m = HnppConstants.getMonthsDifference(dobDate,dateTime);
        Log.v("GMP_WEIGHT","m:"+m);

        return m;
    }
    public enum GMP_STATUS {
        SAM,
        MAM,
        LMAL,
        OVER_WEIGHT,
        NORMAL;

        private GMP_STATUS() {
        }
    }
}
