package org.smartregister.brac.hnpp.repository;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.AllConstants;
import org.smartregister.chw.anc.repository.VisitDetailsRepository;
import org.smartregister.chw.anc.repository.VisitRepository;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.repository.CoreChwRepository;
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
import org.smartregister.repository.PlanDefinitionRepository;
import org.smartregister.repository.PlanDefinitionSearchRepository;
import org.smartregister.repository.Repository;
import org.smartregister.repository.SettingsRepository;
import org.smartregister.repository.UniqueIdRepository;
import org.smartregister.view.activity.DrishtiApplication;

import timber.log.Timber;

public class HnppChwRepository extends CoreChwRepository {
    protected SQLiteDatabase readableDatabase;
    protected SQLiteDatabase writableDatabase;
    private Context context;

    public HnppChwRepository(Context context,org.smartregister.Context openSRPContext) {
        super(context,
                AllConstants.DATABASE_NAME,
                BuildConfig.DATABASE_VERSION,
                openSRPContext.session(),
                openSRPContext.commonFtsObject(),
                openSRPContext.sharedRepositoriesArray());
        this.context = context;
    }

    public void deleteDatabase(){
        DrishtiApplication.getInstance().deleteDatabase(AllConstants.DATABASE_NAME);
    }

    @Override
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

