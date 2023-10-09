package org.smartregister.unicef.mis.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.activity.GuestMemberProfileActivity;
import org.smartregister.unicef.mis.utils.FormApplicability;
import org.smartregister.unicef.mis.utils.GuestMemberData;
import org.smartregister.unicef.mis.utils.HnppConstants;

import static org.smartregister.unicef.mis.utils.HnppConstants.guestEventTypeFormNameMapping;

public class GuestMemberDueFragment extends Fragment implements View.OnClickListener {
    private static final int TAG_OPEN_ANC1 = 101;
    private static final int TAG_OPEN_DELIVERY = 102;
    private GuestMemberData guestMemberData;
    private LinearLayout otherServiceView;
    private Handler handler;

    public static GuestMemberDueFragment getInstance(){
        GuestMemberDueFragment guestMemberDueFragment = new GuestMemberDueFragment();

        return guestMemberDueFragment;
    }
    public void setGuestMemberData(GuestMemberData guestMemberData){
        this.guestMemberData = guestMemberData;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.guest_member_due, container, false);
        otherServiceView = view.findViewById(R.id.other_option);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handler = new Handler();
        updateStaticView();
    }

    public void updateStaticView() {
        // if(FormApplicability.isDueAnyForm(baseEntityId,eventType)){

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
    private void addStaticView(){
        if(otherServiceView.getVisibility() == View.VISIBLE){
            otherServiceView.removeAllViews();
        }
        otherServiceView.setVisibility(View.VISIBLE);
        if(!HnppConstants.isPALogin()){
            if(guestMemberData.getGender().equalsIgnoreCase("F")){
                @SuppressLint("InflateParams") View anc1View = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
                ImageView imageanc1View = anc1View.findViewById(R.id.image_view);
                TextView nameanc1View =  anc1View.findViewById(R.id.patient_name_age);
                anc1View.setTag(TAG_OPEN_ANC1);
                anc1View.setOnClickListener(this);
                int age =  FormApplicability.getAge(guestMemberData.getDob());
                if(FormApplicability.isElco(age)){
                    String eventType = FormApplicability.getGuestMemberDueFormForWomen(guestMemberData.getBaseEntityId(),age);
                    if(FormApplicability.isDueAnyForm(guestMemberData.getBaseEntityId(),eventType) && !TextUtils.isEmpty(eventType)){
                        nameanc1View.setText(HnppConstants.getVisitEventTypeMapping().get(eventType));
                        imageanc1View.setImageResource(HnppConstants.iconMapping.get(eventType));
                        anc1View.setTag(org.smartregister.family.R.id.VIEW_ID,eventType);

                        otherServiceView.addView(anc1View);
                    }
//                    if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY) || eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC1_REGISTRATION)

                    if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_HOME_VISIT)
                            ){
                        @SuppressLint("InflateParams") View ancRegistration = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
                        ImageView image = ancRegistration.findViewById(R.id.image_view);
                        TextView name =  ancRegistration.findViewById(R.id.patient_name_age);
                        ancRegistration.findViewById(R.id.status).setVisibility(View.INVISIBLE);
                        image.setImageResource(HnppConstants.iconMapping.get(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME));
                        name.setText(HnppConstants.getVisitEventTypeMapping().get(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME));
                        ancRegistration.setTag(TAG_OPEN_DELIVERY);
                        ancRegistration.setOnClickListener(this);
                        otherServiceView.addView(ancRegistration);
                    }
                }

            }
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(handler !=null) handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onClick(View v) {

        Integer tag = (Integer) v.getTag();
        if (tag != null) {
            switch (tag) {
                case TAG_OPEN_ANC1:
                    if (getActivity() != null && getActivity() instanceof GuestMemberProfileActivity) {
                        GuestMemberProfileActivity activity = (GuestMemberProfileActivity) getActivity();
                        String eventType = (String) v.getTag(org.smartregister.family.R.id.VIEW_ID);
                        if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_REGISTRATION)){
                            activity.openAncRegisterForm();
                        }else{
                                activity.openHomeVisitSingleForm(guestEventTypeFormNameMapping.get(eventType));

                        }

                    }
                    break;
                case TAG_OPEN_DELIVERY:
                    if (getActivity() != null && getActivity() instanceof GuestMemberProfileActivity) {
                        GuestMemberProfileActivity activity = (GuestMemberProfileActivity) getActivity();
                        activity.openPregnancyRegisterForm();
                    }
                    break;
                case HnppConstants.OTHER_SERVICE_TYPE.TYPE_GIRL_PACKAGE:
                    if (getActivity() != null && getActivity() instanceof GuestMemberProfileActivity) {
                        GuestMemberProfileActivity activity = (GuestMemberProfileActivity) getActivity();
                        activity.openHomeVisitSingleForm(HnppConstants.JSON_FORMS.GIRL_PACKAGE);
                    }

                    break;
                case HnppConstants.OTHER_SERVICE_TYPE.TYPE_NCD:
                    if (getActivity() != null && getActivity() instanceof GuestMemberProfileActivity) {
                        GuestMemberProfileActivity activity = (GuestMemberProfileActivity) getActivity();
                        activity.openHomeVisitSingleForm(HnppConstants.JSON_FORMS.NCD_PACKAGE);
                    }
                    break;
                case HnppConstants.OTHER_SERVICE_TYPE.TYPE_IYCF:
                    if (getActivity() != null && getActivity() instanceof GuestMemberProfileActivity) {
                        GuestMemberProfileActivity activity = (GuestMemberProfileActivity) getActivity();
                        activity.openHomeVisitSingleForm(HnppConstants.JSON_FORMS.IYCF_PACKAGE);
                    }
                    break;
                case HnppConstants.OTHER_SERVICE_TYPE.TYPE_WOMEN_PACKAGE:
                    if (getActivity() != null && getActivity() instanceof GuestMemberProfileActivity) {
                        GuestMemberProfileActivity activity = (GuestMemberProfileActivity) getActivity();
                        activity.openHomeVisitSingleForm(HnppConstants.JSON_FORMS.WOMEN_PACKAGE);
                    }
                    break;
                case HnppConstants.OTHER_SERVICE_TYPE.TYPE_BLOOD:
                    if (getActivity() != null && getActivity() instanceof GuestMemberProfileActivity) {
                        GuestMemberProfileActivity activity = (GuestMemberProfileActivity) getActivity();
                        activity.openHomeVisitSingleForm(HnppConstants.JSON_FORMS.BLOOD_TEST);
                    }
                    break;
                case HnppConstants.OTHER_SERVICE_TYPE.TYPE_EYE:
                    if (getActivity() != null && getActivity() instanceof GuestMemberProfileActivity) {
                        GuestMemberProfileActivity activity = (GuestMemberProfileActivity) getActivity();
                        activity.openHomeVisitSingleForm(HnppConstants.JSON_FORMS.EYE_TEST);
                    }
                    break;
            }
        }

    }
}
