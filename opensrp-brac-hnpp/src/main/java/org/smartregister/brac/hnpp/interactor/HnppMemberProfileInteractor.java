package org.smartregister.brac.hnpp.interactor;

import static org.smartregister.brac.hnpp.utils.HnppConstants.eventTypeMapping;
import static org.smartregister.brac.hnpp.utils.HnppConstants.iconMapping;

import android.support.v4.app.Fragment;
import android.text.TextUtils;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.HnppMemberProfileContract;
import org.smartregister.brac.hnpp.fragment.HnppMemberProfileDueFragment;
import org.smartregister.brac.hnpp.fragment.HouseHoldMemberDueFragment;
import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.MemberProfileDueData;
import org.smartregister.brac.hnpp.utils.OtherServiceData;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;
import java.util.Objects;

public class HnppMemberProfileInteractor implements HnppMemberProfileContract.Interactor {

    private static final int TAG_OPEN_ANC1 = 101;

    public static final int TAG_OPEN_FAMILY = 111;
    public static final int TAG_OPEN_REFEREAL = 222;
    private static final int TAG_OPEN_CORONA = 88888;
    private static final int TAG_OPEN_ANC_REGISTRATION = 555;
    private static final int TAG_PREGNANT_WOMAN_DIETARY_DIVERSITY = 556;

    private AppExecutors appExecutors;

    public HnppMemberProfileInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    String eventType = "";

    private ArrayList<MemberProfileDueData> getOtherService(CommonPersonObjectClient commonPersonObjectClient, Fragment context, String baseEntityId) {
        ArrayList<MemberProfileDueData> memberProfileDueDataArrayList = new ArrayList<>();
        try {
            String gender = org.smartregister.util.Utils.getValue(commonPersonObjectClient.getColumnmaps(), "gender", false);
            String maritalStatus = org.smartregister.util.Utils.getValue(commonPersonObjectClient.getColumnmaps(), "marital_status", false);
            if (gender.equalsIgnoreCase("F") && maritalStatus.equalsIgnoreCase("Married")) {
                MemberProfileDueData memberProfileDueData = new MemberProfileDueData();
                memberProfileDueData.setType(TAG_OPEN_ANC1);
                //if women
                eventType = FormApplicability.getDueFormForMarriedWomen(baseEntityId, FormApplicability.getAge(commonPersonObjectClient));
                if (FormApplicability.isDueAnyForm(baseEntityId, eventType) && !TextUtils.isEmpty(eventType)) {

                        memberProfileDueData.setTitle(HnppConstants.visitEventTypeMapping.get(eventType));
                        memberProfileDueData.setImageSource(HnppConstants.iconMapping.get(eventType));
                        memberProfileDueData.setEventType(eventType);
                        memberProfileDueDataArrayList.add(memberProfileDueData);

                    if(eventType.equals(HnppConstants.EVENT_TYPE.ANC1_REGISTRATION) ||
                            eventType.equals(HnppConstants.EVENT_TYPE.ANC2_REGISTRATION) ||
                            eventType.equals(HnppConstants.EVENT_TYPE.ANC3_REGISTRATION)
                    ){
                        memberProfileDueData.setVisibilityStatus(0);
                    }

                }

                if (eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ELCO) && FormApplicability.isPregnant(baseEntityId)) {
                    MemberProfileDueData memberProfileDueData2 = new MemberProfileDueData();
                    memberProfileDueData2.setImageSource(R.drawable.childrow_family);
                    memberProfileDueData2.setType(TAG_OPEN_ANC_REGISTRATION);
                    memberProfileDueData2.setTitle("গর্ভবতী রেজিস্ট্রেশন");
                    memberProfileDueData.setEventType(eventType);
                    memberProfileDueDataArrayList.add(memberProfileDueData2);
                }
                //eventype = anc1||anc2||anc3{
                //(FormApplicability.isDueAnyForm(baseEntityId, "pregency") && !TextUtils.isEmpty(pregency)//}

                switch (eventType) {
                    case HnppConstants.EVENT_TYPE.ANC1_REGISTRATION:
                    case HnppConstants.EVENT_TYPE.ANC2_REGISTRATION:
                    case HnppConstants.EVENT_TYPE.ANC3_REGISTRATION:
                        if (FormApplicability.isDueAnyForm(baseEntityId, HnppConstants.EVENT_TYPE.PREGNANT_WOMAN_DIETARY_DIVERSITY)) {
                            MemberProfileDueData memberProfileDueData2 = new MemberProfileDueData();
                            memberProfileDueData2.setImageSource(R.mipmap.ic_anc_pink);
                            memberProfileDueData2.setType(TAG_PREGNANT_WOMAN_DIETARY_DIVERSITY);
                            memberProfileDueData.setEventType(eventType);
                            memberProfileDueData2.setTitle("গর্ভবতী মহিলাদের খাদ্যতালিকাগত বৈচিত্র্য");
                            if (eventType.equals(HnppConstants.EVENT_TYPE.ANC3_REGISTRATION)) {
                                memberProfileDueData2.setFrom(HnppConstants.EVENT_TYPE.ANC3_REGISTRATION);
                            }
                            memberProfileDueDataArrayList.add(memberProfileDueData2);
                        }else if(FormApplicability.isOnlyANCDue(baseEntityId)){
                            MemberProfileDueData memberProfileDueData2 = new MemberProfileDueData();
                            memberProfileDueData2.setImageSource(R.mipmap.ic_anc_pink);
                            memberProfileDueData2.setType(TAG_OPEN_ANC1);
                            memberProfileDueData2.setEventType(eventType);
                            memberProfileDueData2.setTitle(HnppConstants.visitEventTypeMapping.get(eventType));
                            if (eventType.equals(HnppConstants.EVENT_TYPE.ANC3_REGISTRATION)) {
                                memberProfileDueData2.setFrom(HnppConstants.EVENT_TYPE.ANC3_REGISTRATION);
                            }
                            memberProfileDueDataArrayList.add(memberProfileDueData2);
                        }
                        break;
                }
            }

            if (context instanceof HnppMemberProfileDueFragment) {
                {
                    MemberProfileDueData memberProfileDueData = new MemberProfileDueData();
                    memberProfileDueData.setImageSource(R.drawable.childrow_family);
                    memberProfileDueData.setTitle("পরিবারের অন্যান্য সদস্য সেবা (বাকি)");
                    memberProfileDueData.setType(TAG_OPEN_FAMILY);
                    memberProfileDueDataArrayList.add(memberProfileDueData);
                }
            }

            {
                MemberProfileDueData memberProfileDueData = new MemberProfileDueData();
                memberProfileDueData.setImageSource(R.mipmap.ic_refer);
                memberProfileDueData.setTitle("রেফারেল");
                memberProfileDueData.setType(TAG_OPEN_REFEREAL);
                if (gender.equalsIgnoreCase("F")) {
                    memberProfileDueData.setEventType(HnppConstants.JSON_FORMS.WOMEN_REFERRAL);
                } else {
                    memberProfileDueData.setEventType(HnppConstants.JSON_FORMS.MEMBER_REFERRAL);
                }
                memberProfileDueDataArrayList.add(memberProfileDueData);
            }

            ArrayList<ReferralFollowUpModel> getList = FormApplicability.getReferralFollowUp(baseEntityId);
            for (ReferralFollowUpModel referralFollowUpModel : getList) {
                MemberProfileDueData memberProfileDueData = new MemberProfileDueData();
                memberProfileDueData.setImageSource(iconMapping.get(HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP));
                memberProfileDueData.setTitle(eventTypeMapping.get(HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP));
                memberProfileDueData.setType(TAG_OPEN_REFEREAL);
                memberProfileDueData.setSubTitle(referralFollowUpModel.getReferralReason());
                memberProfileDueData.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_REFERRAL_FOLLOW_UP);
                memberProfileDueData.setReferralFollowUpModel(referralFollowUpModel);
                memberProfileDueData.setEventType(HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP);
                memberProfileDueDataArrayList.add(memberProfileDueData);

            }

