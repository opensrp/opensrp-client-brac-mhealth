package org.smartregister.brac.hnpp.repository;

import android.util.Log;

import net.sqlcipher.Cursor;

import org.smartregister.brac.hnpp.model.HHVisitDurationModel;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.model.Survey;
import org.smartregister.brac.hnpp.utils.MemberTypeEnum;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;

import java.util.ArrayList;

public class MemberListRepository extends BaseRepository {
    public MemberListRepository(Repository repository) {
        super(repository);
    }

    public ArrayList<Member> getMemberList(String familyId, MemberTypeEnum memberTypeEnum) {
        Cursor cursor = null;
        ArrayList<Member> memberArrayList = new ArrayList<>();
        try {
            String query = "";
            if(memberTypeEnum == MemberTypeEnum.DEATH || memberTypeEnum == MemberTypeEnum.MIGRATION){
                query = "SELECT ec_family_member.*,ec_family.first_name as house_hold_name FROM ec_family_member LEFT JOIN ec_family ON  ec_family_member.relational_id = ec_family.id COLLATE NOCASE where ec_family_member.relational_id = '"+familyId+"' and ec_family_member.date_removed is null";
            }else if(memberTypeEnum == MemberTypeEnum.ELCO){
                query = "Select ec_family_member.id as _id , ec_family_member.first_name , ec_family_member.last_name ," +
                        " ec_family_member.middle_name , ec_family_member.phone_number , ec_family_member.base_entity_id , ec_family_member.estimated_age," +
                        " ec_family_member.relational_id as relational_id , ec_family_member.entity_type , ec_family.village_town as village_name ," +
                        " ec_family_member.unique_id , ec_family_member.gender , ec_family_member.dob , ec_family.unique_id as house_hold_id ," +
                        " ec_family.first_name as house_hold_name , ec_family.module_id , ec_family.last_home_visit , ec_family.ss_name ," +
                        " ec_family.serial_no FROM ec_family_member LEFT JOIN ec_family ON  ec_family_member.relational_id = ec_family.id COLLATE NOCASE" +
                        "  WHERE ec_family_member.relational_id = '"+familyId+"' AND ec_family_member.date_removed is null AND  " +
                        "((( julianday('now') - julianday(dob))/365) >13) AND  " +
                        "((( julianday('now') - julianday(dob))/365) <50) AND  " +
                        "marital_status = 'Married' and gender = 'F' AND " +
                        "ec_family_member.base_entity_id  NOT IN  " +
                        "(select ec_anc_register.base_entity_id from ec_anc_register where ec_anc_register.is_closed = '0'" +
                        " group by ec_anc_register.base_entity_id)  AND" +
                        " ec_family_member.base_entity_id  NOT IN" +
                        " (select ec_pregnancy_outcome.base_entity_id from ec_pregnancy_outcome where ec_pregnancy_outcome.is_closed = '0' " +
                        "group by ec_pregnancy_outcome.base_entity_id)   ORDER BY ec_family_member.last_interacted_with DESC";
            }
            cursor = getReadableDatabase().rawQuery(query, null);

            while (cursor.moveToNext()) {
                memberArrayList.add(readCursor(cursor));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return memberArrayList;
    }

    protected Member readCursor(Cursor cursor) {
        String memberName = cursor.getString(cursor.getColumnIndex("first_name"));
        String gender = cursor.getString(cursor.getColumnIndex("gender"));
        String dob = cursor.getString(cursor.getColumnIndex("dob"));
        String age = cursor.getString(cursor.getColumnIndex("estimated_age"));
        String baseEntityId = cursor.getString(cursor.getColumnIndex("base_entity_id"));
        String familyBaseEntityId = cursor.getString(cursor.getColumnIndex("relational_id"));
        String familyName = cursor.getString(cursor.getColumnIndex("house_hold_name"));
        String mobileNo = cursor.getString(cursor.getColumnIndex("phone_number"));


        return new Member(
                memberName,
                gender,
                age,
                dob,
                baseEntityId,
                familyBaseEntityId,
                "",
                "",
                mobileNo,
                familyName,
                ""
        );
    }
}
