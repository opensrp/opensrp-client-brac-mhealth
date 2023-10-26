package org.smartregister.brac.hnpp.activity;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.ForumHistoryAdapter;
import org.smartregister.brac.hnpp.model.ForumDetails;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;

public class ForumHistoryActivity extends SecuredActivity {
    private RecyclerView recyclerView;
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_forum_history);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        recyclerView = findViewById(R.id.recycler_view);
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        fetchPreviousForum();

    }
    public void fetchPreviousForum(){

        ArrayList<ForumDetails> forumDetailsArrayList = HnppDBUtils.getPreviousForum();
        ForumHistoryAdapter adapter = new ForumHistoryAdapter(this, new ForumHistoryAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, ForumDetails content) {

                ForumHistoryDetailsActivity.startForumHistoryDetailsActivity(ForumHistoryActivity.this,content);


            }
        });
        adapter.setData(forumDetailsArrayList);
        recyclerView.setAdapter(adapter);


    }

    @Override
    protected void onResumption() {

    }
}
