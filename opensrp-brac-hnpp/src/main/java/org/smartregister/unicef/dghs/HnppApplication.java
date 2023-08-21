package org.smartregister.unicef.dghs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.evernote.android.job.JobManager;

import org.jetbrains.annotations.NotNull;
import org.smartregister.AllConstants;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.chw.core.helper.RulesEngineHelper;
import org.smartregister.chw.core.repository.AncRegisterRepository;
import org.smartregister.chw.core.sync.ChwClientProcessor;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.unicef.dghs.activity.AdoMemberRegisterActivity;
import org.smartregister.unicef.dghs.activity.AdultMemberRegisterActivity;
import org.smartregister.unicef.dghs.activity.AdultRiskRegisterActivity;
import org.smartregister.unicef.dghs.activity.GuestMemberActivity;
import org.smartregister.unicef.dghs.activity.HNPPJsonFormActivity;
import org.smartregister.unicef.dghs.activity.HNPPMemberJsonFormActivity;
import org.smartregister.unicef.dghs.activity.HnppAllMemberRegisterActivity;
import org.smartregister.unicef.dghs.activity.HnppAncRegisterActivity;
import org.smartregister.unicef.dghs.activity.HnppAncRiskRegisterActivity;
import org.smartregister.unicef.dghs.activity.HnppChildRiskRegisterActivity;
import org.smartregister.unicef.dghs.activity.HnppElcoMemberRegisterActivity;
import org.smartregister.unicef.dghs.activity.HnppElcoRiskRegisterActivity;
import org.smartregister.unicef.dghs.activity.HnppPncRegisterActivity;
import org.smartregister.unicef.dghs.activity.HnppPncRiskRegisterActivity;
import org.smartregister.unicef.dghs.activity.IYCFRegisterActivity;
import org.smartregister.unicef.dghs.activity.WomenServiceRegisterActivity;
import org.smartregister.unicef.dghs.nativation.view.HnppNavigationTopView;
import org.smartregister.unicef.dghs.nativation.view.NavigationMenu;
import org.smartregister.unicef.dghs.job.ZScoreRefreshServiceJob;
import org.smartregister.unicef.dghs.listener.HnppNavigationListener;
import org.smartregister.unicef.dghs.nativation.presenter.HnppNavigationPresenter;
import org.smartregister.unicef.dghs.location.HALocationHelper;
import org.smartregister.unicef.dghs.repository.CampRepository;
import org.smartregister.unicef.dghs.repository.GlobalLocationRepository;
import org.smartregister.unicef.dghs.repository.HALocationRepository;
import org.smartregister.unicef.dghs.repository.GuestMemberIdRepository;
import org.smartregister.unicef.dghs.repository.HnppChwRepository;
import org.smartregister.unicef.dghs.repository.HnppVisitLogRepository;
import org.smartregister.unicef.dghs.repository.IndicatorRepository;
import org.smartregister.unicef.dghs.repository.NotificationRepository;
import org.smartregister.unicef.dghs.repository.PaymentHistoryRepository;
import org.smartregister.unicef.dghs.repository.RiskDetailsRepository;
import org.smartregister.unicef.dghs.repository.HouseholdIdRepository;
import org.smartregister.unicef.dghs.repository.StockRepository;
import org.smartregister.unicef.dghs.repository.TargetVsAchievementRepository;
import org.smartregister.unicef.dghs.sync.HnppClientProcessor;
import org.smartregister.unicef.dghs.sync.HnppSyncConfiguration;
import org.smartregister.unicef.dghs.utils.HNPPApplicationUtils;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.chw.anc.AncLibrary;

import org.smartregister.chw.core.contract.CoreApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.unicef.dghs.activity.ChildRegisterActivity;
import org.smartregister.unicef.dghs.activity.FamilyProfileActivity;
import org.smartregister.unicef.dghs.activity.FamilyRegisterActivity;
import org.smartregister.unicef.dghs.nativation.view.HnppNavigationMenu;
import org.smartregister.unicef.dghs.job.HnppJobCreator;
import org.smartregister.unicef.dghs.model.HnppNavigationModel;
import org.smartregister.chw.pnc.PncLibrary;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.helper.JsonSpecHelper;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.domain.FamilyMetadata;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.jsonmapping.Vaccine;
import org.smartregister.immunization.domain.jsonmapping.VaccineGroup;
import org.smartregister.immunization.util.VaccinatorUtils;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Repository;
import org.smartregister.util.LangUtils;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.DrishtiApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import timber.log.Timber;

