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

public class HnppPncRegisterFragmentModel extends BaseAncRegisterFragmentModel {

    @Override
    public String mainSelect(String tableName, String mainCondition) {
        SmartRegisterQueryBuilder queryBuilder = new SmartRegisterQueryBuilder();
        queryBuilder.SelectInitiateMainTable(tableName, mainColumns(tableName));
        queryBuilder.customJoin("INNER JOIN " + HnppConstants.TABLE_NAME.FAMILY_MEMBER + " ON  " + tableName + "." + DBConstants.KEY.BASE_ENTITY_ID + " = " + HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.BASE_ENTITY_ID + " AND " + tableName + "." + HnppDBConstants.IS_CLOSED + " IS " + 0 + " AND " + tableName + "." + HnppDBConstants.DELIVERY_DATE + " IS NOT NULL COLLATE NOCASE ");
        queryBuilder.customJoin("INNER JOIN " + HnppConstants.TABLE_NAME.FAMILY + " ON  " + HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.RELATIONAL_ID + " = " + HnppConstants.TABLE_NAME.FAMILY + "." + DBConstants.KEY.BASE_ENTITY_ID + " COLLATE NOCASE ");
        queryBuilder.customJoin("INNER JOIN " + HnppConstants.TABLE_NAME.ANC_MEMBER + " ON  " + tableName + "." + DBConstants.KEY.BASE_ENTITY_ID + " = " + HnppConstants.TABLE_NAME.ANC_MEMBER + "." + DBConstants.KEY.BASE_ENTITY_ID + " AND " + tableName + "." + HnppDBConstants.IS_CLOSED + " IS " + 0 + " AND " + tableName + "." + HnppDBConstants.DELIVERY_DATE + " IS NOT NULL COLLATE NOCASE ");

        queryBuilder.customJoin(" AND " + HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + org.smartregister.chw.anc.util.DBConstants.KEY.DATE_REMOVED + " is null");

        return queryBuilder.mainCondition(mainCondition);
    }

    @Override
    protected String[] mainColumns(String tableName) {
        Set<String> columnList = new HashSet<>();

        columnList.add(tableName + "." + HnppDBConstants.DELIVERY_DATE);
        columnList.add(tableName + "." + DBConstants.KEY.BASE_ENTITY_ID);
        columnList.add(CoreConstants.TABLE_NAME.FAMILY+ "." + HnppConstants.KEY.BLOCK_NAME);
        columnList.add(CoreConstants.TABLE_NAME.FAMILY + "." + HnppConstants.KEY.SERIAL_NO);
        columnList.add(HnppConstants.TABLE_NAME.ANC_MEMBER + "." + org.smartregister.chw.anc.util.DBConstants.KEY.PHONE_NUMBER);
        columnList.add(HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.RELATIONAL_ID + " as " + ChildDBConstants.KEY.RELATIONAL_ID);
        columnList.add(HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.FIRST_NAME);
        columnList.add(HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + HnppConstants.KEY.SHR_ID);
        columnList.add(HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.MIDDLE_NAME);
        columnList.add(HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.LAST_NAME);
        columnList.add(HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.DOB);
        columnList.add(HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.RELATIONAL_ID);
        columnList.add(HnppConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.UNIQUE_ID);
        columnList.add(HnppConstants.TABLE_NAME.FAMILY + "." + DBConstants.KEY.FAMILY_HEAD);
        columnList.add(HnppConstants.TABLE_NAME.FAMILY + "." + DBConstants.KEY.PRIMARY_CAREGIVER);
        columnList.add(HnppConstants.TABLE_NAME.FAMILY + "." + HnppConstants.KEY.VILLAGE_NAME);
        columnList.add(HnppConstants.TABLE_NAME.FAMILY + "." + DBConstants.KEY.FIRST_NAME + " as " + org.smartregister.chw.anc.util.DBConstants.KEY.FAMILY_NAME);

        return columnList.toArray(new String[columnList.size()]);
    }

}
