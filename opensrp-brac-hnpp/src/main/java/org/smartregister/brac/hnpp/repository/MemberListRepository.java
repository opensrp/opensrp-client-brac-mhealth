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
            if(memberTypeEnum == MemberTypeEnum.DEATH){
                query = "SELECT * FROM ec_family_member where relational_id = '"+familyId+"' and date_removed is null";
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
        String age = cursor.getString(cursor.getColumnIndex("estimated_age"));
        String baseEntityId = cursor.getString(cursor.getColumnIndex("base_entity_id"));
        String familyBaseEntityId = cursor.getString(cursor.getColumnIndex("relational_id"));


        return new Member(
                memberName,
                gender,
                age,
                baseEntityId,
                familyBaseEntityId,
                "",
                ""
        );
    }
}
