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
//        HnppVisitLogRepository.createTable(database);
//        VaccineRepository.createTable(database);
//        VaccineNameRepository.createTable(database);
//        VaccineTypeRepository.createTable(database);
//        RecurringServiceTypeRepository.createTable(database);
//        RecurringServiceRecordRepository.createTable(database);
//        RecurringServiceTypeRepository recurringServiceTypeRepository = ImmunizationLibrary.getInstance().recurringServiceTypeRepository();
//        IMDatabaseUtils.populateRecurringServices(context, database, recurringServiceTypeRepository);
//
        upgradeToVersion11(context,database);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v("DB_UPGRADE",
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 12:
                    //try{
                        HnppVisitLogRepository.createTable(db);
                        VaccineRepository.createTable(db);
                        VaccineNameRepository.createTable(db);
                        RecurringServiceTypeRepository.createTable(db);
                        RecurringServiceTypeRepository recurringServiceTypeRepository = ImmunizationLibrary.getInstance().recurringServiceTypeRepository();
                        IMDatabaseUtils.populateRecurringServices(context, db, recurringServiceTypeRepository);

//                    }catch (Exception e){
//                        Log.v("DB_UPGRADE","exception_1");
//                        e.printStackTrace();
//
//                    }
                    upgradeToVersion11(context, db);
                    break;
                default:
                    break;
            }
            upgradeTo++;
        }
    }

    private void upgradeToVersion11(Context context, SQLiteDatabase db) {
        //try {
            db.execSQL("ALTER TABLE ec_child ADD COLUMN birth_weight_taken VARCHAR;");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN birth_weight VARCHAR;");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN chlorohexadin VARCHAR;");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN breastfeeding_time VARCHAR;");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN head_body_covered VARCHAR;");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN breast_feeded VARCHAR;");
            db.execSQL("ALTER TABLE ec_child ADD COLUMN birth_id VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN serial_no VARCHAR;");
            db.execSQL("ALTER TABLE ec_family_member ADD COLUMN mother_name VARCHAR;");
            db.execSQL("ALTER TABLE ec_family_member ADD COLUMN mother_entity_id VARCHAR;");
            db.execSQL("ALTER TABLE ec_family_member ADD COLUMN is_risk VARCHAR;");
            db.execSQL("ALTER TABLE ec_family_member ADD COLUMN is_corona VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN occupation VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN occupation_other VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN financial_status VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN list_of_assets VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN floor_material VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN wall_material VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN roof_material VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN last_home_visit VARCHAR;");
            db.execSQL("ALTER TABLE ec_anc_register ADD COLUMN height VARCHAR;");
            db.execSQL("ALTER TABLE ec_family ADD COLUMN date_created VARCHAR;");
            db.execSQL("CREATE TABLE ec_visit_log (visit_id VARCHAR,visit_type VARCHAR,base_entity_id VARCHAR NOT NULL,visit_date VARCHAR,event_type VARCHAR,visit_json TEXT)");
            db.execSQL("ALTER TABLE ec_visit_log ADD COLUMN family_id VARCHAR;");
            db.execSQL("ALTER TABLE ec_visit_log ADD COLUMN refer_place VARCHAR;");
            db.execSQL("ALTER TABLE ec_visit_log ADD COLUMN refer_reason VARCHAR;");
            db.execSQL("ALTER TABLE ec_visit_log ADD COLUMN pregnant_status VARCHAR;");
           // try{
                db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_EVENT_ID_COL);
                db.execSQL(VaccineRepository.EVENT_ID_INDEX);
                db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL);
                db.execSQL(VaccineRepository.FORMSUBMISSION_INDEX);
                db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL);
                db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);
                db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_HIA2_STATUS_COL);
                db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_TEAM_COL);
                db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
                db.execSQL(VaccineRepository.ALTER_ADD_CREATED_AT_COLUMN);
                db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
                IMDatabaseUtils.accessAssetsAndFillDataBaseForVaccineTypes(context, db);

                VaccineRepository.migrateCreatedAt(db);
                db.execSQL(RecurringServiceRecordRepository.ALTER_ADD_CREATED_AT_COLUMN);
                db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_TEAM_COL);
                db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);

                db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
                RecurringServiceRecordRepository.migrateCreatedAt(db);


                db.execSQL(AlertRepository.ALTER_ADD_OFFLINE_COLUMN);
                db.execSQL(AlertRepository.OFFLINE_INDEX);
//            }catch (Exception e){
//                Log.v("DB_UPGRADE","exception_2");
//                e.printStackTrace();
//            }
//        } catch (Exception e) {
//            Log.v("DB_UPGRADE","exception_3");
//                e.printStackTrace();
//        }
    }
}
