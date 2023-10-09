package org.smartregister.unicef.mis.model;

import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.chw.core.model.CoreChildRegisterFragmentModel;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.DBConstants;

import java.util.ArrayList;

public class HnppElcoMemberRegisterFragmentModel extends CoreChildRegisterFragmentModel {
    @Override
    public String mainSelect(String tableName, String familyName, String familyMemberName, String mainCondition) {
        return mainSelectRegisterWithoutGroupby(familyName,familyMemberName,mainCondition);
    }
    public static String mainSelectRegisterWithoutGroupby( String familyTableName, String familyMemberTableName, String mainCondition) {
        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable(familyMemberTableName,mainColumns(familyTableName, familyMemberTableName));
        queryBUilder.customJoin("LEFT JOIN " + familyTableName + " ON  " + familyMemberTableName + "." + DBConstants.KEY.RELATIONAL_ID + " = " + familyTableName + ".id COLLATE NOCASE ");
        return queryBUilder.mainCondition(mainCondition);
    }

    @Override
    public String countSelect(String tableName, String mainCondition) {
        return "SELECT COUNT(*) from "+tableName+" where "+mainCondition;
    }

    public static String[] mainColumns(String familyTable, String familyMemberTable) {
        ArrayList<String> columnList = new ArrayList<>();
        columnList.add(familyMemberTable + "." + DBConstants.KEY.FIRST_NAME);
        columnList.add(familyMemberTable + "." + DBConstants.KEY.LAST_NAME);
        columnList.add(familyMemberTable + "." + DBConstants.KEY.MIDDLE_NAME );
        columnList.add(familyMemberTable + "." + ChildDBConstants.PHONE_NUMBER);
        columnList.add(familyMemberTable + "." + DBConstants.KEY.BASE_ENTITY_ID);
        columnList.add(familyMemberTable + "." + DBConstants.KEY.RELATIONAL_ID +" as "+ChildDBConstants.KEY.RELATIONAL_ID);
        columnList.add(familyMemberTable + "." +DBConstants.KEY.ENTITY_TYPE);
        columnList.add(familyTable + "." + HnppConstants.KEY.VILLAGE_NAME +" as "+HnppConstants.KEY.VILLAGE_NAME);
        columnList.add(familyMemberTable + "." + DBConstants.KEY.UNIQUE_ID);
        columnList.add(familyMemberTable + "." + DBConstants.KEY.GENDER);
        columnList.add(familyMemberTable + "." + DBConstants.KEY.DOB);
        columnList.add(familyTable + "." + DBConstants.KEY.UNIQUE_ID + " as " + HnppConstants.KEY.HOUSE_HOLD_ID);
        columnList.add(familyTable + "." + DBConstants.KEY.FIRST_NAME + " as " + HnppConstants.KEY.HOUSE_HOLD_NAME);
        columnList.add(familyTable+ "." + HnppConstants.KEY.MODULE_ID);
        columnList.add(familyTable+ "." + HnppConstants.KEY.LAST_HOME_VISIT);
        columnList.add(familyTable+ "." + HnppConstants.KEY.BLOCK_NAME);
        columnList.add(familyTable+ "." + HnppConstants.KEY.SERIAL_NO);
        return columnList.toArray(new String[columnList.size()]);
    }
}
