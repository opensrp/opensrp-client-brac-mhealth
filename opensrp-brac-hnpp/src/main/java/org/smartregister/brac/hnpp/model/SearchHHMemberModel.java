package org.smartregister.brac.hnpp.model;

import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import org.apache.commons.lang3.text.WordUtils;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.family.util.Utils;

import java.util.ArrayList;

public class SearchHHMemberModel {

    ArrayList<HHMemberProperty> hhList = new ArrayList<>();
    ArrayList<HHMemberProperty> adoArrayList = new ArrayList<>();
    ArrayList<HHMemberProperty> womenArrayList = new ArrayList<>();
    ArrayList<HHMemberProperty> childArrayList = new ArrayList<>();
    ArrayList<HHMemberProperty> ncdArrayList = new ArrayList<>();

    public ArrayList<HHMemberProperty> getHhList() {
        return hhList;
    }

    public ArrayList<HHMemberProperty> getAdoArrayList() {
        return adoArrayList;
    }

    public ArrayList<HHMemberProperty> getWomenArrayList() {
        return womenArrayList;
    }

    public ArrayList<HHMemberProperty> getChildArrayList() {
        return childArrayList;
    }

    public ArrayList<HHMemberProperty> getNcdArrayList() {
        return ncdArrayList;
    }

    public ArrayList<HHMemberProperty> searchHH(String name){
        if(TextUtils.isEmpty(name)) return hhList;
        ArrayList<HHMemberProperty> tempList = new ArrayList<>();
        for(HHMemberProperty hhMemberProperty : hhList){
            if(hhMemberProperty.name.contains(name)){
                tempList.add(hhMemberProperty);
            }
        }
        return tempList;
    }
    public ArrayList<HHMemberProperty> searchAdo(String name){
        if(TextUtils.isEmpty(name)) return adoArrayList;
        ArrayList<HHMemberProperty> tempList = new ArrayList<>();
        for(HHMemberProperty hhMemberProperty : adoArrayList){
            if(hhMemberProperty.name.contains(name)){
                tempList.add(hhMemberProperty);
            }
        }
        return tempList;
    }
    public ArrayList<HHMemberProperty> searchWomen(String name){
        if(TextUtils.isEmpty(name)) return womenArrayList;
        ArrayList<HHMemberProperty> tempList = new ArrayList<>();
        for(HHMemberProperty hhMemberProperty : womenArrayList){
            if(hhMemberProperty.name.contains(name)){
                tempList.add(hhMemberProperty);
            }
        }
        return tempList;
    }
    public ArrayList<HHMemberProperty> searchChild(String name){
        if(TextUtils.isEmpty(name)) return childArrayList;
        ArrayList<HHMemberProperty> tempList = new ArrayList<>();
        for(HHMemberProperty hhMemberProperty : childArrayList){
            if(hhMemberProperty.name.contains(name)){
                tempList.add(hhMemberProperty);
            }
        }
        return tempList;
    }
    public ArrayList<HHMemberProperty> searchNcd(String name){
        if(TextUtils.isEmpty(name)) return ncdArrayList;
        ArrayList<HHMemberProperty> tempList = new ArrayList<>();
        for(HHMemberProperty hhMemberProperty : ncdArrayList){
            if(hhMemberProperty.name.contains(name)){
                tempList.add(hhMemberProperty);
            }
        }
        return tempList;
    }

