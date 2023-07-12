package org.smartregister.unicef.dghs.model;

import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.chw.core.model.CoreChildRegisterFragmentModel;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.DBConstants;

import java.util.ArrayList;

public class HnppChildRegisterFragmentModel extends CoreChildRegisterFragmentModel {
    @Override
    public String mainSelect(String tableName, String familyName, String familyMemberName, String mainCondition) {
        return mainSelectRegisterWithoutGroupby(tableName, familyName, familyMemberName, mainCondition);
    }
    public static String mainSelectRegisterWithoutGroupby(String tableName, String familyTableName, String familyMemberTableName, String mainCondition) {
        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable(tableName, mainColumns(tableName, familyTableName, familyMemberTableName));
        queryBUilder.customJoin("LEFT JOIN " + familyTableName + " ON  " + tableName + "." + DBConstants.KEY.RELATIONAL_ID + " = " + familyTableName + ".id COLLATE NOCASE ");
        queryBUilder.customJoin("LEFT JOIN " + familyMemberTableName + " ON  " + familyMemberTableName + "." + DBConstants.KEY.BASE_ENTITY_ID + " = " + familyTableName + ".primary_caregiver COLLATE NOCASE ");

        return queryBUilder.mainCondition(mainCondition);
    }

    public static String[] mainColumns(String tableName, String familyTable, String familyMemberTable) {
        ArrayList<String> columnList = new ArrayList<>();
        columnList.add(tableName + "." + DBConstants.KEY.RELATIONAL_ID + " as " + ChildDBConstants.KEY.RELATIONAL_ID);
        columnList.add(tableName + "." + DBConstants.KEY.LAST_INTERACTED_WITH);
        columnList.add(tableName + "." + DBConstants.KEY.BASE_ENTITY_ID);
        columnList.add(tableName + "." + DBConstants.KEY.FIRST_NAME);
        columnList.add(tableName + "." + DBConstants.KEY.MIDDLE_NAME);
        columnList.add(familyMemberTable + "." + HnppConstants.KEY.IS_RISK);
        columnList.add(familyMemberTable + "." + DBConstants.KEY.FIRST_NAME + " as " + ChildDBConstants.KEY.FAMILY_FIRST_NAME);
        columnList.add(familyMemberTable + "." + DBConstants.KEY.LAST_NAME + " as " + ChildDBConstants.KEY.FAMILY_LAST_NAME);
        columnList.add(familyMemberTable + "." + DBConstants.KEY.MIDDLE_NAME + " as " + ChildDBConstants.KEY.FAMILY_MIDDLE_NAME);
        columnList.add(familyMemberTable + "." + ChildDBConstants.PHONE_NUMBER + " as " + ChildDBConstants.KEY.FAMILY_MEMBER_PHONENUMBER);
        columnList.add(familyMemberTable + "." + ChildDBConstants.OTHER_PHONE_NUMBER + " as " + ChildDBConstants.KEY.FAMILY_MEMBER_PHONENUMBER_OTHER);
        columnList.add(familyTable + "." + HnppConstants.KEY.VILLAGE_NAME + " as " + ChildDBConstants.KEY.FAMILY_HOME_ADDRESS);
        columnList.add(tableName + "." + DBConstants.KEY.LAST_NAME);
        columnList.add(tableName + "." + DBConstants.KEY.UNIQUE_ID);
        columnList.add(tableName + "." + DBConstants.KEY.GENDER);
        columnList.add(tableName + "." + DBConstants.KEY.DOB);
        columnList.add(tableName + "." + HnppConstants.KEY.SHR_ID);
        columnList.add(familyTable+ "." + HnppConstants.KEY.BLOCK_NAME);
        columnList.add(familyTable + "." + HnppConstants.KEY.SERIAL_NO);
        columnList.add(tableName + "." + ChildDBConstants.KEY.LAST_HOME_VISIT);
        columnList.add(familyTable + "." + DBConstants.KEY.UNIQUE_ID + " as " + HnppConstants.KEY.HOUSE_HOLD_ID);
        columnList.add(familyTable + "." + DBConstants.KEY.FIRST_NAME + " as " + HnppConstants.KEY.HOUSE_HOLD_NAME);
        columnList.add(tableName + "." + HnppConstants.KEY.CHILD_MOTHER_NAME);
        columnList.add(tableName + "." + ChildDBConstants.KEY.MOTHER_ENTITY_ID);
        columnList.add(tableName + "." + HnppConstants.KEY.WEIGHT_STATUS);
        columnList.add(tableName + "." + HnppConstants.KEY.HEIGHT_STATUS);
        columnList.add(tableName + "." + HnppConstants.KEY.MUAC_STATUS);
        columnList.add(tableName + "." + HnppConstants.KEY.LAST_VACCINE_NAME);
        columnList.add(tableName + "." + HnppConstants.KEY.LAST_VACCINE_DATE);
        columnList.add(tableName + "." + HnppConstants.KEY.NEW_BORN_INFO);
        columnList.add(tableName + "." + HnppConstants.KEY.DUE_VACCINE_NAME);
        columnList.add(tableName + "." + HnppConstants.KEY.DUE_VACCINE_DATE);
        columnList.add(tableName + "." + HnppConstants.KEY.HAS_AEFI);
        columnList.add(tableName + "." + HnppConstants.KEY.AEFI_VACCINE);
        columnList.add(tableName + "." + HnppConstants.KEY.CHILD_MUAC);
        columnList.add(tableName + "." + HnppConstants.KEY.CHILD_HEIGHT);
        columnList.add(tableName + "." + HnppConstants.KEY.CHILD_WEIGHT);
        return columnList.toArray(new String[columnList.size()]);
    }
}
