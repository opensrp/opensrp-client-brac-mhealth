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
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.brac.hnpp.utils.HnppConstants.eventTypeFormNameMapping;
import static org.smartregister.brac.hnpp.utils.HnppConstants.eventTypeMapping;
import static org.smartregister.brac.hnpp.utils.HnppConstants.iconMapping;

public class HnppMemberProfileDueFragment extends BaseFamilyProfileDueFragment implements View.OnClickListener {
    private static final int TAG_OPEN_ANC1 = 101;
    private static final int TAG_OPEN_ANC2 = 102;
    private static final int TAG_OPEN_ANC3 = 103;

    private static final int TAG_OPEN_FAMILY = 111;
    private static final int TAG_OPEN_REFEREAL = 222;
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
        BaseFamilyProfileDueFragment fragment = new HnppMemberProfileDueFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }
    public void setCommonPersonObjectClient(CommonPersonObjectClient commonPersonObjectClient){
        this.commonPersonObjectClient = commonPersonObjectClient;
    }
//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if(isVisibleToUser && !isStart){
//            addStaticView();
//        }
//    }

    public void updateStaticView() {
       // if(FormApplicability.isDueAnyForm(baseEntityId,eventType)){
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    addStaticView();
                }
            },500);
//        }else{
//           if(otherServiceView!=null && anc1View !=null) otherServiceView.removeView(anc1View);
//        }

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
    String eventType = "";
    View anc1View;
    private void addStaticView(){
                if(otherServiceView.getVisibility() == View.VISIBLE){
                    otherServiceView.removeAllViews();
                }
            String gender = org.smartregister.util.Utils.getValue(commonPersonObjectClient.getColumnmaps(), "gender", false);
            String maritalStatus  = org.smartregister.util.Utils.getValue(commonPersonObjectClient.getColumnmaps(), "marital_status", false);
            otherServiceView.setVisibility(View.VISIBLE);
            if(gender.equalsIgnoreCase("F") && maritalStatus.equalsIgnoreCase("Married")){
                //if women

                anc1View = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
                ImageView imageanc1View = anc1View.findViewById(R.id.image_view);
                TextView nameanc1View =  anc1View.findViewById(R.id.patient_name_age);
                anc1View.setTag(TAG_OPEN_ANC1);
                anc1View.setOnClickListener(this);
                eventType = FormApplicability.getDueFormForMarriedWomen(baseEntityId,FormApplicability.getAge(commonPersonObjectClient));
                if(FormApplicability.isDueAnyForm(baseEntityId,eventType) && !TextUtils.isEmpty(eventType)){
                    if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY)) {
                        isFirstAnc = true;
                        nameanc1View.setText(HnppConstants.visitEventTypeMapping.get(HnppConstants.EVENT_TYPE.ANC1_REGISTRATION));
                    }else{
                        isFirstAnc = false;
                        nameanc1View.setText(HnppConstants.visitEventTypeMapping.get(eventType));
                    }
                    imageanc1View.setImageResource(HnppConstants.iconMapping.get(eventType));
                    anc1View.setTag(org.smartregister.family.R.id.VIEW_ID,eventType);

                    otherServiceView.addView(anc1View);
                }
                if(getActivity() instanceof HnppFamilyOtherMemberProfileActivity){
                    HnppFamilyOtherMemberProfileActivity aaa = (HnppFamilyOtherMemberProfileActivity) getActivity();
                    aaa.updatePregnancyOutcomeVisible(eventType);
                    aaa.updateAncRegisterVisible(eventType);
                }
            }


        View familyView = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
        ImageView image = familyView.findViewById(R.id.image_view);
        TextView name =  familyView.findViewById(R.id.patient_name_age);
        familyView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
        image.setImageResource(R.drawable.childrow_family);
        name.setText("পরিবারের অন্যান্য সদস্য সেবা (বাকি)");
        familyView.setTag(TAG_OPEN_FAMILY);
        familyView.setOnClickListener(this);
        otherServiceView.addView(familyView);

        View referelView = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
        ImageView imageReferel = referelView.findViewById(R.id.image_view);
        TextView nameReferel =  referelView.findViewById(R.id.patient_name_age);
        referelView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
        imageReferel.setImageResource(R.mipmap.ic_refer);
        nameReferel.setText("রেফারেল");
        referelView.setTag(TAG_OPEN_REFEREAL);
        referelView.setOnClickListener(this);
        otherServiceView.addView(referelView);
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
            if (getActivity() != null && getActivity() instanceof HnppFamilyOtherMemberProfileActivity) {
                HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) getActivity();
                activity.openReferealFollowUp(referralFollowUpModel);
            }
            return;
        }
        Integer tag = (Integer) v.getTag();
        if (tag != null) {
            switch (tag) {
                case TAG_OPEN_FAMILY:
                    if (getActivity() != null && getActivity() instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) getActivity();
                        activity.openFamilyDueTab();
                    }
                    break;
                case TAG_OPEN_REFEREAL:
                    if (getActivity() != null && getActivity() instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) getActivity();
                        activity.openRefereal();
                    }
                    break;
                case TAG_OPEN_ANC1:
                    if (getActivity() != null && getActivity() instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) getActivity();
                        String eventType = (String) v.getTag(org.smartregister.family.R.id.VIEW_ID);
                        if(isFirstAnc){
                            activity.openHomeVisitForm();
                        }else {
                            activity.openHomeVisitSingleForm(eventTypeFormNameMapping.get(eventType));
                        }
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