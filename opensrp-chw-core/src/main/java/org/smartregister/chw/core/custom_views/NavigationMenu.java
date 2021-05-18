package org.smartregister.chw.core.custom_views;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.FadingCircle;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.activity.ChwP2pModeSelectActivity;
import org.smartregister.chw.core.adapter.NavigationAdapter;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.contract.NavigationContract;
import org.smartregister.chw.core.listener.NavigationListener;
import org.smartregister.chw.core.model.NavigationModel;
import org.smartregister.chw.core.model.NavigationOption;
import org.smartregister.chw.core.presenter.NavigationPresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.domain.FetchStatus;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.util.LangUtils;
import org.smartregister.util.PermissionUtils;

import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class NavigationMenu implements NavigationContract.View, SyncStatusBroadcastReceiver.SyncStatusListener {
    private static NavigationMenu instance;
    private static WeakReference<Activity> activityWeakReference;
    public static CoreChwApplication application;
    private static NavigationMenu.Flavour menuFlavor;
    private static NavigationMenu.FlavorTop topFlavor;
    private static NavigationModel.Flavor modelFlavor;
    private static Map<String, Class> registeredActivities;
    private static NavigationListener navigationListener;
    private static boolean showDeviceToDeviceSync = true;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationAdapter navigationAdapter;
    private RecyclerView recyclerView;
    private TextView tvLogout;
    private TextView tvCovid19;
    private TextView tvForceSync;
    private RelativeLayout ss_info_browse;
    private RelativeLayout notification_layout;
    private RelativeLayout migration_layout;
    private RelativeLayout payment_layout;
    private RelativeLayout dashboard_layout;

    private View rootView = null;
    private ImageView ivSync;
    private ProgressBar syncProgressBar;
    private static NavigationContract.Presenter mPresenter;
    private View parentView;

    private NavigationMenu() {

    }

    public static void setupNavigationMenu(CoreChwApplication application, NavigationMenu.Flavour menuFlavor,
                                           NavigationModel.Flavor modelFlavor, Map<String, Class> registeredActivities, boolean showDeviceToDeviceSync) {
        NavigationMenu.application = application;
        NavigationMenu.menuFlavor = menuFlavor;
        NavigationMenu.modelFlavor = modelFlavor;
        NavigationMenu.registeredActivities = registeredActivities;
        NavigationMenu.showDeviceToDeviceSync = showDeviceToDeviceSync;
        NavigationMenu.navigationListener = new NavigationListener();
    }
    public static void setupNavigationMenu(NavigationListener navigationListener,NavigationPresenter presenter, CoreChwApplication application,NavigationMenu.FlavorTop topFlavor, NavigationMenu.Flavour menuFlavor,
                                           NavigationModel.Flavor modelFlavor, Map<String, Class> registeredActivities, boolean showDeviceToDeviceSync) {
        NavigationMenu.application = application;
        NavigationMenu.topFlavor = topFlavor;
        NavigationMenu.menuFlavor = menuFlavor;
        NavigationMenu.modelFlavor = modelFlavor;
        NavigationMenu.registeredActivities = registeredActivities;
        NavigationMenu.showDeviceToDeviceSync = showDeviceToDeviceSync;
        NavigationMenu.navigationListener = navigationListener;
        NavigationMenu.mPresenter = presenter;
    }

    public static NavigationMenu getInstance(Activity activity, View parentView, Toolbar myToolbar) {
        SyncStatusBroadcastReceiver.getInstance().removeSyncStatusListener(instance);
        activityWeakReference = new WeakReference<>(activity);
        int orientation = activity.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (instance == null) {
                instance = new NavigationMenu();
            }

            SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(instance);
            instance.init(activity, parentView, myToolbar);
            return instance;
        } else {
            return null;
        }
    }

    private void init(Activity activity, View myParentView, Toolbar myToolbar) {
        // parentActivity = activity;
        try {
            setParentView(activity, parentView);
            toolbar = myToolbar;
            parentView = myParentView;
            if(mPresenter == null){
                mPresenter = new NavigationPresenter(application, this, modelFlavor);
            }

            prepareViews(activity);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void setParentView(Activity activity, View parentView) {
        if (parentView != null) {
            rootView = parentView;
        } else {
            // get current view
            // ViewGroup current = parentActivity.getWindow().getDecorView().findViewById(android.R.id.content);
            ViewGroup current = (ViewGroup) ((ViewGroup) (activity.findViewById(android.R.id.content))).getChildAt(0);
            if (!(current instanceof DrawerLayout)) {

                if (current.getParent() != null) {
                    ((ViewGroup) current.getParent()).removeView(current); // <- fix
                }

                // swap content view
                LayoutInflater mInflater = LayoutInflater.from(activity);
                ViewGroup contentView = (ViewGroup) mInflater.inflate(R.layout.activity_base, null);
                activity.setContentView(contentView);

                rootView = activity.findViewById(R.id.nav_view);
                RelativeLayout rl = activity.findViewById(R.id.nav_content);

                if (current.getParent() != null) {
                    ((ViewGroup) current.getParent()).removeView(current); // <- fix
                }

                if (current instanceof RelativeLayout) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                    current.setLayoutParams(params);
                    rl.addView(current);
                } else {
                    rl.addView(current);
                }
            } else {
                rootView = current;
            }
        }
        //
    }

    @Override
    public void prepareViews(Activity activity) {

        drawer = activity.findViewById(R.id.drawer_layout);
        recyclerView = rootView.findViewById(R.id.rvOptions);
        // NavigationView navigationView = rootView.findViewById(R.id.nav_view);
        tvLogout = rootView.findViewById(R.id.tvLogout);
        tvCovid19 = rootView.findViewById(R.id.covid19);
        tvForceSync = rootView.findViewById(R.id.tvForceSync);
        ss_info_browse = rootView.findViewById(R.id.ss_info_browse);
        notification_layout = rootView.findViewById(R.id.notification_view);
        migration_layout = rootView.findViewById(R.id.migration_view);
        payment_layout = rootView.findViewById(R.id.payment_view);
        dashboard_layout = rootView.findViewById(R.id.dashboard_view);

        recyclerView = rootView.findViewById(R.id.rvOptions);
        ivSync = rootView.findViewById(R.id.ivSyncIcon);
        syncProgressBar = rootView.findViewById(R.id.pbSync);

        ImageView ivLogo = rootView.findViewById(R.id.ivLogo);
        ivLogo.setContentDescription(activity.getString(R.string.nav_logo));
        ivLogo.setImageResource(topFlavor !=null ? topFlavor.getTopLogo() : R.drawable.ic_logo);
        TextView tvLogo = rootView.findViewById(R.id.tvLogo);
        tvLogo.setText(topFlavor !=null ? topFlavor.topText() : activity.getString(R.string.nav_logo));
        TextView tvAppVersion = rootView.findViewById(R.id.tvappversion);
        tvAppVersion.setText(topFlavor !=null ? topFlavor.appVersionText(): "");
        if (syncProgressBar != null) {
            FadingCircle circle = new FadingCircle();
            syncProgressBar.setIndeterminateDrawable(circle);
        }

        // register all objects
        registerDrawer(activity);
        registerNavigation(activity);
        registerLogout(activity);
        registerSync(activity);
        registerLanguageSwitcher(activity);
        registerCovid19(activity);
        registerForceSync(activity);
        registerBrowseSSInfo(activity);
        registerNotification(activity);
        registerMigration(activity);
        registerPayment(activity);
        registerDashboard(activity);

        registerDeviceToDeviceSync(activity);
        // update all actions
        mPresenter.refreshLastSync();
        mPresenter.refreshNavigationCount(activity);
    }



    @Override
    public void refreshLastSync(Date lastSync) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa, MMM d", Locale.getDefault());
        if (rootView != null) {
            TextView tvLastSyncTime = rootView.findViewById(R.id.tvSyncTime);
            if (lastSync != null) {
                tvLastSyncTime.setVisibility(View.VISIBLE);
                tvLastSyncTime.setText(MessageFormat.format(" {0}", sdf.format(lastSync)));
            } else {
                tvLastSyncTime.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void refreshCurrentUser(String name) {
        if (tvLogout != null && activityWeakReference.get() != null) {
            tvLogout.setText(String.format("%s %s", activityWeakReference.get().getResources().getString(R.string.log_out_as), name));
        }
    }

    @Override
    public void logout(Activity activity) {
        try{
            if(activity !=null && !activity.isFinishing()){
                Toast.makeText(activity, activity.getResources().getText(R.string.action_log_out), Toast.LENGTH_SHORT).show();

            }
            application.forceLogout();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void covid19(Activity activity) {
        if(mPresenter!=null){
            mPresenter.covid19(activity);
        }

    }
    public void forceSync(Activity activity) {
        if(mPresenter!=null){
            mPresenter.forceSync(activity);
        }
    }
    public void browseSSInfo(Activity activity) {
        if(mPresenter!=null){
            mPresenter.browseSSInfo(activity);
        }
    }
    public void browseNotification(Activity activity) {
        if(mPresenter!=null){
            mPresenter.browseNotification(activity);
        }
    }
    public void browseMigration(Activity activity) {
        if(mPresenter!=null){
            mPresenter.browseMigration(activity);
        }
    }
    public void browsePayment(Activity activity) {
        if(mPresenter!=null){
            mPresenter.browsePayment(activity);
        }
    }
    public void browseDashboard(Activity activity) {
        if(mPresenter!=null){
            mPresenter.browseDashboard(activity);
        }
    }

    @Override
    public void refreshCount() {
        navigationAdapter.notifyDataSetChanged();
    }

    @Override
    public void displayToast(Activity activity, String message) {
        try{
            if (activity != null && !activity.isFinishing()) {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void registerDrawer(Activity parentActivity) {
        if (drawer != null) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    parentActivity, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

        }
    }

    private void registerNavigation(Activity parentActivity) {
        if (recyclerView != null) {

            List<NavigationOption> navigationOptions = mPresenter.getOptions();
            if (navigationAdapter == null) {
                navigationAdapter = new NavigationAdapter(navigationOptions, parentActivity, registeredActivities);
                navigationListener.setAdapter(navigationAdapter,parentActivity);
                navigationAdapter.setNavigationListener(navigationListener);
            }

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(parentActivity);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(navigationAdapter);
        }
    }

    private void registerLogout(final Activity parentActivity) {
        mPresenter.displayCurrentUser();
        tvLogout.setOnClickListener(v -> logout(parentActivity));
    }
    private void registerCovid19(final Activity parentActivity) {
        mPresenter.displayCurrentUser();
        tvCovid19.setOnClickListener(v -> covid19(parentActivity));
    }
    private void registerForceSync(final Activity parentActivity){
        mPresenter.displayCurrentUser();
        tvForceSync.setOnClickListener(v -> forceSync(parentActivity));
    }
    private void registerBrowseSSInfo(final Activity parentActivity){
        mPresenter.displayCurrentUser();
        ss_info_browse.setOnClickListener(v -> browseSSInfo(parentActivity));
    }
    private void registerNotification(final Activity parentActivity){
        mPresenter.displayCurrentUser();
        notification_layout.setOnClickListener(v -> browseNotification(parentActivity));
    }
    private void registerMigration(final Activity parentActivity){
        mPresenter.displayCurrentUser();
        migration_layout.setOnClickListener(v -> browseMigration(parentActivity));
    }
    private void registerPayment(final Activity parentActivity){
        mPresenter.displayCurrentUser();
        payment_layout.setOnClickListener(v -> browsePayment(parentActivity));
    }
    private void registerDashboard(final Activity parentActivity){
        mPresenter.displayCurrentUser();
        dashboard_layout.setOnClickListener(v -> browseDashboard(parentActivity));
    }


    private void registerSync(final Activity parentActivity) {

        TextView tvSync = rootView.findViewById(R.id.tvSync);
        ivSync = rootView.findViewById(R.id.ivSyncIcon);
        syncProgressBar = rootView.findViewById(R.id.pbSync);

        View.OnClickListener syncClicker = v -> {
            if(parentActivity!=null && !parentActivity.isFinishing()){
                try{
                    Toast.makeText(parentActivity, parentActivity.getResources().getText(R.string.action_start_sync), Toast.LENGTH_SHORT).show();
                    mPresenter.sync(parentActivity);
                }catch (Exception e) {

                }

            }

        };


        tvSync.setOnClickListener(syncClicker);
        ivSync.setOnClickListener(syncClicker);

        refreshSyncProgressSpinner();
    }

    private void registerLanguageSwitcher(final Activity context) {

        View rlIconLang = rootView.findViewById(R.id.rlIconLang);
        final TextView tvLang = rootView.findViewById(R.id.tvLang);

        final String[] languages = menuFlavor.getSupportedLanguages();
        Locale current = context.getResources().getConfiguration().locale;
        tvLang.setText(StringUtils.capitalize(current.getDisplayLanguage()));

        rlIconLang.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.choose_language));
            builder.setItems(languages, (dialog, which) -> {
                String lang = languages[which];
                Locale LOCALE;
                switch (lang) {
                    case "English":
                        LOCALE = Locale.ENGLISH;
                        break;
                    case "Français":
                        LOCALE = Locale.FRENCH;
                        break;
                    case "Kiswahili":
                        LOCALE = new Locale("sw");
                        break;
                    case "Bangla":
                        LOCALE = new Locale("bn");
                        break;
                    default:
                        LOCALE = Locale.ENGLISH;
                        break;
                }
                tvLang.setText(languages[which]);
                LangUtils.saveLanguage(context.getApplicationContext(), LOCALE.getLanguage());

                // destroy current instance
                drawer.closeDrawers();
                instance = null;
                Intent intent = context.getIntent();
                context.finish();
                context.startActivity(intent);
                application.notifyAppContextChange();
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void registerDeviceToDeviceSync(@NonNull final Activity activity) {
        if (!showDeviceToDeviceSync) {
            rootView.findViewById(R.id.rlIconDevice).setVisibility(View.GONE);
        }
        rootView.findViewById(R.id.rlIconDevice)
                .setOnClickListener(v -> startP2PActivity(activity));
    }

    private void refreshSyncProgressSpinner() {
        if (SyncStatusBroadcastReceiver.getInstance().isSyncing()) {
            syncProgressBar.setVisibility(View.VISIBLE);
            ivSync.setVisibility(View.INVISIBLE);
        } else {
            syncProgressBar.setVisibility(View.INVISIBLE);
            ivSync.setVisibility(View.VISIBLE);
        }
    }

    public void startP2PActivity(@NonNull Activity activity) {
        if (PermissionUtils.isPermissionGranted(activity
                , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}
                , CoreConstants.RQ_CODE.STORAGE_PERMISIONS)) {
            activity.startActivity(new Intent(activity, ChwP2pModeSelectActivity.class));
        }
    }

    public NavigationAdapter getNavigationAdapter() {
        return navigationAdapter;
    }

    public void lockDrawer(Activity activity) {
        prepareViews(activity);
        if (drawer != null) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    public boolean onBackPressed() {
        boolean res = false;
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            res = true;
        }
        return res;
    }

    @Override
    public void onSyncStart() {
        // set the sync icon to be a rotating menu
        refreshSyncProgressSpinner();
    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {
        Timber.v("onSyncInProgress");
    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        // hide the rotating menu
        refreshSyncProgressSpinner();
        if (!fetchStatus.equals(FetchStatus.noConnection) && !fetchStatus.equals(FetchStatus.fetchedFailed)) {
            // update the time
            mPresenter.refreshLastSync();
        }

        // refreshLastSync(new Date());

        if (activityWeakReference.get() != null && !activityWeakReference.get().isDestroyed()) {
            mPresenter.refreshNavigationCount(activityWeakReference.get());
        }
    }

    public interface Flavour {
        String[] getSupportedLanguages();
    }
    public interface FlavorTop {
        int getTopLogo();
        String topText();
        String appVersionText();
    }
}
