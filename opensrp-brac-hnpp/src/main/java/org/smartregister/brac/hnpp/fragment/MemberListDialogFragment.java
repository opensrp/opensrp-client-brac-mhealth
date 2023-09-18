package org.smartregister.brac.hnpp.fragment;

import static org.smartregister.chw.anc.util.JsonFormUtils.updateFormField;
import static org.smartregister.util.JsonFormUtils.getFieldJSONObject;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.HnppAncJsonFormActivity;
import org.smartregister.brac.hnpp.activity.HouseHoldVisitActivity;
import org.smartregister.brac.hnpp.adapter.MemberListAdapter;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.MemberTypeEnum;
import org.smartregister.chw.core.contract.FamilyRemoveMemberContract;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.configurableviews.model.RegisterConfiguration;
import org.smartregister.configurableviews.model.ViewConfiguration;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

public class MemberListDialogFragment extends DialogFragment implements FamilyRemoveMemberContract.Model{
    static int REQUEST_CODE = 601;
    ArrayList<Member> memberArrayList;
    public static final String DIALOG_TAG = "member_list_dialog";

    private Context context;
    MemberTypeEnum memberTypeEnum;

    public static MemberListDialogFragment newInstance() {
        return new MemberListDialogFragment();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setData(ArrayList<Member> memberArrayList, MemberTypeEnum memberTypeEnum) {
        this.memberArrayList = memberArrayList;
        this.memberTypeEnum = memberTypeEnum;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(android.app.DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_NoActionBar);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        // without a handler, the window sizes itself correctly
        // but the keyboard does not show up
        new Handler().post(() -> getDialog().getWindow().setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_member_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
      /*  view.findViewById(org.smartregister.chw.core.R.id.close).setOnClickListener(this);
        view.findViewById(org.smartregister.chw.core.R.id.layout_add_child_under_five).setOnClickListener(this);
        view.findViewById(org.smartregister.chw.core.R.id.layout_add_other_family_member).setOnClickListener(this);*/
        MemberListAdapter adapter = new MemberListAdapter(getActivity(), new MemberListAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, Member content) {
               /* IndividualProfileRemoveActivity.startIndividualProfileActivity(
                        getActivity(),
                        getClientDetailsByBaseEntityID(
                                content.getBaseEntityId()),
                                content.getFamilyBaseEntityId(),
                                content.getFamilyHead(),
                                content.getCareGiver(),
                                BaseMalariaRegisterActivity.class.getCanonicalName());*/
                startAnyFormActivity(HnppConstants.EventType.REMOVE_MEMBER,memberTypeEnum,content,REQUEST_CODE);
            }
        });
        adapter.setData(memberArrayList);
        RecyclerView recyclerView = view.findViewById(R.id.memberListRv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE){
            if(memberTypeEnum == MemberTypeEnum.DEATH){
                HouseHoldVisitActivity.removedMemberListJson.add(data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON));
            }else if(memberTypeEnum == MemberTypeEnum.MIGRATION){
                HouseHoldVisitActivity.migratedMemberListJson.add(data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON));
            }

        }

    }

    public void startAnyFormActivity(String formName, MemberTypeEnum memberTypeEnum, Member member, int requestCode) {
        if(!HnppApplication.getStockRepository().isAvailableStock(HnppConstants.formNameEventTypeMapping.get(formName))){
            HnppConstants.showOneButtonDialog(getActivity(),getString(R.string.dialog_stock_sell_end),"");
            return;
        }
        HnppConstants.getGPSLocationContext((HouseHoldVisitActivity) getActivity(), new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                try {
                    if(TextUtils.isEmpty(member.getBaseEntityId())){
                        Toast.makeText(getActivity(), "baseentityid null", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    HnppConstants.appendLog("SAVE_VISIT", "open form>>childBaseEntityId:"+member.getBaseEntityId()+":formName:"+formName);

                    JSONObject jsonForm =prepareJsonForm(getClientDetailsByBaseEntityID(member.getBaseEntityId()),formName);
                    try{
                        HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    try{
                        HnppJsonFormUtils.addAddToStockValue(jsonForm);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"details",member.getName());

                    JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                    JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);


                    if(memberTypeEnum == MemberTypeEnum.DEATH){
                        updateFormField(jsonArray, "remove_reason", "মৃত্যু নিবন্ধন");
                        JSONObject remove_reason = getFieldJSONObject(jsonArray, "remove_reason");
                        remove_reason.put(org.smartregister.family.util.JsonFormUtils.READ_ONLY, true);
                    }else if(memberTypeEnum == MemberTypeEnum.MIGRATION){
                        updateFormField(jsonArray, "remove_reason", "স্থানান্তর");
                        JSONObject remove_reason = getFieldJSONObject(jsonArray, "remove_reason");
                        remove_reason.put(org.smartregister.family.util.JsonFormUtils.READ_ONLY, true);
                    }

                    updateFormField(jsonArray, "details", member.getName());

                    jsonForm.put(JsonFormUtils.ENTITY_ID, member.getFamilyHead());
                    Intent intent = new Intent(getActivity(), HnppAncJsonFormActivity.class);
                    intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

                    Form form = new Form();
                    form.setWizard(false);
                    if(!HnppConstants.isReleaseBuild()){
                        form.setActionBarBackground(R.color.test_app_color);

                    }else{
                        form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                    }
                    intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
                    intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, true);
                    startActivityForResult(intent, requestCode);

                }catch (Exception e){

                }
            }
        });


    }

    private static CommonPersonObjectClient getClientDetailsByBaseEntityID(@NonNull String baseEntityId) {
        CommonRepository commonRepository = Utils.context().commonrepository(Utils.metadata().familyMemberRegister.tableName);

        final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(baseEntityId);
        final CommonPersonObjectClient client =
                new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
        client.setColumnmaps(commonPersonObject.getColumnmaps());
        return client;

    }

    /**
     * prepare json form for member remove
     * @param client
     * @param formType
     * @return
     */
    @Override
    public JSONObject prepareJsonForm(CommonPersonObjectClient client, String formType) {
        try {
            FormUtils formUtils = FormUtils.getInstance(Utils.context().applicationContext());
            JSONObject form = FormParser.loadFormFromAsset(formType);

            form.put(JsonFormUtils.ENTITY_ID, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false));
            // inject data into the form

            JSONObject stepOne = form.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if (jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.DOB)) {

                    String dobString = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false);
                    if (StringUtils.isNotBlank(dobString)) {
                        Date dob = Utils.dobStringToDate(dobString);
                        if (dob != null) {
                            jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, JsonFormUtils.dd_MM_yyyy.format(dob));
                            JSONObject min_date = CoreJsonFormUtils.getFieldJSONObject(jsonArray, "date_moved");
                            JSONObject date_died = CoreJsonFormUtils.getFieldJSONObject(jsonArray, "date_died");

                            // dobString = Utils.getDuration(dobString);
                            //dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : "";
                            int days = CoreJsonFormUtils.getDayFromDate(dobString);
                            min_date.put("min_date", "today-" + days + "d");
                            date_died.put("min_date", "today-" + days + "d");
                        }
                    }
                } else if (jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY).equalsIgnoreCase(CoreConstants.JsonAssets.DETAILS)) {

                    String dob = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false);
                    String dobString = Utils.getDuration(dob);
                    dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString;

                    String details = String.format("%s %s %s, %s %s",
                            Utils.getValue(client.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true),
                            Utils.getValue(client.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true),
                            Utils.getValue(client.getColumnmaps(), DBConstants.KEY.LAST_NAME, true),
                            dobString,
                            Utils.getValue(client.getColumnmaps(), DBConstants.KEY.GENDER, true)
                    );

                    jsonObject.put("text", details);

                }
            }

            return form;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getForm(CommonPersonObjectClient client) {
        return null;
    }

    @Override
    public JSONObject prepareFamilyRemovalForm(String familyID, String familyName, String details) {
        return null;
    }

    @Override
    public RegisterConfiguration defaultRegisterConfiguration() {
        return null;
    }

    @Override
    public ViewConfiguration getViewConfiguration(String s) {
        return null;
    }

    @Override
    public Set<org.smartregister.configurableviews.model.View> getRegisterActiveColumns(String s) {
        return null;
    }

    @Override
    public String countSelect(String s, String s1) {
        return null;
    }

    @Override
    public String mainSelect(String s, String s1) {
        return null;
    }
}
