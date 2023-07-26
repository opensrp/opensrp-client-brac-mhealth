package org.smartregister.brac.hnpp.fragment;

import static org.smartregister.chw.anc.util.JsonFormUtils.updateFormField;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.HnppAncJsonFormActivity;
import org.smartregister.brac.hnpp.activity.HnppChildProfileActivity;
import org.smartregister.brac.hnpp.activity.HouseHoldVisitActivity;
import org.smartregister.brac.hnpp.activity.IndividualProfileRemoveActivity;
import org.smartregister.brac.hnpp.adapter.MemberListAdapter;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.presenter.HnppChildProfilePresenter;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.malaria.activity.BaseMalariaRegisterActivity;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.Utils;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.Date;

public class MemberListDialogFragment extends DialogFragment {
    ArrayList<Member> memberArrayList;
    public static final String DIALOG_TAG = "member_list_dialog";

    private Context context;

    public static MemberListDialogFragment newInstance() {
        return new MemberListDialogFragment();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setData(ArrayList<Member> memberArrayList) {
        this.memberArrayList = memberArrayList;
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

                startAnyFormActivity(HnppConstants.EventType.REMOVE_MEMBER,content,200);
            }
        });
        adapter.setData(memberArrayList);
        RecyclerView recyclerView = view.findViewById(R.id.memberListRv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("nnnnnnn",""+requestCode+" "+resultCode+" "+data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON));
        HouseHoldVisitActivity.removedMemberListJson.add(data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON));
    }

    public void startAnyFormActivity(String formName, Member member, int requestCode) {
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

                    JSONObject jsonForm = FormParser.loadFormFromAsset(formName);
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
}
