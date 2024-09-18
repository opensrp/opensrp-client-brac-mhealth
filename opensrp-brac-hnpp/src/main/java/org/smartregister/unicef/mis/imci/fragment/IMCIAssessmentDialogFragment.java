package org.smartregister.unicef.mis.imci.fragment;
import static org.smartregister.unicef.mis.imci.activity.ImciMainActivity.REQUEST_IMCI_ANAEMIA_2_59;
import static org.smartregister.unicef.mis.imci.activity.ImciMainActivity.REQUEST_IMCI_DIARRHEA_0_2;
import static org.smartregister.unicef.mis.imci.activity.ImciMainActivity.REQUEST_IMCI_DIARRHEA_2_59;
import static org.smartregister.unicef.mis.imci.activity.ImciMainActivity.REQUEST_IMCI_FEEDING_0_2;
import static org.smartregister.unicef.mis.imci.activity.ImciMainActivity.REQUEST_IMCI_FEVER_2_59;
import static org.smartregister.unicef.mis.imci.activity.ImciMainActivity.REQUEST_IMCI_MALNUTRITION_2_59;
import static org.smartregister.unicef.mis.imci.activity.ImciMainActivity.REQUEST_IMCI_PNEUMONIA_2_59;
import static org.smartregister.unicef.mis.imci.activity.ImciMainActivity.REQUEST_IMCI_SEVERE_0_2;
import static org.smartregister.unicef.mis.imci.activity.ImciMainActivity.REQUEST_IMCI_SEVERE_2_59;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.activity.HnppChildProfileActivity;
import org.smartregister.unicef.mis.activity.HnppFormViewActivity;
import org.smartregister.unicef.mis.adapter.MemberHistoryAdapter;
import org.smartregister.unicef.mis.contract.MemberHistoryContract;
import org.smartregister.unicef.mis.imci.Utility;
import org.smartregister.unicef.mis.imci.activity.ImciMainActivity;
import org.smartregister.unicef.mis.imci.model.IMCIReport;
import org.smartregister.unicef.mis.presenter.MemberHistoryPresenter;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppJsonFormUtils;
import org.smartregister.unicef.mis.utils.MemberHistoryData;

import java.util.HashMap;

public class IMCIAssessmentDialogFragment extends DialogFragment implements MemberHistoryContract.View {
    public static final String DIALOG_TAG = "MemberHistoryDialogFragment_DIALOG_TAG";
    public static final String IS_GUEST_USER = "IS_GUEST_USER";

