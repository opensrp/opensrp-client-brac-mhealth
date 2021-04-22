package org.smartregister.brac.hnpp.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.PaymentContract;
import org.smartregister.brac.hnpp.interactor.PaymentDetailsInteractor;
import org.smartregister.brac.hnpp.model.PaymentRequest;
import org.smartregister.brac.hnpp.utils.BkashJavaScriptInterface;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;

public class BkashActivity extends AppCompatActivity {
    WebView wvBkashPayment;
    ProgressBar progressBar;
    private String url;
    private String trnsactionId;
    public static final String BKASH_TRANSACTION_ID = "bkash_transaction_id";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bkash);

        wvBkashPayment = findViewById(R.id.wvBkashPayment);
        progressBar = findViewById(R.id.progressBar);
        url = getIntent().getStringExtra("url");
        trnsactionId = getIntent().getStringExtra("trxId");
        CoreLibrary.getInstance().context().allSharedPreferences().savePreference(BKASH_TRANSACTION_ID,trnsactionId);
        WebSettings webSettings = wvBkashPayment.getSettings();
        webSettings.setJavaScriptEnabled(true);

        /*
         * Below part is for enabling webview settings for using javascript and accessing html files and other assets
         */
        wvBkashPayment.setClickable(true);
        wvBkashPayment.getSettings().setDomStorageEnabled(true);
        wvBkashPayment.getSettings().setAppCacheEnabled(false);
        wvBkashPayment.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        wvBkashPayment.clearCache(true);
        wvBkashPayment.getSettings().setAllowFileAccessFromFileURLs(true);
        wvBkashPayment.getSettings().setAllowUniversalAccessFromFileURLs(true);
        /*
         * To control any kind of interaction from html file
         */
        wvBkashPayment.addJavascriptInterface(new BkashJavaScriptInterface(BkashActivity.this), "KinYardsPaymentData");

        wvBkashPayment.loadUrl(url);   // api host link .

        wvBkashPayment.setWebViewClient(new CheckoutWebViewClient());
        //}
    }


    private class CheckoutWebViewClient extends WebViewClient {

        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("External URL: ", url);
           /* if (url.equals("https://www.bkash.com/terms-and-conditions")) {
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(myIntent);
                return true;
            }*/
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar.setVisibility(view.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(view.GONE);

            if(url.contains("status=success") || url.contains("status=failure") || url.contains("status=cancel")){
                //showStatusDialog();
                Intent intent = new Intent(BkashActivity.this,BkashStatusActivity.class);
                startActivity(intent);
            }

           /* String paymentRequest = "{paymentRequest:" + request + "}";
            wvBkashPayment.loadUrl("javascript:callReconfigure(" + paymentRequest + " )");
            // wvBkashPayment.loadUrl("javascript:getAmount(" + orderModel.getSub_total() + " )");
            wvBkashPayment.loadUrl("javascript:clickPayButton()");*/

        }

    }

    @Override
    public void onBackPressed() {
        popupPaymentCancelAlert();  //people can press backBtn and payment may cancel. so alert he really want to cancel payment or not
    }

    private void popupPaymentCancelAlert() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Want to cancel payment process?");
        alert.setCancelable(false);
        alert.setIcon(R.drawable.ic_launcher_background);
        alert.setTitle("Alert!");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(BkashActivity.this, "Payment canceled", Toast.LENGTH_SHORT).show();
                BkashActivity.super.onBackPressed();
            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        final AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }


}
