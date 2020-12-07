package org.smartregister.brac.hnpp.activity;

import android.support.v7.widget.RecyclerView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.view.activity.SecuredActivity;

public class SkSelectionActivity extends SecuredActivity {

    private RecyclerView recyclerView;
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_sk_select);
        recyclerView = findViewById(R.id.recycler_view);

    }

    @Override
    protected void onResumption() {

    }
}
