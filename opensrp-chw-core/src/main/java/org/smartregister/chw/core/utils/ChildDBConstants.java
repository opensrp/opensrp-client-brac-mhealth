package org.smartregister.chw.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.family.util.DBConstants;

public class ChildDBConstants {
    public static final String OTHER_PHONE_NUMBER = "other_phone_number";
    public static final String PHONE_NUMBER = "phone_number";
    private static final int FIVE_YEAR = 5;

    public static String childAgeLimitFilter() {
        return childAgeLimitFilter(DBConstants.KEY.DOB, FIVE_YEAR);
    }
    public static String riskChildAgeLimitFilter() {
        return childAgeLimitFilter(DBConstants.KEY.DOB, FIVE_YEAR)+" "+riskChildPatient();
    }
    public static String riskChildPatient(){
        return " AND ec_child.is_risk = 'true'";
    }
    public static String riskAncPatient(){
        return " AND ec_family_member.is_risk = 'true'";
    }
    public static String riskElcoFilterWithTableName(){
        String query = " ((( julianday('now') - julianday(ec_family_member.dob))/365) >" + 10 + ")";
        String query2 = " ((( julianday('now') - julianday(ec_family_member.dob))/365) <" + 50 + ")";
        String married = " ec_family_member.marital_status = 'Married' and ec_family_member.gender = 'F'";
        return query+" AND "+query2 +" AND "+married +riskAncPatient()+ " AND "+CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.BASE_ENTITY_ID+" " +
                " NOT IN (select ec_anc_register.base_entity_id from ec_anc_register where ec_anc_register.is_closed = '0' group by ec_anc_register.base_entity_id)" +
                " and ec_family_member.base_entity_id  NOT IN (select ec_pregnancy_outcome.base_entity_id from ec_pregnancy_outcome where ec_pregnancy_outcome.is_closed = '0' group by ec_pregnancy_outcome.base_entity_id)";
    }
    public static String riskElcoFilter(){
        String query = " ((( julianday('now') - julianday(dob))/365) >" + 10 + ")";
        String query2 = " ((( julianday('now') - julianday(dob))/365) <" + 50 + ")";
        String married = " marital_status = 'Married' and gender = 'F'";
        return query+" AND "+query2 +" AND "+married +riskAncPatient()+ " AND "+CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.BASE_ENTITY_ID+" " +
                " NOT IN  (select ec_anc_register.base_entity_id from ec_anc_register where ec_anc_register.is_closed = '0' group by ec_anc_register.base_entity_id) " +
                " and ec_family_member.base_entity_id  NOT IN (select ec_pregnancy_outcome.base_entity_id from ec_pregnancy_outcome where ec_pregnancy_outcome.is_closed = '0' group by ec_pregnancy_outcome.base_entity_id)";

    }
    public static String riskAdultFilterWithTableName(){
        String query = " ((( julianday('now') - julianday(ec_family_member.dob))/365) >" + 18 + ")";
        return query+riskAncPatient();
    }
    public static String AdultFilterWithTableName(){
        String query = " ((( julianday('now') - julianday(ec_family_member.dob))/365) >" + 18 + ")";
        return query;
    }
    public static String AdoFilterWithTableName(){
        String query = " ((( julianday('now') - julianday(dob))/365) >" + 10 + ")";
        String query2 = " ((( julianday('now') - julianday(dob))/365) <" + 20 + ")";
        String gender = " gender = 'F'";
        return query+" AND "+query2 +" AND "+gender ;
    }
    public static String IycfFilterWithTableName(){
        String query = " ((( julianday('now') - julianday(ec_child.dob))/365) >" + 0.7 + ")";
        String query2 = " ((( julianday('now') - julianday(ec_child.dob))/365) <" + 2 + ")";
        return query+" AND "+query2 ;
    }
    public static String WomenFilterWithTableName(){
        String query = " ((( julianday('now') - julianday(dob))/365) >=" + 18 + ")";
        String gender = " gender = 'F'";
        return query+" AND "+gender + " AND "+CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.BASE_ENTITY_ID+" " +
                " NOT IN  (select ec_anc_register.base_entity_id from ec_anc_register where ec_anc_register.is_closed = '0' group by ec_anc_register.base_entity_id) " +
                " and ec_family_member.base_entity_id  NOT IN (select ec_pregnancy_outcome.base_entity_id from ec_pregnancy_outcome where ec_pregnancy_outcome.is_closed = '0' group by ec_pregnancy_outcome.base_entity_id)";

    }
    public static String elcoFilterWithTableName(){
        String query = " ((( julianday('now') - julianday(ec_family_member.dob))/365) >" + 10 + ")";
        String query2 = " ((( julianday('now') - julianday(ec_family_member.dob))/365) <" + 50 + ")";
        String married = " ec_family_member.marital_status = 'Married' and ec_family_member.gender = 'F'";
        return query+" AND "+query2 +" AND "+married + " AND "+CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.BASE_ENTITY_ID+" " +
                " NOT IN (select ec_anc_register.base_entity_id from ec_anc_register where ec_anc_register.is_closed = '0' group by ec_anc_register.base_entity_id)" +
                " and ec_family_member.base_entity_id  NOT IN (select ec_pregnancy_outcome.base_entity_id from ec_pregnancy_outcome where ec_pregnancy_outcome.is_closed = '0' group by ec_pregnancy_outcome.base_entity_id)";
    }
    public static String elcoFilter(){
        String query = " ((( julianday('now') - julianday(dob))/365) >" + 10 + ")";
        String query2 = " ((( julianday('now') - julianday(dob))/365) <" + 50 + ")";
        String married = " marital_status = 'Married' and gender = 'F'";
        return query+" AND "+query2 +" AND "+married + " AND "+CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.BASE_ENTITY_ID+" " +
                " NOT IN  (select ec_anc_register.base_entity_id from ec_anc_register where ec_anc_register.is_closed = '0' group by ec_anc_register.base_entity_id) " +
                " and ec_family_member.base_entity_id  NOT IN (select ec_pregnancy_outcome.base_entity_id from ec_pregnancy_outcome where ec_pregnancy_outcome.is_closed = '0' group by ec_pregnancy_outcome.base_entity_id)";

    }
    public static String adolocentElcoFilter(){
        String query = " ((( julianday('now') - julianday(dob))/365) >" + 10 + ")";
        String query2 = " ((( julianday('now') - julianday(dob))/365) <" + 19 + ")";
        String married = " marital_status = 'Married' and gender = 'F'";
        return query+" AND "+query2 +" AND "+married + " AND "+CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.BASE_ENTITY_ID+" " +
                " NOT IN  (select ec_anc_register.base_entity_id from ec_anc_register where ec_anc_register.is_closed = '0' group by ec_anc_register.base_entity_id) " +
                " and ec_family_member.base_entity_id  NOT IN (select ec_pregnancy_outcome.base_entity_id from ec_pregnancy_outcome where ec_pregnancy_outcome.is_closed = '0' group by ec_pregnancy_outcome.base_entity_id)";

    }