            if (context instanceof HouseHoldMemberDueFragment) {

                int age = FormApplicability.getAge(commonPersonObjectClient);
                long day = FormApplicability.getDay(commonPersonObjectClient);
                //String gender = FormApplicability.getGender(commonPersonObjectClient);

                if (FormApplicability.isNcdApplicable(age) && FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(), HnppConstants.EVENT_TYPE.NCD_PACKAGE)) {
                    MemberProfileDueData otherServiceData3 = new MemberProfileDueData();
                    otherServiceData3.setImageSource(R.drawable.ic_sugar_blood_level);
                    otherServiceData3.setTitle("অসংক্রামক রোগের সেবা");
                    otherServiceData3.setEventType(HnppConstants.EVENT_TYPE.NCD_PACKAGE);
                    otherServiceData3.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_NCD);
                    memberProfileDueDataArrayList.add(otherServiceData3);
                }

                if (FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(), HnppConstants.EVENT_TYPE.EYE_TEST)) {
                    MemberProfileDueData otherServiceDataEye = new MemberProfileDueData();
                    otherServiceDataEye.setImageSource(R.drawable.ic_eye);
                    otherServiceDataEye.setTitle("চক্ষু পরীক্ষা");
                    otherServiceDataEye.setEventType(HnppConstants.EVENT_TYPE.EYE_TEST);
                    otherServiceDataEye.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_EYE);
                    memberProfileDueDataArrayList.add(otherServiceDataEye);
                }
            }
        } catch (Exception e) {
        }

        return memberProfileDueDataArrayList;
    }

    @Override
    public String getLastEvent() {
        return eventType;
    }

    @Override
    public void fetchData(CommonPersonObjectClient commonPersonObjectClient, Fragment fragment, String baseEntityId, HnppMemberProfileContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            ArrayList<MemberProfileDueData> memberProfileDueData;
           /* if(HnppConstants.isPALogin()){
                otherServiceData =  getPAService(commonPersonObjectClient);
            }else{*/
            memberProfileDueData = getOtherService(commonPersonObjectClient, fragment, baseEntityId);
            // }
            appExecutors.mainThread().execute(() -> callBack.onUpdateList(memberProfileDueData));
        };
        appExecutors.diskIO().execute(runnable);
    }
}
