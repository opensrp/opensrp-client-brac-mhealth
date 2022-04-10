package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.ForumHistoryAdapter;
import org.smartregister.brac.hnpp.adapter.SurveyHistoryAdapter;
import org.smartregister.brac.hnpp.model.ForumDetails;
import org.smartregister.brac.hnpp.model.Survey;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;

public class SurveyHistoryActivity extends SecuredActivity {
    public static final String EXTRA_BASE_ENTITY = "base_entity";
    public static final String TYPE = "type";

    public static void startSurveyHistoryActivity(Activity activity,String type, String baseEntityId){
        Intent intent  = new Intent(activity,SurveyHistoryActivity.class);
        intent.putExtra(EXTRA_BASE_ENTITY,baseEntityId);
        intent.putExtra(TYPE,type);
        activity.startActivity(intent);
    }
    private RecyclerView recyclerView;
    String baseEntityId,type;
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_survey_history);
        baseEntityId = getIntent().getStringExtra(EXTRA_BASE_ENTITY);
        type = getIntent().getStringExtra(TYPE);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        recyclerView = findViewById(R.id.recycler_view);
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        fetchPreviousHistory();

    }
    public void fetchPreviousHistory(){

        ArrayList<Survey> surveyArrayList = HnppApplication.getSurveyHistoryRepository().getSurveyList(baseEntityId);
        SurveyHistoryAdapter adapter = new SurveyHistoryAdapter(this, new SurveyHistoryAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, Survey content) {
                JSONObject mmObj = HnppConstants.viewSurveyForm(type,content.formId,content.uuid,baseEntityId);
                Intent intent = HnppConstants.viewModeSurveyApp(mmObj.toString());
                startActivityForResult(intent, HnppConstants.SURVEY_KEY.VIEW_SURVEY_REQUEST_CODE);
            }
        });
        adapter.setData(surveyArrayList);
        recyclerView.setAdapter(adapter);


    }

    @Override
    protected void onResumption() {

    }
}