        upgradeToVersion18(database);
        upgradeToVersion19(database);
        upgradeToVersion20(database);
        upgradeToVersion21(database);
        upgradeToVersion22(database);
        upgradeToVersion25(database);
        upgradeToVersion26(database);
        upgradeToVersion27(database);
        upgradeToVersion28(database);
        upgradeToVersion29(database);
        upgradeToVersion30(database);
        upgradeToVersion31(database);
        upgradeToVersion32(database);
        upgradeToVersion33(database);
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
                    upgradeToVersion9( db);
                    break;
                case 10:
                    upgradeToVersion10(db);
                    break;
                case 11:
                    upgradeToVersion11( db);
                    break;
                case 12:
                    upgradeToVersion12( db);
                    break;
                case 13:
                    upgradeToVersion13( db);
                    break;
                case 16:
                    upgradeToVersion16( db);
                    break;
                case 17:
                    upgradeToVersion17( db);
                    break;
                case 18:
                    upgradeToVersion18( db);
                    break;
                case 19:
                    upgradeToVersion19( db);
                    break;
                case 20:
                    upgradeToVersion20(db);
                    break;
                case 21:
                    upgradeToVersion21(db);
                    break;
                case 22:
                    upgradeToVersion22(db);
                    break;
                case 23:
                    upgradeToVersion23(db);
                    break;
                case 24:
                    upgradeToVersion24(db);
                    break;
                case 25:
                    upgradeToVersion25(db);
                    break;
                case 26:
                    upgradeToVersion26(db);
                    break;
                case 27:
                    upgradeToVersion27(db);
                    break;
                case 28:
                    upgradeToVersion28(db);
                    break;
                case 29:
                    upgradeToVersion29(db);
                    break;
                case 30:
                    upgradeToVersion30(db);
                    break;
                case 31:
                    upgradeToVersion31(db);
                    break;
                case 32:
                    upgradeToVersion32(db);
                    break;
                case 33:
                    upgradeToVersion33(db);
                    break;
                default:
                    break;
            }
            upgradeTo++;
        }
    }
    @Override
    public synchronized SQLiteDatabase getReadableDatabase(String password) {
        if (StringUtils.isBlank(password)) {
            throw new IllegalStateException("Password is blank");
        }
        try {
            if (readableDatabase == null || !readableDatabase.isOpen()) {
                readableDatabase = super.getReadableDatabase(password);
            }
            return readableDatabase;
        } catch (Exception e) {
            Timber.e(e, "Database Error. ");
            return null;
        }

    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase(String password) {
        if (StringUtils.isBlank(password)) {
            throw new IllegalStateException("Password is blank");
        } else if (writableDatabase == null || !writableDatabase.isOpen()) {
            writableDatabase = super.getWritableDatabase(password);
        }
        return writableDatabase;
    }

    @Override
    public synchronized void close() {
        if (readableDatabase != null) {
            readableDatabase.close();
        }

        if (writableDatabase != null) {
            writableDatabase.close();
        }
        super.close();
    }
    private void upgradeToVersion20(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ec_child ADD COLUMN birth_id VARCHAR;");

        } catch (Exception e) {

        }
    }
    private void upgradeToVersion21(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ec_visit_log ADD COLUMN pregnant_status VARCHAR;");

        } catch (Exception e) {

        }
    }
    private void upgradeToVersion22(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ec_family_member ADD COLUMN is_risk VARCHAR;");
            db.execSQL("ALTER TABLE ec_family_member ADD COLUMN is_corona VARCHAR;");

        } catch (Exception e) {

        }
    }
    private void upgradeToVersion23(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ec_family ADD COLUMN homestead_land VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN cultivable_land VARCHAR;");

        } catch (Exception e) {

        }
    }
    private void upgradeToVersion24(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE ec_guest_member (id VARCHAR,_id VARCHAR,base_entity_id VARCHAR,ss_name VARCHAR,village_name VARCHAR,village_id VARCHAR,unique_id VARCHAR,first_name VARCHAR,father_name VARCHAR,phone_number VARCHAR," +
                    "is_birthday_known VARCHAR,dob VARCHAR,estimated_age VARCHAR,gender VARCHAR,dod VARCHAR,entity_type VARCHAR,date_removed VARCHAR,last_interacted_with LONG,is_closed VARCHAR" +
                    ",details VARCHAR,relationalid VARCHAR)");
        } catch (Exception e) {

        }
    }
    private void upgradeToVersion19(SQLiteDatabase db) {
        DistrictListRepository.createTable(db);

    }
    private void upgradeToVersion25(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE ec_family_member ADD COLUMN risk_event_type VARCHAR;");
        db.execSQL("ALTER TABLE ec_child ADD COLUMN is_risk VARCHAR;");
        RiskDetailsRepository.createTable(db);

    }
    private void upgradeToVersion26(SQLiteDatabase db) {
        TargetVsAchievementRepository.createTable(db);

    }
    private void upgradeToVersion27(SQLiteDatabase db) {
        NotificationRepository.createTable(db);

    }
    private void upgradeToVersion28(SQLiteDatabase db) {
        StockRepository.createTable(db);
        GuestMemberIdRepository.createTable(db);
        try {
            db.execSQL("ALTER TABLE ec_visit_log ADD COLUMN ss_name VARCHAR;");

        } catch (Exception e) {

        }

    }
    private void upgradeToVersion29(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ss_location ADD COLUMN sk_name VARCHAR;");
            db.execSQL("ALTER TABLE ss_location ADD COLUMN sk_user_name VARCHAR;");
            db.execSQL("ALTER TABLE ss_location ADD COLUMN is_selected VARCHAR;");
            db.execSQL("ALTER TABLE ss_location ADD COLUMN ss_id VARCHAR;");

        } catch (Exception e) {

        }

    }
    private void upgradeToVersion30(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ec_family ADD COLUMN provider_id VARCHAR;");

        } catch (Exception e) {

        }

    }
    private void upgradeToVersion31(SQLiteDatabase db) {
        IndicatorRepository.createTable(db);
        try{
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN _id VARCHAR;");
        }catch (Exception e){
            e.printStackTrace();

        }


    }
    private void upgradeToVersion32(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ec_family_member ADD COLUMN difficulty_seeing_hearing VARCHAR;");
            db.execSQL("ALTER TABLE ec_family_member ADD COLUMN difficulty_walking_up_down VARCHAR;");
            db.execSQL("ALTER TABLE ec_family_member ADD COLUMN trouble_remembering_concentrating VARCHAR;");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN trouble_seeing_hearing VARCHAR;");

        } catch (Exception e) {

        }


    }
    private void upgradeToVersion33(SQLiteDatabase db) {
        try {
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_IS_VOIDED_COL);
            db.execSQL("ALTER TABLE visits ADD COLUMN visit_group VARCHAR;");
            PlanDefinitionRepository.createTable(db);
            PlanDefinitionSearchRepository.createTable(db);

        } catch (Exception e) {

        }


    }

    private void upgradeToVersion18( SQLiteDatabase db) {
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


    private void upgradeToVersion9(SQLiteDatabase db) {
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
    private void upgradeToVersion10(SQLiteDatabase db) {

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
    private void upgradeToVersion11(SQLiteDatabase db) {

        try{
            db.execSQL("CREATE TABLE ec_visit_log (visit_id VARCHAR,visit_type VARCHAR,base_entity_id VARCHAR NOT NULL,visit_date VARCHAR,event_type VARCHAR,visit_json TEXT)");
        }catch (SQLiteException e){

            e.printStackTrace();
        }

    }
    private void upgradeToVersion13(SQLiteDatabase db) {

        try{
            db.execSQL("ALTER TABLE ec_family ADD COLUMN last_home_visit VARCHAR;");
        }catch (SQLiteException e){
            e.printStackTrace();
        }

    }
    private void upgradeToVersion12(SQLiteDatabase db) {

        try{
            db.execSQL("ALTER TABLE ec_anc_register ADD COLUMN height VARCHAR;");
        }catch (SQLiteException e){
            e.printStackTrace();
        }

    }
    private void upgradeToVersion16(SQLiteDatabase db) {

        try{
            db.execSQL("ALTER TABLE ec_family ADD COLUMN date_created VARCHAR;");
        }catch (SQLiteException e){
            e.printStackTrace();
        }

    }
    private void upgradeToVersion17(SQLiteDatabase db) {

        try{
            db.execSQL("ALTER TABLE ec_visit_log ADD COLUMN family_id VARCHAR;");
        }catch (SQLiteException e){
            e.printStackTrace();
        }

    }
}
