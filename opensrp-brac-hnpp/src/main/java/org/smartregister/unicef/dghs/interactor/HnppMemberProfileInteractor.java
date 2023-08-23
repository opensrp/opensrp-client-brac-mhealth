package org.smartregister.unicef.dghs.interactor;

import static org.smartregister.unicef.dghs.utils.HnppConstants.eventTypeMapping;
import static org.smartregister.unicef.dghs.utils.HnppConstants.iconMapping;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.smartregister.unicef.dghs.BuildConfig;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.unicef.dghs.contract.HnppMemberProfileContract;
import org.smartregister.unicef.dghs.contract.OtherServiceContract;
import org.smartregister.unicef.dghs.fragment.HnppMemberProfileDueFragment;
import org.smartregister.unicef.dghs.model.ReferralFollowUpModel;
import org.smartregister.unicef.dghs.utils.FormApplicability;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppDBUtils;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.unicef.dghs.utils.MemberProfileDueData;
import org.smartregister.unicef.dghs.utils.OtherServiceData;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class HnppMemberProfileInteractor implements HnppMemberProfileContract.Interactor {

    private static final int TAG_OPEN_ANC1 = 101;

    public static final int TAG_OPEN_FAMILY = 111;
    public static final int TAG_OPEN_REFEREAL = 222;
    private static final int TAG_OPEN_CORONA = 88888;
    public static final int TAG_OPEN_ANC_HISTORY = 111111;
    private AppExecutors appExecutors;
    private Context context;

    public HnppMemberProfileInteractor(AppExecutors appExecutors,Context context){
        this.appExecutors = appExecutors;
        this.context = context;
    }
    String eventType = "";
    private ArrayList<MemberProfileDueData> getOtherService(CommonPersonObjectClient commonPersonObjectClient,String baseEntityId) {
        ArrayList<MemberProfileDueData> memberProfileDueDataArrayList = new ArrayList<>();
        //try{
            String gender = org.smartregister.util.Utils.getValue(commonPersonObjectClient.getColumnmaps(), "gender", false);
            String maritalStatus  = org.smartregister.util.Utils.getValue(commonPersonObjectClient.getColumnmaps(), "marital_status", false);

            if(gender.equalsIgnoreCase("F") && maritalStatus.equalsIgnoreCase("Married")){
                MemberProfileDueData memberProfileDueData = new MemberProfileDueData();
                memberProfileDueData.setType(TAG_OPEN_ANC1);
                //if women
                eventType = FormApplicability.getDueFormForMarriedWomen(baseEntityId,FormApplicability.getAge(commonPersonObjectClient));
                if(!eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ELCO) && FormApplicability.isDueAnyForm(baseEntityId,eventType) && !TextUtils.isEmpty(eventType)){
                    if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_HOME_VISIT)){
                        memberProfileDueData.setTitle(FormApplicability.getANCTitle(baseEntityId));
                        memberProfileDueData.setImageSource(R.mipmap.ic_anc_pink);
                        memberProfileDueData.setEventType(HnppConstants.EVENT_TYPE.ANC_HOME_VISIT);
                        String lmpDate = HnppDBUtils.getLmpDate(baseEntityId);
                        int noOfAnc = (FormApplicability.getANCCount(baseEntityId)+1);
                        String date = HnppConstants.getScheduleLmpDate(lmpDate,noOfAnc);
                        memberProfileDueData.setSubTitle(context.getString(R.string.schedule_date)+date);

                    }else{
                        memberProfileDueData.setTitle(FormApplicability.getPncTitle(baseEntityId));
                        memberProfileDueData.setImageSource(HnppConstants.iconMapping.get(eventType));
                        memberProfileDueData.setEventType(eventType);
                        String deliveryDate = FormApplicability.getDeliveryDate(baseEntityId);
                        int pncCount = (FormApplicability.getPNCCount(baseEntityId)+1);
                        String date = HnppConstants.getSchedulePncDate(deliveryDate,pncCount);
                        memberProfileDueData.setSubTitle(context.getString(R.string.schedule_date)+date);
                    }
                    memberProfileDueDataArrayList.add(memberProfileDueData);

                }

                if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ELCO)// && FormApplicability.isPregnant(baseEntityId)
                ){
                    MemberProfileDueData memberProfileDueData2 = new MemberProfileDueData();
                    memberProfileDueData2.setImageSource(R.drawable.childrow_family);
                    memberProfileDueData2.setType(HnppMemberProfileDueFragment.TAG_OPEN_ANC_REGISTRATION);
                    memberProfileDueData2.setImageSource(HnppConstants.iconMapping.get(HnppConstants.EVENT_TYPE.ANC_REGISTRATION));
                    memberProfileDueData2.setTitle(HnppConstants.getVisitEventTypeMapping().get(HnppConstants.EVENT_TYPE.ANC_REGISTRATION));
                    memberProfileDueDataArrayList.add(memberProfileDueData2);
                }
//                if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY) || eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC1_REGISTRATION)

                if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_HOME_VISIT)
                        ){
                    MemberProfileDueData memberProfileDueData2 = new MemberProfileDueData();
                    memberProfileDueData2.setImageSource(HnppConstants.iconMapping.get(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME));
                    memberProfileDueData2.setType(HnppMemberProfileDueFragment.TAG_OPEN_DELIVERY);
                    memberProfileDueData2.setTitle(HnppConstants.getVisitEventTypeMapping().get(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME));
                    memberProfileDueData2.setSubTitle(context.getString(R.string.edd_date)+FormApplicability.getEdd(baseEntityId));
                    memberProfileDueDataArrayList.add(memberProfileDueData2);

                }
            }else{


                MemberProfileDueData memberProfileDueData = new MemberProfileDueData();
                memberProfileDueData.setImageSource(R.drawable.rowavatar_member);
                memberProfileDueData.setTitle(HnppConstants.getVisitEventTypeMapping().get(HnppConstants.EVENT_TYPE.MEMBER_DISEASE));
                memberProfileDueData.setType(HnppMemberProfileDueFragment.TAG_MEMBER_DISEASE);
                memberProfileDueDataArrayList.add(memberProfileDueData);


            }

        return memberProfileDueDataArrayList;
    }

    @Override
    public String getLastEvent() {
        return eventType;
    }

    @Override
    public void fetchData(CommonPersonObjectClient commonPersonObjectClient, Context context, String baseEntityId, HnppMemberProfileContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            ArrayList<MemberProfileDueData> memberProfileDueData;
           /* if(HnppConstants.isPALogin()){
                otherServiceData =  getPAService(commonPersonObjectClient);
            }else{*/
            memberProfileDueData = getOtherService(commonPersonObjectClient,baseEntityId);
            // }
            appExecutors.mainThread().execute(() -> callBack.onUpdateList(memberProfileDueData));
        };
        appExecutors.diskIO().execute(runnable);
    }
}
