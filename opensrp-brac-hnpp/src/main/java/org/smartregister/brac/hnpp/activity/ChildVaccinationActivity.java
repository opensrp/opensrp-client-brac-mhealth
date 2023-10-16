package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.fragment.ChildImmunizationFragment;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.listener.ServiceActionListener;
import org.smartregister.immunization.listener.VaccinationActionListener;
import org.smartregister.view.activity.SecuredActivity;
import java.util.ArrayList;

public class ChildVaccinationActivity extends SecuredActivity implements VaccinationActionListener, ServiceActionListener {

    private static final String INTENT_BUNDLE =" intent_bundle";
    private static final String INTENT_COMMONOBJECT ="intent_common_object";
    public static final int VACCINE_REQUEST_CODE = 10000;
    public static final int VACCINE_RESULT_CODE = 51211;

    private CommonPersonObjectClient childDetails;
    private Bundle bundle;
    private boolean isActionTaken;

    public boolean isOnlyService = false;
    public boolean isReadOnly = false;

    public static void startChildVaccinationActivity(Activity activity, Bundle bundle , CommonPersonObjectClient childDetails){

        Intent intent = new Intent(activity,ChildVaccinationActivity.class);
        intent.putExtra(INTENT_BUNDLE,bundle);
        intent.putExtra(INTENT_COMMONOBJECT,childDetails);
        activity.startActivityForResult(intent,VACCINE_REQUEST_CODE);
    }

    public static void startChildVaccinationActivity(Activity activity, Bundle bundle , CommonPersonObjectClient childDetails,boolean isOnlyVacc){

        Intent intent = new Intent(activity,ChildVaccinationActivity.class);
        intent.putExtra(INTENT_BUNDLE,bundle);
        intent.putExtra(INTENT_COMMONOBJECT,childDetails);
        intent.putExtra(ChildFollowupActivity.IS_ONLY_SERVICE,isOnlyVacc);
        activity.startActivityForResult(intent,VACCINE_REQUEST_CODE);
    }

    public static void startChildVaccinationActivity(Activity activity, Bundle bundle, CommonPersonObjectClient childDetails, boolean isOnlyVacc, boolean isReadOnly){

        Intent intent = new Intent(activity,ChildVaccinationActivity.class);
        intent.putExtra(INTENT_BUNDLE,bundle);
        intent.putExtra(INTENT_COMMONOBJECT,childDetails);
        intent.putExtra(ChildFollowupActivity.IS_ONLY_SERVICE,isOnlyVacc);
        intent.putExtra(ChildFollowupActivity.IS_READ_ONLY,isReadOnly);
        activity.startActivityForResult(intent,VACCINE_REQUEST_CODE);
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_child_immunization);
        setUpToolbar();
        bundle = getIntent().getParcelableExtra(INTENT_BUNDLE);
        childDetails = (CommonPersonObjectClient) getIntent().getSerializableExtra(INTENT_COMMONOBJECT);
        isOnlyService = getIntent().getBooleanExtra(ChildFollowupActivity.IS_ONLY_SERVICE,false);
        isReadOnly = getIntent().getBooleanExtra(ChildFollowupActivity.IS_READ_ONLY,false);
        initializeFragment();
    }
    private void setUpToolbar(){
        if(!HnppConstants.isReleaseBuild()){
            findViewById(R.id.action_bar).setBackgroundResource(R.color.test_app_color);

        }else{
            findViewById(R.id.action_bar).setBackgroundResource(org.smartregister.family.R.color.customAppThemeBlue);

        }
        findViewById(R.id.backBtn).setOnClickListener(v -> onBackPressed());

    }

    @Override
    protected void onResumption() {

    }
    ChildImmunizationFragment immunizationFragment;
    private void initializeFragment(){
        immunizationFragment = ChildImmunizationFragment.newInstance(bundle);
        immunizationFragment.setChildDetails(childDetails);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container, immunizationFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onGiveToday(ServiceWrapper serviceWrapper, View view) {
        isActionTaken = true;
        immunizationFragment.onGiveToday(serviceWrapper,view);
    }

    @Override
    public void onGiveEarlier(ServiceWrapper serviceWrapper, View view) {
        isActionTaken = true;
        immunizationFragment.onGiveEarlier(serviceWrapper,view);
    }

    @Override
    public void onUndoService(ServiceWrapper serviceWrapper, View view) {
        isActionTaken = true;
        immunizationFragment.onUndoService(serviceWrapper,view);
    }

    @Override
    public void onVaccinateToday(ArrayList<VaccineWrapper> arrayList, View view) {
        isActionTaken = true;
        immunizationFragment.onVaccinateToday(arrayList,view);
    }

    @Override
    public void onVaccinateEarlier(ArrayList<VaccineWrapper> arrayList, View view) {
        isActionTaken = true;
        immunizationFragment.onVaccinateEarlier(arrayList,view);
    }

    @Override
    public void onUndoVaccination(VaccineWrapper vaccineWrapper, View view) {
        isActionTaken = true;
        immunizationFragment.onUndoVaccination(vaccineWrapper,view);
    }

    @Override
    public void onBackPressed() {

        Intent intent = getIntent();
        intent.putExtra("VACCINE_TAKEN", isActionTaken);
        setResult(VACCINE_RESULT_CODE, intent);
        finish();
    }
}
