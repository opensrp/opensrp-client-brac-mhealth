package org.smartregister.unicef.mis.risky_patient.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.unicef.mis.risky_patient.adapter.SpecialFUpListAdapter;
import org.smartregister.unicef.mis.risky_patient.contract.SpecialFUpContract;
import org.smartregister.unicef.mis.risky_patient.model.AncFollowUpModel;
import org.smartregister.unicef.mis.risky_patient.model.RiskyPatientFilterType;
import org.smartregister.unicef.mis.risky_patient.presenter.SpecialFUpPresenter;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppDBUtils;

import java.util.ArrayList;


/**
 * A routine f/up fragment
 */
public class SpecialFUpFragment extends Fragment implements SpecialFUpContract.View {
    final String TAG = "SpecialFUpFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    SpecialFUpPresenter presenter;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    TextInputEditText searchField;
    AppCompatButton filterBt;
    TextView noDataFoundTv;

    String searchedText = "";
    RiskyPatientFilterType riskyPatientFilterType = new RiskyPatientFilterType();

    public static SpecialFUpFragment newInstance(int index) {
        SpecialFUpFragment fragment = new SpecialFUpFragment();
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
        View root = inflater.inflate(R.layout.fragment_special_f_up, container, false);
        recyclerView = root.findViewById(R.id.specialFollowUpListRv);
        progressBar = root.findViewById(R.id.progress_bar);
        searchField = root.findViewById(R.id.editText);
        filterBt = root.findViewById(R.id.filter_bt);
        noDataFoundTv = root.findViewById(R.id.no_data_found_tv);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        initializePresenter();

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
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void noDataFound() {
        noDataFoundTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void initializePresenter() {
        showProgressBar();
        presenter = new SpecialFUpPresenter(this);
        ArrayList<AncFollowUpModel> list =  presenter.fetchSpecialFUp();
        if(list.isEmpty()){
            noDataFound();
        }else {
            noDataFoundTv.setVisibility(View.GONE);
        }
        SpecialFUpListAdapter adapter = new SpecialFUpListAdapter(getActivity(), new SpecialFUpListAdapter.OnClickAdapter() {
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
        ArrayList<AncFollowUpModel> list =  presenter.fetchSearchedSpecialFUp(charSequence.toString(),riskyPatientFilterType);
        if(list.isEmpty()){
            noDataFound();
        }else {
            noDataFoundTv.setVisibility(View.GONE);
        }
        SpecialFUpListAdapter adapter = new SpecialFUpListAdapter(getActivity(), new SpecialFUpListAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, AncFollowUpModel content) {
                openProfile(content);
            }
        });
        adapter.setData(list);
        recyclerView.setAdapter(adapter);
        hideProgressBar();
    }

    @Override
    public SpecialFUpContract.Presenter getPresenter() {
        return presenter;
    }

    void openProfile(AncFollowUpModel ancFollowUpModel) {

        CommonPersonObjectClient patient = HnppDBUtils.createFromBaseEntity(ancFollowUpModel.baseEntityId);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
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
        }
    }
}