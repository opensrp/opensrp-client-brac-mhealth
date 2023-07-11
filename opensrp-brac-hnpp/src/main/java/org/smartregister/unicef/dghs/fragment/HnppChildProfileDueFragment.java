package org.smartregister.unicef.dghs.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.activity.HnppChildProfileActivity;
import org.smartregister.unicef.dghs.model.MemberProfileDueModel;
import org.smartregister.unicef.dghs.model.ReferralFollowUpModel;
import org.smartregister.unicef.dghs.presenter.HnppChildProfileDuePresenter;
import org.smartregister.unicef.dghs.provider.HnppFamilyDueRegisterProvider;
import org.smartregister.unicef.dghs.utils.FormApplicability;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
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

import static org.smartregister.unicef.dghs.utils.HnppConstants.eventTypeMapping;
import static org.smartregister.unicef.dghs.utils.HnppConstants.iconMapping;

public class HnppChildProfileDueFragment extends BaseFamilyProfileDueFragment implements View.OnClickListener {
    private static final int TAG_OPEN_ANC1 = 101;
    private static final int TAG_OPEN_ANC2 = 102;
    private static final int TAG_OPEN_ANC3 = 103;

    private static final int TAG_OPEN_FAMILY = 111;
    private static final int TAG_OPEN_REFEREAL = 222;
    private static final int TAG_CHILD_FOLLOWUP = 3330;
    private static final int TAG_NEW_BORN_PNC_1_4 = 2121;
    private static final int TAG_AEFI_CHILD = 33301;
    private static final int TAG_CHILD_DISEASE = 333012;
    private static final int TAG_MEMBER_DISEASE = 333013;
    private static final int TAG_CHILD_INFO_EBF12 = 1212;
    private static final int TAG_CHILD_INFO_7_24_months = 1213;
    private static final int TAG_CHILD_INFO_25_months = 1214;
    private static final int TAG_ENC= 333;
    private static final int TAG_CHILD_DUE= 444;
    private static final int TAG_OPEN_CORONA = 88888;
    private static final int TAG_OPEN_GMP = 99999;
    private static final int TAG_PROFILE_UPDATE= 3333;
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
    public void setBaseEntityId(String baseEntityId){
        this.baseEntityId = baseEntityId;
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
                if(getActivity() ==null || getActivity().isFinishing()) return;
                addStaticView();
                String dobString = Utils.getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.DOB, false);
                Date dob = Utils.dobStringToDate(dobString);
                boolean isImmunizationVisible = FormApplicability.isImmunizationVisible(dob);
                if(isImmunizationVisible){
                    if(getActivity() instanceof HnppChildProfileActivity){
                        HnppChildProfileActivity b = (HnppChildProfileActivity) getActivity();
                        b.updateImmunizationData();
                    }
                }


            }
        },500);



    }

    @Override
    protected void initializePresenter() {
        familyBaseEntityId = getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        familyName = getArguments().getString(Constants.INTENT_KEY.FAMILY_NAME);
        presenter = new HnppChildProfileDuePresenter(this, new MemberProfileDueModel(), null, familyBaseEntityId);
        //TODO need to pass this value as this value using at homevisit rule
        dateFamilyCreated = getArguments().getLong("");

    }

    @Override
    protected boolean isValidFilterForFts(CommonRepository commonRepository) {
        return false;
    }

    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {
        //TODO
        Timber.d("setAdvancedSearchFormData");
    }

    @Override
    public void setupViews(View view) {
        try{
            super.setupViews(view);
        }catch (Exception e){
            HnppApplication.getHNPPInstance().forceLogout();
            return;
        }
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
    protected String getMainCondition() {
        return super.getMainCondition();
    }

    @SuppressLint("NonConstantResourceId")
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
        if(getActivity() == null || getActivity().isFinishing() || otherServiceView==null || TextUtils.isEmpty(serviceName))return;
        serviceName = HnppConstants.immunizationMapping.get(serviceName.toUpperCase());
//       if(handler !=null){
//           handler.postDelayed(new Runnable() {
//               @Override
//               public void run() {
                   otherServiceView.setVisibility(View.VISIBLE);
                   if(encView !=null) otherServiceView.removeView(encView);
                   encView = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due,null);
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
    String eventType = "";
    View childInfo1View, childInfo2View, childInfo3View;
    private void addStaticView(){
        if(getActivity() ==null || getActivity().isFinishing()) return;
        if(otherServiceView.getVisibility() == View.VISIBLE){
            otherServiceView.removeAllViews();
        }
        otherServiceView.setVisibility(View.VISIBLE);
        if(FormApplicability.isDueAnyForm(baseEntityId, HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP)){
            View followupView = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due,null);
            ImageView fImg = followupView.findViewById(R.id.image_view);
            TextView fName =  followupView.findViewById(R.id.patient_name_age);
            followupView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP));
            fName.setText(eventTypeMapping.get(HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP));
            followupView.setTag(TAG_CHILD_FOLLOWUP);
            followupView.setOnClickListener(this);
            otherServiceView.addView(followupView);
        }
        if(FormApplicability.isDueAnyForm(baseEntityId, HnppConstants.EVENT_TYPE.CHILD_DISEASE)){
            View followupView = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due,null);
            ImageView fImg = followupView.findViewById(R.id.image_view);
            TextView fName =  followupView.findViewById(R.id.patient_name_age);
            followupView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(HnppConstants.EVENT_TYPE.CHILD_DISEASE));
            fName.setText(eventTypeMapping.get(HnppConstants.EVENT_TYPE.CHILD_DISEASE));
            followupView.setTag(TAG_CHILD_DISEASE);
            followupView.setOnClickListener(this);
            otherServiceView.addView(followupView);
        }
        if(FormApplicability.isDueAnyForm(baseEntityId, HnppConstants.EVENT_TYPE.AEFI_CHILD)){
            View followupView = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due,null);
            ImageView fImg = followupView.findViewById(R.id.image_view);
            TextView fName =  followupView.findViewById(R.id.patient_name_age);
            followupView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(HnppConstants.EVENT_TYPE.AEFI_CHILD));
            fName.setText(eventTypeMapping.get(HnppConstants.EVENT_TYPE.AEFI_CHILD));
            followupView.setTag(TAG_AEFI_CHILD);
            followupView.setOnClickListener(this);
            otherServiceView.addView(followupView);
        }
        String dobString = Utils.getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.DOB, false);
        Date dob = Utils.dobStringToDate(dobString);
        long day = FormApplicability.getDay(commonPersonObjectClient);
        if(FormApplicability.isDueChildProfileVisit(baseEntityId)){
            @SuppressLint("InflateParams") View homeVisitView = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
            ImageView image1 = homeVisitView.findViewById(R.id.image_view);
            TextView name1 =  homeVisitView.findViewById(R.id.patient_name_age);
            homeVisitView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            image1.setImageResource(R.drawable.rowavatar_child);
            name1.setText("খানা সদস্য জরিপ");
            homeVisitView.setTag(TAG_PROFILE_UPDATE);
            homeVisitView.setOnClickListener(this);

            otherServiceView.addView(homeVisitView);
        }

        boolean isEnc = FormApplicability.isEncVisible(dob);

        if(isEnc){
            View newBornView = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due,null);
            ImageView fImg = newBornView.findViewById(R.id.image_view);
            TextView fName =  newBornView.findViewById(R.id.patient_name_age);
            newBornView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(iconMapping.get(HnppConstants.EVENT_TYPE.NEW_BORN_PNC_1_4));
            fName.setText(eventTypeMapping.get(HnppConstants.EVENT_TYPE.NEW_BORN_PNC_1_4));
            newBornView.setTag(TAG_NEW_BORN_PNC_1_4);
            newBornView.setOnClickListener(this);
            otherServiceView.addView(newBornView);
        }
