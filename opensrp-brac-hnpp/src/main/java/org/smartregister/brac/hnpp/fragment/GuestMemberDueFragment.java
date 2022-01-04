package org.smartregister.brac.hnpp.fragment;

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

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.GuestMemberProfileActivity;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.GuestMemberData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.OtherServiceData;

import java.util.ArrayList;

import static org.smartregister.brac.hnpp.utils.HnppConstants.eventTypeFormNameMapping;
import static org.smartregister.brac.hnpp.utils.HnppConstants.eventTypeMapping;
import static org.smartregister.brac.hnpp.utils.HnppConstants.guestEventTypeFormNameMapping;
import static org.smartregister.brac.hnpp.utils.HnppConstants.iconMapping;

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
                View anc1View = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
                ImageView imageanc1View = anc1View.findViewById(R.id.image_view);
                TextView nameanc1View =  anc1View.findViewById(R.id.patient_name_age);
                anc1View.setTag(TAG_OPEN_ANC1);
                anc1View.setOnClickListener(this);
                int age =  FormApplicability.getAge(guestMemberData.getDob());
                if(FormApplicability.isElco(age)){
                    String eventType = FormApplicability.getGuestMemberDueFormForWomen(guestMemberData.getBaseEntityId(),age);
                    if(FormApplicability.isDueAnyForm(guestMemberData.getBaseEntityId(),eventType) && !TextUtils.isEmpty(eventType)){
                        nameanc1View.setText(HnppConstants.visitEventTypeMapping.get(eventType));
                        imageanc1View.setImageResource(HnppConstants.iconMapping.get(eventType));
                        anc1View.setTag(org.smartregister.family.R.id.VIEW_ID,eventType);

                        otherServiceView.addView(anc1View);
                    }
                    if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY) || eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC1_REGISTRATION)
                            || eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC2_REGISTRATION) || eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC3_REGISTRATION)){
                        View ancRegistration = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
                        ImageView image = ancRegistration.findViewById(R.id.image_view);
                        TextView name =  ancRegistration.findViewById(R.id.patient_name_age);
                        ancRegistration.findViewById(R.id.status).setVisibility(View.INVISIBLE);
                        image.setImageResource(HnppConstants.iconMapping.get(eventType));
                        name.setText(HnppConstants.visitEventTypeMapping.get(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME));
                        ancRegistration.setTag(TAG_OPEN_DELIVERY);
                        ancRegistration.setOnClickListener(this);
                        otherServiceView.addView(ancRegistration);
                    }
                }

            }
        }

        updateServiceForm();



    }
    private void updateServiceForm(){
        int age =  FormApplicability.getAge(guestMemberData.getDob());
        ArrayList<OtherServiceData> otherServiceDataList = new ArrayList<>();
        if(FormApplicability.isNcdApplicable(age) && FormApplicability.isDueAnyForm(guestMemberData.getBaseEntityId(),HnppConstants.EVENT_TYPE.NCD_PACKAGE)){
            OtherServiceData otherServiceData3 = new OtherServiceData();
            otherServiceData3.setImageSource(R.drawable.ic_sugar_blood_level);
            otherServiceData3.setTitle("অসংক্রামক রোগের সেবা");
            otherServiceData3.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_NCD);
            otherServiceDataList.add(otherServiceData3);
        }

        if(FormApplicability.isWomenPackageApplicable(guestMemberData.getBaseEntityId(),age,guestMemberData.getGender().equalsIgnoreCase("F"))&&
                FormApplicability.isDueAnyForm(guestMemberData.getBaseEntityId(),HnppConstants.EVENT_TYPE.WOMEN_PACKAGE)){
            OtherServiceData otherServiceData = new OtherServiceData();
            otherServiceData.setImageSource(R.drawable.ic_women);
            otherServiceData.setTitle("নারী সেবা প্যাকেজ");
            otherServiceData.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_WOMEN_PACKAGE);
            otherServiceDataList.add(otherServiceData);
        }

        if(FormApplicability.isAdolescentApplicable(age,guestMemberData.getGender().equalsIgnoreCase("F"))&&
                FormApplicability.isDueAnyForm(guestMemberData.getBaseEntityId(),HnppConstants.EVENT_TYPE.GIRL_PACKAGE)){
            OtherServiceData otherServiceData2 = new OtherServiceData();
            otherServiceData2.setImageSource(R.drawable.ic_adolescent);
            otherServiceData2.setTitle("কিশোরী সেবা প্যাকেজ");
            otherServiceData2.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_GIRL_PACKAGE);
            otherServiceDataList.add(otherServiceData2);
        }

        if(FormApplicability.isIycfApplicable(FormApplicability.getDay(guestMemberData.getDob())) && FormApplicability.isDueAnyForm(guestMemberData.getBaseEntityId(),HnppConstants.EVENT_TYPE.IYCF_PACKAGE)){
            OtherServiceData otherServiceData4 = new OtherServiceData();
            otherServiceData4.setImageSource(R.drawable.ic_child);
            otherServiceData4.setTitle("শিশু সেবা প্যাকেজ (আই.ওয়াই.সি.এফ)");
            otherServiceData4.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_IYCF);
            otherServiceDataList.add(otherServiceData4);
        }
        if(HnppConstants.isPALogin()){
            if(FormApplicability.isDueAnyForm(guestMemberData.getBaseEntityId(),HnppConstants.EVENT_TYPE.EYE_TEST)){
                OtherServiceData otherServiceData = new OtherServiceData();
                otherServiceData.setImageSource(R.drawable.ic_eye);
                otherServiceData.setTitle("চক্ষু পরীক্ষা");
                otherServiceData.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_EYE);
                otherServiceDataList.add(otherServiceData);
            }
            if( FormApplicability.isDueAnyForm(guestMemberData.getBaseEntityId(),HnppConstants.EVENT_TYPE.BLOOD_GROUP)){
                OtherServiceData otherServiceData = new OtherServiceData();
                otherServiceData.setImageSource(R.drawable.ic_blood);
                otherServiceData.setTitle("রক্ত পরীক্ষা");
                otherServiceData.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_BLOOD);
                otherServiceDataList.add(otherServiceData);
            }
        }
        if(otherServiceDataList.size()>0){
            for(OtherServiceData otherServiceData : otherServiceDataList){
                View serviceLayout = LayoutInflater.from(getContext()).inflate(R.layout.view_member_due,null);
                ImageView imgFollowup = serviceLayout.findViewById(R.id.image_view);
                TextView nReferel =  serviceLayout.findViewById(R.id.patient_name_age);
                TextView lastVisitRow = serviceLayout.findViewById(R.id.last_visit);
                lastVisitRow.setVisibility(View.GONE);
                serviceLayout.findViewById(R.id.status).setVisibility(View.INVISIBLE);
                imgFollowup.setImageResource(otherServiceData.getImageSource());
                nReferel.setText(otherServiceData.getTitle());
                serviceLayout.setTag(otherServiceData.getType());
                serviceLayout.setOnClickListener(this);
                otherServiceView.addView(serviceLayout);
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