    private RecyclerView assessmentResultRV,treatmentResultRV;
    private String baseEntityId;
    private boolean isStart = true;
    private ProgressBar client_list_progress;
    long startVisitDate,endVisitDate;
    TextView assesment_result_txt, assessment_result_tv,treatment_result_tv,treatment_label_tv;
    Button next_button;
    ImageView ruleImage;
    String jsonData;
    int requestType;
    boolean isReferred = false;


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && !isStart){
        }
    }

    public void setJsonData(int requestType, String jsonData) {
        this.jsonData = jsonData;
        this.requestType = requestType;
    }

    public static IMCIAssessmentDialogFragment getInstance(Activity activity){
        IMCIAssessmentDialogFragment memberHistoryFragment = new IMCIAssessmentDialogFragment();
       FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        android.app.Fragment prev = activity.getFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        memberHistoryFragment.show(ft, DIALOG_TAG);
        return memberHistoryFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_assessment,null);
        assessmentResultRV = view.findViewById(R.id.assessment_result_recycler_view);
        treatmentResultRV = view.findViewById(R.id.treatment__result_recycler_view);
        assesment_result_txt = view.findViewById(R.id.assesment_result_txt);
        assessment_result_tv = view.findViewById(R.id.assessment_result_tv);
        treatment_result_tv = view.findViewById(R.id.treatment_result_tv);
        client_list_progress = view.findViewById(R.id.client_list_progress);
        treatment_label_tv = view.findViewById(R.id.treatment_label_tv);
        ruleImage = view.findViewById(R.id.rule_image);
        next_button = view.findViewById(R.id.next_button);
        view.findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if(getActivity() instanceof ImciMainActivity){
                    ImciMainActivity imciMainActivity = (ImciMainActivity) getActivity();
                    if(isReferred){
                        imciMainActivity.openRefereal(assessmentResultTypeId,requestType);
                    }else if(requestType == REQUEST_IMCI_SEVERE_0_2){
                        imciMainActivity.startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_DIARRHEA_0_2,REQUEST_IMCI_DIARRHEA_0_2);
                    }else if(requestType == REQUEST_IMCI_DIARRHEA_0_2){
                        imciMainActivity.startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_FEEDING_0_2,REQUEST_IMCI_FEEDING_0_2);
                    }else if(requestType == REQUEST_IMCI_SEVERE_2_59){
                        imciMainActivity.startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_PNEUMONIA_2_59,REQUEST_IMCI_PNEUMONIA_2_59);
                    }else if(requestType == REQUEST_IMCI_PNEUMONIA_2_59){
                        imciMainActivity.startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_DIARRHEA_2_59,REQUEST_IMCI_DIARRHEA_2_59);
                    }else if(requestType == REQUEST_IMCI_DIARRHEA_2_59){
                        imciMainActivity.startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_FEVER_2_59,REQUEST_IMCI_FEVER_2_59);
                    }else if(requestType == REQUEST_IMCI_FEVER_2_59){
                        imciMainActivity.startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_MALNUTRITION_2_59,REQUEST_IMCI_MALNUTRITION_2_59);
                    }else if(requestType == REQUEST_IMCI_MALNUTRITION_2_59){
                        imciMainActivity.startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_ANAEMIA_2_59,REQUEST_IMCI_ANAEMIA_2_59);
                    }
                }
            }
        });
        if(isReferred) next_button.setText(getString(R.string.referrel));
        isStart = false;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializePresenter();
    }

    @Override
    public void initializePresenter() {
        switch (requestType){
            case REQUEST_IMCI_SEVERE_0_2:
                processSevereAssessment();
                break;
            case REQUEST_IMCI_DIARRHEA_0_2:
                processDiarrheaAssessment();
                break;
            case REQUEST_IMCI_FEEDING_0_2:
                processFeedingAssessment();
                break;
            case REQUEST_IMCI_SEVERE_2_59:
                processDangerSignAssessment();
                break;
            case REQUEST_IMCI_DIARRHEA_2_59:
                processDiarrhea2_59_Assessment();
                break;
            case REQUEST_IMCI_PNEUMONIA_2_59:
                processPneumoniaAssessment();
                break;
            case REQUEST_IMCI_FEVER_2_59:
                processFeverAssessment();
                break;
            case REQUEST_IMCI_MALNUTRITION_2_59:
                processMalnutritionAssessment();
                break;
            case REQUEST_IMCI_ANAEMIA_2_59:
                processAnemiaAssessment();
                break;
        }
        if(isReferred) next_button.setText(getString(R.string.referrel));
        if(!TextUtils.isEmpty(assessmentResultTypeId)){
            IMCIReport imciReport = new IMCIReport();
            imciReport.setAssessmentTimeStamp(System.currentTimeMillis());
            imciReport.setAssessmentResultType(assessmentResultTypeId);
            imciReport.setAssessmentResult(builder.toString());
            imciReport.setImciType(HnppConstants.getAssessmentTypeNameMapping().get(requestType));
            if(getActivity() instanceof ImciMainActivity){
                ImciMainActivity imciMainActivity = (ImciMainActivity)getActivity();
                imciMainActivity.setImciReportHashMap(requestType,imciReport);
            }
        }

//        presenter.fetchCurrentTimeLineHistoryData(baseEntityId,startVisitDate,endVisitDate);
    }
    StringBuilder builder;
    String assessmentResultTypeId = "";
    private void processDiarrhea2_59_Assessment(){
        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = HnppJsonFormUtils.validateParameters(jsonData);
        JSONArray fields = (JSONArray)registrationFormParams.getRight();
        String type_1 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_1");
        String type_2 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_2");
        String type_3 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_3");
        String type_4 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_4");
        String type_5 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_5");
        String type_6 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_6");
        String fourteen_days_more = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"14_days_more");
        String child_lethargic = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"child_lethargic");
        String sunken_eyes = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"sunken_eyes");
        String Pinch_the_skin = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"Pinch_the_skin");
        String restless_and_irritable = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"restless_and_irritable");
        String drinking_eagerly = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"drinking_eagerly");
        String blood_stool = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"blood_stool");
        String drink_drinking_poorly = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"drink_drinking_poorly");
        String have_diarrhea = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"have_diarrhea");
        builder = new StringBuilder();
        if(type_1.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.SIX.getValue();
            if(!TextUtils.isEmpty(fourteen_days_more) && fourteen_days_more.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                if(type_2.equalsIgnoreCase("1") || type_3.equalsIgnoreCase("1")){
                    builder.append(getString(R.string.diarreah_dehydration_14));
                }else{
                    builder.append(getString(R.string.diarreah_14_days_plus));
                }

            }
        }else  if(type_2.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.FIVE.getValue();
            if(!TextUtils.isEmpty(child_lethargic) && child_lethargic.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.fainted_or_unconscious));
                isReferred = true;
            }
            if(!TextUtils.isEmpty(drink_drinking_poorly) && drink_drinking_poorly.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.Cannot_drink_or_drinks_less));
                isReferred = true;
            }
            if(!TextUtils.isEmpty(sunken_eyes) && sunken_eyes.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.eyes_are_closed));
            }
            if(!TextUtils.isEmpty(Pinch_the_skin) && Pinch_the_skin.contains("very_slowly")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.skin_pulled_released));
            }
        }
        else  if(type_3.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.FOUR.getValue();
            if(!TextUtils.isEmpty(restless_and_irritable) && restless_and_irritable.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.restless_and_irritable));
            }
            if(!TextUtils.isEmpty(drinking_eagerly) && drinking_eagerly.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.drinks_eagerly_thirsty));
            }
            if(!TextUtils.isEmpty(Pinch_the_skin) && Pinch_the_skin.contains("slowly")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.pinch_the_skin));
            }
        }
        else  if(type_4.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.THREE.getValue();
            if(!TextUtils.isEmpty(fourteen_days_more) && fourteen_days_more.contains("yes") && type_6.equalsIgnoreCase("1")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.diarreah_14_days));

            }
        }
        else  if(type_5.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.TWO.getValue();
            if(!TextUtils.isEmpty(blood_stool) && blood_stool.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.blood_in_stool));

            }
        }
        else if(type_6.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.ONE.getValue();
            if(!TextUtils.isEmpty(blood_stool) && blood_stool.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.not_blood_stool));
            }
            if(!TextUtils.isEmpty(fourteen_days_more) && fourteen_days_more.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.diarrah_below_14_days));
            }
            if(!TextUtils.isEmpty(restless_and_irritable) && restless_and_irritable.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.not_restless_and_irritable));
            }
            if(!TextUtils.isEmpty(drinking_eagerly) && drinking_eagerly.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.not_thirsty));
            }
            if(!TextUtils.isEmpty(Pinch_the_skin) && Pinch_the_skin.contains("normally")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.Pinch_skin_normally));
            }
            if(!TextUtils.isEmpty(drink_drinking_poorly) && drink_drinking_poorly.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.can_drink));
            }
            if(!TextUtils.isEmpty(sunken_eyes) && sunken_eyes.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.eyes_not_sitting));
            }
            if(!TextUtils.isEmpty(sunken_eyes) && sunken_eyes.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.eye_sitting_down));
            }
            if(!TextUtils.isEmpty(have_diarrhea) && have_diarrhea.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.have_diarrhea));
            }
            if(!TextUtils.isEmpty(have_diarrhea) && have_diarrhea.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.have_not_diarreha));
            }
        }
        if(builder.length()>0) assessment_result_tv.setText(Html.fromHtml(builder.toString()));
        if(!assessmentResultTypeId.isEmpty()){
            assesment_result_txt.setText(assessmentResultTypeId);
            if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.SIX.getValue())
             || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.FIVE.getValue())){
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_red));
            }else if( assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.FOUR.getValue())
            || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.THREE.getValue())
            || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.TWO.getValue())){
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_yello));
            }else{
                assesment_result_txt.setText(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.ONE.getValue());
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_green));
            }
            if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.SIX.getValue())){

                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.treatment_water_shortage) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.treatment_sent_hospital) ;
                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            } else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.FIVE.getValue())){

                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.treatment_sent_hospital) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.ors_taken) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.continue_brest_feeding)+
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.colera_antibiotic);

                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            }else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.FOUR.getValue())){
                ruleImage.setVisibility(View.VISIBLE);
                ruleImage.setImageResource(R.drawable.rule_kha);
                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.water_shortage_liquid) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.treatment_sent_hospital) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.ors_taken)+
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.continue_brest_feeding) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.next_visit_advice) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.followup_within_two_days) ;
                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            }else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.THREE.getValue())){

                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.advice_mother_long_diarreha) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.multivitamin_vitamin_a)+
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.next_visit_advice) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.followup_within_5_days) ;
                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            }else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.TWO.getValue())){

                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.next_visit_advice) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.followup_within_two_days);
                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            }else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA_2_59.ONE.getValue())){
                ruleImage.setVisibility(View.VISIBLE);
                ruleImage.setImageResource(R.drawable.rule_ka);
                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.liquid_zin_nomal) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.next_visit_advice) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.followup_within_two_days)+
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.refer_14_days);
                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            }


        }

    }
    private void processAnemiaAssessment(){
        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = HnppJsonFormUtils.validateParameters(jsonData);
        JSONArray fields = (JSONArray)registrationFormParams.getRight();
        String type_1 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_1");
        String type_2 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_2");
        String type_3 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_3");
        String hand_is_faded = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"hand_is_faded");

        builder = new StringBuilder();
        assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_ANEMIA.ONE.getValue();
        if(type_1.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_ANEMIA.THREE.getValue();
            if(!TextUtils.isEmpty(hand_is_faded) && hand_is_faded.contains("Very_Faded")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.Palms_are_very_pale));
            }
        }else  if(type_2.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_ANEMIA.TWO.getValue();
            if(!TextUtils.isEmpty(hand_is_faded) && hand_is_faded.contains("Medium_Faded")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.Palms_are_somewhat_pale));
            }
        }
        else  if(type_3.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_ANEMIA.ONE.getValue();
            if(!TextUtils.isEmpty(hand_is_faded) && hand_is_faded.contains("Not_Faded")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.Palms_are_not_pale));
            }
        }
        if(builder.length()>0) assessment_result_tv.setText(Html.fromHtml(builder.toString()));
        if(!assessmentResultTypeId.isEmpty()){
            assesment_result_txt.setText(assessmentResultTypeId);
            if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_ANEMIA.THREE.getValue())){
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_red));
            }else if( assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_ANEMIA.TWO.getValue())){
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_yello));
            }else{
                assesment_result_txt.setText(Utility.ASSESSMENT_RESULT_TYPE_ANEMIA.ONE.getValue());
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_green));
            }
            if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_ANEMIA.THREE.getValue())){

                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.treatment_sent_hospital) ;
                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            } else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_ANEMIA.TWO.getValue())){

                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.iron_multiple_micronutrient) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.next_visit_advice) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.followup_within_14days) ;
                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            }else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_ANEMIA.ONE.getValue())){
                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.iron_6_month_above) +
                        getString(R.string.ifa_multivitamin) ;
                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            }

        }
    }
    private void processMalnutritionAssessment(){
        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = HnppJsonFormUtils.validateParameters(jsonData);
        JSONArray fields = (JSONArray)registrationFormParams.getRight();
        String type_1 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_1");
        String type_2 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_2");
        String type_3 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_3");
        String type_4 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_4");
        String oedema_both_feet = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"oedema_both_feet");
        String any_medical_complications = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"any_medical_complications");
        String Finish_nutrition_therapy = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"Finish_nutrition_therapy");
        String breastfeeding_problem = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"breastfeeding_problem");
        String Measure_WFH = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"Measure_WFH");
        String Measure_MUAC = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"Measure_MUAC");

        builder = new StringBuilder();
        if(type_1.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_MALNUTRITION.FOUR.getValue();
            if(!TextUtils.isEmpty(oedema_both_feet) && oedema_both_feet.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.both_legs_swollen));
                            }
            if(!TextUtils.isEmpty(any_medical_complications) && any_medical_complications.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.health_complications));
            }
            if(!TextUtils.isEmpty(Finish_nutrition_therapy) && Finish_nutrition_therapy.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.nutritional_treatment));
            }
            if(!TextUtils.isEmpty(breastfeeding_problem) && breastfeeding_problem.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.problems_with_breastfeeding));
            }
        }else  if(type_2.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_MALNUTRITION.THREE.getValue();
            if(!TextUtils.isEmpty(Measure_WFH) && Measure_WFH.contains("less_minus_3")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.WFH_less_three));
            }
            if(!TextUtils.isEmpty(Measure_MUAC) && Measure_MUAC.contains("less_115_ml")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.muac_115_less));
            }
            if(!TextUtils.isEmpty(any_medical_complications) && any_medical_complications.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.no_health_complications));
            }
            if(!TextUtils.isEmpty(Finish_nutrition_therapy) && Finish_nutrition_therapy.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.nutritional_treatment_completed));
            }
            if(!TextUtils.isEmpty(breastfeeding_problem) && breastfeeding_problem.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.brest_feeding_not_problem));
            }
        }
        else  if(type_3.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_MALNUTRITION.TWO.getValue();
            if(!TextUtils.isEmpty(Measure_WFH) && Measure_WFH.contains("minus_2_3")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.wfh_3_2));
            }
            if(!TextUtils.isEmpty(Measure_MUAC) && Measure_MUAC.contains("between_115_125")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.muac_115_125));
            }
        }
        else  if(type_4.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_MALNUTRITION.ONE.getValue();
            if(!TextUtils.isEmpty(oedema_both_feet) && oedema_both_feet.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.Both_legs_not_swollen));
            }
            if(!TextUtils.isEmpty(Measure_WFH) && Measure_WFH.contains("minus_2_grater")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.wfh_greater_2));
            }
            if(!TextUtils.isEmpty(Measure_MUAC) && Measure_MUAC.contains("grater_125_cm")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.muac_125));
            }
        }
        if(builder.length()>0) assessment_result_tv.setText(Html.fromHtml(builder.toString()));
        if(!assessmentResultTypeId.isEmpty()){
            assesment_result_txt.setText(assessmentResultTypeId);
            if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_MALNUTRITION.FOUR.getValue())){
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_red));
            }else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_MALNUTRITION.THREE.getValue())||
                    assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_MALNUTRITION.TWO.getValue())){
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_yello));
            }else{
                assesment_result_txt.setText(Utility.ASSESSMENT_RESULT_TYPE_MALNUTRITION.ONE.getValue());
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_green));
            }
            if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_MALNUTRITION.ONE.getValue())){



                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.advice_to_feeding_chart) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.next_visit_advice) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.followup_feeding_problem);
                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            }else{
                String treatmentBuilder =
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.treatment_prevent_low_b_g) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.next_visit_advice) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.Keep_the_baby_warm) ;
                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            }

        }
    }
    private void processFeverAssessment(){
        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = HnppJsonFormUtils.validateParameters(jsonData);
        JSONArray fields = (JSONArray)registrationFormParams.getRight();
        String type_1 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_1");
        String type_2 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_2");
        String type_3 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_3");
        String type_4 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_4");
        String type_5 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_5");
        String Stiff_neck = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"Stiff_neck");
        String Rapid_Diagnostic_Test = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"Rapid_Diagnostic_Test");
        String have_fever = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"have_fever");
        String seven_days_more = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"7_days_more");
        String check_more_7_days = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"check_more_7_days");
        String fever_Bacterial = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"fever_Bacterial");

        builder = new StringBuilder();
        if(type_1.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_FEVER.FIVE.getValue();
            if(!TextUtils.isEmpty(Stiff_neck) && Stiff_neck.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.Neck_pain));
            }
        }else  if(type_2.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_FEVER.FOUR.getValue();
            if(!TextUtils.isEmpty(Rapid_Diagnostic_Test) && Rapid_Diagnostic_Test.contains("rdt_positive")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.rdt_positive));
            }
        }
        else  if(type_3.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_FEVER.THREE.getValue();

            if(!TextUtils.isEmpty(Rapid_Diagnostic_Test) && Rapid_Diagnostic_Test.contains("rdt_negative")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.rdt_negetive));
            }
