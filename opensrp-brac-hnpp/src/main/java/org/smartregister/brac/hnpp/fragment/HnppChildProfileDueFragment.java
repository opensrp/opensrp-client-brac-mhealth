package org.smartregister.brac.hnpp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.HnppChildProfileActivity;
import org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.brac.hnpp.model.MemberProfileDueModel;
import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;
import org.smartregister.brac.hnpp.presenter.HnppMemberProfileDuePresenter;
import org.smartregister.brac.hnpp.provider.HnppFamilyDueRegisterProvider;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.adapter.FamilyRecyclerViewCustomAdapter;
import org.smartregister.family.fragment.BaseFamilyProfileDueFragment;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.brac.hnpp.utils.HnppConstants.eventTypeFormNameMapping;
import static org.smartregister.brac.hnpp.utils.HnppConstants.eventTypeMapping;
import static org.smartregister.brac.hnpp.utils.HnppConstants.iconMapping;

public class HnppChildProfileDueFragment extends BaseFamilyProfileDueFragment implements View.OnClickListener {
    private static final int TAG_OPEN_ANC1 = 101;
    private static final int TAG_OPEN_ANC2 = 102;
    private static final int TAG_OPEN_ANC3 = 103;

    private static final int TAG_OPEN_FAMILY = 111;
    private static final int TAG_OPEN_REFEREAL = 222;
    private static final int TAG_CHILD_FOLLOWUP = 3330;
    private static final int TAG_ENC= 333;
    private static final int TAG_CHILD_DUE= 444;


    private int dueCount = 0;
    private View emptyView;
    private String familyName;
    private long dateFamilyCreated;
    private String familyBaseEntityId;
    private String baseEntityId;
    private LinearLayout otherServiceView;
    private CommonPersonObjectClient commonPersonObjectClient;
    private boolean isFirstAnc = false;
    private Handler handler;
    private boolean isStart = true;


    public static BaseFamilyProfileDueFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        BaseFamilyProfileDueFragment fragment = new HnppChildProfileDueFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }
    public void setCommonPersonObjectClient(CommonPersonObjectClient commonPersonObjectClient){
        this.commonPersonObjectClient = commonPersonObjectClient;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && !isStart){
            updateStaticView();

        }
    }

    private void updateStaticView() {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                addStaticView();
                if(getActivity() instanceof HnppChildProfileActivity){
                    HnppChildProfileActivity b = (HnppChildProfileActivity) getActivity();
                    b.updateImmunizationData();
                }
            }
        },500);



    }

    @Override
    protected void initializePresenter() {
        familyBaseEntityId = getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        baseEntityId = getArguments().getString(Constants.INTENT_KEY.BASE_ENTITY_ID);
        familyName = getArguments().getString(Constants.INTENT_KEY.FAMILY_NAME);
        presenter = new HnppMemberProfileDuePresenter(this, new MemberProfileDueModel(), null, familyBaseEntityId);
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
        super.setupViews(view);
        emptyView = view.findViewById(R.id.empty_view);
        otherServiceView = view.findViewById(R.id.other_option);

        isStart = false;

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
        }

    }
    View encView;
    public void  updateChildDueEntry(int type, String serviceName, String dueDate){
        serviceName = HnppConstants.immunizationMapping.get(serviceName.toUpperCase());
//       if(handler !=null){
//           handler.postDelayed(new Runnable() {
//               @Override
//               public void run() {
                   otherServiceView.setVisibility(View.VISIBLE);
                   if(encView !=null) otherServiceView.removeView(encView);
                   encView = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
                   ImageView image1 = encView.findViewById(R.id.image_view);
                   TextView name1 =  encView.findViewById(R.id.patient_name_age);
                   encView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
                   image1.setImageResource(R.mipmap.ic_child);
                   switch (type){
                       case 1:
                           name1.setText(CoreChildUtils.fromHtml(getString(org.smartregister.chw.core.R.string.vaccine_service_due, serviceName, dueDate)));

                           break;
                       case 2:
                           name1.setText(CoreChildUtils.fromHtml(getString(org.smartregister.chw.core.R.string.vaccine_service_overdue, serviceName, dueDate)));
                           break;
                       case 3:
                           name1.setText(CoreChildUtils.fromHtml(getString(org.smartregister.chw.core.R.string.vaccine_service_upcoming, serviceName, dueDate)));
                           break;
                   }
                   encView.setTag(TAG_CHILD_DUE);
                   encView.setOnClickListener(HnppChildProfileDueFragment.this);
                   otherServiceView.addView(encView);
//               }
//           },500);
       //}

    }
    private void addStaticView(){
        if(otherServiceView.getVisibility() == View.VISIBLE){
            otherServiceView.removeAllViews();
        }
        otherServiceView.setVisibility(View.VISIBLE);
        String dobString = Utils.getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.DOB, false);
        Date dob = Utils.dobStringToDate(dobString);
        boolean isEnc = FormApplicability.isEncVisible(dob);
        if(isEnc){
            View encView = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
            ImageView image1 = encView.findViewById(R.id.image_view);
            TextView name1 =  encView.findViewById(R.id.patient_name_age);
            encView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            image1.setImageResource(R.mipmap.ic_child);
            name1.setText("ENC ");
            encView.setTag(TAG_ENC);
            encView.setOnClickListener(this);
            otherServiceView.addView(encView);
        }


        View familyView = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
        ImageView image = familyView.findViewById(R.id.image_view);
        TextView name =  familyView.findViewById(R.id.patient_name_age);
        familyView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
        image.setImageResource(R.drawable.childrow_family);
        name.setText("ফেমেলির অন্যান্য সদস্য সেবা (বাকি)");
        familyView.setTag(TAG_OPEN_FAMILY);
        familyView.setOnClickListener(this);
        otherServiceView.addView(familyView);

        View referelView = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
        ImageView imageReferel = referelView.findViewById(R.id.image_view);
        TextView nameReferel =  referelView.findViewById(R.id.patient_name_age);
        referelView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
        imageReferel.setImageResource(R.mipmap.ic_refer);
        nameReferel.setText("রেফেরেল");
        referelView.setTag(TAG_OPEN_REFEREAL);
        referelView.setOnClickListener(this);
        otherServiceView.addView(referelView);
        if(FormApplicability.isDueAnyForm(baseEntityId, HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP)){
            View followupView = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
            ImageView fImg = followupView.findViewById(R.id.image_view);
            TextView fName =  followupView.findViewById(R.id.patient_name_age);
            followupView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP));
            fName.setText(eventTypeMapping.get(HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP));
            followupView.setTag(TAG_CHILD_FOLLOWUP);
            followupView.setOnClickListener(this);
            otherServiceView.addView(followupView);
        }
        ArrayList<ReferralFollowUpModel> getList = FormApplicability.getReferralFollowUp(baseEntityId);

        for(ReferralFollowUpModel referralFollowUpModel : getList){

            View referrelFollowUp = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
            ImageView imgFollowup = referrelFollowUp.findViewById(R.id.image_view);
            TextView nReferel =  referrelFollowUp.findViewById(R.id.patient_name_age);
            TextView lastVisitRow = referrelFollowUp.findViewById(R.id.last_visit);
            lastVisitRow.setVisibility(View.VISIBLE);
            referelView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            imgFollowup.setImageResource(iconMapping.get(HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP));
            nReferel.setText(eventTypeMapping.get(HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP));
            lastVisitRow.setText(referralFollowUpModel.getReferralReason());
            referrelFollowUp.setTag(referralFollowUpModel);
            referrelFollowUp.setOnClickListener(this);
            otherServiceView.addView(referrelFollowUp);

        }


    }

    @Override
    public void countExecute() {
        super.countExecute();
    }

    public void onEmptyRegisterCount(final boolean has_no_records) {
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }


    @Override
    public void onClick(View v) {
        if(v.getTag() instanceof ReferralFollowUpModel){
            ReferralFollowUpModel referralFollowUpModel = (ReferralFollowUpModel) v.getTag();
            if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                activity.openReferealFollowUp(referralFollowUpModel);
            }
            return;
        }
        Integer tag = (Integer) v.getTag();
        if (tag != null) {
            switch (tag) {
                case TAG_ENC:
                    if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                        activity.openEnc();
                    }
                    break;
                case TAG_CHILD_DUE:
                    if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                        activity.openVisitHomeScreen(false);
                    }
                    break;
                case TAG_OPEN_FAMILY:
                    if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                        activity.openFamilyDueTab();
                    }
                    break;
                case TAG_OPEN_REFEREAL:
                    if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                        activity.openRefereal();
                    }
                    break;

                case TAG_CHILD_FOLLOWUP:
                    if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                        activity.openFollowUp();
                    }
                    break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(handler != null) handler.removeCallbacksAndMessages(null);
    }
}