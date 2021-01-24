package org.smartregister.brac.hnpp.interactor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.OtherServiceContract;
import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.OtherServiceData;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;

import java.util.ArrayList;
import java.util.Date;

import static org.smartregister.brac.hnpp.utils.HnppConstants.eventTypeMapping;
import static org.smartregister.brac.hnpp.utils.HnppConstants.iconMapping;

public class MemberOtherServiceInteractor implements OtherServiceContract.Interactor {

    private AppExecutors appExecutors;

    public MemberOtherServiceInteractor(AppExecutors appExecutors){
        this.appExecutors = appExecutors;
    }


    @Override
    public void fetchData(CommonPersonObjectClient commonPersonObjectClient, Context context, OtherServiceContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            ArrayList<OtherServiceData> otherServiceData;
                    if(HnppConstants.isPALogin()){
                        otherServiceData =  getPAService(commonPersonObjectClient);
                    }else{
                        otherServiceData = getOtherService(commonPersonObjectClient);
                    }
            appExecutors.mainThread().execute(() -> callBack.onUpdateList(otherServiceData));
        };
        appExecutors.diskIO().execute(runnable);

    }
    private ArrayList<OtherServiceData> getPAService(CommonPersonObjectClient commonPersonObjectClient){

        int age = FormApplicability.getAge(commonPersonObjectClient);

        ArrayList<OtherServiceData> otherServiceDataList = new ArrayList<>();


        if(FormApplicability.isNcdApplicable(age) && FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(),HnppConstants.EVENT_TYPE.NCD_PACKAGE)){
            OtherServiceData otherServiceData3 = new OtherServiceData();
            otherServiceData3.setImageSource(R.drawable.ic_sugar_blood_level);
            otherServiceData3.setTitle("অসংক্রামক রোগের সেবা");
            otherServiceData3.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_NCD);
            otherServiceDataList.add(otherServiceData3);
        }

        if(FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(),HnppConstants.EVENT_TYPE.EYE_TEST)){
            OtherServiceData otherServiceData = new OtherServiceData();
            otherServiceData.setImageSource(R.drawable.ic_eye);
            otherServiceData.setTitle("চক্ষু পরীক্ষা");
            otherServiceData.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_EYE);
            otherServiceDataList.add(otherServiceData);
        }
        if( FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(),HnppConstants.EVENT_TYPE.BLOOD_GROUP)){
            OtherServiceData otherServiceData = new OtherServiceData();
            otherServiceData.setImageSource(R.drawable.ic_blood);
            otherServiceData.setTitle("রক্ত পরীক্ষা");
            otherServiceData.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_BLOOD);
            otherServiceDataList.add(otherServiceData);
        }
        {
            OtherServiceData otherServiceData = new OtherServiceData();
            otherServiceData.setImageSource(R.mipmap.ic_refer);
            otherServiceData.setTitle("রেফারেল");
            otherServiceData.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_REFERRAL);
            otherServiceDataList.add(otherServiceData);
        }
        ArrayList<ReferralFollowUpModel> getList = FormApplicability.getReferralFollowUp(commonPersonObjectClient.getCaseId());

        for(ReferralFollowUpModel referralFollowUpModel : getList){
            OtherServiceData otherServiceData = new OtherServiceData();
            otherServiceData.setImageSource(iconMapping.get(HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP));
            otherServiceData.setTitle(eventTypeMapping.get(HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP));
            otherServiceData.setSubTitle(referralFollowUpModel.getReferralReason());
            otherServiceData.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_REFERRAL_FOLLOW_UP);
            otherServiceData.setReferralFollowUpModel(referralFollowUpModel);
            otherServiceDataList.add(otherServiceData);

        }

        return otherServiceDataList;
    }
    private ArrayList<OtherServiceData> getOtherService(CommonPersonObjectClient commonPersonObjectClient){

        int age = FormApplicability.getAge(commonPersonObjectClient);
        long day = FormApplicability.getDay(commonPersonObjectClient);
        String gender = FormApplicability.getGender(commonPersonObjectClient);
        String dobString = Utils.getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.DOB, false);
        Date dob = Utils.dobStringToDate(dobString);

        ArrayList<OtherServiceData> otherServiceDataList = new ArrayList<>();


        if(FormApplicability.isNcdApplicable(age) && FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(),HnppConstants.EVENT_TYPE.NCD_PACKAGE)){
            OtherServiceData otherServiceData3 = new OtherServiceData();
            otherServiceData3.setImageSource(R.drawable.ic_sugar_blood_level);
            otherServiceData3.setTitle("অসংক্রামক রোগের সেবা");
            otherServiceData3.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_NCD);
            otherServiceDataList.add(otherServiceData3);
        }

        if(FormApplicability.isWomenPackageApplicable(commonPersonObjectClient.getCaseId(),age,gender.equalsIgnoreCase("F"))&& FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(),HnppConstants.EVENT_TYPE.WOMEN_PACKAGE)){
            OtherServiceData otherServiceData = new OtherServiceData();
            otherServiceData.setImageSource(R.drawable.ic_women);
            otherServiceData.setTitle("নারী সেবা প্যাকেজ");
            otherServiceData.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_WOMEN_PACKAGE);
            otherServiceDataList.add(otherServiceData);
        }

        if(FormApplicability.isAdolescentApplicable(age,gender.equalsIgnoreCase("F"))&& FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(),HnppConstants.EVENT_TYPE.GIRL_PACKAGE)){
            OtherServiceData otherServiceData2 = new OtherServiceData();
            otherServiceData2.setImageSource(R.drawable.ic_adolescent);
            otherServiceData2.setTitle("কিশোরী সেবা প্যাকেজ");
            otherServiceData2.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_GIRL_PACKAGE);
            otherServiceDataList.add(otherServiceData2);
        }


        if(FormApplicability.isIycfApplicable(dob) && FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(),HnppConstants.EVENT_TYPE.IYCF_PACKAGE)){
            OtherServiceData otherServiceData4 = new OtherServiceData();
            otherServiceData4.setImageSource(R.drawable.ic_child);
            otherServiceData4.setTitle("শিশু সেবা প্যাকেজ (আই.ওয়াই.সি.এফ)");
            otherServiceData4.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_IYCF);
            otherServiceDataList.add(otherServiceData4);
        }
        return otherServiceDataList;
    }
}
