package org.smartregister.unicef.dghs.model;

import org.smartregister.unicef.dghs.utils.ChildDBConstants;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppDBConstants;
import org.smartregister.chw.anc.model.BaseAncRegisterFragmentModel;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.DBConstants;

import java.util.HashSet;
import java.util.Set;

public class HnppPncRiskRegisterFragmentModel extends HnppPncRegisterFragmentModel {

    @Override
    public String mainSelect(String tableName, String mainCondition) {
        SmartRegisterQueryBuilder queryBuilder = new SmartRegisterQueryBuilder();
        queryBuilder.SelectInitiateMainTable(tableName, mainColumns(tableName));
        queryBuilder.customJoin("INNER JOIN " + HnppConstants.TABLE_NAME.FAMILY_MEMBER + " ON  " + tableName + "." + DBConstants.KEY.BASE_ENTITY_ID + " = " + HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.BASE_ENTITY_ID + " AND " + tableName + "." + HnppDBConstants.IS_CLOSED + " IS " + 0 + " AND " + tableName + "." + HnppDBConstants.DELIVERY_DATE + " IS NOT NULL COLLATE NOCASE ");
        queryBuilder.customJoin("INNER JOIN " + HnppConstants.TABLE_NAME.FAMILY + " ON  " + HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.RELATIONAL_ID + " = " + HnppConstants.TABLE_NAME.FAMILY + "." + DBConstants.KEY.BASE_ENTITY_ID + " COLLATE NOCASE ");
        queryBuilder.customJoin("INNER JOIN " + HnppConstants.TABLE_NAME.ANC_MEMBER + " ON  " + tableName + "." + DBConstants.KEY.BASE_ENTITY_ID + " = " + HnppConstants.TABLE_NAME.ANC_MEMBER + "." + DBConstants.KEY.BASE_ENTITY_ID + " AND " + tableName + "." + HnppDBConstants.IS_CLOSED + " IS " + 0 + " AND " + tableName + "." + HnppDBConstants.DELIVERY_DATE + " IS NOT NULL COLLATE NOCASE ");

        queryBuilder.customJoin(" AND " + HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + org.smartregister.chw.anc.util.DBConstants.KEY.DATE_REMOVED + " is null");
        queryBuilder.customJoin(" AND " + HnppConstants.TABLE_NAME.FAMILY_MEMBER + ".is_risk = 'true' and "+HnppConstants.TABLE_NAME.FAMILY_MEMBER+".risk_event_type ='"+ HnppConstants.EVENT_TYPE.PNC_REGISTRATION +"'");

        return queryBuilder.mainCondition(mainCondition);
    }


}
