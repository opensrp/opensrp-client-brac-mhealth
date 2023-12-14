package org.smartregister.brac.hnpp.activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.PaymentContract;
import org.smartregister.brac.hnpp.interactor.PaymentDetailsInteractor;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;

public class BkashActivity extends SecuredActivity implements View.OnClickListener{
    WebView wvBkashPayment;
    ProgressBar progressBar;
    private String url;
    private String trnsactionId;
    private Handler myHandler;

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_bkash);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        findViewById(R.id.backBtn).setOnClickListener(this);
        myHandler = new Handler();
        wvBkashPayment = findViewById(R.id.wvBkashPayment);
        progressBar = findViewById(R.id.progressBar);
        url = getIntent().getStringExtra("url");
        trnsactionId = getIntent().getStringExtra("trxId");
        WebSettings webSettings = wvBkashPayment.getSettings();
        webSettings.setJavaScriptEnabled(true);

        /*
         * Below part is for enabling webview settings for using javascript and accessing html files and other assets
         */
        wvBkashPayment.setClickable(true);
        wvBkashPayment.getSettings().setDomStorageEnabled(true);
        //wvBkashPayment.getSettings().setAppCacheEnabled(false);
        wvBkashPayment.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        wvBkashPayment.clearCache(true);
        wvBkashPayment.getSettings().setAllowFileAccessFromFileURLs(true);
        wvBkashPayment.getSettings().setAllowUniversalAccessFromFileURLs(true);
        /*
         * To control any kind of interaction from html file
         */
//        wvBkashPayment.addJavascriptInterface(new BkashJavaScriptInterface(BkashActivity.this), "KinYardsPaymentData");

        wvBkashPayment.loadUrl(url);   // api host link .

        wvBkashPayment.setWebViewClient(new CheckoutWebViewClient());
        //}
    }

    @Override
    protected void onResumption() {

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backBtn:
                popupPaymentCancelAlert();
                break;
        }
    }


    private class CheckoutWebViewClient extends WebViewClient {


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("External URL: ", url);
            if (url.equals("https://www.bkash.com/terms-and-conditions")) {
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(myIntent);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            Log.v("STATUS_URL:",url);

            if(url.contains("status=success")){
                String paymentId = HnppConstants.getPaymentIdFromUrl(url);
                progressBar.setVisibility(View.VISIBLE);
                new PaymentDetailsInteractor(new AppExecutors()).executeBKashPayment(paymentId, new PaymentContract.PaymentPostInteractorCallBack() {
                    @Override
                    public void onSuccess(ArrayList<String> responses) {

                    }

                    @Override
                    public void onFail(String message) {
                        progressBar.setVisibility(View.GONE);
                        HnppConstants.showButtonWithImageDialog(BkashActivity.this, 2,message, new Runnable() {
                            @Override
                            public void run() {

                                progressBar.setVisibility(View.VISIBLE);
                                myHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setVisibility(View.GONE);
                                        DFSActivity.startPaymentActivity(BkashActivity.this,true);
                                        finish();
                                    }
                                },2000);

                            }
                        });
                    }

                    @Override
                    public void onSuccess(String message) {
                        HnppConstants.showButtonWithImageDialog(BkashActivity.this, 1,message, new Runnable() {
                            @Override
                            public void run() {

                                progressBar.setVisibility(View.VISIBLE);
                                myHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setVisibility(View.GONE);
                                        DFSActivity.startPaymentActivity(BkashActivity.this,true);
                                        finish();
                                    }
                                },2000);


                            }
                        });

                    }
                });


            }else if(url.contains("status=failure") ){
                HnppConstants.showButtonWithImageDialog(BkashActivity.this, 2,"", new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                        myHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                DFSActivity.startPaymentActivity(BkashActivity.this,true);
                                finish();
                            }
                        },2000);
                    }
                });
            }
            else if(url.contains("status=cancel") ){
                HnppConstants.showButtonWithImageDialog(BkashActivity.this, 3,"", new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        BkashActivity.super.onBackPressed();
                    }
                });
            }else{
                progressBar.setVisibility(View.GONE);
            }

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
