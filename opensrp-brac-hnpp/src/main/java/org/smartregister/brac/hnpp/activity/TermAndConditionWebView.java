package org.smartregister.brac.hnpp.activity;

import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;

public class TermAndConditionWebView extends SecuredActivity {
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_bkash);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        ((TextView)findViewById(R.id.textview_detail_two)).setText(R.string.terms_condition);
        //((WebView)findViewById(R.id.wvBkashPayment)).loadUrl("file:///android_asset/term_condition.html");
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onResumption() {

    }
}
