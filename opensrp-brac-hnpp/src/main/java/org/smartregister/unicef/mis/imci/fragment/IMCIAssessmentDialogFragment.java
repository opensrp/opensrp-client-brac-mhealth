package org.smartregister.unicef.mis.imci.fragment;

import static org.smartregister.unicef.mis.fragment.MemberHistoryFragment.END_TIME;
import static org.smartregister.unicef.mis.fragment.MemberHistoryFragment.START_TIME;
import static org.smartregister.unicef.mis.imci.activity.ImciMainActivity.REQUEST_IMCI_DIARRHEA_0_2;
import static org.smartregister.unicef.mis.imci.activity.ImciMainActivity.REQUEST_IMCI_FEEDING_0_2;
import static org.smartregister.unicef.mis.imci.activity.ImciMainActivity.REQUEST_IMCI_SEVERE_0_2;

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
import org.smartregister.unicef.mis.presenter.MemberHistoryPresenter;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppJsonFormUtils;
import org.smartregister.unicef.mis.utils.MemberHistoryData;

public class IMCIAssessmentDialogFragment extends DialogFragment implements MemberHistoryContract.View {
    public static final String DIALOG_TAG = "MemberHistoryDialogFragment_DIALOG_TAG";
    public static final String IS_GUEST_USER = "IS_GUEST_USER";

    private MemberHistoryPresenter presenter;
    private RecyclerView assessmentResultRV,treatmentResultRV;
    private String baseEntityId;
    private boolean isStart = true;
    private ProgressBar client_list_progress;
    long startVisitDate,endVisitDate;
    TextView assesment_result_txt, assessment_result_tv,treatment_result_tv,treatment_label_tv;
    Button next_button;
    String jsonData;
    int requestType;
    boolean isReferred = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && !isStart){
            presenter.fetchCurrentTimeLineHistoryData(baseEntityId,startVisitDate,endVisitDate);
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
                        imciMainActivity.openRefereal(assessmentResultTypeId);
                    }else if(requestType == REQUEST_IMCI_SEVERE_0_2){
                        imciMainActivity.startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_DIARRHEA_0_2,REQUEST_IMCI_DIARRHEA_0_2);
                    }else if(requestType == REQUEST_IMCI_DIARRHEA_0_2){
                        imciMainActivity.startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_FEEDING_0_2,REQUEST_IMCI_FEEDING_0_2);
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
        presenter = new MemberHistoryPresenter(this);
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
        }
