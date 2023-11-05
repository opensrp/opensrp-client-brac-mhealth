package org.smartregister.brac.hnpp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;

public class TermAndConditionWebView extends SecuredActivity {
    private ProgressBar progressBar;
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_bkash);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        ((TextView)findViewById(R.id.textview_detail_two)).setText(R.string.terms_condition);
        ((WebView)findViewById(R.id.wvBkashPayment)).loadUrl("file:///android_asset/term_condition.html");
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        progressBar = findViewById(R.id.progressBar);
        ((WebView)findViewById(R.id.wvBkashPayment)).setWebViewClient(new CheckoutWebViewClient());

    }

    @Override
    protected void onResumption() {

    }
    private class CheckoutWebViewClient extends WebViewClient {


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            progressBar.setVisibility(View.GONE);

        }

    }
}
