package org.smartregister.brac.hnpp.interactor;

import android.content.Context;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.OtherServiceContract;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.OtherServiceData;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class MemberOtherServiceInteractor implements OtherServiceContract.Interactor {

    private AppExecutors appExecutors;

    public MemberOtherServiceInteractor(AppExecutors appExecutors){
        this.appExecutors = appExecutors;
    }


    @Override
    public void fetchData(CommonPersonObjectClient commonPersonObjectClient, Context context, OtherServiceContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            ArrayList<OtherServiceData> otherServiceData = getOtherService(commonPersonObjectClient);
            appExecutors.mainThread().execute(() -> callBack.onUpdateList(otherServiceData));
        };
        appExecutors.diskIO().execute(runnable);

    }
    private ArrayList<OtherServiceData> getOtherService(CommonPersonObjectClient commonPersonObjectClient){

        int age = FormApplicability.getAge(commonPersonObjectClient);
        int day = FormApplicability.getDay(commonPersonObjectClient);
        String gender = FormApplicability.getGender(commonPersonObjectClient);

        ArrayList<OtherServiceData> otherServiceDataList = new ArrayList<>();


        if(FormApplicability.isNcdApplicable(age) && FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(),HnppConstants.EVENT_TYPE.NCD_PACKAGE)){
            OtherServiceData otherServiceData3 = new OtherServiceData();
            otherServiceData3.setImageSource(R.drawable.ic_sugar_blood_level);
            otherServiceData3.setTitle("অসংক্রামক রোগের সেবা");
            otherServiceData3.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_NCD);
            otherServiceDataList.add(otherServiceData3);
        }

        if(FormApplicability.isWomenPackageApplicable(age,gender.equalsIgnoreCase("F"))&& FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(),HnppConstants.EVENT_TYPE.WOMEN_PACKAGE)){
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

        if(FormApplicability.isIycfApplicable(day) && FormApplicability.isDueAnyForm(commonPersonObjectClient.getCaseId(),HnppConstants.EVENT_TYPE.IYCF_PACKAGE)){
            OtherServiceData otherServiceData4 = new OtherServiceData();
            otherServiceData4.setImageSource(R.drawable.ic_child);
            otherServiceData4.setTitle("শিশু সেবা প্যাকেজ (আই.ওয়াই.সি.এফ)");
            otherServiceData4.setType(HnppConstants.OTHER_SERVICE_TYPE.TYPE_IYCF);
            otherServiceDataList.add(otherServiceData4);
        }
        return otherServiceDataList;
    }
}
