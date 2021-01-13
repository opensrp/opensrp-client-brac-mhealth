package org.smartregister.brac.hnpp.repository;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;

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
import org.smartregister.repository.SettingsRepository;
import org.smartregister.repository.UniqueIdRepository;

import timber.log.Timber;

public class HnppChwRepository extends CoreChwRepository {
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
        upgradeToVersion18(context,database);
        upgradeToVersion19(context,database);
        upgradeToVersion20(context,database);
        upgradeToVersion21(context,database);
        upgradeToVersion22(context,database);
        upgradeToVersion24(context,database);
        upgradeToVersion25(context,database);
        upgradeToVersion26(context,database);
        upgradeToVersion27(context,database);
        upgradeToVersion28(context,database);
        upgradeToVersion29(context,database);
        upgradeToVersion30(context,database);
        upgradeToVersion31(context,database);

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
                case 25:
                    upgradeToVersion25(context,db);
                    break;
                case 26:
                    upgradeToVersion26(context,db);
                    break;
                case 27:
                    upgradeToVersion27(context,db);
                    break;
                case 28:
                    upgradeToVersion28(context,db);
                    break;
                case 29:
                    upgradeToVersion29(context,db);
                    break;
                case 30:
                    upgradeToVersion30(context,db);
                    break;
                case 31:
                    upgradeToVersion31(context,db);
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
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN _id VARCHAR;");
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN base_entity_id VARCHAR;");
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN ss_name VARCHAR;");
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN village_name VARCHAR;");
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN village_id VARCHAR;");
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN unique_id VARCHAR;");
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN first_name VARCHAR;");
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN father_name VARCHAR;");
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN phone_number VARCHAR;");
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN is_birthday_known VARCHAR;");
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN dob VARCHAR;");
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN estimated_age VARCHAR;");
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN gender VARCHAR;");
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN dod VARCHAR;");
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN entity_type VARCHAR;");
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN last_interacted_with LONG;");

        } catch (Exception e) {

        }
    }
    private void upgradeToVersion19(Context context, SQLiteDatabase db) {
        DistrictListRepository.createTable(db);

    }
    private void upgradeToVersion25(Context context, SQLiteDatabase db) {
        db.execSQL("ALTER TABLE ec_family_member ADD COLUMN risk_event_type VARCHAR;");
        db.execSQL("ALTER TABLE ec_child ADD COLUMN is_risk VARCHAR;");
        RiskDetailsRepository.createTable(db);

    }
    private void upgradeToVersion26(Context context, SQLiteDatabase db) {
        TargetVsAchievementRepository.createTable(db);

    }
    private void upgradeToVersion27(Context context, SQLiteDatabase db) {
        NotificationRepository.createTable(db);

    }
    private void upgradeToVersion28(Context context, SQLiteDatabase db) {
        StockRepository.createTable(db);
        GuestMemberIdRepository.createTable(db);
        try {
            db.execSQL("ALTER TABLE ec_visit_log ADD COLUMN ss_name VARCHAR;");

        } catch (Exception e) {

        }

    }
    private void upgradeToVersion29(Context context, SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ss_location ADD COLUMN sk_name VARCHAR;");
            db.execSQL("ALTER TABLE ss_location ADD COLUMN sk_user_name VARCHAR;");
            db.execSQL("ALTER TABLE ss_location ADD COLUMN is_selected VARCHAR;");
            db.execSQL("ALTER TABLE ss_location ADD COLUMN ss_id VARCHAR;");

        } catch (Exception e) {

        }

    }
    private void upgradeToVersion30(Context context, SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ec_family ADD COLUMN provider_id VARCHAR;");

        } catch (Exception e) {

        }

    }
    private void upgradeToVersion31(Context context, SQLiteDatabase db) {
        IndicatorRepository.createTable(db);
        try{
            db.execSQL("ALTER TABLE ec_guest_member ADD COLUMN _id VARCHAR;");
        }catch (Exception e){

        }


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
