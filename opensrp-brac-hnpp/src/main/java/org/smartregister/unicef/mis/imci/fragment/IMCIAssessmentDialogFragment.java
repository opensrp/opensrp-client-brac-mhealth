package org.smartregister.unicef.mis.imci.fragment;

import static org.smartregister.unicef.mis.fragment.MemberHistoryFragment.END_TIME;
import static org.smartregister.unicef.mis.fragment.MemberHistoryFragment.START_TIME;
import static org.smartregister.unicef.mis.imci.activity.ImciMainActivity.REQUEST_IMCI_DIARRHEA_0_2;
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
import org.smartregister.unicef.mis.activity.HnppFormViewActivity;
import org.smartregister.unicef.mis.adapter.MemberHistoryAdapter;
import org.smartregister.unicef.mis.contract.MemberHistoryContract;
import org.smartregister.unicef.mis.imci.Utility;
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
    TextView assesment_result_txt, assessment_result_tv,treatment_result_tv;
    Button next_button;
    String jsonData;
    int requestType;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && !isStart){
            presenter.fetchCurrentTimeLineHistoryData(baseEntityId,startVisitDate,endVisitDate);
        }
    }

    public void setJsonData(int requestType,String jsonData) {
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
        view.findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
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
        }
//        presenter.fetchCurrentTimeLineHistoryData(baseEntityId,startVisitDate,endVisitDate);
    }
    private void processDiarrheaAssessment(){

    }
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
            String assessmentResultTypeId = "";
            if(!TextUtils.isEmpty(unconsciousValue) && unconsciousValue.equalsIgnoreCase("yes")){
                assessmentResultTypeId = Utility.ASSESSMENT_RESULT_TYPE_SEVERE.TWO.getValue();
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