//
//            if(!TextUtils.isEmpty(fever_Bacterial) && fever_Bacterial.contains("yes")){
//                builder.append("<br>");
//                builder.append(getString(R.string.right_arrow));
//                builder.append("জ্বরের অন্য কোন কারণ আছে");
//            }
        }
        else  if(type_4.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_FEVER.TWO.getValue();
            if(!TextUtils.isEmpty(Stiff_neck) && Stiff_neck.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.nick_not_pain));
            }
            if(!TextUtils.isEmpty(fever_Bacterial) && fever_Bacterial.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.visit_30_days_more));
            }
        }
        else  if(type_5.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_FEVER.ONE.getValue();
            if(!TextUtils.isEmpty(have_fever) && have_fever.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.fever_not_present));
            }
        }
        if(builder.length()>0) assessment_result_tv.setText(Html.fromHtml(builder.toString()));
        if(!TextUtils.isEmpty(assessmentResultTypeId)){
            assesment_result_txt.setText(assessmentResultTypeId);
            if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_FEVER.FIVE.getValue())){
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_red));
            }else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_FEVER.FOUR.getValue())){
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_yello));
            }else{
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_green));
            }

            if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_FEVER.FIVE.getValue())){

                String treatmentBuilder =
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.blood_glucose_prevent) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.treatment_sent_hospital) ;
                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            } else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_FEVER.FOUR.getValue())){

                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.mouth_antimeleria_treatment) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.advice_mother_advice) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.followup_within_two_days) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.refer_hospital_7_days);
                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            }
            else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_FEVER.THREE.getValue())
                    || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_FEVER.TWO.getValue())){

                String treatmentBuilder =
                        "<br>" +
                        getString(R.string.right_arrow) +
                                getString(R.string.advice_mother_advice) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.followup_within_two_days) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                         getString(R.string.refer_hospital_7_days);
                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            }
        }
    }
    private void processPneumoniaAssessment(){
        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = HnppJsonFormUtils.validateParameters(jsonData);
        JSONArray fields = (JSONArray)registrationFormParams.getRight();
        String type_1 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_1");
        String type_2 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_2");
        String type_3 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_3");
        String type_4 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_4");
        String oxygen_saturation_level = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"oxygen_saturation_level");
        String Look_chest_indrawing = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"Look_chest_indrawing");
        String number_of_times_taking_breathe = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"number_of_times_taking_breathe");
        String has_cough_breathing = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"has_cough_breathing");

        builder = new StringBuilder();
        if(type_1.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_PNEUMONIA.FOUR.getValue();

            if(!TextUtils.isEmpty(oxygen_saturation_level) && oxygen_saturation_level.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.spo2));
                isReferred = true;
            }
        }else  if(type_2.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_PNEUMONIA.THREE.getValue();
            if(!TextUtils.isEmpty(Look_chest_indrawing) && Look_chest_indrawing.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.lower_part_chest_sinks));
            }
            if(!TextUtils.isEmpty(number_of_times_taking_breathe)){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.quick_breathing));
            }
        }
        else  if(type_3.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_PNEUMONIA.TWO.getValue();
            if(!TextUtils.isEmpty(has_cough_breathing) && has_cough_breathing.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.Cough_trouble_breathing));
            }
            if(!TextUtils.isEmpty(oxygen_saturation_level) && oxygen_saturation_level.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.spo2_normal));
            }
            if(!TextUtils.isEmpty(Look_chest_indrawing) && Look_chest_indrawing.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.chest_not_goes_inside));
            }
            if(!TextUtils.isEmpty(number_of_times_taking_breathe) && (number_of_times_taking_breathe.contains("bellow_40") || number_of_times_taking_breathe.contains("above_40"))){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.normal_breath));
            }
        }
        else  if(type_4.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_PNEUMONIA.ONE.getValue();
            if(!TextUtils.isEmpty(has_cough_breathing) && has_cough_breathing.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.No_cough_breathing_problems));
            }
        }
        if(builder.length()>0) assessment_result_tv.setText(Html.fromHtml(builder.toString()));
        if(!assessmentResultTypeId.isEmpty()){
            assesment_result_txt.setText(assessmentResultTypeId);
            if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_PNEUMONIA.FOUR.getValue())){
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_red));
            }else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_PNEUMONIA.THREE.getValue())){
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_yello));
            }else{
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_green));
            }
                if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_PNEUMONIA.FOUR.getValue())) {
                    String treatmentBuilder =
                            "<br>" +
                            getString(R.string.right_arrow) +
                            getString(R.string.treatment_sent_hospital) ;
                    treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                    treatment_label_tv.setVisibility(View.VISIBLE);
                }
                else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_PNEUMONIA.THREE.getValue())){

                    String treatmentBuilder = "</br>" +
                            getString(R.string.right_arrow) +
                            getString(R.string.safe_remedy) +
                            "<br>" +
                            getString(R.string.right_arrow) +
                            getString(R.string.cough_more_than_14_days) +
                            "<br>" +
                            getString(R.string.right_arrow) +
                            getString(R.string.advice_mother_advice) +
                            "<br>" +
                            getString(R.string.right_arrow) +
                            getString(R.string.followup_2_days) ;
                    treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                    treatment_label_tv.setVisibility(View.VISIBLE);
                }
               else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_PNEUMONIA.TWO.getValue())){

                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.safe_remedy) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.cough_more_than_14_days) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.advice_mother_advice) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.followup_5_days)  ;
                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            }
        }
    }
    private void processDangerSignAssessment(){
        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = HnppJsonFormUtils.validateParameters(jsonData);
        JSONArray fields = (JSONArray)registrationFormParams.getRight();
        String type_1 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_1");
        String type_2 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_2");
        String drink_or_breastfeed = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"drink_or_breastfeed");
        String vomit_everything = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"vomit_everything");
        String had_convulsions = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"had_convulsions");
        String lethargic_or_unconsciou = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"lethargic_or_unconscious");
        String convulsing_now = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"convulsing_now");

        builder = new StringBuilder();
        assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DANGER_SIGN.ONE.getValue();
        if(type_1.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DANGER_SIGN.TWO.getValue();
            if(!TextUtils.isEmpty(drink_or_breastfeed) && drink_or_breastfeed.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.cant_drink_brest_feeding));
                isReferred = true;
            }
            if(!TextUtils.isEmpty(vomit_everything) && vomit_everything.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.vomits_all_food));
                isReferred = true;
            }
            if(!TextUtils.isEmpty(had_convulsions) && had_convulsions.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.Had_a_seizure));
                isReferred = true;
            }
            if(!TextUtils.isEmpty(lethargic_or_unconsciou) && lethargic_or_unconsciou.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.Fainted_or_unconscious));
                isReferred = true;
            }
            if(!TextUtils.isEmpty(convulsing_now) && convulsing_now.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.Now_have_seizures));
                isReferred = true;
            }
        }else  if(type_2.equalsIgnoreCase("1")) {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DANGER_SIGN.ONE.getValue();
            if(!TextUtils.isEmpty(drink_or_breastfeed) && drink_or_breastfeed.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.can_drink_brest_feeding));
            }
            if(!TextUtils.isEmpty(vomit_everything) && vomit_everything.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.not_vomitting));
            }
            if(!TextUtils.isEmpty(had_convulsions) && had_convulsions.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.No_seizures));
            }
            if(!TextUtils.isEmpty(lethargic_or_unconsciou) && lethargic_or_unconsciou.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.Not_fainting_or_fainting));
            }
            if(!TextUtils.isEmpty(convulsing_now) && convulsing_now.contains("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.No_seizures_now));
            }
        }
        if(builder.length()>0) assessment_result_tv.setText(Html.fromHtml(builder.toString()));
        if(!assessmentResultTypeId.isEmpty()){
            assesment_result_txt.setText(assessmentResultTypeId);
           if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DANGER_SIGN.TWO.getValue())){
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_red));
            }else{
                assesment_result_txt.setText(Utility.ASSESSMENT_RESULT_TYPE_DANGER_SIGN.ONE.getValue());
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_green));
            }

            if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DANGER_SIGN.TWO.getValue())){

                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.Complete_the_quick_assessment) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.pre_refferal) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.prevent_low_blood_glucose)+
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.child_warm) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.treatment_sent_hospital) ;
                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            }
        }
    }


    private void processFeedingAssessment(){
        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = HnppJsonFormUtils.validateParameters(jsonData);
        JSONArray fields = (JSONArray)registrationFormParams.getRight();
        String type_1 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_1");
        String type_2 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_2");
        String type_3 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_3");
        String sucking_effectively = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"sucking_effectively");
        String drink_breast_milk = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"drink_breast_milk");
        String other_food_drink = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"other_food_drink");
        String weight_less_than_2_kg = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"weight_less_than_2_kg");
        String weight_proportion_zscore = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"weight_proportion_zscore");
        String Thrush = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"Thrush");

        String chin_touching_breast = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"chin_touching_breast");
        String mouth_wide_open = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"mouth_wide_open");
        String Lower_lip_turned_outward = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"Lower_lip_turned_outward");
        String areola_mouth = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"areola_mouth");
        String Straight_head_body = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"Straight_head_body");
        String Body_close_mother = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"Body_close_mother");
        String body_fully_supported = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"body_fully_supported");
        String Facing_breast = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"Facing_breast");
        builder = new StringBuilder();
        assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_FEEDING.ONE.getValue();
        if(type_1.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_FEEDING.THREE.getValue();
            builder.append("<br>");
            builder.append(getString(R.string.right_arrow));
            builder.append(getString(R.string.age_7_days));
        }
        else if(type_2.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_FEEDING.TWO.getValue();
            if(!TextUtils.isEmpty(sucking_effectively) && !sucking_effectively.contains("effectively")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.not_brest_feeding));
            }
            if(!TextUtils.isEmpty(drink_breast_milk) && drink_breast_milk.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.brest_feeding_less_than_24));
            }
            if(!TextUtils.isEmpty(other_food_drink) && other_food_drink.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.other_food));
            }
            if((!TextUtils.isEmpty(weight_less_than_2_kg) && weight_less_than_2_kg.equalsIgnoreCase("yes") )||
                    (!TextUtils.isEmpty(weight_proportion_zscore) && weight_proportion_zscore.equalsIgnoreCase("yes") )){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.weight_less_age));
            }
            if(!TextUtils.isEmpty(Thrush) && Thrush.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.thrush));
            }
            if((!TextUtils.isEmpty(chin_touching_breast) && chin_touching_breast.equalsIgnoreCase("no") )
                    || (!TextUtils.isEmpty(mouth_wide_open) && mouth_wide_open.equalsIgnoreCase("no") )
                    || (!TextUtils.isEmpty(Lower_lip_turned_outward) && Lower_lip_turned_outward.equalsIgnoreCase("no") )
                    || (!TextUtils.isEmpty(areola_mouth) && areola_mouth.equalsIgnoreCase("no") )
                    || (!TextUtils.isEmpty(Straight_head_body) && Straight_head_body.equalsIgnoreCase("no") )
                    || (!TextUtils.isEmpty(Body_close_mother) && Body_close_mother.equalsIgnoreCase("no") )
                    || (!TextUtils.isEmpty(body_fully_supported) && body_fully_supported.equalsIgnoreCase("no") )
                    || (!TextUtils.isEmpty(Facing_breast) && Facing_breast.equalsIgnoreCase("no") )
            )
            {
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.not_good_position));
            }
        }
        else if(type_3.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_FEEDING.ONE.getValue();
            if(!TextUtils.isEmpty(sucking_effectively) && sucking_effectively.contains("effectively")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.good_sucking));
            }
            if(!TextUtils.isEmpty(drink_breast_milk) && drink_breast_milk.equalsIgnoreCase("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.eat_8_times));
            }
            if(!TextUtils.isEmpty(other_food_drink) && other_food_drink.equalsIgnoreCase("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.not_other_food));
            }
            if((!TextUtils.isEmpty(weight_less_than_2_kg) && weight_less_than_2_kg.equalsIgnoreCase("no") )){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.eweight_vs_age_not));
            }
            if(!TextUtils.isEmpty(Thrush) && Thrush.equalsIgnoreCase("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.thrush_not));
            }
            if(((!TextUtils.isEmpty(chin_touching_breast) && chin_touching_breast.equalsIgnoreCase("yes") )
                    && (!TextUtils.isEmpty(mouth_wide_open) && mouth_wide_open.equalsIgnoreCase("yes") )
                    && (!TextUtils.isEmpty(Lower_lip_turned_outward) && Lower_lip_turned_outward.equalsIgnoreCase("yes") )
                    && (!TextUtils.isEmpty(areola_mouth) && areola_mouth.equalsIgnoreCase("yes") ))
                    || ((!TextUtils.isEmpty(Straight_head_body) && Straight_head_body.equalsIgnoreCase("yes") )
                    && (!TextUtils.isEmpty(Body_close_mother) && Body_close_mother.equalsIgnoreCase("yes") )
                    && (!TextUtils.isEmpty(body_fully_supported) && body_fully_supported.equalsIgnoreCase("yes") )
                    && (!TextUtils.isEmpty(Facing_breast) && Facing_breast.equalsIgnoreCase("yes") ))
            )
            {
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.good_position));
            }
        }

        if(builder.length()>0) assessment_result_tv.setText(Html.fromHtml(builder.toString()));
        if(!assessmentResultTypeId.isEmpty()){
            assesment_result_txt.setText(assessmentResultTypeId);
            if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_FEEDING.TWO.getValue())){
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_yello));
            }else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_FEEDING.THREE.getValue())){
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_red));
            }else{
                assesment_result_txt.setText(Utility.ASSESSMENT_RESULT_TYPE_FEEDING.ONE.getValue());
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_green));
            }
        }
        if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_FEEDING.THREE.getValue())){

            String treatmentBuilder = "</br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.treatment_for_blood_glucose) +
                    "<br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.refer_hospital_kmc) +
                    "<br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.warm_child_on_hopsital);
            treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
            treatment_label_tv.setVisibility(View.VISIBLE);
        }else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_FEEDING.TWO.getValue())){

            String treatmentBuilder = "</br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.advice_good_position) +
                    "<br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.advice_brest_feeding) +
                    "<br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.advicec_24_hours)+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.advice_other_food)+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.advice_not_brest_feeding)+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.advice_not_brest_feeding_every)+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.advice_mother_weight_less)+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.advice_thrush)+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.advice_take_care)+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.next_followup)+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.followup_thrush)+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.weight_followup_14_days);

                    treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                    treatment_label_tv.setVisibility(View.VISIBLE);
        }else{
            String treatmentBuilder = "</br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.advice_at_home) +
                    "<br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.Appreciate_mother);
            treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
            treatment_label_tv.setVisibility(View.VISIBLE);
        }
    }
    private void processDiarrheaAssessment(){
        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = HnppJsonFormUtils.validateParameters(jsonData);
        JSONArray fields = (JSONArray)registrationFormParams.getRight();
        String type_1 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_1");
        String type_2 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_2");
        String type_3 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_3");
        String infant_move_stimulated = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"infant_move_stimulated");
        String infant_not_move_at_all = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"infant_not_move_at_all");
        String Pinch_the_skin = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"Pinch_the_skin");
        String sunken_eyes = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"sunken_eyes");
        String restless_irritable = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"restless_irritable");
        builder = new StringBuilder();
        assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA.ONE.getValue();
        if(type_1.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA.THREE.getValue();
            if(!TextUtils.isEmpty(sunken_eyes) && sunken_eyes.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.eye_drop));
                builder.append("<br>");
            }
            if(!TextUtils.isEmpty(Pinch_the_skin) && Pinch_the_skin.contains("very_slowly")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.skin_slowlly));
                builder.append("<br>");
            }
            if((!TextUtils.isEmpty(infant_move_stimulated) && infant_move_stimulated.contains("yes")) ||
                    (!TextUtils.isEmpty(infant_not_move_at_all) && infant_not_move_at_all.contains("no"))){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.child_movement));
            }

        }else if(type_2.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA.TWO.getValue();

            if(!TextUtils.isEmpty(sunken_eyes) && sunken_eyes.contains("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.eye_closed));
                builder.append("<br>");
            }
            if(!TextUtils.isEmpty(Pinch_the_skin) && Pinch_the_skin.contains("slowly")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.skin_slowly));
                builder.append("<br>");
            }
            if((!TextUtils.isEmpty(restless_irritable) && restless_irritable.contains("yes"))){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.unsteady));
            }

        }else {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA.ONE.getValue();
            builder.append("<br>");
            builder.append(getString(R.string.right_arrow));
            builder.append(getString(R.string.no_signdrome_waterless));
        }
        if(builder.length()>0) assessment_result_tv.setText(Html.fromHtml(builder.toString()));
        if(!assessmentResultTypeId.isEmpty()){
            assesment_result_txt.setText(assessmentResultTypeId);
            if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA.TWO.getValue())){
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_yello));
            }else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA.THREE.getValue())){
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_red));
            }else{
                assesment_result_txt.setText(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA.ONE.getValue());
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_green));
            }
        }
        if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA.THREE.getValue()) ||
                assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA.TWO.getValue())){

            String treatmentBuilder = "</br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.advice_hospital) +
                    "<br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.ors_mother) +
                    "<br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.hospital_body_ward) +
                    "<br>" +
                    getString(R.string.right_arrow) +
                    getString(R.string.continue_brest_feeding);
            treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
            treatment_label_tv.setVisibility(View.VISIBLE);
        }
    }

    private void processSevereAssessment(){
        try {
            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = HnppJsonFormUtils.validateParameters(jsonData);
            JSONArray fields = (JSONArray)registrationFormParams.getRight();
            String unconsciousValue = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"unconscious");
            String convulsionValue = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"convulsion");
            String feedingProblem = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"able_to_feed");
            String vomiting = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"vomiting");
            String bulging = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"bulging_fontanels");
            String stopBreathing = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"stop_breathing");
            String centralCyanosis = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"central_cyanosis");
            String bleeding = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"bleeding");
            String weight = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"weight");
            String malformation = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"malformation");
            String surgicalCondition = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"surgical_condition");
            String chestIndrawing = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"chest_indrawing");
            String lowBodyTemperature = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"low_body_temperature");
            String difficultyInFeeding = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"difficulty_in_feeding");
            String infantNotMoveAtAll = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"infant_not_move_at_all");
            String infantMoveStimulated = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"infant_move_stimulated");
            String umbilicusRed = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"Umbilicus_red");
            String skinPustules = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"skin_pustules");
            String calm_state_breathing = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"calm_state_breathing");
            builder = new StringBuilder();
            String type_1 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_1");
            String type_2 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_2");
            String type_3 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_3");
            String type_4 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_4");
            if(type_1.equalsIgnoreCase("1")) {
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.TWO.getValue();
            }else if(type_2.equalsIgnoreCase("1")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.FOUR.getValue();
            }else if(type_3.equalsIgnoreCase("1")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.FIVE.getValue();
            }else if(type_4.equalsIgnoreCase("1")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.SIX.getValue();
            }
            if(!TextUtils.isEmpty(unconsciousValue) && unconsciousValue.equalsIgnoreCase("yes")){

                next_button.setText(getString(R.string.referrel));
                isReferred = true;
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.senseless));
            }
            if(!TextUtils.isEmpty(convulsionValue) && convulsionValue.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.convulsions_history));
                isReferred = true;
            }
            if(!TextUtils.isEmpty(feedingProblem) && feedingProblem.equalsIgnoreCase("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.not_eating));
                isReferred = true;
            }
            if(!TextUtils.isEmpty(vomiting) && vomiting.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.Persistent_vomiting));
            }
            if(!TextUtils.isEmpty(bulging) && bulging.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.head_spit));
            }
            if(!TextUtils.isEmpty(stopBreathing) && stopBreathing.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.sleep_20_sec));
            }
            if(!TextUtils.isEmpty(centralCyanosis) && centralCyanosis.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.body_blue_shape));
            }
            if(!TextUtils.isEmpty(bleeding) && bleeding.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.Severe_bleeding));
            }
            if(!TextUtils.isEmpty(weight) && weight.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.weight_less_1500));
                builder.append("<br>");
            }
            if(!TextUtils.isEmpty(malformation) && malformation.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.disability));
            }
            if(!TextUtils.isEmpty(surgicalCondition) && surgicalCondition.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.ot_refer_hospital));
            }
            if(!TextUtils.isEmpty(chestIndrawing) && chestIndrawing.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.chest_down));
            }
            if(!TextUtils.isEmpty(lowBodyTemperature) && lowBodyTemperature.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.fever_37));
            }
            if(!TextUtils.isEmpty(difficultyInFeeding) && difficultyInFeeding.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.not_eating_well));
            }
            if((!TextUtils.isEmpty(infantNotMoveAtAll) && infantNotMoveAtAll.equalsIgnoreCase("no"))
              ||(!TextUtils.isEmpty(infantMoveStimulated) && infantMoveStimulated.equalsIgnoreCase("yes"))){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.movement_exciting));
            }
            if(!TextUtils.isEmpty(umbilicusRed) && umbilicusRed.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.red_navel));
            }
            if(!TextUtils.isEmpty(skinPustules) && skinPustules.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.skin_worship));
            }
            if(!TextUtils.isEmpty(calm_state_breathing) && calm_state_breathing.equalsIgnoreCase("yes") ){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                if(type_2.equalsIgnoreCase("1")){
                    builder.append(getString(R.string.breath_60_more));
                }else{
                    builder.append(getString(R.string.breating_60_7_59));
                }

            }
            if(builder.length()>0) assessment_result_tv.setText(Html.fromHtml(builder.toString()));
            if(!assessmentResultTypeId.isEmpty()){
                assesment_result_txt.setText(assessmentResultTypeId);
                if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.SIX.getValue())){
                    assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_yello));
                }else{
                    assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_red));
                }
            }else{
                assesment_result_txt.setText(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.SEVEN.getValue());
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append(getString(R.string.no_signdrome_vectoriea));
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_green));
                assessment_result_tv.setText(Html.fromHtml(builder.toString()));
            }
            if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.TWO.getValue())
            || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.THREE.getValue())
            || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.FOUR.getValue())
            || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.FIVE.getValue())){
                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.treatment_blood_glucose) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.treatment_sent_hospital) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.warm_child_on_hopsital);
                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            }
            if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.SIX.getValue())){
                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.home_local_skin) +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.care_at_home) ;

                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);

            }
            if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.SEVEN.getValue())){
                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        getString(R.string.care_mother) ;

                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void showProgressBar() {
        client_list_progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        client_list_progress.setVisibility(View.GONE);
    }

    @Override
    public void updateAdapter() {

        MemberHistoryAdapter adapter = new MemberHistoryAdapter(getActivity(),onClickAdapter);
        this.assessmentResultRV.setAdapter(adapter);
    }
    @Override
    public void onStart()
    {
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
    public MemberHistoryContract.Presenter getPresenter() {
        return null;
    }

    private MemberHistoryAdapter.OnClickAdapter onClickAdapter = new MemberHistoryAdapter.OnClickAdapter() {
        @Override
        public void onClick(int position, MemberHistoryData content) {

            startFormActivity(content);
        }
    };

    @Override
    public void startFormWithVisitData(MemberHistoryData content, JSONObject jsonForm) {
        try {
            hideProgressBar();
            String eventType = content.getEventType();
//            if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME)){
//                HnppDBUtils.populatePNCChildDetails(content.getBaseEntityId(),jsonForm);
//            }

            if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.NCD_PACKAGE)){
                HnppJsonFormUtils.addNcdSugerPressure(baseEntityId,jsonForm);
            }
            makeReadOnlyFields(jsonForm);

            Intent intent = new Intent(getActivity(), HnppFormViewActivity.class);
            intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

            Form form = new Form();
            form.setWizard(false);
            if(!HnppConstants.isReleaseBuild()){
                form.setActionBarBackground(R.color.test_app_color);

            }else{
                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

            }
            form.setHideSaveLabel(true);
            form.setSaveLabel("");
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
            intent.putExtra(Constants.WizardFormActivity.EnableOnCloseDialog, false);
            if (this != null) {
                this.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void updateANCTitle() {

    }

    private void startFormActivity(MemberHistoryData content){
        showProgressBar();

    }

    public void makeReadOnlyFields(JSONObject jsonObject){
        try {
            int count = jsonObject.getInt("count");
            for(int i= 1;i<=count;i++){
                JSONObject steps = jsonObject.getJSONObject("step"+i);
                JSONArray ja = steps.getJSONArray(JsonFormUtils.FIELDS);

                for (int k = 0; k < ja.length(); k++) {
                    JSONObject fieldObject =ja.getJSONObject(k);
                    fieldObject.put(JsonFormUtils.READ_ONLY, true);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(presenter!=null)
//        presenter.fetchData(baseEntityId);
//    }
}