package org.smartregister.unicef.dghs.interactor;

import android.database.Cursor;
import android.text.TextUtils;

import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.contract.FamilyProfileExtendedContract;
import org.smartregister.chw.core.interactor.CoreFamilyProfileInteractor;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;

import timber.log.Timber;

public class HnppFamilyProfileInteractor extends CoreFamilyProfileInteractor {
    private String phoneNumber;

    @Override
    public void saveRegistration(FamilyEventClient familyEventClient, String jsonString, boolean isEditMode, FamilyProfileContract.InteractorCallBack callBack) {
        super.saveRegistration(familyEventClient, jsonString, isEditMode, callBack);
    }

    @Override
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
                cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
