package org.smartregister.brac.hnpp;

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
import org.smartregister.brac.hnpp.activity.HNPPJsonFormActivity;
import org.smartregister.brac.hnpp.activity.HNPPMemberJsonFormActivity;
import org.smartregister.brac.hnpp.activity.HnppAllMemberRegisterActivity;
import org.smartregister.brac.hnpp.activity.HnppAncRegisterActivity;
import org.smartregister.brac.hnpp.activity.HnppElcoMemberRegisterActivity;
import org.smartregister.brac.hnpp.activity.HnppPncRegisterActivity;
import org.smartregister.brac.hnpp.custom_view.HnppNavigationTopView;
import org.smartregister.brac.hnpp.listener.HnppNavigationListener;
import org.smartregister.brac.hnpp.presenter.HnppNavigationPresenter;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.presenter.HnppNavigationPresenter;
import org.smartregister.brac.hnpp.repository.HnppChwRepository;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.brac.hnpp.repository.SSLocationRepository;
import org.smartregister.brac.hnpp.repository.HouseholdIdRepository;
import org.smartregister.brac.hnpp.sync.HnppClientProcessor;
import org.smartregister.brac.hnpp.sync.HnppSyncConfiguration;
import org.smartregister.brac.hnpp.utils.HNPPApplicationUtils;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.contract.CoreApplication;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.brac.hnpp.activity.ChildRegisterActivity;
import org.smartregister.brac.hnpp.activity.FamilyProfileActivity;
import org.smartregister.brac.hnpp.activity.FamilyRegisterActivity;
import org.smartregister.brac.hnpp.activity.ReferralRegisterActivity;
import org.smartregister.brac.hnpp.custom_view.HnppNavigationMenu;
import org.smartregister.brac.hnpp.job.HnppJobCreator;
import org.smartregister.brac.hnpp.model.HnppNavigationModel;
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
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.util.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import timber.log.Timber;

public class HnppApplication extends CoreChwApplication implements CoreApplication {

    private HouseholdIdRepository householdIdRepository;
    private HnppVisitLogRepository hnppVisitLogRepository;
    private static SSLocationRepository locationRepository;
    private static CommonFtsObject commonFtsObject = null;
    private EventClientRepository eventClientRepository;
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        context = Context.getInstance();

        //init Job Manager
        SyncStatusBroadcastReceiver.init(this);
        JobManager.create(this).addJobCreator(new HnppJobCreator());

        //Necessary to determine the right form to pick from assets
        CoreConstants.JSON_FORM.setLocaleAndAssetManager(HnppApplication.getCurrentLocale(),
                HnppApplication.getInstance().getApplicationContext().getAssets());

        context.updateApplicationContext(getApplicationContext());
        context.updateCommonFtsObject(createCommonFtsObject());

        // init json helper
        this.jsonSpecHelper = new JsonSpecHelper(this);
        CoreLibrary.init(context,new HnppSyncConfiguration(),BuildConfig.BUILD_TIMESTAMP);
        ConfigurableViewsLibrary.init(context, getRepository());
        FamilyLibrary.init(context, getRepository(), getMetadata(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        ImmunizationLibrary.init(context, getRepository(), null, BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
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
    public void initOfflineSchedules() {
        try {
            List<VaccineGroup> childVaccines = VaccinatorUtils.getSupportedVaccines(this);
            List<Vaccine> specialVaccines = VaccinatorUtils.getSpecialVaccines(this);
            VaccineSchedule.init(childVaccines, specialVaccines, "child");
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
//        Intent intent = new Intent(this,org.smartregister.brac.hnpp.activity.LoginActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addCategory(Intent.CATEGORY_HOME);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
//        context.userService().logoutSession();
    }
    @Override
    public void forceLogout() {
        SSLocationHelper.clearLocation();
        Intent intent = new Intent(this,org.smartregister.brac.hnpp.activity.LoginActivity.class);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.userService().logoutSession();
        startActivity(intent);

    }
    public void forceLogoutForRemoteLogin() {
        SSLocationHelper.clearLocation();
        Intent intent = new Intent(this,org.smartregister.brac.hnpp.activity.LoginActivity.class);
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
        SSLocationHelper.clearLocation();
        Intent intent = new Intent(this,org.smartregister.brac.hnpp.activity.LoginActivity.class);
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
    public void clearSharePreference(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.edit().clear().commit();
    }

    public @NotNull Map<String, Class> getRegisteredActivities() {
        Map<String, Class> registeredActivities = new HashMap<>();
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.ANC_REGISTER_ACTIVITY, HnppAncRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.FAMILY_REGISTER_ACTIVITY, FamilyRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.ALL_MEMBER_REGISTER_ACTIVITY, HnppAllMemberRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.ELCO_REGISTER_ACTIVITY, HnppElcoMemberRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.CHILD_REGISTER_ACTIVITY, ChildRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.PNC_REGISTER_ACTIVITY, HnppPncRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.REFERRALS_REGISTER_ACTIVITY, ReferralRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.MALARIA_REGISTER_ACTIVITY, FamilyRegisterActivity.class);
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

    public static SSLocationRepository getSSLocationRepository() {
        if ( locationRepository == null) {
            locationRepository = new SSLocationRepository(getInstance().getRepository());
        }
        return locationRepository;
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
}
