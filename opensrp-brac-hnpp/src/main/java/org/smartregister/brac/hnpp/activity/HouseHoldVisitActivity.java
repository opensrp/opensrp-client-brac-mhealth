package org.smartregister.brac.hnpp.activity;

import static com.vijay.jsonwizard.constants.JsonFormConstants.FIELDS;
import static com.vijay.jsonwizard.constants.JsonFormConstants.STEP1;
import static org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;
import static org.smartregister.chw.anc.util.Constants.TABLES.EC_CHILD;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rey.material.widget.Button;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.Context;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.fragment.HouseHoldFormTypeFragment;
import org.smartregister.brac.hnpp.fragment.HouseHoldMemberDueFragment;
import org.smartregister.brac.hnpp.fragment.HouseHoldMemberFragment;
import org.smartregister.brac.hnpp.fragment.MemberListDialogFragment;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.listener.OnEachMemberDueValidate;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.listener.OnUpdateMemberList;
import org.smartregister.brac.hnpp.model.HnppFamilyProfileModel;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;
import org.smartregister.brac.hnpp.presenter.FamilyProfilePresenter;
import org.smartregister.brac.hnpp.service.HnppHomeVisitIntentService;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.MemberTypeEnum;
import org.smartregister.brac.hnpp.utils.MigrationSearchContentData;
import org.smartregister.brac.hnpp.utils.OnDialogOptionSelect;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.activity.CoreFamilyProfileMenuActivity;
import org.smartregister.chw.core.activity.CoreFamilyRemoveMemberActivity;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.util.FormUtils;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class HouseHoldVisitActivity extends CoreFamilyProfileActivity {
    Button nextButton;
    public int currentFragmentIndex = 0;
    List<Fragment> fragmentList = Arrays.asList(new HouseHoldFormTypeFragment(), new HouseHoldMemberFragment(), new HouseHoldMemberDueFragment());
    List<String> fragmentTagList = Arrays.asList(HouseHoldFormTypeFragment.TAG, HouseHoldMemberFragment.TAG, HouseHoldMemberDueFragment.TAG);
    public String moduleId;
    public String houseHoldId;
    public HnppFamilyProfileModel model;
    public MigrationSearchContentData migrationSearchContentData;

    public OnUpdateMemberList onUpdateMemberList;
    public OnEachMemberDueValidate onEachMemberDueValidate;


    public FragmentManager fragmentManager;

    public void listenMemberUpdateStatus(OnUpdateMemberList onUpdateMemberList) {
        this.onUpdateMemberList = onUpdateMemberList;
    }

    public void isValidateDueData(OnEachMemberDueValidate onEachMemberDueValidate) {
        this.onEachMemberDueValidate = onEachMemberDueValidate;
    }

    @Override
    protected void onCreation() {
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_hh_visit);
        initializePresenter();

        nextButton = findViewById(R.id.next_button);


        setupFragment(fragmentList.get(currentFragmentIndex), fragmentTagList.get(currentFragmentIndex));
        currentFragmentIndex = 1;

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentFragmentIndex < 3) {
                    setupFragment(fragmentList.get(currentFragmentIndex), fragmentTagList.get(currentFragmentIndex));

                    //change text is user on last fragment
                    if (currentFragmentIndex == fragmentList.size() - 1) {
                        nextButton.setText("Submit");
                    }

                    currentFragmentIndex++;
                }
            }
        });

    }

    /**
     * fragment transaction
     *
     * @param fragment for rendering
     * @param tag      to add backstack
     */
    public void setupFragment(Fragment fragment, String tag) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, getFamilyBaseEntityId());
        bundle.putString(Constants.INTENT_KEY.FAMILY_HEAD, familyHead);
        bundle.putString(Constants.INTENT_KEY.PRIMARY_CAREGIVER, primaryCaregiver);
        fragment.setArguments(bundle);
        if (fragmentManager == null) {
            fragmentManager = this.getSupportFragmentManager();
        }
        fragmentManager.beginTransaction()
                .add(R.id.hh_visit_container, fragment)
                .addToBackStack(tag)
                .commit();
    }

    public void setupFragment(Fragment fragment, String tag, Bundle bdl) {
        fragment.setArguments(bdl);
        if (fragmentManager == null) {
            fragmentManager = this.getSupportFragmentManager();
        }
        fragmentManager
                .beginTransaction()
                .add(R.id.hh_visit_container, fragment)
                .addToBackStack(tag)
                .commit();
    }

    @Override
    protected void onResumption() {

    }

    @Override
    public void refreshPresenter() {
        presenter = new FamilyProfilePresenter(this, new HnppFamilyProfileModel(familyName, moduleId, houseHoldId, familyBaseEntityId), houseHoldId, familyBaseEntityId, familyHead, primaryCaregiver, familyName);

    }

    @Override
    protected void refreshList(Fragment item) {

    }

    @Override
    protected Class<? extends CoreFamilyRemoveMemberActivity> getFamilyRemoveMemberClass() {
        return FamilyRemoveMemberActivity.class;
    }

    @Override
    protected Class<? extends CoreFamilyProfileMenuActivity> getFamilyProfileMenuClass() {
        return null;
    }


    @Override
    protected void initializePresenter() {
        super.initializePresenter();
        moduleId = getIntent().getStringExtra(HnppConstants.KEY.MODULE_ID);
        houseHoldId = getIntent().getStringExtra(DBConstants.KEY.UNIQUE_ID);
        familyName = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_NAME);
        familyBaseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        primaryCaregiver = getIntent().getStringExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER);

        model = new HnppFamilyProfileModel(familyName, moduleId, houseHoldId, familyBaseEntityId);
        presenter = new FamilyProfilePresenter(this, model, houseHoldId, familyBaseEntityId, familyHead, primaryCaregiver, familyName);
    }


    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        return null;
    }

    @Override
    public void onBackPressed() {
            currentFragmentIndex--;
            if (currentFragmentIndex == 0) {
                finish();
                return;
            }
            if (currentFragmentIndex == fragmentList.size() - 1) {
                nextButton.setText("Submit");
            }
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment instanceof HouseHoldMemberDueFragment) {
                    ((HouseHoldMemberDueFragment) fragment).validate();
                }
            }
        super.onBackPressed();

    }

    @Override
    public void errorOccured(String message) {

    }

    public FamilyProfilePresenter getfamilyProfilePresenter() {
        return ((FamilyProfilePresenter) presenter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.hh_visit_container);
        if(currentFragment instanceof HouseHoldFormTypeFragment){
            currentFragment.onActivityResult(requestCode, resultCode, data);
        }else if(currentFragment instanceof  HouseHoldMemberDueFragment){
            currentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void startFormActivity(JSONObject jsonForm) {
        if (houseHoldId == null) {
            new AlertDialog.Builder(this).setMessage(R.string.household_id_null_message)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }
                    }).show();
            return;
        }
        try {
            if (jsonForm.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Utils.metadata().familyRegister.updateEventType)) {
                if (HnppConstants.isPALogin()) {
                    openAsReadOnlyMode(jsonForm);
                    return;
                }
                Intent intent = new Intent(this, Utils.metadata().familyFormActivity);
                intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
                Form form = new Form();
                if (!HnppConstants.isReleaseBuild()) {
                    form.setActionBarBackground(R.color.test_app_color);

                } else {
                    form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                }
                form.setWizard(false);

                intent.putExtra("form", form);
                this.startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
            } else {
                if (HnppConstants.isPALogin()) {
                    openAsReadOnlyMode(jsonForm);
                    return;
                }
                HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
                    @Override
                    public void onPost(double latitude, double longitude) {
                        try {
                            Intent intent = new Intent(HouseHoldVisitActivity.this, Utils.metadata().familyMemberFormActivity);
                            HnppJsonFormUtils.updateLatitudeLongitude(jsonForm, latitude, longitude);
                            intent.putExtra("json", jsonForm.toString());
                            intent.putExtra("json", jsonForm.toString());
                            Form form = new Form();
                            if (!HnppConstants.isReleaseBuild()) {
                                form.setActionBarBackground(R.color.test_app_color);

                            } else {
                                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                            }
                            form.setWizard(false);
                            intent.putExtra("form", form);
                            startActivityForResult(intent, 2244);
                        } catch (Exception e) {

                        }

                    }
                });

            }
        } catch (Exception e) {

        }
    }

    private void openAsReadOnlyMode(JSONObject jsonForm) {
        Intent intent = new Intent(this, HnppFormViewActivity.class);
        intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        Form form = new Form();
        form.setWizard(false);
        if (!HnppConstants.isReleaseBuild()) {
            form.setActionBarBackground(R.color.test_app_color);

        } else {
            form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

        }
        form.setHideSaveLabel(true);
        form.setSaveLabel("");
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
        intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, false);
        if (this != null) {
            this.startActivity(intent);
        }
    }

    @Override
    public void refreshMemberList(FetchStatus fetchStatus) {
        ///
    }
}