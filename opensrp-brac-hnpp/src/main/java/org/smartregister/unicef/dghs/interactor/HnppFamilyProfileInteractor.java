package org.smartregister.unicef.dghs.interactor;

import android.database.Cursor;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;


import org.smartregister.chw.core.contract.FamilyProfileExtendedContract;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.unicef.dghs.HnppApplication;

import timber.log.Timber;

public class HnppFamilyProfileInteractor extends org.smartregister.family.interactor.FamilyProfileInteractor {
    private String phoneNumber;
    protected AppExecutors appExecutors;

    public HnppFamilyProfileInteractor() {
        this(new AppExecutors());
    }
    @VisibleForTesting
    private HnppFamilyProfileInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }
    @Override
    public void saveRegistration(FamilyEventClient familyEventClient, String jsonString, boolean isEditMode, FamilyProfileContract.InteractorCallBack callBack) {
        super.saveRegistration(familyEventClient, jsonString, isEditMode, callBack);
    }

    public void verifyHasPhone(String familyID, FamilyProfileExtendedContract.PresenterCallBack profilePresenter) {
        Runnable runnable = () -> {

            phoneNumber = getPhoneNumber(familyID);

            final boolean hasPhone = !TextUtils.isEmpty(phoneNumber) && !phoneNumber.equalsIgnoreCase("0");

            appExecutors.mainThread().execute(() -> profilePresenter.notifyHasPhone(hasPhone));
        };

        appExecutors.diskIO().execute(runnable);
    }
    public String getPhoneNumber(){
        return phoneNumber;
    }
    private String getPhoneNumber(String familyId){
            String query = "select phone_number from ec_family where base_entity_id = '"+familyId+"'";
            Cursor cursor = null;
            String phone="";
            try {
                cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
                if(cursor !=null && cursor.getCount() >0){
                    cursor.moveToFirst();
                    phone = cursor.getString(0);
                    cursor.close();
                }

                return phone;
            } catch (Exception e) {
                Timber.e(e);
            }
            return phone;

    };
}
