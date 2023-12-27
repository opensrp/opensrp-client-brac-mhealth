package org.smartregister.unicef.mis.activity;

import android.annotation.SuppressLint;
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

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;

import java.util.List;

public class WebViewActivity extends SecuredActivity {
    private ProgressBar progressBar;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_bkash);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        ((TextView)findViewById(R.id.textview_detail_two)).setText(R.string.show_tika_card);
        String baseEntityId = getIntent().getStringExtra("BASE_ENTITY_ID");
        if(TextUtils.isEmpty(baseEntityId)){
            baseEntityId = "e9c1679c-6651-4bf3-a1ab-a180752ce124-pros";
        }
//        String htmlUrl = "http://unicef-ha.mpower-social.com/opensrp-dashboard/people/mobile/"+baseEntityId+"/vaccine-card.html";
        WebView webView = (WebView)findViewById(R.id.wvBkashPayment);
        WebSettings webSettings = webView.getSettings();
        webSettings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        //webView.loadUrl("file:///android_asset/gmp/index.html");
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        progressBar = findViewById(R.id.progressBar);
        ((WebView)findViewById(R.id.wvBkashPayment)).setWebViewClient(new CheckoutWebViewClient());
        webView.loadDataWithBaseURL("file:///android_asset/", getHtmlText(), "text/html", "UTF-8", null);
    }
    private String getHtmlText(){
       return "<html>\n" +
               "\n" +
               "<head>\n" +
               "    <style>\n" +
               "        .svg-container {\n" +
               "            width: 700px;\n" +
               "            height: 500px;\n" +
               "            resize: both;\n" +
               "            overflow: auto;\n" +
               "            border: 1px dashed #aaa;\n" +
               "            }\n" +
               "\n" +
               "            svg {\n" +
               "            width: 100%;\n" +
               "            height: 100%;\n" +
               "            }\n" +
               "\n" +
               "        .bangla-text {\n" +
               "            font-size: 12px;\n" +
               "        }\n" +
               "\n" +
               "        .base-point{\n" +
               "           fill: rgb(200, 0, 255);\n" +
               "        }\n" +
               "\n" +
               "    </style>\n" +
               "\n" +
               "    \n" +
               "</head>\n" +
               "<body>\n" +
               "\n" +
               "<div class=\"svg-container\">\n" +
               "    <svg viewBox=\"0 0 700 500\" id=\"svg-container\">\n" +
               "       \n" +
               "    </svg>\n" +
               "</div>\n" +
               "\n" +
               "\n" +
               "\n" +
               "\n" +
               "<script type=\"text/javascript\" src=\"gmp.js\" ></script>\n" +
               "\n" +
               "<script>\n" +
               "    var child = [\n" +
               "                {x:0  , y: 2.9 },\n" +
               "                {x:1  , y: 3 },\n" +
               "                {x:2 , y: 3.1},\n" +
               "                {x:3  , y: 3.2 },\n" +
               "                {x:4  , y: 3.5 },\n" +
               "                {x:5  , y: 3.8},\n" +
               "                {x:6  , y: 4},\n" +
               "                {x:7  , y: 4.3},\n" +
               "                {x:8  , y: 4.5},\n" +
               "                {x:9  , y: 5},\n" +
               "                {x:10  , y: 5.1},\n" +
               "                {x:11  , y: 5.5},\n" +
               "                {x:12  , y: 6},\n" +
               "                {x:13  , y: 6.1},\n" +
               "                {x:14  , y: 6.3},\n" +
               "                {x:15  , y: 6.5},\n" +
               "                {x:16  , y: 6.7},\n" +
               "                {x:17  , y: 7},\n" +
               "                {x:18  , y: 7.2},\n" +
               "                {x:19  , y: 7.4}\n" +
               "            ];\n" +
               "\n" +
               "\n" +
               "gmp = new GMPChart(\"svg-container\");\n" +
               "\n" +
               "gmp.draw_chart(child);\n" +
               "</script>\n" +
               "\n" +
               "</body>\n" +
               "</html> \n";
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
    public class CheckoutWebViewClient extends WebViewClient {


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
