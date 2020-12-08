package org.smartregister.brac.hnpp.activity;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Spinner;

import org.smartregister.brac.hnpp.R;
import org.smartregister.view.activity.SecuredActivity;

public class SkSelectionActivity extends SecuredActivity {

    private RecyclerView recyclerView;
    private Spinner skSpinner,ssSpinner;

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_sk_select);
        recyclerView = findViewById(R.id.recycler_view);
        skSpinner = findViewById(R.id.sk_filter_spinner);
        ssSpinner = findViewById(R.id.ss_filter_spinner);
        findViewById(R.id.add_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        findViewById(R.id.update_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });
        updateSpinner();

    }
    private void updateSpinner(){

    }

    @Override
    protected void onResumption() {

    }
}