//        boolean isEnc = FormApplicability.isEncVisible(dob);
//        if(isEnc){
//            if(FormApplicability.isDueAnyForm(baseEntityId, HnppConstants.EVENT_TYPE.ENC_REGISTRATION)){
//                View encView = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due,null);
//                ImageView image1 = encView.findViewById(R.id.image_view);
//                TextView name1 =  encView.findViewById(R.id.patient_name_age);
//                encView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
//                image1.setImageResource(R.mipmap.ic_child);
//                name1.setText("নবজাতকের সেবা");
//                encView.setTag(TAG_ENC);
//                encView.setOnClickListener(this);
//                otherServiceView.addView(encView);
//            }
//
//        }


//        View familyView = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due,null);
//        ImageView image = familyView.findViewById(R.id.image_view);
//        TextView name =  familyView.findViewById(R.id.patient_name_age);
//        familyView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
//        image.setImageResource(R.drawable.childrow_family);
//        name.setText("ফেমেলির অন্যান্য সদস্য সেবা (বাকি)");
//        familyView.setTag(TAG_OPEN_FAMILY);
//        familyView.setOnClickListener(this);
//        otherServiceView.addView(familyView);

//        {
//            View referelView = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due,null);
//            ImageView imageReferel = referelView.findViewById(R.id.image_view);
//            TextView nameReferel =  referelView.findViewById(R.id.patient_name_age);
//            referelView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
//            imageReferel.setImageResource(R.mipmap.ic_refer);
//            nameReferel.setText("রেফেরেল");
//            referelView.setTag(TAG_OPEN_REFEREAL);
//            referelView.setOnClickListener(this);
//            otherServiceView.addView(referelView);
//        }
//        if(!isEnc){