//        presenter.fetchCurrentTimeLineHistoryData(baseEntityId,startVisitDate,endVisitDate);
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
        StringBuilder builder = new StringBuilder();
        if(type_1.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_FEEDING.THREE.getValue();
            builder.append("<br>");
            builder.append(getString(R.string.right_arrow));
            builder.append("বয়স ৭ দিন এবং ওজন ২ কেজি থেকে কম");
        }
        else if(type_2.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_FEEDING.TWO.getValue();
            if(!TextUtils.isEmpty(sucking_effectively) && !sucking_effectively.contains("effectively")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("ভালভাবে দুধ চুষে না ");
            }
            if(!TextUtils.isEmpty(drink_breast_milk) && drink_breast_milk.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("২৪ ঘন্টার মধ্যে বুকের দুধ ৮ বারের চেয়ে কম খায় ");
            }
            if(!TextUtils.isEmpty(other_food_drink) && other_food_drink.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("অন্য খাবার বা তরল খাবার খায়");
            }
            if((!TextUtils.isEmpty(weight_less_than_2_kg) && weight_less_than_2_kg.equalsIgnoreCase("yes") )||
                    (!TextUtils.isEmpty(weight_proportion_zscore) && weight_proportion_zscore.equalsIgnoreCase("yes") )){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("বয়স অনুপাতে ওজন কম ");
            }
            if(!TextUtils.isEmpty(Thrush) && Thrush.equalsIgnoreCase("yes")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("থ্রাশ (মুখে ঘা অথবা সাদা ঘা) ");
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
                builder.append("ভালো পজিশন নয় বা ভালভাবে বুকে লাগানো হয় নি");
            }
        }
        else if(type_3.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_FEEDING.ONE.getValue();
            if(!TextUtils.isEmpty(sucking_effectively) && sucking_effectively.contains("effectively")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("ভালভাবে দুধ চুষে");
            }
            if(!TextUtils.isEmpty(drink_breast_milk) && drink_breast_milk.equalsIgnoreCase("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("২৪ ঘন্টার মধ্যে বুকের দুধ ৮ বারের চেয়ে বেশি খায়");
            }
            if(!TextUtils.isEmpty(other_food_drink) && other_food_drink.equalsIgnoreCase("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("অন্য খাবার বা তরল খাবার খায় না");
            }
            if((!TextUtils.isEmpty(weight_less_than_2_kg) && weight_less_than_2_kg.equalsIgnoreCase("no") )&&
                    (!TextUtils.isEmpty(weight_proportion_zscore) && weight_proportion_zscore.equalsIgnoreCase("no") )){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("বয়স অনুপাতে ওজন কম নয়");
            }
            if(!TextUtils.isEmpty(Thrush) && Thrush.equalsIgnoreCase("no")){
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("থ্রাশ (মুখে ঘা অথবা সাদা ঘা) নেই");
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
                builder.append("ভালো পজিশন বা ভালভাবে বুকে লাগানো হয়েছে");
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
                    " রক্তে গ্লুকোজের স্বল্পতা রোধ করতে যথাযথ চিকিৎসা দিন।" +
                    "<br>" +
                    getString(R.string.right_arrow) +
                    " ক্যাংগারু মাদার কেয়ারের জন্য হাসপাতালে প্রেরন করুন।" +
                    "<br>" +
                    getString(R.string.right_arrow) +
                    " হাসপাতালে যাওয়ার পথে ছোট শিশুটির গা কেমন করে গরম রাখতে হবে সে সম্পর্কে মাকে পরামর্শ দিন।";
            treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
            treatment_label_tv.setVisibility(View.VISIBLE);
        }else if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_FEEDING.TWO.getValue())){

            String treatmentBuilder = "</br>" +
                    getString(R.string.right_arrow) +
                    " যদি ভালভাবে বুকে লাগানো না হয় অথবা ভালভাবে দুধ না চুষে, তাহলে মাকে ভাল পজিশন এবং ভাল ভাবে বুকে লাগানো সম্পর্কে শিখিয়ে দিন।" +
                    "<br>" +
                    getString(R.string.right_arrow) +
                    " যদি ভালভাবে বুকে লাগাতে না পারে তবে বুকের দুধ কি ভাবে চেপে বের করে বাচ্চাকে খাওয়াতে হয় সে সম্পর্কে শিখিয়ে দিন।" +
                    "<br>" +
                    getString(R.string.right_arrow) +
                    " ২৪ ঘন্টার মধ্যে বুকের দুধ ৮ বারের চেয়ে কম খাওয়ানো হলে, আরো বেশী বার খাওয়ানোর পরামর্শ দিন। দিনে ও রাতে ছোট শিশুটিকে যতবার এবং যতক্ষণ খেতে চায়, ততবার খাওয়াতে মা-কে পরামর্শ দিন।"+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    " যদি অন্য কোন খাবার বা তরল খাবার খায় তবে মাকে পরামর্শ দিন ঐ সব খাবারের পরিমান কমিয়ে মা যেন বার বার বুকের দুধ খাওয়ান এবং কাপ দিয়ে খাওয়ান।"+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    " যদি বুকের দুধ একেবারেই না খায় - বুকের দুধ খাওয়ানো সম্ভব হলে পুনরায় বুকের দুধ চালুর পরামর্শ গ্রহণের জন্য হাসপাতালে রেফার করুন।"+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    " যদি বুকের দুধ একেবারেই না খায় - কি ভাবে বুকের দুধের পরিপূরক খাবার তৈরী করতে হয় সে ব্যাপারে মাকে পরামর্শ দিন।"+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    " কম জন্ম ওজনের ছোট শিশুকে কিভাবে খাওয়াতে হবে এবং শরীর গরম রাখতে হবে সে সম্পর্কে মাকে উপদেশ দিন।"+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    " থ্রাশ হয়ে থাকলে, বাড়ীতে থ্রাশের চিকিৎসা ব্যবস্থা মাকে বুঝিয়ে দিন।"+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    " ছোট শিশুটিকে বাড়ীতে যত্ন নেয়ার জন্য মাকে পরামর্শ দিন।"+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    " অবিলম্বে কখন আসতে হবে সে সম্পর্কে মাকে পরামর্শ দিন।"+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    " খাওয়ানোর সমস্যা বা থ্রাশ দেখা দিলে দুই দিনের মধ্যেই ফলোআপ-এর জন্য আসুন।"+
                    "<br>" +
                    getString(R.string.right_arrow) +
                    " বয়স অনুপাতে ওজন কম হলে ১৪ দিন পর ফলোআপ।";

                    treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                    treatment_label_tv.setVisibility(View.VISIBLE);
        }else{
            String treatmentBuilder = "</br>" +
                    getString(R.string.right_arrow) +
                    " ছোট শিশুটিকে বাড়ীতে যত্ন নেয়ার জন্য মাকে পরামর্শ দিন।" +
                    "<br>" +
                    getString(R.string.right_arrow) +
                    " ছোট শিশুকে ভালো ভাবে খাওয়ানোর জন্য মায়ের প্রশংসা করুন।";
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
        String type_4 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_4");
        String type_5 = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"cal_assessment_type_5");
        StringBuilder builder = new StringBuilder();
        if(type_1.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA.THREE.getValue();
            builder.append("<br>");
            builder.append(getString(R.string.right_arrow));
            builder.append("চোখ বসে গেছে");
            builder.append("<br>");
            builder.append(getString(R.string.right_arrow));
            builder.append("চামড়া টেনে ধরে ছেড়ে দিতে হবে খুব ধীরে ধীরে স্বাভাবিক অবস্থায় ফিরে যায়");
            builder.append("<br>");
            builder.append(getString(R.string.right_arrow));
            builder.append("শিশু কে নড়াচড়া করানোর চেষ্টা করলে শুধুমাত্র নড়াচড়া করতে পারে অথবা একবারেই নড়াচড়া করতে পারে না");
        }else if(type_2.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA.TWO.getValue();
            builder.append("<br>");
            builder.append(getString(R.string.right_arrow));
            builder.append("অস্থির, খিটখিটে");
            builder.append("<br>");
            builder.append(getString(R.string.right_arrow));
            builder.append("চোখ বসে গেছে");
            builder.append("<br>");
            builder.append(getString(R.string.right_arrow));
            builder.append("চামড়া টেনে ধরে ছেড়ে দিতে হবে খুব ধীরে ধীরে স্বাভাবিক অবস্থায় ফিরে যায়");
        }else if(type_3.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA.TWO.getValue();
            builder.append("<br>");
            builder.append(getString(R.string.right_arrow));
            builder.append("অস্থির, খিটখিটে");
            builder.append("<br>");
            builder.append(getString(R.string.right_arrow));
            builder.append("চোখ বসে গেছে");
        }else if(type_4.equalsIgnoreCase("1")){
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA.TWO.getValue();
            builder.append("<br>");
            builder.append(getString(R.string.right_arrow));
            builder.append("চোখ বসে গেছে");
            builder.append("<br>");
            builder.append(getString(R.string.right_arrow));
            builder.append("চামড়া টেনে ধরে ছেড়ে দিতে হবে খুব ধীরে ধীরে স্বাভাবিক অবস্থায় ফিরে যায়");
        }else {
            assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_DIARRHEA.ONE.getValue();
            builder.append("<br>");
            builder.append(getString(R.string.right_arrow));
            builder.append("কিছু অথবা চরম পানি স্বল্পতার কোন চিহ্ন নেই ");
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
                    " জরুরীভিত্তিতে হাসপাতালে প্রেরন করুন।" +
                    "<br>" +
                    getString(R.string.right_arrow) +
                    " মাকে বলুন, হাসপাতালে যাওয়ার পথে ছোট শিশুকে বারবার খাবার স্যলাইন (ORS) খাওয়াতে।" +
                    "<br>" +
                    getString(R.string.right_arrow) +
                    " হাসপাতালে যাওয়ার পথে ছোট শিশুটির গা কেমন করে গরম রাখতে হবে সে সম্পর্কে মাকে পরামর্শ দিন।" +
                    "<br>" +
                    getString(R.string.right_arrow) +
                    " বুকের দুধ খাওয়ানো অব্যাহত রাখতে মাকে পরার্মশ দিন।";
            treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
            treatment_label_tv.setVisibility(View.VISIBLE);
        }
    }
    String assessmentResultTypeId = "";
    private void processSevereAssessment(){
        try {
            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = HnppJsonFormUtils.validateParameters(jsonData);
            JSONObject jsonForm = (JSONObject)registrationFormParams.getMiddle();
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
            StringBuilder builder = new StringBuilder();

            if(!TextUtils.isEmpty(unconsciousValue) && unconsciousValue.equalsIgnoreCase("yes")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.TWO.getValue();
                next_button.setText(getString(R.string.referrel));
                isReferred = true;
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("অচেতন/ঝিমুনি ");
            }
            if(!TextUtils.isEmpty(convulsionValue) && convulsionValue.equalsIgnoreCase("yes")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.TWO.getValue();
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("খিচুনি অথবা খিঁচুনির ইতিহাস ");
            }
            if(!TextUtils.isEmpty(feedingProblem) && feedingProblem.equalsIgnoreCase("no")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.TWO.getValue();
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("শিশু খাওয়া খেতে পারছে না");
            }
            if(!TextUtils.isEmpty(vomiting) && vomiting.equalsIgnoreCase("yes")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.TWO.getValue();
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("ক্রমাগত বমি");
            }
            if(!TextUtils.isEmpty(bulging) && bulging.equalsIgnoreCase("yes")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.TWO.getValue();
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("মাথার তালু স্ফীত হয়ে যাওয়া ");
            }
            if(!TextUtils.isEmpty(stopBreathing) && stopBreathing.equalsIgnoreCase("yes")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.TWO.getValue();
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("ঘুমের ২০ সেকেন্ড বা তার বেশি সময়ের জন্য শ্বাস বন্ধ হয়");
            }
            if(!TextUtils.isEmpty(centralCyanosis) && centralCyanosis.equalsIgnoreCase("yes")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.TWO.getValue();
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("শরীরের বিশেষ অংশ নীলবর্ণ ধারন ");
            }
            if(!TextUtils.isEmpty(bleeding) && bleeding.equalsIgnoreCase("yes")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.TWO.getValue();
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("গুরুতর রক্তপাত");
            }
            if(!TextUtils.isEmpty(weight) && weight.equalsIgnoreCase("yes")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.TWO.getValue();
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("জন্ম ওজন <১৫০০ গ্রাম");
            }
            if(!TextUtils.isEmpty(malformation) && malformation.equalsIgnoreCase("yes")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.TWO.getValue();
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("গুরুতর জন্মগত বিকলাঙ্গতা ");
            }
            if(!TextUtils.isEmpty(surgicalCondition) && surgicalCondition.equalsIgnoreCase("yes")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.TWO.getValue();
                builder.append("\n");
                builder.append(getString(R.string.right_arrow));
                builder.append("এমন অস্ত্রোপচার যার জন্য হাসপাতালে থাকতে হয়েছিল ");
            }
            if(!TextUtils.isEmpty(chestIndrawing) && chestIndrawing.equalsIgnoreCase("yes")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.THREE.getValue();
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("বুকের নীচের অংশ মারাÍক ডেবে যায়");
            }
            if(!TextUtils.isEmpty(lowBodyTemperature) && lowBodyTemperature.equalsIgnoreCase("yes")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.THREE.getValue();
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("জ্বর (৩৭.৫০ সেন্টি* বা বেশী বা গরম অনুভুতি) অথবা শরীরে অল্প তাপমাত্রা (৩৫.৫০ সেন্টি* কম বা ঠান্ডা অনুভব হয়)");
            }
            if(!TextUtils.isEmpty(difficultyInFeeding) && difficultyInFeeding.equalsIgnoreCase("yes")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.THREE.getValue();
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("ভালোভাবে খায় না");
            }
            if((!TextUtils.isEmpty(infantNotMoveAtAll) && infantNotMoveAtAll.equalsIgnoreCase("no"))
              ||(!TextUtils.isEmpty(infantMoveStimulated) && infantMoveStimulated.equalsIgnoreCase("yes"))){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.THREE.getValue();
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("শিশুটি কি শুধু মাত্র উত্তেজিত করলে নড়াচড়া করে / একেবারেই নড়াচড়া করে না");
            }
            if(!TextUtils.isEmpty(umbilicusRed) && umbilicusRed.equalsIgnoreCase("yes")){
                if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.TWO.getValue())
                 || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.THREE.getValue())){

                }else{
                    assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.SIX.getValue();
                }
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("নাভী লাল অথবা পুঁজ পড়ছে");
            }
            if(!TextUtils.isEmpty(skinPustules) && skinPustules.equalsIgnoreCase("yes")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.SIX.getValue();
                builder.append("<br>");
                builder.append(getString(R.string.right_arrow));
                builder.append("চামড়ায় কিছু পূঁজ সহ দানা");
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
                assesment_result_txt.setBackgroundColor(getResources().getColor(R.color.imci_green));
            }
            if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.TWO.getValue())
            || assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.THREE.getValue())){
                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        " মুখে খাওয়ার এমক্সিলিনের প্রথম ডোজ দিন।" +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        " রক্তে গ্লুকোজের স্বল্পতা রোধ করতে যথাযথ চিকিৎসা দিন।" +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        " জরুরীভিত্তিতে হাসপাতালে প্রেরন করুন।" +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        " হাসপাতালে যাওয়ার পথে ছোট শিশুটির গা কেমন করে গরম রাখতে হবে সে সম্পর্কে মাকে পরামর্শ দিন।";
                treatment_result_tv.setText(Html.fromHtml(treatmentBuilder));
                treatment_label_tv.setVisibility(View.VISIBLE);
            }
            if(assessmentResultTypeId.equalsIgnoreCase(Utility.ASSESSMENT_RESULT_TYPE_SEVERE.SIX.getValue())){
                String treatmentBuilder = "</br>" +
                        getString(R.string.right_arrow) +
                        " ৫ দিন ধরে মুখে খাওয়ার এমক্সিলিন দিন।" +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        " বাড়ীতে স্থানীয় চামড়ার সংক্রমণের চিকিৎসা দিতে মাকে বুঝিয়ে দিন।" +
                        "<br>" +
                        getString(R.string.right_arrow) +
                        "  ছোট শিশুটিকে বাড়ীতে যত্ন নেয়ার জন্য মাকে পরামর্শ দিন।" ;

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
        adapter.setData(presenter.getMemberHistory());
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
        return presenter;
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
        presenter.getVisitFormWithData(content);

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