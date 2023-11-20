package org.smartregister.unicef.mis.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.adapter.OutreachAdapter;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.OutreachContentData;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;

public class OutreachActivity extends SecuredActivity implements View.OnClickListener{

    private RecyclerView recyclerView;
    private OutreachAdapter adapter;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_outreach);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar_layout));
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.fab_add_outreach).setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_view);

    }


    @Override
    protected void onResumption() {
        ArrayList<OutreachContentData> outreachList = HnppApplication.getOutreachRepository().getAllOutreachData();
        if(outreachList.size()>0){
            adapter = new OutreachAdapter(this, new OutreachAdapter.OnClickAdapter() {
                @Override
                public void onViewClick(int position, OutreachContentData content) {

                }

                @Override
                public void onEditClick(int position, OutreachContentData content) {
                    AddOutreachActivity.startAddOutreachActivity(OutreachActivity.this,content);
                }
            });
            adapter.setData(outreachList);
            recyclerView.setAdapter(adapter);

        }


    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backBtn:
                finish();
                break;
            case R.id.fab_add_outreach:
                AddOutreachActivity.startAddOutreachActivity(OutreachActivity.this,null);
                break;
        }
    }
}