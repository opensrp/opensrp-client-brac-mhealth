package org.smartregister.brac.hnpp.activity;

import static org.smartregister.brac.hnpp.utils.HnppJsonFormUtils.makeReadOnlyFields;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.ForumHistoryAdapter;
import org.smartregister.brac.hnpp.adapter.PAHistoryAdapter;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.model.ForumDetails;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;
import java.util.List;

public class PANewHistoryActivity extends SecuredActivity {
    private RecyclerView recyclerView;
    private AppExecutors appExecutors;
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_pa_history);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        recyclerView = findViewById(R.id.recycler_view);
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        appExecutors = new AppExecutors();
        fetchPreviousForum();

    }
    public void fetchPreviousForum(){
        Runnable runnable = () -> {
            ArrayList<Visit> visits = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getPAVisit();
            appExecutors.mainThread().execute(() -> updateAdapter(visits));
        };
        appExecutors.diskIO().execute(runnable);

    }

    private void updateAdapter(ArrayList<Visit>visits) {
        PAHistoryAdapter adapter = new PAHistoryAdapter(this, (position, content) -> {

            JSONObject jsonForm = HnppJsonFormUtils.getVisitFormWithData(content.getJson(),PANewHistoryActivity.this);
            Intent intent = new Intent(PANewHistoryActivity.this, HnppFormViewActivity.class);
            makeReadOnlyFields(jsonForm);

            try{
                HnppJsonFormUtils.updateFormWithSKSSVillageName(jsonForm);
            }catch (Exception e){
                e.printStackTrace();
            }
            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
            Form form = new Form();
            form.setWizard(false);
            if(!HnppConstants.isReleaseBuild()){
                form.setActionBarBackground(R.color.test_app_color);

            }else{
                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

            }
            form.setHideSaveLabel(true);
            form.setSaveLabel("");
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
            intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, false);
            startActivity(intent);


        });
        adapter.setData(visits);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResumption() {

    }

}
