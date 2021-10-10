package org.smartregister.brac.hnpp.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.brac.hnpp.adapter.HnppMemberProfileDueAdapter;
import org.smartregister.brac.hnpp.adapter.OtherServiceAdapter;
import org.smartregister.brac.hnpp.contract.HnppMemberProfileContract;
import org.smartregister.brac.hnpp.contract.OtherServiceContract;
import org.smartregister.brac.hnpp.model.MemberProfileDueModel;
import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;
import org.smartregister.brac.hnpp.presenter.HnppMemberProfilePresenter;
import org.smartregister.brac.hnpp.presenter.MemberOtherServicePresenter;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.MemberProfileDueData;
import org.smartregister.brac.hnpp.utils.OtherServiceData;
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

import static org.smartregister.brac.hnpp.utils.HnppConstants.EVENT_TYPE.CORONA_INDIVIDUAL;
import static org.smartregister.brac.hnpp.utils.HnppConstants.eventTypeFormNameMapping;
import static org.smartregister.brac.hnpp.utils.HnppConstants.eventTypeMapping;
import static org.smartregister.brac.hnpp.utils.HnppConstants.iconMapping;

public class HnppMemberProfileDueFragment extends Fragment implements View.OnClickListener, HnppMemberProfileContract.View {
    private static final int TAG_OPEN_ANC1 = 101;

    private static final int TAG_OPEN_FAMILY = 111;
    private static final int TAG_OPEN_REFEREAL = 222;
    private static final int TAG_OPEN_CORONA = 88888;
    private static final int TAG_OPEN_ANC_REGISTRATION= 555;

    private String baseEntityId;
    private LinearLayout otherServiceView;
    private ProgressBar loadingProgressBar;
    private CommonPersonObjectClient commonPersonObjectClient;
    private Handler handler;
    private boolean isStart = true;
    Activity mActivity;
    private HnppMemberProfilePresenter presenter;
    private RecyclerView dueRecyclerView;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    public static HnppMemberProfileDueFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        HnppMemberProfileDueFragment fragment = new HnppMemberProfileDueFragment();
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
            //addStaticView();
            presenter.fetchData(commonPersonObjectClient,baseEntityId);
        }
    }

    public void updateStaticView() {
       // if(FormApplicability.isDueAnyForm(baseEntityId,eventType)){

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                   // addStaticView();
                    fetchData();

                }
            },500);
