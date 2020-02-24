package org.smartregister.chw.core.interactor;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import org.smartregister.chw.core.R;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.contract.FamilyCallDialogContract;
import org.smartregister.chw.core.model.FamilyCallDialogModel;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;

import timber.log.Timber;

public class FamilyCallDialogInteractor implements FamilyCallDialogContract.Interactor {

    private AppExecutors appExecutors;
    private String familyBaseEntityId;


    public FamilyCallDialogInteractor(String familyBaseEntityId) {
        this(new AppExecutors(), familyBaseEntityId);
    }

    @VisibleForTesting
    FamilyCallDialogInteractor(AppExecutors appExecutors, String familyBaseEntityId) {
        this.appExecutors = appExecutors;
        this.familyBaseEntityId = familyBaseEntityId;
    }

    @Override
    public void getHeadOfFamily(final FamilyCallDialogContract.Presenter presenter, final Context context) {

        Runnable runnable = () -> {

            final CommonPersonObject personObject = getCommonRepository(Utils.metadata().familyRegister.tableName).findByBaseEntityId(familyBaseEntityId);
            final CommonPersonObjectClient client = new CommonPersonObjectClient(personObject.getCaseId(), personObject.getDetails(), "");
            client.setColumnmaps(personObject.getColumnmaps());

            String primaryCaregiverID = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.PRIMARY_CAREGIVER, false);
            String familyHeadID = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.FAMILY_HEAD, false);

            if (primaryCaregiverID != null) {
                // load primary care giver
                final FamilyCallDialogModel headModel = prepareModel(context, familyHeadID, primaryCaregiverID, true);
                appExecutors.mainThread().execute(() -> presenter.updateHeadOfFamily((headModel == null || headModel.getPhoneNumber() == null) ? null : headModel));
            }

            if (familyHeadID != null && !familyHeadID.equals(primaryCaregiverID) && primaryCaregiverID != null) {
                final FamilyCallDialogModel careGiverModel = prepareModel(context, familyHeadID, primaryCaregiverID, false);
                appExecutors.mainThread().execute(() -> presenter.updateCareGiver((careGiverModel == null || careGiverModel.getPhoneNumber() == null) ? null : careGiverModel));

            }
        };

        if (familyBaseEntityId != null) {
            appExecutors.diskIO().execute(runnable);
        }
    }

    public CommonRepository getCommonRepository(String tableName) {
        return Utils.context().commonrepository(tableName);
    }

    private FamilyCallDialogModel prepareModel(
            Context context,
            String familyHeadID, String primaryCaregiverID,
            Boolean isHead
    ) {

        if (primaryCaregiverID.equalsIgnoreCase(familyHeadID) && !isHead) {
            return null;
        }
        String tableName =Utils.metadata().familyMemberRegister.tableName;
        String role = context.getString(R.string.head_of_family);
        String baseID = getHouseHoldHeadIdByFamilyId(familyHeadID);//(isHead && StringUtils.isNotBlank(familyHeadID)) ? familyHeadID : primaryCaregiverID;
        if(TextUtils.isEmpty(baseID)){
            baseID = familyHeadID;
            tableName = Utils.metadata().familyRegister.tableName;
            role = "পারিবারিক নাম্বার";
        }
        String[] namePhone = getNameMobile(baseID,tableName);
        String phNo = namePhone[1];
        if(TextUtils.isEmpty(phNo) || phNo.equalsIgnoreCase("0")){
            phNo = getNameMobile(familyHeadID,Utils.metadata().familyRegister.tableName)[1];
        }

        FamilyCallDialogModel model = new FamilyCallDialogModel();
        model.setPhoneNumber(phNo);
        model.setName(
                String.format("%s",namePhone[0])
        );

        model.setRole(role);

        return model;
    }
    public static String getHouseHoldHeadIdByFamilyId(String familyId){
        String query = "select base_entity_id from ec_family_member where  relational_id = '"+familyId+"' and relation_with_household_head = 'Household Head'";
        Cursor cursor = null;
        String entityId="";
        try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() > 0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    entityId = cursor.getString(0);
                    cursor.moveToNext();
                }

            }

        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return entityId;
    }
    public static String[] getNameMobile(String familyID,String tableName){
        String query = "select first_name,phone_number from "+tableName+" where base_entity_id = '"+familyID+"'";
        Cursor cursor = null;
        String[] nameNumber = new String[2];
        try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            cursor.moveToFirst();
            nameNumber[0] = cursor.getString(0);
            nameNumber[1] = cursor.getString(1);
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return nameNumber;
    }

}
