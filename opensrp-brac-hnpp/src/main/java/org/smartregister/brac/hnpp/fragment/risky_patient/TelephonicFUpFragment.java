package org.smartregister.brac.hnpp.fragment.risky_patient;

import static org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;
import com.vijay.jsonwizard.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.FamilyRegisterActivity;
import org.smartregister.brac.hnpp.activity.GuestAddMemberJsonFormActivity;
import org.smartregister.brac.hnpp.activity.GuestMemberActivity;
import org.smartregister.brac.hnpp.activity.HnppAncJsonFormActivity;
import org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.brac.hnpp.activity.HouseHoldVisitActivity;
import org.smartregister.brac.hnpp.activity.RiskyPatientActivity;
import org.smartregister.brac.hnpp.adapter.RoutinFUpListAdapter;
import org.smartregister.brac.hnpp.adapter.SpecialFUpListAdapter;
import org.smartregister.brac.hnpp.adapter.TelephonicFUpListAdapter;
import org.smartregister.brac.hnpp.contract.TelephonicFUpContract;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.model.AncFollowUpModel;
import org.smartregister.brac.hnpp.model.RiskyPatientFilterType;
import org.smartregister.brac.hnpp.presenter.SpecialFUpPresenter;
import org.smartregister.brac.hnpp.presenter.TelephonicFUpPresenter;
import org.smartregister.brac.hnpp.repository.AncFollowUpRepository;
import org.smartregister.brac.hnpp.service.HnppHomeVisitIntentService;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.util.FormUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * A telephonic f/up fragment
 */
public class TelephonicFUpFragment extends Fragment implements TelephonicFUpContract.View {
    final String TAG = "TelephonicFUpFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final int REQUEST_PHONE_CALL = 101;
    public static final int REQUEST_CODE_GET_JSON = 2244;
    TelephonicFUpPresenter presenter;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    boolean isProcessing = false;
    private AncFollowUpModel currentFollowupContent;
    TextInputEditText searchField;
    AppCompatButton filterBt;
    TextView noDataFoundTv;

    String searchedText = "";
    RiskyPatientFilterType riskyPatientFilterType = new RiskyPatientFilterType();

