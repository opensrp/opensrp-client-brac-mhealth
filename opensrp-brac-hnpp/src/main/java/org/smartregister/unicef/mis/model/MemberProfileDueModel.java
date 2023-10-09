package org.smartregister.unicef.mis.model;

import org.apache.commons.lang3.ArrayUtils;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.family.model.BaseFamilyProfileDueModel;

public class MemberProfileDueModel extends BaseFamilyProfileDueModel {

    @Override
    protected String[] mainColumns(String tableName) {
        String[] columns = super.mainColumns(tableName);
        String[] newColumns = new String[]{
                tableName + "." + ChildDBConstants.KEY.LAST_HOME_VISIT,
                tableName + "." + ChildDBConstants.KEY.DATE_CREATED
        };

        return ArrayUtils.addAll(columns, newColumns);
    }
}
