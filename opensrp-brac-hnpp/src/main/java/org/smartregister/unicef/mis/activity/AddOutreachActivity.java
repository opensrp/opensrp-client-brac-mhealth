package org.smartregister.unicef.mis.activity;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;

public class AddOutreachActivity extends SecuredActivity implements View.OnClickListener{

    protected Spinner unionSpinner,wardSpinner,blockSpinner,centerTypeSpinner;
    protected EditText outreachNameTxt,addressTxt,mobileTxt,latitudeTxt,longitudeTxt;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_add_outreach);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar_layout));
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.manual_btn).setOnClickListener(this);
        findViewById(R.id.auto_btn).setOnClickListener(this);
        findViewById(R.id.manual_btn).setOnClickListener(this);
        findViewById(R.id.submit_btn).setOnClickListener(this);
        initUi();
    }
    private void initUi(){
        unionSpinner = findViewById(R.id.union_spinner);
        wardSpinner = findViewById(R.id.ward_spinner);
        blockSpinner = findViewById(R.id.block_spinner);
        centerTypeSpinner = findViewById(R.id.center_type_spinner);
        outreachNameTxt = findViewById(R.id.outreach_name);
        addressTxt = findViewById(R.id.address);
        mobileTxt = findViewById(R.id.mobile);
        latitudeTxt = findViewById(R.id.latitude_edit);
        longitudeTxt = findViewById(R.id.longitude_edit);
    }
    @Override
    protected void onResumption() {

    }
    private void loadLocation(){

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backBtn:
                finish();
                break;
            case R.id.fab_add_outreach:
                GlobalSearchActivity.startMigrationFilterActivity(AddOutreachActivity.this,HnppConstants.MIGRATION_TYPE.Member.name());
                //showDetailsDialog(HnppConstants.MIGRATION_TYPE.Member.name());
                break;
        }
    }
}