//        }else{
//           if(otherServiceView!=null && anc1View !=null) otherServiceView.removeView(anc1View);
//        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_profile_due,null);
       // otherServiceView = view.findViewById(R.id.other_option);
        dueRecyclerView = view.findViewById(R.id.due_recycler_view);
        dueRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        loadingProgressBar = view.findViewById(R.id.client_list_progress);
        isStart = false;

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new HnppMemberProfilePresenter(this);
        baseEntityId = getArguments().getString(Constants.INTENT_KEY.BASE_ENTITY_ID);
        handler = new Handler();
        //addStaticView();
        fetchData();
    }

    private void fetchData() {
        presenter.fetchData(commonPersonObjectClient,baseEntityId);
    }

    ///old
   /* String eventType = "";
    View anc1View;
    private void addStaticView(){
        loadingProgressBar.setVisibility(View.VISIBLE);
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
                    nameanc1View.setText(HnppConstants.visitEventTypeMapping.get(eventType));
                    imageanc1View.setImageResource(HnppConstants.iconMapping.get(eventType));
                    anc1View.setTag(org.smartregister.family.R.id.VIEW_ID,eventType);

                    otherServiceView.addView(anc1View);
                }
                if(mActivity instanceof HnppFamilyOtherMemberProfileActivity){
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            HnppFamilyOtherMemberProfileActivity aaa = (HnppFamilyOtherMemberProfileActivity) mActivity;
                            try{
                                aaa.updatePregnancyOutcomeVisible(eventType);
                                aaa.updateAncRegisterVisible(eventType);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    },500);


                }
                if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ELCO) && FormApplicability.isPregnant(baseEntityId)){
                   View ancRegistration = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
                        ImageView image = ancRegistration.findViewById(R.id.image_view);
                        TextView name =  ancRegistration.findViewById(R.id.patient_name_age);
                        ancRegistration.findViewById(R.id.status).setVisibility(View.INVISIBLE);
                        image.setImageResource(R.drawable.childrow_family);
                        name.setText("গর্ভবতী রেজিস্ট্রেশন");
                        ancRegistration.setTag(TAG_OPEN_ANC_REGISTRATION);
                        ancRegistration.setOnClickListener(this);
                        otherServiceView.addView(ancRegistration);
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
        {
            View referelView = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due, null);
            ImageView imageReferel = referelView.findViewById(R.id.image_view);
            TextView nameReferel = referelView.findViewById(R.id.patient_name_age);
            referelView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            imageReferel.setImageResource(R.mipmap.ic_refer);
            nameReferel.setText("রেফারেল");
            referelView.setTag(TAG_OPEN_REFEREAL);
            referelView.setOnClickListener(this);
            otherServiceView.addView(referelView);
        }
        ArrayList<ReferralFollowUpModel> getList = FormApplicability.getReferralFollowUp(baseEntityId);

        for(ReferralFollowUpModel referralFollowUpModel : getList){

            View referrelFollowUp = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
            ImageView imgFollowup = referrelFollowUp.findViewById(R.id.image_view);
            TextView nReferel =  referrelFollowUp.findViewById(R.id.patient_name_age);
            TextView lastVisitRow = referrelFollowUp.findViewById(R.id.last_visit);
            lastVisitRow.setVisibility(View.VISIBLE);
            referrelFollowUp.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            imgFollowup.setImageResource(iconMapping.get(HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP));
            nReferel.setText(eventTypeMapping.get(HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP));
            lastVisitRow.setText(referralFollowUpModel.getReferralReason());
            referrelFollowUp.setTag(referralFollowUpModel);
            referrelFollowUp.setOnClickListener(this);
            otherServiceView.addView(referrelFollowUp);

        }
        if(FormApplicability.isDueCoronaForm(baseEntityId)){
            View referelView = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
            ImageView imageReferel = referelView.findViewById(R.id.image_view);
            TextView nameReferel =  referelView.findViewById(R.id.patient_name_age);
            referelView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            imageReferel.setImageResource(R.drawable.ic_virus);
            nameReferel.setText("করোনা তথ্য");
            referelView.setTag(TAG_OPEN_CORONA);
            referelView.setOnClickListener(this);
            otherServiceView.addView(referelView);
        }
        loadingProgressBar.setVisibility(View.GONE);

    }*/

    private HnppMemberProfileDueAdapter.OnClickAdapter onClickAdapter = (position, content) -> startFormActivity(content);

    private void startFormActivity(MemberProfileDueData content) {
        if(content.getReferralFollowUpModel() instanceof ReferralFollowUpModel){
            ReferralFollowUpModel referralFollowUpModel = (ReferralFollowUpModel) content.getReferralFollowUpModel();
            if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                activity.openReferealFollowUp(referralFollowUpModel);
            }
            return;
        }
        Integer tag = (Integer) content.getType();
        if (tag != null) {
            switch (tag) {
                case TAG_OPEN_CORONA:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.openCoronaIndividualForm();
                    }
                    break;
                case TAG_OPEN_ANC_REGISTRATION:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.startAncRegister();
                    }
                    break;
                case TAG_OPEN_FAMILY:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.openFamilyDueTab();
                    }
                    break;
                case TAG_OPEN_REFEREAL:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.openRefereal();
                    }
                    break;
                case TAG_OPEN_ANC1:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        String eventType = (String) content.getEventType();
                        if(!eventType.equals(HnppConstants.EVENT_TYPE.ELCO)
                                && !eventType.equals(HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour)
                                && !eventType.equals(HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour)
                                && FormApplicability.isFirstTimeAnc(baseEntityId)){
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
    public void onClick(View v) {
        if(v.getTag() instanceof ReferralFollowUpModel){
            ReferralFollowUpModel referralFollowUpModel = (ReferralFollowUpModel) v.getTag();
            if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                activity.openReferealFollowUp(referralFollowUpModel);
            }
            return;
        }
        Integer tag = (Integer) v.getTag();
        if (tag != null) {
            switch (tag) {
                case TAG_OPEN_CORONA:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.openCoronaIndividualForm();
                    }
                    break;
                case TAG_OPEN_ANC_REGISTRATION:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.startAncRegister();
                    }
                    break;
                case TAG_OPEN_FAMILY:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.openFamilyDueTab();
                    }
                    break;
                case TAG_OPEN_REFEREAL:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.openRefereal();
                    }
                    break;
                case TAG_OPEN_ANC1:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        String eventType = (String) v.getTag(org.smartregister.family.R.id.VIEW_ID);
                        if(!eventType.equals(HnppConstants.EVENT_TYPE.ELCO)
                                && !eventType.equals(HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour)
                                && !eventType.equals(HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour)
                                && FormApplicability.isFirstTimeAnc(baseEntityId)){
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
        mActivity = null;
    }

    @Override
    public void showProgressBar() {

    }

    @Override
    public void hideProgressBar() {

    }

    @Override
    public void updateView() {
        HnppMemberProfileDueAdapter adapter = new HnppMemberProfileDueAdapter(getActivity(),onClickAdapter);
        adapter.setData(presenter.getData());
        this.dueRecyclerView.setAdapter(adapter);
    }

    @Override
    public HnppMemberProfileContract.Presenter getPresenter() {
        return presenter;
    }


}