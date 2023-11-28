package org.smartregister.brac.hnpp.fragment.risky_patient;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.FamilyRegisterActivity;
import org.smartregister.brac.hnpp.activity.GuestAddMemberJsonFormActivity;
import org.smartregister.brac.hnpp.activity.GuestMemberActivity;
import org.smartregister.brac.hnpp.activity.HnppAncJsonFormActivity;
import org.smartregister.brac.hnpp.activity.RiskyPatientActivity;
import org.smartregister.brac.hnpp.adapter.RoutinFUpListAdapter;
import org.smartregister.brac.hnpp.adapter.TelephonicFUpListAdapter;
import org.smartregister.brac.hnpp.contract.TelephonicFUpContract;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.model.AncFollowUpModel;
import org.smartregister.brac.hnpp.presenter.SpecialFUpPresenter;
import org.smartregister.brac.hnpp.presenter.TelephonicFUpPresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.util.FormUtils;

import java.util.ArrayList;


/**
 * A routine f/up fragment
 */
public class TelephonicFUpFragment extends Fragment implements TelephonicFUpContract.View {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final int REQUEST_PHONE_CALL = 101;
    TelephonicFUpPresenter presenter;
    RecyclerView recyclerView;
    ProgressBar progressBar;

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

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return root;
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
    public void initializePresenter() {
        presenter = new TelephonicFUpPresenter(this);
        showProgressBar();
        ArrayList<AncFollowUpModel> list = presenter.fetchData();
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
                        startFollowupActivity(content);
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
}