    private static String childAgeLimitFilter(String dateColumn, int age) {
        return " ((( julianday('now') - julianday(" + dateColumn + "))/365) <" + age + ")";
    }

    public static String childAgeLimitFilter(String tableName) {
        return childAgeLimitFilter(tableColConcat(tableName, DBConstants.KEY.DOB), FIVE_YEAR);
    }

    public static String tableColConcat(String tableName, String columnName) {
        if (StringUtils.isBlank(tableName) || StringUtils.isBlank(columnName)) {
            return "";
        }
        return tableName.concat(".").concat(columnName);
    }

    public static String childDueFilter() {
        return "(( " +
                "IFNULL(STRFTIME('%Y%m%d%H%M%S', datetime((" + KEY.LAST_HOME_VISIT + ")/1000,'unixepoch')),0) " +
                "< STRFTIME('%Y%m%d%H%M%S', datetime('now','start of month')) " +
                "AND IFNULL(STRFTIME('%Y%m%d%H%M%S', datetime((" + KEY.VISIT_NOT_DONE + ")/1000,'unixepoch')),0) " +
                "< STRFTIME('%Y%m%d%H%M%S', datetime('now','start of month')) " +
                " ))";
    }

    public static String childMainFilter(String mainCondition, String mainMemberCondition, String filters, String sort, int limit, int offset) {
        return "SELECT " + CommonFtsObject.idColumn + " FROM " + CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.CHILD) + " WHERE " + CommonFtsObject.idColumn + " IN " +
                " ( SELECT " + CommonFtsObject.idColumn + " FROM " + CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.CHILD) + " WHERE  " + mainCondition + "  AND " + CommonFtsObject.phraseColumn + matchPhrase(filters) +
                " UNION " +
                " SELECT " + tableColConcat(CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.CHILD), CommonFtsObject.idColumn) + " FROM " + CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.CHILD) +
                " JOIN " + CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.FAMILY) + " on " + tableColConcat(CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.CHILD), CommonFtsObject.relationalIdColumn) + " = " + tableColConcat(CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.FAMILY), CommonFtsObject.idColumn) +
                " JOIN " + CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.FAMILY_MEMBER) + " on " + tableColConcat(CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.FAMILY_MEMBER), CommonFtsObject.idColumn) + " = " + tableColConcat(CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.FAMILY), DBConstants.KEY.PRIMARY_CAREGIVER) +
                " WHERE  " + mainMemberCondition.trim() + " AND " + tableColConcat(CommonFtsObject.searchTableName(CoreConstants.TABLE_NAME.FAMILY_MEMBER), CommonFtsObject.phraseColumn + matchPhrase(filters)) +
                ")  " + orderByClause(sort) + limitClause(limit, offset);
    }

    public static String matchPhrase(String phrase) {
        String stringPhrase = phrase;
        if (stringPhrase == null) {
            stringPhrase = "";
        }

        // Underscore does not work well in fts search
        if (stringPhrase.contains("_")) {
            stringPhrase = stringPhrase.replace("_", "");
        }
        return " MATCH '" + stringPhrase + "*' ";

    }

    public static String orderByClause(String sort) {
        if (StringUtils.isNotBlank(sort)) {
            return " ORDER BY " + sort;
        }
        return "";
    }

    public static String limitClause(int limit, int offset) {
        return " LIMIT " + offset + "," + limit;
    }

    public static final class KEY {
        //public static final String VISIT_STATUS = "visit_status";
        public static final String VISIT_NOT_DONE = "visit_not_done";
        public static final String LAST_HOME_VISIT = "last_home_visit";
        public static final String DATE_CREATED = "date_created";
        public static final String RELATIONAL_ID = "relationalid";
        public static final String FAMILY_FIRST_NAME = "family_first_name";
        public static final String FAMILY_MIDDLE_NAME = "family_middle_name";
        public static final String FAMILY_MEMBER_PHONENUMBER = "family_member_phone_number";
        public static final String FAMILY_MEMBER_PHONENUMBER_OTHER = "family_member_phone_number_other";
        public static final String FAMILY_LAST_NAME = "family_last_name";
        public static final String FAMILY_HOME_ADDRESS = "family_home_address";
        public static final String ENTITY_TYPE = "entity_type";
        public static final String CHILD_BF_HR = "early_bf_1hr";
        public static final String CHILD_PHYSICAL_CHANGE = "physically_challenged";
        public static final String BIRTH_CERT = "birth_cert";
        public static final String BIRTH_CERT_ISSUE_DATE = "birth_cert_issue_date";
        public static final String BIRTH_CERT_NUMBER = "birth_cert_num";
        public static final String BIRTH_CERT_NOTIFIICATION = "birth_notification";
        public static final String ILLNESS_DATE = "date_of_illness";
        public static final String ILLNESS_DESCRIPTION = "illness_description";
        public static final String ILLNESS_ACTION = "action_taken";
        public static final String ILLNESS_ACTION_BA = "action_taken_1m5yr";
        public static final String OTHER_ACTION = "other_treat_1m5yr";
        public static final String EVENT_DATE = "event_date";
        public static final String EVENT_TYPE = "event_type";
        public static final String INSURANCE_PROVIDER = "insurance_provider";
        public static final String INSURANCE_PROVIDER_NUMBER = "insurance_provider_number";
        public static final String INSURANCE_PROVIDER_OTHER = "insurance_provider_other";
        public static final String TYPE_OF_DISABILITY = "type_of_disability";
        public static final String RHC_CARD = "rhc_card";
        public static final String NUTRITION_STATUS = "nutrition_status";
        public static final String VACCINE_CARD = "vaccine_card";
        public static final String MOTHER_ENTITY_ID = "mother_entity_id";

        public static final String BIRTH_WEIGHT_TAKEN = "birth_weight_taken";
        public static final String BIRTH_WEIGHT = "birth_weight";
        public static final String CHLOROHEXADIN = "chlorohexadin";
        public static final String BREASTFEEDING_TIME = "breastfeeding_time";
        public static final String HEAD_BODY_COVERED = "head_body_covered";
        public static final String PHYSICALLY_CHALLENGED = "physically_challenged";
        public static final String BREAST_FEEDED = "breast_feeded";

        public static final String WHICH_PROBLEM = "which_problem";








        // Family child visit status
        //public static final String CHILD_VISIT_STATUS = "child_visit_status";
    }
}