    public static TelephonicFUpFragment newInstance(int index) {
        TelephonicFUpFragment fragment = new TelephonicFUpFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_telephonic_f_up, container, false);
        recyclerView = root.findViewById(R.id.telephonyFollowUpListRv);
        progressBar = root.findViewById(R.id.progress_bar);
        searchField = root.findViewById(R.id.editText);
        filterBt = root.findViewById(R.id.filter_bt);
        noDataFoundTv = root.findViewById(R.id.no_data_found_tv);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchedText = charSequence.toString();
                filterList(searchedText, riskyPatientFilterType);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        filterBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startFilterDialog();
            }
        });

        return root;
    }

    private void startFilterDialog() {
        RiskyPatientFilterDialogFragment dialogFragment = new RiskyPatientFilterDialogFragment();
        dialogFragment.setTargetFragment(this,1);
        dialogFragment.show(getActivity().getSupportFragmentManager(),TAG);
    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void updateView() {

    }

    @Override
    public void noDataFound() {
        noDataFoundTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void initializePresenter() {
        presenter = new TelephonicFUpPresenter(this);
        showProgressBar();
        ArrayList<AncFollowUpModel> list = presenter.fetchData();
        if(list.isEmpty()){
            noDataFound();
        }else {
            noDataFoundTv.setVisibility(View.GONE);
        }
        TelephonicFUpListAdapter adapter = new TelephonicFUpListAdapter(getActivity(),
                new TelephonicFUpListAdapter.OnClickAdapter() {
                    @Override
                    public void onClick(int position, AncFollowUpModel content) {
                        launchCall(content);
                    }
                },
                new TelephonicFUpListAdapter.OnClickAdapter() {
                    @Override
                    public void onClick(int position, AncFollowUpModel content) {
                        currentFollowupContent = content;
                        startFollowupActivity(content);
                    }
                },
                new TelephonicFUpListAdapter.OnClickAdapter() {
                    @Override
                    public void onClick(int position, AncFollowUpModel content) {
                        openProfile(content);
                    }
                });
        adapter.setData(list);
        recyclerView.setAdapter(adapter);
        hideProgressBar();
    }

    private void filterList(CharSequence charSequence, RiskyPatientFilterType riskyPatientFilterType) {
        showProgressBar();
        ArrayList<AncFollowUpModel> list =  presenter.fetchSearchedData(charSequence.toString(),riskyPatientFilterType);
        if(list.isEmpty()){
            noDataFound();
        }else {
            noDataFoundTv.setVisibility(View.GONE);
        }
        TelephonicFUpListAdapter adapter = new TelephonicFUpListAdapter(getActivity(),
                new TelephonicFUpListAdapter.OnClickAdapter() {
                    @Override
                    public void onClick(int position, AncFollowUpModel content) {
                        launchCall(content);
                    }
                },
                new TelephonicFUpListAdapter.OnClickAdapter() {
                    @Override
                    public void onClick(int position, AncFollowUpModel content) {
                        currentFollowupContent = content;
                        startFollowupActivity(content);
                    }
                },
                new TelephonicFUpListAdapter.OnClickAdapter() {
                    @Override
                    public void onClick(int position, AncFollowUpModel content) {
                        openProfile(content);
                    }
                });
        adapter.setData(list);
        recyclerView.setAdapter(adapter);
        hideProgressBar();
    }

    private void startFollowupActivity(AncFollowUpModel content) {
        HnppConstants.getGPSLocation((RiskyPatientActivity) getActivity(), new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                try{
                    Intent intent = new Intent(getActivity(), HnppAncJsonFormActivity.class);
                    JSONObject jsonForm = FormUtils.getInstance(getActivity()).getFormJson(HnppConstants.JSON_FORMS.ANC_FOLLOWUP_FORM);
                    HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude);
                    String formSubmissionId = HnppDBUtils.getAncHomeVisitFormSubId(content.baseEntityId);
                    JSONObject stepOne = jsonForm.getJSONObject(JsonFormUtils.STEP1);
                    JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);

                    org.smartregister.chw.anc.util.JsonFormUtils.updateFormField(jsonArray, "form_submission_id", formSubmissionId);
                    intent.putExtra(org.smartregister.chw.anc.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
                    Form form = new Form();
                    form.setWizard(false);
                    form.setActionBarBackground(R.color.test_app_color);

                    intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

                    startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1){
            if(resultCode == RiskyPatientFilterDialogFragment.RESULT_CODE){
                riskyPatientFilterType.setVisitScheduleToday(data.getIntExtra(RiskyPatientFilterDialogFragment.VIS_TODAY,0));
                riskyPatientFilterType.setVisitScheduleNextThree(data.getIntExtra(RiskyPatientFilterDialogFragment.VIS_NEXT_THREE,0));
                riskyPatientFilterType.setVisitScheduleNextSeven(data.getIntExtra(RiskyPatientFilterDialogFragment.VIS_NEXT_SEVEN,0));
                riskyPatientFilterType.setVisitScheduleLastDay(data.getIntExtra(RiskyPatientFilterDialogFragment.VIS_LAST_DAY,0));
                riskyPatientFilterType.setVisitScheduleLastThree(data.getIntExtra(RiskyPatientFilterDialogFragment.VIS_LAST_THREE,0));
                riskyPatientFilterType.setVisitScheduleLastSeven(data.getIntExtra(RiskyPatientFilterDialogFragment.VIS_LAST_SEVEN,0));
                riskyPatientFilterType.setVisitScheduleAllDue(data.getIntExtra(RiskyPatientFilterDialogFragment.VIS_ALL_DAY,0));
                filterList(searchedText,riskyPatientFilterType);
            }
        }else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_GET_JSON){
            if(isProcessing) return;
            AtomicInteger isSave = new AtomicInteger(2);
            Utils.showProgressDialog(R.string.empty_string,R.string.please_wait_message,getActivity());

            isProcessing = true;
            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
            String formSubmissionId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
            String visitId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
            processAndSaveVisitForm(jsonString,formSubmissionId,visitId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Integer aInteger) {
                            isSave.set(aInteger);
                            Log.v("SAVE_VISIT","onError>>"+aInteger);
                        }

                        @Override
                        public void onError(Throwable e) {
                            //hideProgressDialog();
                        }

                        @Override
                        public void onComplete() {
                            Log.d("visitCalledCompleted","true");
                            if(isSave.get() == 1){
                                Utils.hideProgressDialog();
                                showServiceDoneDialog(1);
                                HnppApplication.getAncFollowUpRepository().resetTelephonicDate(currentFollowupContent);
                            }else if(isSave.get() == 3){
                                Utils.hideProgressDialog();
                                showServiceDoneDialog(3);
                            }else {
                                Utils.hideProgressDialog();
                                //showServiceDoneDialog(false);
                            }
                        }
                    });
        }
    }

    Dialog dialog;
    private void showServiceDoneDialog(Integer isSuccess){
        if(dialog!=null) return;
        dialog = new Dialog(getActivity());
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        titleTv.setText(isSuccess==1?"সার্ভিসটি দেওয়া সম্পূর্ণ হয়েছে":isSuccess==3?"সার্ভিসটি ইতিমধ্যে দেওয়া হয়েছে":"সার্ভিসটি দেওয়া সফল হয়নি। পুনরায় চেষ্টা করুন ");
        Button ok_btn = dialog.findViewById(R.id.ok_btn);

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialog = null;
                isProcessing = false;
                initializePresenter();
            }
        });
        dialog.show();

    }

    private Observable<Integer> processAndSaveVisitForm(String jsonString, String formSubmissionId, String visitId){
        return  Observable.create(e-> {
            if (TextUtils.isEmpty(currentFollowupContent.baseEntityId)) {
                e.onNext(2);
            }
            Map<String, String> jsonStrings = new HashMap<>();
            //jsonStrings.put("First",jsonString);
            try {
                JSONObject form = new JSONObject(jsonString);
                HnppJsonFormUtils.setEncounterDateTime(form);

                jsonStrings.put("First",form.toString());

                String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                type = HnppJsonFormUtils.getEncounterType(type);

                Visit visit = HnppJsonFormUtils.saveVisit(false,false,false,"", currentFollowupContent.baseEntityId, type, jsonStrings, "",formSubmissionId,visitId);
                if(visit!=null && !visit.getVisitId().equals("0")){
                    HnppHomeVisitIntentService.processVisits();
                    FormParser.processVisitLog(visit);
                    //VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
                    e.onNext(1);
                    e.onComplete();
                }else if(visit!=null && visit.getVisitId().equals("0")){
                    e.onNext(3);
                    e.onComplete();
                }else{
                    e.onNext(2);
                    e.onComplete();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                e.onNext(2);
                e.onComplete();
            }

        });
    }


    @Override
    public void onResume() {
        super.onResume();
        initializePresenter();
    }

    private void launchCall(AncFollowUpModel content) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + content.memberPhoneNum));
        HnppApplication.getAncFollowUpRepository().updateCallStatus(content);
        startActivity(intent);
    }

    @Override
    public TelephonicFUpContract.Presenter getPresenter() {
        return presenter;
    }

    void openProfile(AncFollowUpModel ancFollowUpModel) {

        CommonPersonObjectClient patient = HnppDBUtils.createFromBaseEntity(ancFollowUpModel.baseEntityId);
        if(patient==null) return;
        String familyId = org.smartregister.util.Utils.getValue(patient.getColumnmaps(), ChildDBConstants.KEY.RELATIONAL_ID, false);
        patient.getColumnmaps().put(org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
        String houseHoldHead = org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.HOUSE_HOLD_NAME, true);
        String address = org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.VILLAGE_NAME, true);
        String houseHoldId = org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.HOUSE_HOLD_ID, true);
        String moduleId = org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), HnppConstants.KEY.MODULE_ID, true);
        Intent intent = new Intent(getActivity(), HnppFamilyOtherMemberProfileActivity.class);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, familyId);
        intent.putExtra(CoreConstants.INTENT_KEY.CHILD_COMMON_PERSON, patient);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_HEAD, familyId);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.PRIMARY_CAREGIVER, familyId);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.VILLAGE_TOWN, address);
        intent.putExtra(DBConstants.KEY.UNIQUE_ID,houseHoldId);
        intent.putExtra(HnppConstants.KEY.HOUSE_HOLD_ID,moduleId);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_NAME, houseHoldHead);
        intent.putExtra(HnppFamilyOtherMemberProfileActivity.IS_COMES_IDENTITY,true);
        startActivity(intent);
    }

}