package org.smartregister.unicef.dghs.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;

public class WebViewActivity extends SecuredActivity {
    private ProgressBar progressBar;
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_bkash);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        ((TextView)findViewById(R.id.textview_detail_two)).setText(R.string.show_tika_card);
        String baseEntityId = getIntent().getStringExtra("BASE_ENTITY_ID");
        if(TextUtils.isEmpty(baseEntityId)){
            baseEntityId = "e9c1679c-6651-4bf3-a1ab-a180752ce124-pros";
        }
        String htmlUrl = "http://unicef-ha.mpower-social.com/opensrp-dashboard/people/mobile/"+baseEntityId+"/vaccine-card.html";
        WebView webView = (WebView)findViewById(R.id.wvBkashPayment);
        webView.setVisibility(View.GONE);
//        WebSettings webSettings = webView.getSettings();
//        webSettings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        webView.loadUrl(htmlUrl);
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        progressBar = findViewById(R.id.progressBar);
        ((WebView)findViewById(R.id.wvBkashPayment)).setWebViewClient(new CheckoutWebViewClient());

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void createWebPagePrint(WebView webView) {

        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();
        String jobName = getString(R.string.app_name) + " Document";
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setMediaSize(PrintAttributes.MediaSize.ISO_A5);
        PrintJob printJob = printManager.print(jobName, printAdapter, builder.build());

        if (printJob.isCompleted()) {
            Toast.makeText(getApplicationContext(), "Print completed", Toast.LENGTH_LONG).show();
        } else if (printJob.isFailed()) {
            Toast.makeText(getApplicationContext(), "Fail to print", Toast.LENGTH_LONG).show();
        }
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
