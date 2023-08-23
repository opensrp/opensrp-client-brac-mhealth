package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.fragment.GMPFragment;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.growthmonitoring.domain.HeightWrapper;
import org.smartregister.growthmonitoring.domain.MUACWrapper;
import org.smartregister.growthmonitoring.domain.WeightWrapper;
import org.smartregister.growthmonitoring.listener.HeightActionListener;
import org.smartregister.growthmonitoring.listener.MUACActionListener;
import org.smartregister.growthmonitoring.listener.WeightActionListener;
import org.smartregister.view.activity.SecuredActivity;

public class ChildGMPActivity extends SecuredActivity implements WeightActionListener, HeightActionListener, MUACActionListener {

    private static final String INTENT_BUNDLE =" intent_bundle";
    private static final String INTENT_COMMONOBJECT ="intent_common_object";
    private static final String INTENT_IS_READ_ONLY ="intent_is_read_only";
    public static final int VACCINE_REQUEST_CODE = 10000;
    public static final int GMP_RESULT_CODE = 12121;

    private CommonPersonObjectClient childDetails;
    private Bundle bundle;
    private boolean isActionTaken;
    public boolean isReadOnly = false;

    public static void startGMPActivity(Activity activity, Bundle bundle , CommonPersonObjectClient childDetails){
        Intent intent = new Intent(activity,ChildGMPActivity.class);
        intent.putExtra(INTENT_BUNDLE,bundle);
        intent.putExtra(INTENT_COMMONOBJECT,childDetails);
        activity.startActivityForResult(intent,VACCINE_REQUEST_CODE);
    }
    public static void startGMPActivity(Activity activity, Bundle bundle , CommonPersonObjectClient childDetails,boolean isReadOnly){
        Intent intent = new Intent(activity,ChildGMPActivity.class);
        intent.putExtra(INTENT_BUNDLE,bundle);
        intent.putExtra(INTENT_COMMONOBJECT,childDetails);
        intent.putExtra(INTENT_IS_READ_ONLY,isReadOnly);
        activity.startActivityForResult(intent,VACCINE_REQUEST_CODE);
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_child_immunization);
        setUpToolbar();
        bundle = getIntent().getParcelableExtra(INTENT_BUNDLE);
        isReadOnly = getIntent().getBooleanExtra(INTENT_IS_READ_ONLY,false);
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
        gmpFragment = GMPFragment.newInstance(bundle,isReadOnly);
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
            setResult(GMP_RESULT_CODE, intent);
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
