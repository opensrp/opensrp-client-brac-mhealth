package org.smartregister.unicef.mis.interactor;

import android.content.Context;

import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.contract.OtherServiceContract;
import org.smartregister.unicef.mis.model.ReferralFollowUpModel;
import org.smartregister.unicef.mis.utils.FormApplicability;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.OtherServiceData;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

import static org.smartregister.unicef.mis.utils.HnppConstants.eventTypeMapping;
import static org.smartregister.unicef.mis.utils.HnppConstants.iconMapping;

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
            otherServiceData3.setTitle(HnppApplication.appContext.getString(R.string.ncd_package));
            otherServiceData3.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_NCD);
            otherServiceDataList.add(otherServiceData3);
        }

        if(FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(),HnppConstants.EVENT_TYPE.EYE_TEST)){
            OtherServiceData otherServiceData = new OtherServiceData();
            otherServiceData.setImageSource(R.drawable.ic_eye);
            otherServiceData.setTitle(HnppApplication.appContext.getString(R.string.eye_test));
            otherServiceData.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_EYE);
            otherServiceDataList.add(otherServiceData);
        }
        if( FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(),HnppConstants.EVENT_TYPE.BLOOD_GROUP)){
            OtherServiceData otherServiceData = new OtherServiceData();
            otherServiceData.setImageSource(R.drawable.ic_blood);
            otherServiceData.setTitle(HnppApplication.appContext.getString(R.string.blood_test));
            otherServiceData.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_BLOOD);
            otherServiceDataList.add(otherServiceData);
        }
        {
            OtherServiceData otherServiceData = new OtherServiceData();
            otherServiceData.setImageSource(R.mipmap.ic_refer);
            otherServiceData.setTitle(HnppApplication.appContext.getString(R.string.referrel));
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
        if(commonPersonObjectClient == null) return new ArrayList<>();

        int age = FormApplicability.getAge(commonPersonObjectClient);
        long day = FormApplicability.getDay(commonPersonObjectClient);
        String gender = FormApplicability.getGender(commonPersonObjectClient);

        ArrayList<OtherServiceData> otherServiceDataList = new ArrayList<>();


        if(FormApplicability.isNcdApplicable(age) && FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(),HnppConstants.EVENT_TYPE.NCD_PACKAGE)){
            OtherServiceData otherServiceData3 = new OtherServiceData();
            otherServiceData3.setImageSource(R.drawable.ic_sugar_blood_level);
            otherServiceData3.setTitle(HnppApplication.appContext.getString(R.string.ncd_package));
            otherServiceData3.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_NCD);
            otherServiceDataList.add(otherServiceData3);
        }

        if(FormApplicability.isWomenPackageApplicable(commonPersonObjectClient.getCaseId(),age,gender.equalsIgnoreCase("F"))&& FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(),HnppConstants.EVENT_TYPE.WOMEN_PACKAGE)){
            OtherServiceData otherServiceData = new OtherServiceData();
            otherServiceData.setImageSource(R.drawable.ic_women);
            otherServiceData.setTitle(HnppApplication.appContext.getString(R.string.woman_package));
            otherServiceData.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_WOMEN_PACKAGE);
            otherServiceDataList.add(otherServiceData);
        }

        if(FormApplicability.isAdolescentApplicable(age,gender.equalsIgnoreCase("F"))&& FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(),HnppConstants.EVENT_TYPE.GIRL_PACKAGE)){
            OtherServiceData otherServiceData2 = new OtherServiceData();
            otherServiceData2.setImageSource(R.drawable.ic_adolescent);
            otherServiceData2.setTitle(HnppApplication.appContext.getString(R.string.adolescent_package));
            otherServiceData2.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_GIRL_PACKAGE);
            otherServiceDataList.add(otherServiceData2);
        }

        if(FormApplicability.isIycfApplicable(day) && FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(),HnppConstants.EVENT_TYPE.IYCF_PACKAGE)){
            OtherServiceData otherServiceData4 = new OtherServiceData();
            otherServiceData4.setImageSource(R.drawable.ic_child);
            otherServiceData4.setTitle(HnppApplication.appContext.getString(R.string.child_package_iyocf));
            otherServiceData4.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_IYCF);
            otherServiceDataList.add(otherServiceData4);
        }
        return otherServiceDataList;
    }
}