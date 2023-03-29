package org.smartregister.unicef.dghs.dao;

import org.smartregister.unicef.dghs.domain.VisitSummary;
import org.smartregister.unicef.dghs.utils.HnppConstants;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.annotations.Nullable;
import timber.log.Timber;

public class VisitDao extends AbstractDao {

    public static String[] getVisitInfo(String baseEntityId,String visitType){
        String lmp = "SELECT count(*) as count, max(visit_date) as visit_date FROM visits where base_entity_id = ? and visit_type = ? ";
        List<Map<String, String>> valus = AbstractDao.readData(lmp, new String[]{baseEntityId,visitType});
        String[] strs = new String[2];
        if(valus.size()>0){
            strs[0] =  valus.get(0).get("count");
            strs[1] =  valus.get(0).get("visit_date");
        }

        return strs;

    }
    //TODO need to support multiple visit type
    public static String[] getVisitInfo(String baseEntityId,String visitType, String visitType2){
        String lmp = "SELECT count(*) as count, max(visit_date) as visit_date FROM visits where base_entity_id = ? and (visit_type = ? or visit_type = ?)";
        List<Map<String, String>> valus = AbstractDao.readData(lmp, new String[]{baseEntityId,visitType,visitType2});
        String[] strs = new String[2];
        if(valus.size()>0){
            strs[0] =  valus.get(0).get("count");
            strs[1] =  valus.get(0).get("visit_date");
        }

        return strs;

    }
    public static String getNoOfBornChild(String baseEntityId){
        String lmp = "SELECT no_born_alive FROM ec_pregnancy_outcome where base_entity_id = ? ";
        List<Map<String, String>> valus = AbstractDao.readData(lmp, new String[]{baseEntityId});
        String value = "";
        if(valus.size()>0){
            value =  valus.get(0).get("no_born_alive");
        }

        return value;

    }

    @Nullable
    public static Map<String, VisitSummary> getVisitSummary(String baseEntityID) {
        String sql = "select base_entity_id , visit_type , max(visit_date) visit_date from visits " +
                " where base_entity_id = '" + baseEntityID + "' COLLATE NOCASE " +
                " group by base_entity_id , visit_type ";

        DataMap<VisitSummary> dataMap = c -> {
            Long visit_date = getCursorLongValue(c, "visit_date");
            return new VisitSummary(
                    getCursorValue(c, "visit_type"),
                    visit_date != null ? new Date(visit_date) : null,
                    getCursorValue(c, "base_entity_id")
            );
        };

        List<VisitSummary> summaries = AbstractDao.readData(sql, dataMap);
        if (summaries == null)
            return null;

        Map<String, VisitSummary> map = new HashMap<>();
        for (VisitSummary summary : summaries) {
            map.put(summary.getVisitType(), summary);
        }

        return map;
    }

    public static Long getChildDateCreated(String baseEntityID) {
        String sql = "select date_created from ec_child where base_entity_id = '" + baseEntityID + "' COLLATE NOCASE ";

        DataMap<String> dataMap = c -> getCursorValue(c, "date_created");
        List<String> values = AbstractDao.readData(sql, dataMap);
        if (values == null || values.size() == 0)
            return null;

        try {
            return getDobDateFormat().parse(values.get(0)).getTime();
        } catch (ParseException e) {
            Timber.e(e);
            return null;
        }
    }

    public static void undoChildVisitNotDone(String baseEntityID) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);

        long date = calendar.getTime().getTime();

        String sql = "delete from visits where base_entity_id = '" + baseEntityID + "' COLLATE NOCASE and visit_type = '" +
                HnppConstants.EventType.CHILD_VISIT_NOT_DONE + "' and visit_date >= " + date + " and created_at >=  " + date + "";
        updateDB(sql);
    }

    public static boolean memberHasBirthCert(String baseEntityID) {
        String sql = "select count(*) certificates " +
                "from visit_details d " +
                "inner join visits v on v.visit_id = d.visit_id COLLATE NOCASE " +
                "where base_entity_id = '" + baseEntityID + "' COLLATE NOCASE and v.processed = 1 " +
                "and (visit_key in ('birth_certificate','birth_cert') and details = 'GIVEN' or human_readable_details = 'Yes')";

        DataMap<String> dataMap = c -> getCursorValue(c, "certificates");
        List<String> values = AbstractDao.readData(sql, dataMap);
        if (values == null || values.size() == 0)
            return false;

        return Integer.valueOf(values.get(0)) > 0;
    }

    public static boolean memberHasVaccineCard(String baseEntityID) {
        String sql = "select count(*) certificates " +
                "from visit_details d " +
                "inner join visits v on v.visit_id = d.visit_id COLLATE NOCASE " +
                "where base_entity_id = '" + baseEntityID + "' COLLATE NOCASE and v.processed = 1 " +
                "and (visit_key in ('vaccine_card') and human_readable_details = 'Yes')";

        DataMap<String> dataMap = c -> getCursorValue(c, "certificates");
        List<String> values = AbstractDao.readData(sql, dataMap);
        if (values == null || values.size() == 0)
            return false;

        return Integer.valueOf(values.get(0)) > 0;
    }
}
