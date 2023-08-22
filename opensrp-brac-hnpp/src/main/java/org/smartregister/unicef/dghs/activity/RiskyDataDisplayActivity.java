package org.smartregister.unicef.dghs.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.domain.db.Client;
import org.smartregister.domain.db.Event;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.adapter.InvalidDataAdapter;
import org.smartregister.unicef.dghs.adapter.RiskyDataAdapter;
import org.smartregister.unicef.dghs.model.InvalidDataModel;
import org.smartregister.unicef.dghs.utils.FormApplicability;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.RiskyModel;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class RiskyDataDisplayActivity extends SecuredActivity {
    public static final String BASE_ENTITY_ID ="baseEntityId";
    private RecyclerView recyclerView;
    private TextView countTv;
    private AppExecutors appExecutors;
    private String baseEntityId;
    private ArrayList<RiskyModel> riskyDataModels = new ArrayList<>();
    public static void startInvalidActivity(String baseEntityId, Activity activity){
        Intent intent = new Intent(activity, RiskyDataDisplayActivity.class);
        intent.putExtra(BASE_ENTITY_ID,baseEntityId);
        activity.startActivity(intent);
    }
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_risky_details);
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        recyclerView = findViewById(R.id.recycler_view);
        countTv = findViewById(R.id.count_tv);
        this.baseEntityId = getIntent().getStringExtra(BASE_ENTITY_ID);

        appExecutors = new AppExecutors();
        showProgressBar(true);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        loadRiskyDate();

    }
    private void loadRiskyDate(){
        Runnable runnable = () -> {
            riskyDataModels = HnppApplication.getRiskDetailsRepository().getRiskyKeyByEntityId(baseEntityId);

            for (RiskyModel riskyModel:riskyDataModels) {
                StringBuilder builder = new StringBuilder();
                String[] fs= riskyModel.riskyKey.split(",");
                if(fs.length>0){
                    for (String key:fs) {
                        Log.v("RISK_FACTOR","key>>"+key+":value:"+riskyModel.riskyValue);
                        if(builder.length()>0){
                            builder.append(",") ;
                        }
                        builder.append(HnppConstants.getRiskeyFactorMapping().get(key)==null?key:HnppConstants.getRiskeyFactorMapping().get(key));
                    }
                }else{
                    Log.v("RISK_FACTOR","key>>"+riskyModel.riskyKey+":value:"+riskyModel.riskyValue);
                    builder.append(HnppConstants.getRiskeyFactorMapping().get(riskyModel.riskyKey)==null?riskyModel.riskyKey:HnppConstants.getRiskeyFactorMapping().get(riskyModel.riskyKey));
                }
                StringBuilder builderValue = new StringBuilder();
                String[] rV= riskyModel.riskyValue.split(",");
                if(rV.length>0){
                    for (String value:rV) {
                        Log.v("RISK_FACTOR","value>>"+value+":values:"+rV);
                        if(builderValue.length()>0){
                            builderValue.append(",") ;
                        }
                        builderValue.append(HnppConstants.getRiskeyFactorMapping().get(value)==null?value.replace("_"," "):HnppConstants.getRiskeyFactorMapping().get(value));
                    }
                }else{
                    Log.v("RISK_FACTOR","key>>"+riskyModel.riskyKey+":value:"+riskyModel.riskyValue);
                    builderValue.append(HnppConstants.getRiskeyFactorMapping().get(riskyModel.riskyValue)==null?riskyModel.riskyValue:HnppConstants.getRiskeyFactorMapping().get(riskyModel.riskyValue));
                }
                riskyModel.riskyKey = builder.toString();
                riskyModel.riskyValue = builderValue.toString();
                if(riskyModel.eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_HOME_VISIT)){
                    riskyModel.eventType = FormApplicability.getANCTitleForHistory(riskyModel.ancCount);
                }else if(riskyModel.eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.PNC_REGISTRATION)){
                    riskyModel.eventType = FormApplicability.getPNCTitleForHistory(riskyModel.ancCount);
                }

                riskyModel.date = HnppConstants.DDMMYY.format(riskyModel.visitDate);
            }
            appExecutors.mainThread().execute(this::updateAdapter);
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void showProgressBar(boolean isVisible){
        findViewById(R.id.progress_bar).setVisibility(isVisible? View.VISIBLE:View.GONE);
    }
    RiskyDataAdapter adapter;
    private void updateAdapter(){
        showProgressBar(false);
        adapter = new RiskyDataAdapter(this);
        adapter.setData(riskyDataModels);
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onResumption() {

    }
}