    public ArrayList<HHMemberProperty> fetchHH(String village, String claster){
        String query = "Select first_name,base_entity_id,unique_id from ec_family where village_town = '"+village+"' and date_removed is null";
        Cursor cursor = null;
        hhList.clear();
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                HHMemberProperty hhMemberProperty = new HHMemberProperty();
                hhMemberProperty.setName(cursor.getString(cursor.getColumnIndex("first_name")));
                String id = cursor.getString(cursor.getColumnIndex("unique_id"));
                hhMemberProperty.setId(id.length() > HnppConstants.HOUSE_HOLD_ID_SUFFIX?id.substring(id.length() - HnppConstants.HOUSE_HOLD_ID_SUFFIX):id);
                hhMemberProperty.setBaseEntityId(cursor.getString(cursor.getColumnIndex("base_entity_id")));
                hhList.add(hhMemberProperty);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return hhList;
    }
    public ArrayList<HHMemberProperty> fetchAdo(String village, String claster){
        String query = "Select ec_family_member.first_name,ec_family_member.base_entity_id,ec_family_member.unique_id,ec_family_member.dob FROM ec_family_member LEFT JOIN ec_family ON  ec_family_member.relational_id = ec_family.id COLLATE NOCASE  WHERE  ec_family_member.date_removed is null AND  ((( julianday('now') - julianday(dob))/365) >=11) AND  ((( julianday('now') - julianday(dob))/365) <20)" +
                " AND gender = 'F' and ec_family.village_town = '"+village+"'";
        Cursor cursor = null;
        adoArrayList.clear();
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                HHMemberProperty hhMemberProperty = new HHMemberProperty();
                hhMemberProperty.setName(cursor.getString(cursor.getColumnIndex("first_name")));
                String id = cursor.getString(cursor.getColumnIndex("unique_id"));
                hhMemberProperty.setId(id.length() > HnppConstants.MEMBER_ID_SUFFIX?id.substring(id.length() - HnppConstants.MEMBER_ID_SUFFIX):id);
                hhMemberProperty.setBaseEntityId(cursor.getString(cursor.getColumnIndex("base_entity_id")));
                String dobString = Utils.getDuration(cursor.getString(cursor.getColumnIndex("dob")));

                hhMemberProperty.setAge(dobString);
                adoArrayList.add(hhMemberProperty);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return adoArrayList;
    }
    public ArrayList<HHMemberProperty> fetchWomen(String village, String claster){
        String query = "Select ec_family_member.first_name,ec_family_member.base_entity_id,ec_family_member.unique_id,ec_family_member.dob FROM ec_family_member LEFT JOIN ec_family ON  ec_family_member.relational_id = ec_family.id COLLATE NOCASE  WHERE  ec_family_member.date_removed is null AND  ((( julianday('now') - julianday(dob))/365) >14)" +
                " AND gender = 'F' and ec_family.village_town = '"+village+"' ";
        Cursor cursor = null;
        womenArrayList.clear();
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                HHMemberProperty hhMemberProperty = new HHMemberProperty();
                hhMemberProperty.setName(cursor.getString(cursor.getColumnIndex("first_name")));
                String id = cursor.getString(cursor.getColumnIndex("unique_id"));
                hhMemberProperty.setId(id.length() > HnppConstants.MEMBER_ID_SUFFIX?id.substring(id.length() - HnppConstants.MEMBER_ID_SUFFIX):id);
                hhMemberProperty.setBaseEntityId(cursor.getString(cursor.getColumnIndex("base_entity_id")));
                String dobString = Utils.getDuration(cursor.getString(cursor.getColumnIndex("dob")));
                hhMemberProperty.setAge(dobString);
                womenArrayList.add(hhMemberProperty);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return womenArrayList;
    }
    public ArrayList<HHMemberProperty> fetchChild(String village, String claster){
        String query = "Select ec_child.first_name,ec_child.base_entity_id,ec_child.unique_id,ec_child.dob FROM ec_child LEFT JOIN ec_family ON  ec_child.relational_id = ec_family.id COLLATE NOCASE  WHERE  ec_child.date_removed is null AND  (( julianday('now') - julianday(dob)) >=183) AND  (( julianday('now') - julianday(dob)) <= 1830)" +
                " AND ec_family.village_town = '"+village+"'";
        Log.v("CHILD_QUERY","query:"+query);

        Cursor cursor = null;
        childArrayList.clear();
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                HHMemberProperty hhMemberProperty = new HHMemberProperty();
                hhMemberProperty.setName(cursor.getString(cursor.getColumnIndex("first_name")));
                String id = cursor.getString(cursor.getColumnIndex("unique_id"));
                hhMemberProperty.setId(id.length() > HnppConstants.MEMBER_ID_SUFFIX?id.substring(id.length() - HnppConstants.MEMBER_ID_SUFFIX):id);
                hhMemberProperty.setBaseEntityId(cursor.getString(cursor.getColumnIndex("base_entity_id")));
                String dobString = Utils.getDuration(cursor.getString(cursor.getColumnIndex("dob")));
                hhMemberProperty.setAge(dobString);
                childArrayList.add(hhMemberProperty);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return childArrayList;
    }
    public ArrayList<HHMemberProperty> fetchNcd(String village, String claster){
        String query = "Select ec_family_member.first_name,ec_family_member.base_entity_id,ec_family_member.unique_id,ec_family_member.dob FROM ec_family_member LEFT JOIN ec_family ON  ec_family_member.relational_id = ec_family.id COLLATE NOCASE  WHERE  ec_family_member.date_removed is null AND  ((( julianday('now') - julianday(dob))/365) >=18)" +
                " and ec_family.village_town = '"+village+"'";
        Cursor cursor = null;
        ncdArrayList.clear();
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                HHMemberProperty hhMemberProperty = new HHMemberProperty();
                hhMemberProperty.setName(cursor.getString(cursor.getColumnIndex("first_name")));
                String id = cursor.getString(cursor.getColumnIndex("unique_id"));
                hhMemberProperty.setId(id.length() > HnppConstants.MEMBER_ID_SUFFIX?id.substring(id.length() - HnppConstants.MEMBER_ID_SUFFIX):id);
                hhMemberProperty.setBaseEntityId(cursor.getString(cursor.getColumnIndex("base_entity_id")));
                String dobString = Utils.getDuration(cursor.getString(cursor.getColumnIndex("dob")));
                hhMemberProperty.setAge(dobString);
                ncdArrayList.add(hhMemberProperty);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return ncdArrayList;
    }

}
