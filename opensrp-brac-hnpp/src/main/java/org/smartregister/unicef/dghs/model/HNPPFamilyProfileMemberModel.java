package org.smartregister.unicef.dghs.model;

import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.chw.core.model.CoreFamilyProfileMemberModel;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.DBConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HNPPFamilyProfileMemberModel extends CoreFamilyProfileMemberModel {
    @Override
    protected String[] mainColumns(String tableName) {
        String[] column = super.mainColumns(tableName);
        Set<String> columnList = new HashSet<>(Arrays.asList(column));
        columnList.add(CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.PHONE_NUMBER);
        columnList.add(CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + HnppConstants.KEY.RELATION_WITH_HOUSEHOLD);
        columnList.add(CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + HnppConstants.KEY.GU_ID);
        columnList.add(CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + HnppConstants.KEY.SHR_ID);
        columnList.add(CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + HnppConstants.KEY.MARITAL_STATUS);
        columnList.add(CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.DOD);
        columnList.add(CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.DATE_REMOVED);
        return columnList.toArray(new String[columnList.size()]);
    }
}
