package org.smartregister.brac.hnpp.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;

public class ForumActivity extends SecuredActivity implements View.OnClickListener {
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_forum);
        if(HnppConstants.isPALogin()){
            findViewById(R.id.kishori_forum).setVisibility(View.GONE);
            findViewById(R.id.child_forum).setVisibility(View.GONE);
            findViewById(R.id.nari_forum).setVisibility(View.GONE);
            findViewById(R.id.ncd_forum).setVisibility(View.GONE);
            findViewById(R.id.adult_forum).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.adult_forum).setVisibility(View.GONE);
        }
        findViewById(R.id.kishori_forum).setOnClickListener(this);
        findViewById(R.id.child_forum).setOnClickListener(this);
        findViewById(R.id.nari_forum).setOnClickListener(this);
        findViewById(R.id.ncd_forum).setOnClickListener(this);
        findViewById(R.id.adult_forum).setOnClickListener(this);
        findViewById(R.id.history_forum).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);

        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));

    }

    @Override
    protected void onResumption() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.kishori_forum:
                ForumDetailsActivity.startDetailsActivity(this, HnppConstants.SEARCH_TYPE.ADO.toString(),getString(R.string.title_kishori));
                break;
            case R.id.child_forum:
                ForumDetailsActivity.startDetailsActivity(this, HnppConstants.SEARCH_TYPE.CHILD.toString(),getString(R.string.title_child));
                break;
            case R.id.nari_forum:
                ForumDetailsActivity.startDetailsActivity(this, HnppConstants.SEARCH_TYPE.WOMEN.toString(),getString(R.string.title_nari));

                break;
            case R.id.ncd_forum:
                ForumDetailsActivity.startDetailsActivity(this, HnppConstants.SEARCH_TYPE.NCD.toString(),getString(R.string.title_ncd));

                break;
            case R.id.adult_forum:
                ForumDetailsActivity.startDetailsActivity(this, HnppConstants.SEARCH_TYPE.ADULT.toString(),getString(R.string.title_adult));

                break;
            case R.id.history_forum:
                startActivity(new Intent(this,ForumHistoryActivity.class));
                break;
            case R.id.backBtn:
                finish();
                break;
        }

    }
}
