package org.smartregister.unicef.dghs.activity;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import org.smartregister.growthmonitoring.domain.HeightWrapper;
import org.smartregister.growthmonitoring.domain.MUACWrapper;
import org.smartregister.growthmonitoring.domain.WeightWrapper;
import org.smartregister.growthmonitoring.listener.HeightActionListener;
import org.smartregister.growthmonitoring.listener.MUACActionListener;
import org.smartregister.growthmonitoring.listener.WeightActionListener;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.fragment.ChildImmunizationFragment;
import org.smartregister.unicef.dghs.fragment.GMPFragment;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.listener.ServiceActionListener;
import org.smartregister.immunization.listener.VaccinationActionListener;
import org.smartregister.view.activity.SecuredActivity;
import java.util.ArrayList;

public class ChildGMPActivity extends SecuredActivity implements WeightActionListener, HeightActionListener, MUACActionListener {

    private static final String INTENT_BUNDLE =" intent_bundle";
    private static final String INTENT_COMMONOBJECT ="intent_common_object";
    public static final int VACCINE_REQUEST_CODE = 10000;

    private CommonPersonObjectClient childDetails;
    private Bundle bundle;
    private boolean isActionTaken;

    public static void startGMPActivity(Activity activity, Bundle bundle , CommonPersonObjectClient childDetails){

        Intent intent = new Intent(activity,ChildGMPActivity.class);
        intent.putExtra(INTENT_BUNDLE,bundle);
        intent.putExtra(INTENT_COMMONOBJECT,childDetails);
        activity.startActivityForResult(intent,VACCINE_REQUEST_CODE);


    }
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_child_immunization);
        setUpToolbar();
        bundle = getIntent().getParcelableExtra(INTENT_BUNDLE);
        childDetails = (CommonPersonObjectClient) getIntent().getSerializableExtra(INTENT_COMMONOBJECT);
        initializeFragment();
    }
    private void setUpToolbar(){
        if(!HnppConstants.isReleaseBuild()){
            findViewById(R.id.action_bar).setBackgroundResource(R.color.test_app_color);

        }else{
            findViewById(R.id.action_bar).setBackgroundResource(org.smartregister.family.R.color.customAppThemeBlue);

        }
        ((TextView)findViewById(R.id.textview_detail_two)).setText(getString(R.string.gmp));
        findViewById(R.id.backBtn).setOnClickListener(v -> onBackPressed());

    }

    @Override
    protected void onResumption() {

    }
    GMPFragment gmpFragment;
    private void initializeFragment(){
        gmpFragment = GMPFragment.newInstance(bundle);
        gmpFragment.setChildDetails(childDetails);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container, gmpFragment);
        fragmentTransaction.commit();
    }
    @Override
    public void onBackPressed() {

        if(isActionTaken){
            Intent intent = getIntent();
            intent.putExtra("GMP_TAKEN",true);
            setResult(RESULT_OK, intent);
            finish();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onHeightTaken(HeightWrapper heightWrapper) {
        if(gmpFragment!=null){
            gmpFragment.onHeightTaken(heightWrapper);
        }
        isActionTaken = true;
    }

    @Override
    public void onMUACTaken(MUACWrapper muacWrapper) {
        if(gmpFragment!=null){
            gmpFragment.onMUACTaken(muacWrapper);
        }
        isActionTaken = true;
    }

    @Override
    public void onWeightTaken(WeightWrapper weightWrapper) {
        if(gmpFragment!=null){
            gmpFragment.onWeightTaken(weightWrapper);
        }
        isActionTaken = true;
    }

}