public class HnppApplication extends DrishtiApplication implements CoreApplication {

    private HouseholdIdRepository householdIdRepository;
    private GuestMemberIdRepository guestMemberIdRepository;
    private HnppVisitLogRepository hnppVisitLogRepository;
    private static HALocationRepository HALocationRepository;
    private static GlobalLocationRepository globalLocationRepository;
    private static CampRepository campRepository;
    private static RiskDetailsRepository riskDetailsRepository;
    private static TargetVsAchievementRepository targetVsAchievementRepository;
    private static IndicatorRepository indicatorRepository;
    private static NotificationRepository notificationRepository;
    private static PaymentHistoryRepository paymentHistoryRepository;
    private static StockRepository stockRepository;
    private static CommonFtsObject commonFtsObject = null;
    private EventClientRepository eventClientRepository;
    @SuppressLint("StaticFieldLeak")
    private static ClientProcessorForJava clientProcessor;
    private static AncRegisterRepository ancRegisterRepository;
    public JsonSpecHelper jsonSpecHelper;
    private LocationRepository locationRepository;
    private ECSyncHelper ecSyncHelper;
    private String password;

    private RulesEngineHelper rulesEngineHelper;

    public static android.content.Context appContext;

    public  static void initContext(android.content.Context context){
        appContext = context;
        Context.getInstance().updateApplicationContext(appContext);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        context = Context.getInstance();
        appContext = this;

        //init Job Manager
        SyncStatusBroadcastReceiver.init(this);
        JobManager.create(this).addJobCreator(new HnppJobCreator());

        //Necessary to determine the right form to pick from assets
//        CoreConstants.JSON_FORM.setLocaleAndAssetManager(HnppApplication.getCurrentLocale(),
//                HnppApplication.getInstance().getApplicationContext().getAssets());

        context.updateApplicationContext(getApplicationContext());
        context.updateCommonFtsObject(createCommonFtsObject());

        // init json helper
        this.jsonSpecHelper = new JsonSpecHelper(this);
        CoreLibrary.init(context,new HnppSyncConfiguration(),BuildConfig.BUILD_TIMESTAMP);
        ConfigurableViewsLibrary.init(context, getRepository());
        FamilyLibrary.init(context, getRepository(), getMetadata(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        ImmunizationLibrary.init(context, getRepository(), null, BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        GrowthMonitoringLibrary.init(context, getRepository(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
//        ZScoreRefreshServiceJob.scheduleJobImmediately(ZScoreRefreshServiceJob.TAG);
        initOfflineSchedules();
        LocationHelper.init(new ArrayList<>(Arrays.asList(BuildConfig.ALLOWED_LOCATION_LEVELS)), BuildConfig.DEFAULT_LOCATION);
        //ReportingLibrary.init(context, getRepository(), null, BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        AncLibrary.init(context, getRepository(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        PncLibrary.init(context, getRepository(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
       // MalariaLibrary.init(context, getRepository(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        setOpenSRPUrl();
        // set up processor
        FamilyLibrary.getInstance().setClientProcessorForJava(HnppClientProcessor.getInstance(getApplicationContext()));

    }
    public static JsonSpecHelper getJsonSpecHelper() {
        return getInstance().jsonSpecHelper;
    }
    public static AncRegisterRepository ancRegisterRepository() {
        if (ancRegisterRepository == null) {
            ancRegisterRepository = new AncRegisterRepository(getInstance().getRepository());
        }
        return ancRegisterRepository;
    }
    public static Locale getCurrentLocale() {
        String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();

        if(TextUtils.isEmpty(userName)){
            return new Locale("bn");
        }
        return  HnppApplication.getHNPPInstance().getApplicationContext().getResources().getConfiguration().locale;

        //return mInstance == null ? Locale.getDefault() : mInstance.getResources().getConfiguration().locale;
    }
    @Override
    public String getPassword() {
        if (password == null) {
            String username = getContext().allSharedPreferences().fetchRegisteredANM();
            password = getContext().userService().getGroupId(username);
        }
        return password;
    }


    public static ClientProcessorForJava getClientProcessor(android.content.Context context) {
        if (clientProcessor == null) {
            clientProcessor = ChwClientProcessor.getInstance(context);
        }
        return clientProcessor;
    }
    public VaccineRepository vaccineRepository() {
        return ImmunizationLibrary.getInstance().vaccineRepository();
    }

    public LocationRepository getLocationRepository() {
        if (locationRepository == null) {
            locationRepository = new LocationRepository(getRepository());
        }
        return locationRepository;
    }

    @Override
    public void saveLanguage(String language) {
        HnppApplication.getInstance().getContext().allSharedPreferences().saveLanguagePreference(language);
    }

    @Override
    public Context getContext() {
        return context;
    }


    @Override
    public ECSyncHelper getEcSyncHelper() {
        if (ecSyncHelper == null) {
            ecSyncHelper = ECSyncHelper.getInstance(getApplicationContext());
        }
        return ecSyncHelper;
    }
    @Override
    public void notifyAppContextChange() {
        Locale current = getApplicationContext().getResources().getConfiguration().locale;
        saveLanguage(current.getLanguage());
        CoreConstants.JSON_FORM.setLocaleAndAssetManager(current, getAssets());
        FamilyLibrary.getInstance().setMetadata(getMetadata());
        Runtime.getRuntime().exit(0);
        Intent intent = new Intent(this,org.smartregister.unicef.dghs.activity.LoginActivity.class);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.userService().logoutSession();
        startActivity(intent);
    }

    @Override
    public RulesEngineHelper getRulesEngineHelper() {
        if (rulesEngineHelper == null) {
            rulesEngineHelper = new RulesEngineHelper(getApplicationContext());
        }
        return rulesEngineHelper;
    }

    public AllCommonsRepository getAllCommonsRepository(String table) {
        return HnppApplication.getInstance().getContext().allCommonsRepositoryobjects(table);
    }
    public void initOfflineSchedules() {
        try {
            List<VaccineGroup> childVaccines = VaccinatorUtils.getSupportedVaccines(this);
            List<Vaccine> specialVaccines = VaccinatorUtils.getSpecialVaccines(this);
            VaccineSchedule.init(childVaccines, specialVaccines, "child");
            List<VaccineGroup> womanVaccines = VaccinatorUtils.getSupportedWomanVaccines(this);
            VaccineSchedule.init(womanVaccines, null, "woman");
        } catch (Exception e) {
            Log.e("HnppApplication", Log.getStackTraceString(e));
        }
    }

    public static CommonFtsObject createCommonFtsObject() {
        return HNPPApplicationUtils.getCommonFtsObject(commonFtsObject);
    }
    public static synchronized HnppApplication getHNPPInstance() {
        return (HnppApplication) mInstance;
    }


    private HnppNavigationModel hnppNavigationModel;

    public HnppNavigationModel getHnppNavigationModel() {

        if(hnppNavigationModel == null){

            hnppNavigationModel = new HnppNavigationModel();

        }

        return hnppNavigationModel;

    }

    public void setupNavigation(HnppNavigationPresenter mPresenter){

        NavigationMenu.setupNavigationMenu(new HnppNavigationListener(),mPresenter,this, new HnppNavigationTopView(), new HnppNavigationMenu(), getHnppNavigationModel(),

                getRegisteredActivities(), false);

    }


    @Override
    public void logoutCurrentUser() {
//        Intent intent = new Intent(this,org.smartregister.unicef.dghs.activity.LoginActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addCategory(Intent.CATEGORY_HOME);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
//        context.userService().logoutSession();
    }
    public void forceLogout() {
        HALocationHelper.clearLocation();
        Intent intent = new Intent(this,org.smartregister.unicef.dghs.activity.LoginActivity.class);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.userService().logoutSession();
        startActivity(intent);

    }
    public void forceLogoutForRemoteLogin() {
        JobManager.instance().cancelAll();
        HALocationHelper.clearLocation();
        Intent intent = new Intent(this,org.smartregister.unicef.dghs.activity.LoginActivity.class);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.userService().forceRemoteLogin();
        context.userService().logoutSession();
        startActivity(intent);
    }

    public void appSwitch() {
        Runtime.getRuntime().exit(0);
        //System.exit(0);
        HALocationHelper.clearLocation();
        Intent intent = new Intent(this,org.smartregister.unicef.dghs.activity.LoginActivity.class);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        setOpenSRPUrl();
        context.userService().logoutSession();
        startActivity(intent);

    }
    public void clearDatabase(){
        ((HnppChwRepository)getRepository()).deleteDatabase();
    }
    public void clearSharePreference(String previousName){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.edit().clear().apply();
        //need to set the username at loginscreen
        HnppApplication.getInstance().getContext().allSharedPreferences().updateANMUserName(previousName);
    }
    public static synchronized HnppApplication getInstance() {
        return (HnppApplication) mInstance;
    }
    public @NotNull Map<String, Class> getRegisteredActivities() {
        Map<String, Class> registeredActivities = new HashMap<>();
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.ANC_REGISTER_ACTIVITY, HnppAncRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.FAMILY_REGISTER_ACTIVITY, FamilyRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.ALL_MEMBER_REGISTER_ACTIVITY, HnppAllMemberRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.ELCO_REGISTER_ACTIVITY, HnppElcoMemberRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.CHILD_REGISTER_ACTIVITY, ChildRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.PNC_REGISTER_ACTIVITY, HnppPncRegisterActivity.class);
//        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.REFERRALS_REGISTER_ACTIVITY, ReferralRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.MALARIA_REGISTER_ACTIVITY, FamilyRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.GUEST_MEMBER_ACTIVITY, GuestMemberActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.ANC_RISK_REGISTER_ACTIVITY, HnppAncRiskRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.PNC_RISK_REGISTER_ACTIVITY, HnppPncRiskRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.ELCO_RISK_REGISTER_ACTIVITY, HnppElcoRiskRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.CHILD_RISK_REGISTER_ACTIVITY, HnppChildRiskRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.ADULT_RISK_REGISTER_ACTIVITY, AdultRiskRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.ADULT_REGISTER_ACTIVITY, AdultMemberRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.ADO_REGISTER_ACTIVITY, AdoMemberRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.WOMEN_REGISTER_ACTIVITY, WomenServiceRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.IYCF_REGISTER_ACTIVITY, IYCFRegisterActivity.class);

        return registeredActivities;
    }

    @Override
    public Repository getRepository() {
        try {
            if (repository == null) {
                repository = new HnppChwRepository(getInstance().getApplicationContext(), context);
            }
        } catch (UnsatisfiedLinkError e) {
            Timber.e(e);
        }
        return repository;
    }

    public HnppVisitLogRepository getHnppVisitLogRepository() {
        if (hnppVisitLogRepository == null) {
            hnppVisitLogRepository = new HnppVisitLogRepository(getInstance().getRepository());
        }
        return hnppVisitLogRepository;
    }

    public HouseholdIdRepository getHouseholdIdRepository() {
        if (householdIdRepository == null) {
            householdIdRepository = new HouseholdIdRepository(getInstance().getRepository());
        }
        return householdIdRepository;
    }
    public GuestMemberIdRepository getGuestMemberIdRepository() {
        if (guestMemberIdRepository == null) {
            guestMemberIdRepository = new GuestMemberIdRepository(getInstance().getRepository());
        }
        return guestMemberIdRepository;
    }

    public static HALocationRepository getHALocationRepository() {
        if ( HALocationRepository == null) {
            HALocationRepository = new HALocationRepository(getInstance().getRepository());
        }
        return HALocationRepository;
    }
    public static GlobalLocationRepository getGlobalLocationRepository() {
        if ( globalLocationRepository == null) {
            globalLocationRepository = new GlobalLocationRepository(getInstance().getRepository());
        }
        return globalLocationRepository;
    }
    public static CampRepository getCampRepository() {
        if ( campRepository == null) {
            campRepository = new CampRepository(getInstance().getRepository());
        }
        return campRepository;
    }
    public static RiskDetailsRepository getRiskDetailsRepository() {
        if ( riskDetailsRepository == null) {
            riskDetailsRepository = new RiskDetailsRepository(getInstance().getRepository());
        }
        return riskDetailsRepository;
    }
    public static TargetVsAchievementRepository getTargetRepository() {
        if ( targetVsAchievementRepository == null) {
            targetVsAchievementRepository = new TargetVsAchievementRepository(getInstance().getRepository());
        }
        return targetVsAchievementRepository;
    }
    public static IndicatorRepository getIndicatorRepository() {
        if ( indicatorRepository == null) {
            indicatorRepository = new IndicatorRepository(getInstance().getRepository());
        }
        return indicatorRepository;
    }
    public static NotificationRepository getNotificationRepository() {
        if ( notificationRepository == null) {
            notificationRepository = new NotificationRepository(getInstance().getRepository());
        }
        return notificationRepository;
    }
    public static StockRepository getStockRepository() {
        if ( stockRepository == null) {
            stockRepository = new StockRepository(getInstance().getRepository());
        }
        return stockRepository;
    }
    public void setOpenSRPUrl() {
        AllSharedPreferences preferences = Utils.getAllSharedPreferences();
        boolean isRelease = HnppConstants.isReleaseBuild();
        //if(TextUtils.isEmpty(preferences.getPreference(AllConstants.DRISHTI_BASE_URL))){
          preferences.savePreference(AllConstants.DRISHTI_BASE_URL, isRelease? BuildConfig.opensrp_url_live:BuildConfig.opensrp_url_training);

        //}

    }
    public EventClientRepository getEventClientRepository() {
        if (eventClientRepository == null) {
            eventClientRepository = new EventClientRepository(getRepository());
        }
        return eventClientRepository;
    }
    @Override
    public FamilyMetadata getMetadata() {
        FamilyMetadata metadata = new FamilyMetadata(HNPPJsonFormActivity.class, HNPPMemberJsonFormActivity.class, FamilyProfileActivity.class, CoreConstants.IDENTIFIER.UNIQUE_IDENTIFIER_KEY, false);
        metadata.updateFamilyRegister(CoreConstants.JSON_FORM.getFamilyRegister(), CoreConstants.TABLE_NAME.FAMILY, CoreConstants.EventType.FAMILY_REGISTRATION, CoreConstants.EventType.UPDATE_FAMILY_REGISTRATION, CoreConstants.CONFIGURATION.FAMILY_REGISTER, CoreConstants.RELATIONSHIP.FAMILY_HEAD, CoreConstants.RELATIONSHIP.PRIMARY_CAREGIVER);
        metadata.updateFamilyMemberRegister(CoreConstants.JSON_FORM.getFamilyMemberRegister(), CoreConstants.TABLE_NAME.FAMILY_MEMBER, CoreConstants.EventType.FAMILY_MEMBER_REGISTRATION, CoreConstants.EventType.UPDATE_FAMILY_MEMBER_REGISTRATION, CoreConstants.CONFIGURATION.FAMILY_MEMBER_REGISTER, CoreConstants.RELATIONSHIP.FAMILY);
        metadata.updateFamilyDueRegister(CoreConstants.TABLE_NAME.CHILD, Integer.MAX_VALUE, false);
        metadata.updateFamilyActivityRegister(CoreConstants.TABLE_NAME.CHILD_ACTIVITY, Integer.MAX_VALUE, false);
        metadata.updateFamilyOtherMemberRegister(CoreConstants.TABLE_NAME.FAMILY_MEMBER, Integer.MAX_VALUE, false);
        return metadata;
    }
    public static PaymentHistoryRepository getPaymentHistoryRepository() {
        if ( paymentHistoryRepository == null) {
            paymentHistoryRepository = new PaymentHistoryRepository(getInstance().getRepository());
        }
        return paymentHistoryRepository;
    }
}
