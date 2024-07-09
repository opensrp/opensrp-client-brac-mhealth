package org.smartregister.unicef.mis.repository;

import android.content.Context;
import android.util.Log;
import android.widget.Button;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.growthmonitoring.repository.HeightRepository;
import org.smartregister.growthmonitoring.repository.HeightZScoreRepository;
import org.smartregister.growthmonitoring.repository.MUACRepository;
import org.smartregister.growthmonitoring.repository.WeightForHeightRepository;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.repository.ZScoreRepository;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.chw.anc.repository.VisitDetailsRepository;
import org.smartregister.chw.anc.repository.VisitRepository;

import org.smartregister.unicef.mis.BuildConfig;
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
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.imci.repository.IMCIReportRepository;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppDBUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HnppChwRepository extends Repository {
    private Context context;

    public HnppChwRepository(Context context, org.smartregister.Context openSRPContext) {
        super(context, AllConstants.DATABASE_NAME, BuildConfig.DATABASE_VERSION, openSRPContext.session(), HnppApplication.createCommonFtsObject(), openSRPContext.sharedRepositoriesArray());
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
        HouseholdIdRepository.createTable(database);
        VisitRepository.createTable(database);
        VisitDetailsRepository.createTable(database);
        HnppVisitLogRepository.createTable(database);
        WeightRepository.createTable(database);
        HeightRepository.createTable(database);
        database.execSQL(WeightRepository.EVENT_ID_INDEX);
        database.execSQL(WeightRepository.FORMSUBMISSION_INDEX);
        database.execSQL(WeightRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);
        HeightZScoreRepository.createTable(database);
        ZScoreRepository.createTable(database);
        MUACRepository.createTable(database);
        CampRepository.createTable(database);
        DistrictListRepository.createTable(database);
        TargetVsAchievementRepository.createTable(database);
        NotificationRepository.createTable(database);
        StockRepository.createTable(database);
        GuestMemberIdRepository.createTable(database);
        RiskDetailsRepository.createTable(database);
        IndicatorRepository.createTable(database);
        PaymentHistoryRepository.createTable(database);
        HALocationRepository.createTable(database);
        GlobalLocationRepository.createTable(database);
        VaccineRepository.createTable(database);
        VaccineNameRepository.createTable(database);
        VaccineTypeRepository.createTable(database);
        database.execSQL(VaccineRepository.UPDATE_TABLE_ADD_EVENT_ID_COL);
        database.execSQL(VaccineRepository.EVENT_ID_INDEX);
        database.execSQL(VaccineRepository.UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL);
        database.execSQL(VaccineRepository.FORMSUBMISSION_INDEX);
        database.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL);
        database.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);
        database.execSQL(VaccineRepository.UPDATE_TABLE_ADD_HIA2_STATUS_COL);
        database.execSQL(VaccineRepository.ALTER_ADD_CREATED_AT_COLUMN);
        database.execSQL(VaccineRepository.UPDATE_TABLE_ADD_TEAM_COL);
        database.execSQL(VaccineRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
        database.execSQL(VaccineRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
        IMDatabaseUtils.accessAssetsAndFillDataBaseForVaccineTypes(context, database);
        RecurringServiceTypeRepository.createTable(database);
        RecurringServiceRecordRepository.createTable(database);
        database.execSQL(RecurringServiceRecordRepository.ALTER_ADD_CREATED_AT_COLUMN);
        database.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_TEAM_COL);
        database.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
        database.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
        RecurringServiceTypeRepository recurringServiceTypeRepository = ImmunizationLibrary.getInstance().recurringServiceTypeRepository();
        IMDatabaseUtils.populateRecurringServices(context, database, recurringServiceTypeRepository);
        database.execSQL(AlertRepository.ALTER_ADD_OFFLINE_COLUMN);
        database.execSQL(AlertRepository.OFFLINE_INDEX);
        OtherVaccineRepository.createTable(database);
        ReferralRepository.createTable(database);
        OutreachRepository.createTable(database);
        MicroPlanRepository.createTable(database);
        WeightForHeightRepository.createTable(database);
        try{
            alterChildTable(database);
            alterMemberTable(database);
            alterChildGMPTable(database);


        }catch (Exception e){
            e.printStackTrace();

        }
        alterANCRegisterTable(database);
        alterChildTableKMC(database);
        try{
            alterChildTableIMMATURED(database);
        }catch (Exception e){

        }

        try{
            IMCIReportRepository.createTable(database);
        }catch (Exception e){

        }

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
                case 3:
                    MUACRepository.createTable(db);
                    break;
                case 4:
                    ReferralRepository.createTable(db);
                    break;
                case 5:
                    OutreachRepository.createTable(db);
                    MicroPlanRepository.createTable(db);
                    try{
                        alterChildTable(db);
                        alterMemberTable(db);
                        alterChildGMPTable(db);
                        WeightForHeightRepository.createTable(db);
                    }catch (Exception e){
                        e.printStackTrace();

                    }
                    break;
                case 6:
                    try{
                        alterANCRegisterTable(db);

                    }catch (Exception e){

                    }
                    break;
                case 7:
                    try{
                        alterChildTableKMC(db);
                    }catch (Exception e){

                    }
                    break;
                case 8:
                    try{
                        IMCIReportRepository.createTable(db);
                    }catch (Exception e){

                    }
                    break;
                case 9:
                    try{
                        alterChildTableIMMATURED(db);
                    }catch (Exception e){

                    }
                    break;
                default:
                    break;
            }
            upgradeTo++;
        }
    }
    private void alterChildTable(SQLiteDatabase db){
        db.execSQL("ALTER TABLE ec_child ADD COLUMN is_went_uhc VARCHAR;");
    }
    private void alterChildGMPTable(SQLiteDatabase db){
        db.execSQL("ALTER TABLE ec_child ADD COLUMN session_info_received VARCHAR;");
    }

    private void alterMemberTable(SQLiteDatabase db){
        db.execSQL("ALTER TABLE ec_family_member ADD COLUMN weight VARCHAR;");
        db.execSQL("ALTER TABLE ec_family_member ADD COLUMN weight_date VARCHAR;");
    }
    private void alterANCRegisterTable(SQLiteDatabase db){
        db.execSQL("ALTER TABLE ec_anc_register ADD COLUMN next_visit_date VARCHAR;");
    }
    private void alterChildTableKMC(SQLiteDatabase db){
        db.execSQL("ALTER TABLE ec_child ADD COLUMN kmc_status VARCHAR;");
        db.execSQL("ALTER TABLE ec_child ADD COLUMN identified_date VARCHAR;");
    }
    private void alterChildTableIMMATURED(SQLiteDatabase db){
        db.execSQL("ALTER TABLE ec_child ADD COLUMN is_immature VARCHAR;");
    }

}
