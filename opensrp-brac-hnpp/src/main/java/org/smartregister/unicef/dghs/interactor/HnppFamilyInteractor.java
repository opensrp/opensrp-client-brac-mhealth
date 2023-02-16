package org.smartregister.unicef.dghs.interactor;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.unicef.dghs.utils.HnppDBUtils;
import org.smartregister.chw.core.enums.ImmunizationState;
import org.smartregister.chw.core.model.ChildVisit;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.family.util.DBConstants;

import io.reactivex.Observable;
import timber.log.Timber;

public class HnppFamilyInteractor  {

    public ImmunizationState getChildStatus(Context context, final String childId, Cursor cursor) {
        CommonPersonObject personObject = org.smartregister.family.util.Utils.context().commonrepository(CoreConstants.TABLE_NAME.CHILD).findByBaseEntityId(cursor.getString(1));
        if (!personObject.getCaseId().equalsIgnoreCase(childId)) {

            String dobString = org.smartregister.util.Utils.getValue(personObject.getColumnmaps(), DBConstants.KEY.DOB, false);
            String lastHomeVisitStr = org.smartregister.util.Utils.getValue(personObject.getColumnmaps(), ChildDBConstants.KEY.LAST_HOME_VISIT, false);
            String strDateCreated = org.smartregister.family.util.Utils.getValue(personObject.getColumnmaps(), ChildDBConstants.KEY.DATE_CREATED, false);
            long lastHomeVisit = TextUtils.isEmpty(lastHomeVisitStr) ? 0 : Long.parseLong(lastHomeVisitStr);
            long visitNotDone = 0;

            long dateCreated = 0;
            if (!TextUtils.isEmpty(strDateCreated)) {
                dateCreated = org.smartregister.family.util.Utils.dobStringToDateTime(strDateCreated).getMillis();
            }

            final ChildVisit childVisit = HnppDBUtils.getChildVisitStatus(context, dobString, lastHomeVisit, visitNotDone, dateCreated);
            return getImmunizationStatus(childVisit.getVisitStatus());
        }
        return ImmunizationState.NO_ALERT;
    }
    public static ImmunizationState getImmunizationStatus(String visitStatus) {
        if (visitStatus.equalsIgnoreCase(CoreConstants.VisitType.OVERDUE.name())) {
            return ImmunizationState.OVERDUE;
        }
        if (visitStatus.equalsIgnoreCase(CoreConstants.VisitType.DUE.name())) {
            return ImmunizationState.DUE;
        }
        return ImmunizationState.NO_ALERT;
    }
    public Observable<String> updateFamilyDueStatus(final Context context, final String childId, final String familyId) {
        return Observable.create(e -> {
            ImmunizationState familyImmunizationState = ImmunizationState.NO_ALERT;
            String query = CoreChildUtils.getChildListByFamilyId(CoreConstants.TABLE_NAME.CHILD, familyId);
            Cursor cursor = null;
            try {
                cursor = org.smartregister.family.util.Utils.context().commonrepository(CoreConstants.TABLE_NAME.CHILD).queryTable(query);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        familyImmunizationState = getImmunizationState(familyImmunizationState, cursor, context, childId);
                    } while (cursor.moveToNext());
                }
            } catch (Exception ex) {
                Timber.e(ex.toString());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            e.onNext(toStringFamilyState(familyImmunizationState));
        });

    }
    private ImmunizationState getImmunizationState(ImmunizationState familyImmunizationState, Cursor cursor, Context context, String childId) {
        ImmunizationState finalState = familyImmunizationState;
        switch (this.getChildStatus(context, childId, cursor)) {
            case DUE:
                if (familyImmunizationState != ImmunizationState.OVERDUE) {
                    finalState = ImmunizationState.DUE;
                }
                break;
            case OVERDUE:
                finalState = ImmunizationState.OVERDUE;
                break;
            default:
                break;
        }
        return finalState;
    }

    private static String toStringFamilyState(ImmunizationState state) {
        if (state.equals(ImmunizationState.DUE)) {
            return CoreConstants.FamilyServiceType.DUE.name();
        } else if (state.equals(ImmunizationState.OVERDUE)) {
            return CoreConstants.FamilyServiceType.OVERDUE.name();
        } else {
            return CoreConstants.FamilyServiceType.NOTHING.name();
        }
    }

}