//
//        }
//        eventType = FormApplicability.isDueChildInfo(day);
//        if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_INFO_EBF12) && FormApplicability.isDueChildInfoForm(baseEntityId,eventType)){
//            childInfo1View = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due,null);
//            ImageView fImg = childInfo1View.findViewById(R.id.image_view);
//            TextView fName =  childInfo1View.findViewById(R.id.patient_name_age);
//            childInfo1View.findViewById(R.id.status).setVisibility(View.INVISIBLE);
//            fImg.setImageResource(iconMapping.get(eventType));
//            fName.setText(eventTypeMapping.get(eventType));
//            childInfo1View.setTag(TAG_CHILD_INFO_EBF12);
//            childInfo1View.setOnClickListener(this);
//            otherServiceView.addView(childInfo1View);
//        }
//        else if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_INFO_7_24_MONTHS) && FormApplicability.isDueChildInfoForm(baseEntityId,eventType)){
//            childInfo2View = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due,null);
//            ImageView fImg = childInfo2View.findViewById(R.id.image_view);
//            TextView fName =  childInfo2View.findViewById(R.id.patient_name_age);
//            childInfo2View.findViewById(R.id.status).setVisibility(View.INVISIBLE);
//            fImg.setImageResource(iconMapping.get(eventType));
//            fName.setText(eventTypeMapping.get(eventType));
//            childInfo2View.setTag(TAG_CHILD_INFO_7_24_months);
//            childInfo2View.setOnClickListener(this);
//            otherServiceView.addView(childInfo2View);
//        }
//        else if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_INFO_25_MONTHS) && FormApplicability.isDueChildInfoForm(baseEntityId,eventType)){
//            childInfo3View = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due,null);
//            ImageView fImg = childInfo3View.findViewById(R.id.image_view);
//            TextView fName =  childInfo3View.findViewById(R.id.patient_name_age);
//            childInfo3View.findViewById(R.id.status).setVisibility(View.INVISIBLE);
//            fImg.setImageResource(iconMapping.get(eventType));
//            fName.setText(eventTypeMapping.get(eventType));
//            childInfo3View.setTag(TAG_CHILD_INFO_25_months);
//            childInfo3View.setOnClickListener(this);
//            otherServiceView.addView(childInfo3View);
//        }

//        ArrayList<ReferralFollowUpModel> getList = FormApplicability.getReferralFollowUp(baseEntityId);
//
//        for(ReferralFollowUpModel referralFollowUpModel : getList){
//
//            View referrelFollowUp = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due,null);
//            ImageView imgFollowup = referrelFollowUp.findViewById(R.id.image_view);
//            TextView nReferel =  referrelFollowUp.findViewById(R.id.patient_name_age);
//            TextView lastVisitRow = referrelFollowUp.findViewById(R.id.last_visit);
//            lastVisitRow.setVisibility(View.VISIBLE);
//            referrelFollowUp.findViewById(R.id.status).setVisibility(View.INVISIBLE);
//            imgFollowup.setImageResource(iconMapping.get(HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP));
//            nReferel.setText(eventTypeMapping.get(HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP));
//            lastVisitRow.setText(referralFollowUpModel.getReferralReason());
//            referrelFollowUp.setTag(referralFollowUpModel);
//            referrelFollowUp.setOnClickListener(this);
//            otherServiceView.addView(referrelFollowUp);
//
//        }
//
//            if (FormApplicability.isDueCoronaForm(baseEntityId)) {
//                View referelView = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due, null);
//                ImageView imageReferel = referelView.findViewById(R.id.image_view);
//                TextView nameReferel = referelView.findViewById(R.id.patient_name_age);
//                referelView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
//                imageReferel.setImageResource(R.drawable.ic_virus);
//                nameReferel.setText("করোনা তথ্য");
//                referelView.setTag(TAG_OPEN_CORONA);
//                referelView.setOnClickListener(this);
//                otherServiceView.addView(referelView);
//            }

//            View referelView = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due, null);
//            ImageView imageReferel = referelView.findViewById(R.id.image_view);
//            TextView nameReferel = referelView.findViewById(R.id.patient_name_age);
//            referelView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
//            imageReferel.setImageResource(R.drawable.ic_icon_growth_chart);
//            nameReferel.setText("গ্রোথ মনিটরিং");
//            referelView.setTag(TAG_OPEN_GMP);
//            referelView.setOnClickListener(this);
//            otherServiceView.addView(referelView);

    }

    @Override
    public void countExecute() {
       // super.countExecute();
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
                case TAG_PROFILE_UPDATE:
                    if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                        activity.openChildProfileVisit();
                    }
                    break;
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

                case TAG_NEW_BORN_PNC_1_4:
                    if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                        activity.openNewBorn();
                    }
                    break;

                case TAG_AEFI_CHILD:
                    if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                        activity.openAefiForm();
                    }
                    break;
                case TAG_CHILD_DISEASE:
                    if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                        activity.openChildDiseaseForm();
                    }
                    break;
                case TAG_OPEN_CORONA:
                    if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                        activity.openCoronaIndividualForm();
                    }
                    break;
                case TAG_OPEN_GMP:
                    if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                        activity.openGMPScreen();
                    }
                    break;
                case TAG_CHILD_INFO_EBF12:
                    if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                        activity.openChildInfo(HnppConstants.EVENT_TYPE.CHILD_INFO_EBF12);
                    }
                    break;
                case TAG_CHILD_INFO_7_24_months:
                    if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                        activity.openChildInfo(HnppConstants.EVENT_TYPE.CHILD_INFO_7_24_MONTHS);
                    }
                    break;
                case TAG_CHILD_INFO_25_months:
                    if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                        HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                        activity.openChildInfo(HnppConstants.EVENT_TYPE.CHILD_INFO_25_MONTHS);
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