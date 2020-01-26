package org.smartregister.brac.hnpp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.FamilyProfileActivity;
import org.smartregister.brac.hnpp.activity.HnppChildProfileActivity;
import org.smartregister.brac.hnpp.model.FamilyProfileDueModel;
import org.smartregister.brac.hnpp.presenter.FamilyProfileDuePresenter;
import org.smartregister.brac.hnpp.provider.HnppFamilyDueRegisterProvider;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.ProfileDueInfo;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.adapter.FamilyRecyclerViewCustomAdapter;
import org.smartregister.family.fragment.BaseFamilyProfileDueFragment;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import timber.log.Timber;

public class FamilyProfileDueFragment extends BaseFamilyProfileDueFragment implements View.OnClickListener {

    private int dueCount = 0;
    private View emptyView;
    private String familyName;
    private long dateFamilyCreated;
    private String familyBaseEntityId;
    private LinearLayout otherServiceView;
    private static final int TAG_HOME_VISIT= 666;
    private static final int TAG_PROFILE= 777;
    private Handler handler;
    private boolean isStart = true;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && !isStart){
            updateStaticView();
        }
    }

    public static BaseFamilyProfileDueFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        BaseFamilyProfileDueFragment fragment = new FamilyProfileDueFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initializePresenter() {
        familyBaseEntityId = getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        familyName = getArguments().getString(Constants.INTENT_KEY.FAMILY_NAME);
        presenter = new FamilyProfileDuePresenter(this, new FamilyProfileDueModel(), null, familyBaseEntityId);
        //TODO need to pass this value as this value using at homevisit rule
        dateFamilyCreated = getArguments().getLong("");

    }

    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {
        //TODO
        Timber.d("setAdvancedSearchFormData");
    }

    @Override
    public void setupViews(View view) {
       try {
           super.setupViews(view);
           isStart = false;
           otherServiceView = view.findViewById(R.id.other_option);
           emptyView = view.findViewById(R.id.empty_view);
           emptyView.setVisibility(View.GONE);
       }catch (Exception e){
           Toast.makeText(getActivity(),getString(R.string.fail_result),Toast.LENGTH_SHORT).show();
       }


    }

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        HnppFamilyDueRegisterProvider chwDueRegisterProvider = new HnppFamilyDueRegisterProvider(this.getActivity(), this.commonRepository(), visibleColumns, this.registerActionHandler, this.paginationViewHandler);
        this.clientAdapter = new FamilyRecyclerViewCustomAdapter(null, chwDueRegisterProvider, this.context().commonrepository(this.tablename), Utils.metadata().familyDueRegister.showPagination);
        this.clientAdapter.setCurrentlimit(0);
        this.clientsView.setAdapter(this.clientAdapter);
        this.clientsView.setVisibility(View.GONE);
        updateStaticView();

    }
    private void updateStaticView(){
       // if(FormApplicability.isDueHHVisit(familyBaseEntityId)){
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    addStaticView();
                }
            },500);
//        }else{
//            if(otherServiceView !=null) otherServiceView.setVisibility(View.GONE);
//        }
    }

    private void addStaticView(){
        if(otherServiceView == null) return;
        if(otherServiceView.getVisibility() == View.VISIBLE){
            otherServiceView.removeAllViews();
        }

        otherServiceView.setVisibility(View.VISIBLE);
        if(FormApplicability.isDueHHVisit(familyBaseEntityId)){
            View homeVisitView = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
            ImageView image1 = homeVisitView.findViewById(R.id.image_view);
            TextView name1 =  homeVisitView.findViewById(R.id.patient_name_age);
            homeVisitView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            image1.setImageResource(R.mipmap.ic_icon_home);
            name1.setText("খানা পরিদর্শন");
            homeVisitView.setTag(TAG_HOME_VISIT);
            homeVisitView.setOnClickListener(this);

            otherServiceView.addView(homeVisitView);
        }

        updateDueView();
    }
    private void updateDueView(){
        ArrayList<ProfileDueInfo> getAllMemberDueInfo = HnppDBUtils.getDueListByFamilyId(familyBaseEntityId);
        for(ProfileDueInfo profileDueInfo : getAllMemberDueInfo){
            View homeVisitView = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
            ImageView image1 = homeVisitView.findViewById(R.id.image_view);
            TextView name1 =  homeVisitView.findViewById(R.id.patient_name_age);
            homeVisitView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            homeVisitView.findViewById(R.id.due_button_wrapper).setVisibility(View.VISIBLE);
            Button dueTextBtn = homeVisitView.findViewById(R.id.due_button);
            dueTextBtn.setText(profileDueInfo.getEventType());
            try{
                image1.setImageResource(HnppConstants.iconMapping.get(profileDueInfo.getEventType()));
            }catch (Exception e){
                image1.setImageResource(R.drawable.rowavatar_member);
            }
            name1.setText(profileDueInfo.getName());
            ((TextView)homeVisitView.findViewById(R.id.last_visit)).setVisibility(View.VISIBLE);
            ((TextView)homeVisitView.findViewById(R.id.last_visit)).setText(getString(R.string.boyos)+":"+profileDueInfo.getAge());
            homeVisitView.setTag(TAG_PROFILE);
            homeVisitView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null && getActivity() instanceof FamilyProfileActivity) {
                        FamilyProfileActivity activity = (FamilyProfileActivity) getActivity();
                        activity.openProfile(profileDueInfo.getBaseEntityId());
                    }
                }
            });
            otherServiceView.addView(homeVisitView);
        }
    }

    @Override
    protected void onViewClicked(View view) {
        super.onViewClicked(view);
        switch (view.getId()) {
            case R.id.patient_column:
                if (view.getTag() != null && view.getTag(org.smartregister.family.R.id.VIEW_ID) == CLICK_VIEW_NORMAL) {
                    goToChildProfileActivity(view);
                }
                break;
            case R.id.next_arrow:
                if (view.getTag() != null && view.getTag(org.smartregister.family.R.id.VIEW_ID) == CLICK_VIEW_NEXT_ARROW) {
                    goToChildProfileActivity(view);
                }
                break;
            default:
                break;
        }
    }

    public void goToChildProfileActivity(View view) {
        if (view.getTag() instanceof CommonPersonObjectClient) {
            CommonPersonObjectClient patient = (CommonPersonObjectClient) view.getTag();
            HnppChildProfileActivity.startMe(getActivity(), true, new MemberObject(patient), HnppChildProfileActivity.class);
        }

    }

    @Override
    public void countExecute() {
//        super.countExecute();
//        final int count = clientAdapter.getTotalcount();
//
//        if (getActivity() != null && count != dueCount) {
//            dueCount = count;
//            ((FamilyProfileActivity) getActivity()).updateDueCount(dueCount);
//        }
//        if (getActivity() != null) {
//            getActivity().runOnUiThread(() -> onEmptyRegisterCount(count < 1));
//        }
    }

    public void onEmptyRegisterCount(final boolean has_no_records) {
        if (emptyView != null) {
            emptyView.setVisibility(has_no_records ? View.VISIBLE : View.GONE);
        }
    }


    @Override
    public void onClick(View v) {
        Integer tag = (Integer) v.getTag();
        if (tag != null) {
            switch (tag) {

                case TAG_HOME_VISIT:
                    if (getActivity() != null && getActivity() instanceof FamilyProfileActivity) {
                        FamilyProfileActivity activity = (FamilyProfileActivity) getActivity();
                        activity.openHomeVisitFamily();
                    }
                    break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        if(handler !=null) handler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }
}