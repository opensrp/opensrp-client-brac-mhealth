package org.smartregister.brac.hnpp.repository;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;

import org.smartregister.AllConstants;
import org.smartregister.chw.anc.repository.VisitDetailsRepository;
import org.smartregister.chw.anc.repository.VisitRepository;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.configurableviews.repository.ConfigurableViewsRepository;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineNameRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.repository.VaccineTypeRepository;
import org.smartregister.immunization.util.IMDatabaseUtils;
import org.smartregister.repository.AlertRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.repository.SettingsRepository;
import org.smartregister.repository.UniqueIdRepository;

public class HnppChwRepository extends Repository {
    private Context context;

    public HnppChwRepository(Context context, org.smartregister.Context openSRPContext) {
        super(context, AllConstants.DATABASE_NAME, BuildConfig.DATABASE_VERSION, openSRPContext.session(), CoreChwApplication.createCommonFtsObject(), openSRPContext.sharedRepositoriesArray());
        this.context = context;
    }

    public void deleteDatabase(){
        context.deleteDatabase(AllConstants.DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        super.onCreate(database);
        EventClientRepository.createTable(database, EventClientRepository.Table.client, EventClientRepository.client_column.values());
        EventClientRepository.createTable(database, EventClientRepository.Table.event, EventClientRepository.event_column.values());
        UniqueIdRepository.createTable(database);
        SettingsRepository.onUpgrade(database);
        ConfigurableViewsRepository.createTable(database);
        LocationRepository.createTable(database);
        onCreation(database);
    }

    protected void onCreation(SQLiteDatabase database) {
        SSLocationRepository.createTable(database);
        HouseholdIdRepository.createTable(database);
        VisitRepository.createTable(database);
        VisitDetailsRepository.createTable(database);
        HnppVisitLogRepository.createTable(database);
        VaccineRepository.createTable(database);
        VaccineNameRepository.createTable(database);
        VaccineTypeRepository.createTable(database);
        RecurringServiceTypeRepository.createTable(database);
        RecurringServiceRecordRepository.createTable(database);
        RecurringServiceTypeRepository recurringServiceTypeRepository = ImmunizationLibrary.getInstance().recurringServiceTypeRepository();
        IMDatabaseUtils.populateRecurringServices(context, database, recurringServiceTypeRepository);
        upgradeToVersion18(context,database);
        upgradeToVersion19(context,database);
        upgradeToVersion20(context,database);
        upgradeToVersion21(context,database);
        upgradeToVersion22(context,database);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v("DB_UPGRADE",
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            Log.v("DB_UPGRADE","upgradeTo:"+upgradeTo);
            switch (upgradeTo) {

                case 9:
                    upgradeToVersion9(context, db);
                    break;
                case 10:
                    upgradeToVersion10(context,db);
                    break;
                case 11:
                    upgradeToVersion11(context, db);
                    break;
                case 12:
                    upgradeToVersion12(context, db);
                    break;
                case 13:
                    upgradeToVersion13(context, db);
                    break;
                case 16:
                    upgradeToVersion16(context, db);
                    break;
                case 17:
                    upgradeToVersion17(context, db);
                    break;
                case 18:
                    upgradeToVersion18(context, db);
                    break;
                case 19:
                    upgradeToVersion19(context, db);
                    break;
                case 20:
                    upgradeToVersion20(context,db);
                    break;
                case 21:
                    upgradeToVersion21(context,db);
                    break;
                case 22:
                    upgradeToVersion22(context,db);
                    break;
                case 23:
                    upgradeToVersion23(context,db);
                    break;
                case 24:
                    upgradeToVersion24(context,db);
                    break;
                default:
                    break;
            }
            upgradeTo++;
        }
    }
    private void upgradeToVersion20(Context context, SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ec_child ADD COLUMN birth_id VARCHAR;");

        } catch (Exception e) {

        }
    }
    private void upgradeToVersion21(Context context, SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ec_visit_log ADD COLUMN pregnant_status VARCHAR;");

        } catch (Exception e) {

        }
    }
    private void upgradeToVersion22(Context context, SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ec_family_member ADD COLUMN is_risk VARCHAR;");
            db.execSQL("ALTER TABLE ec_family_member ADD COLUMN is_corona VARCHAR;");

        } catch (Exception e) {

        }
    }
    private void upgradeToVersion23(Context context, SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ec_family ADD COLUMN homestead_land VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN cultivable_land VARCHAR;");

        } catch (Exception e) {

        }
    }
    private void upgradeToVersion24(Context context, SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ec_child ADD COLUMN which_problem VARCHAR;");

        } catch (Exception e) {

        }
    }
    private void upgradeToVersion19(Context context, SQLiteDatabase db) {
        DistrictListRepository.createTable(db);

    }


    private void upgradeToVersion18(Context context, SQLiteDatabase db) {
        db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_EVENT_ID_COL);
        db.execSQL(VaccineRepository.EVENT_ID_INDEX);
        db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL);
        db.execSQL(VaccineRepository.FORMSUBMISSION_INDEX);
        db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL);
        db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);
        db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_HIA2_STATUS_COL);
        IMDatabaseUtils.accessAssetsAndFillDataBaseForVaccineTypes(context, db);
        db.execSQL(VaccineRepository.ALTER_ADD_CREATED_AT_COLUMN);
        VaccineRepository.migrateCreatedAt(db);
        db.execSQL(RecurringServiceRecordRepository.ALTER_ADD_CREATED_AT_COLUMN);
        RecurringServiceRecordRepository.migrateCreatedAt(db);
        db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_TEAM_COL);
        db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
        db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_TEAM_COL);
        db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
        db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
        db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
        db.execSQL(AlertRepository.ALTER_ADD_OFFLINE_COLUMN);
        db.execSQL(AlertRepository.OFFLINE_INDEX);
        db.execSQL("ALTER TABLE ec_visit_log ADD COLUMN refer_place VARCHAR;");
        db.execSQL("ALTER TABLE ec_visit_log ADD COLUMN refer_reason VARCHAR;");


    }


    private void upgradeToVersion9(Context context, SQLiteDatabase db) {
        try {
            VaccineRepository.createTable(db);
            VaccineNameRepository.createTable(db);
            VaccineTypeRepository.createTable(db);
            RecurringServiceTypeRepository.createTable(db);
            RecurringServiceRecordRepository.createTable(db);
            RecurringServiceTypeRepository recurringServiceTypeRepository = ImmunizationLibrary.getInstance().recurringServiceTypeRepository();
            IMDatabaseUtils.populateRecurringServices(context, db, recurringServiceTypeRepository);

            db.execSQL("ALTER TABLE ec_child ADD COLUMN birth_weight_taken VARCHAR;");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN birth_weight VARCHAR;");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN chlorohexadin VARCHAR;");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN breastfeeding_time VARCHAR;");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN head_body_covered VARCHAR;");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN breast_feeded VARCHAR;");
            db.execSQL("ALTER TABLE ec_pregnancy_outcome ADD COLUMN delivery_time VARCHAR;");
            db.execSQL("ALTER TABLE ec_pregnancy_outcome ADD COLUMN no_born_alive VARCHAR;");
            db.execSQL("ALTER TABLE ec_pregnancy_outcome ADD COLUMN other_delivery_place VARCHAR;");
            db.execSQL("ALTER TABLE ec_pregnancy_outcome ADD COLUMN delivery_assistance VARCHAR;");
            db.execSQL("ALTER TABLE ec_pregnancy_outcome ADD COLUMN other_delivery_assistance VARCHAR;");
            db.execSQL("ALTER TABLE ec_pregnancy_outcome ADD COLUMN delivery_method VARCHAR;");
            db.execSQL("ALTER TABLE ec_pregnancy_outcome ADD COLUMN delivery_complication VARCHAR;");
            db.execSQL("ALTER TABLE ec_pregnancy_outcome ADD COLUMN complications VARCHAR;");
            db.execSQL("ALTER TABLE ec_pregnancy_outcome ADD COLUMN other_complications VARCHAR;");
            db.execSQL("ALTER TABLE ec_pregnancy_outcome ADD COLUMN no_still_born VARCHAR;");
            db.execSQL("ALTER TABLE ec_pregnancy_outcome ADD COLUMN no_born_alive_died VARCHAR;");
            db.execSQL("ALTER TABLE ec_pregnancy_outcome ADD COLUMN no_miscarriage VARCHAR;");
            db.execSQL("ALTER TABLE ec_pregnancy_outcome ADD COLUMN type_miscarriage VARCHAR;");
            db.execSQL("ALTER TABLE ec_pregnancy_outcome ADD COLUMN cause_born_alive_died VARCHAR;");

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
    private void upgradeToVersion10(Context context, SQLiteDatabase db) {

        try{
            db.execSQL("ALTER TABLE ec_family ADD COLUMN occupation VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN occupation_other VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN financial_status VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN list_of_assets VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN floor_material VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN wall_material VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN roof_material VARCHAR;");
        }catch (Exception e){
            e.printStackTrace();

        }


    }
    private void upgradeToVersion11(Context context, SQLiteDatabase db) {

        try{
            db.execSQL("CREATE TABLE ec_visit_log (visit_id VARCHAR,visit_type VARCHAR,base_entity_id VARCHAR NOT NULL,visit_date VARCHAR,event_type VARCHAR,visit_json TEXT)");
        }catch (SQLiteException e){

            e.printStackTrace();
        }

    }
    private void upgradeToVersion13(Context context, SQLiteDatabase db) {

        try{
            db.execSQL("ALTER TABLE ec_family ADD COLUMN last_home_visit VARCHAR;");
        }catch (SQLiteException e){
            e.printStackTrace();
        }

    }
    private void upgradeToVersion12(Context context, SQLiteDatabase db) {

        try{
            db.execSQL("ALTER TABLE ec_anc_register ADD COLUMN height VARCHAR;");
        }catch (SQLiteException e){
            e.printStackTrace();
        }

    }
    private void upgradeToVersion16(Context context, SQLiteDatabase db) {

        try{
            db.execSQL("ALTER TABLE ec_family ADD COLUMN date_created VARCHAR;");
        }catch (SQLiteException e){
            e.printStackTrace();
        }

    }
    private void upgradeToVersion17(Context context, SQLiteDatabase db) {

        try{
            db.execSQL("ALTER TABLE ec_visit_log ADD COLUMN family_id VARCHAR;");
        }catch (SQLiteException e){
            e.printStackTrace();
        }

    }
}
