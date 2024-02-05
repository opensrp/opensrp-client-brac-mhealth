package org.smartregister.unicef.mis.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.opensrp.api.constants.Gender;
import org.smartregister.unicef.mis.R;

import java.util.HashMap;
import java.util.Iterator;

public class GMPHeightDialogFragment extends DialogFragment {
    public static final String DIALOG_TAG = "GMPHeightDialogFragment_DIALOG_TAG";
    public static GMPHeightDialogFragment getInstance(Activity activity, Bundle bundle){
        GMPHeightDialogFragment memberHistoryFragment = new GMPHeightDialogFragment();
        Bundle args = bundle;
        if(args == null){
            args = new Bundle();
        }
        memberHistoryFragment.setArguments(args);
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        android.app.Fragment prev = activity.getFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        memberHistoryFragment.show(ft, DIALOG_TAG);
        return memberHistoryFragment;
    }
    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
    private WebView webView;
    private ProgressBar progressBar;
    private HashMap<Integer,Float> heightValues;
    private int currentAge;
    private Gender gender;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_weight_gmp,null);
        webView = view.findViewById(R.id.web_view);
        progressBar = view.findViewById(R.id.client_list_progress);
        view.findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_NoActionBar);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
    }
    public void setHeightValues(HashMap<Integer,Float> weightValues, int currentAge, Gender gender){
        this.heightValues = weightValues;
        this.currentAge = currentAge;
        this.gender = gender;

    }
    @SuppressLint("SetJavaScriptEnabled")
    private void loadData(){
        WebSettings webSettings = webView.getSettings();
        webSettings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webView.setWebViewClient(new CheckoutWebViewClient());
        webView.loadDataWithBaseURL("file:///android_asset/", getHtmlText(), "text/html", "UTF-8", null);

    }

    private String getHtmlText(){
        StringBuilder builder = new StringBuilder();
        Iterator<Integer> it = heightValues.keySet().iterator();       //keyset is a method
        while(it.hasNext())
        {
            int key=(int)it.next();
            if(builder.length()>0)builder.append(",");
            builder.append("{");
            builder.append("x:").append(key).append(",y:").append(heightValues.get(key)).append("}");
            Log.v("GMP_WEIGHT","Key: "+key+"     Value: "+ heightValues.get(key));
        }
        Log.v("GMP_WEIGHT","getHtmlText>>"+builder);
//        String functionByAge = currentAge<=23?"gmp.girl_height_gain_chart_0_24(child);":"gmp.girl_height_gain_chart_24_60(child) (child);";
        String functionByAge = gender.name().equalsIgnoreCase(Gender.FEMALE.name())?"gmp.girl_height_gain_chart_0_60(child);":"gmp.boy_height_gain_chart_0_60(child);";

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
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
                "        .legend-text{\n" +
                "            color: #410404 !important;\n" +
                "            font-size: 12px;\n" +
                "        }\n" +
                "\n" +
                "        .chart-text{\n" +
                "            font-size: 12px;\n" +
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
                "<script src=\"gmp.1.0.3.min.js\"></script>\n" +
                "\n" +
                "<script>\n" +
                "    var child = [\n" +builder+
//                "                {x:0  , y: 45 },  \n" +
//                "                {x:1  , y: 50 },\n" +
//                "                {x:2 , y: 55},\n" +
//                "                {x:3  , y: 52},\n" +
//                "                {x:4  , y: 55 },\n" +
//                "                {x:5  , y: 57},\n" +
//                "                {x:6  , y: 58},\n" +
//                "                {x:7  , y: 59},\n" +
//                "                {x:8  , y: 60},\n" +
//                "                {x:9  , y: 70},\n" +
//                "                {x:10  , y: 72},\n" +
//                "               \n" +
                "            ];\n" +
                "   \n" +
                "\n" +
                "gmp = new GMPChart(\"svg-container\");\n" +
                "gmp.chart_stroke_width = 4;\n" +
                "gmp.chart_stroke_color = 'blue';"+
                "\n" +functionByAge+
                "//gmp.draw_chart(child);"+
                "\n" +
                "//Wight Gain chart 24-60 Months. Uncoment bellow line for example\n" +
                "//gmp.girl_weight_gain_chart_24_60(child);\n" +
                "\n" +
                "//height Gain chart 0-24 Months. Uncoment bellow line for example\n" +
                "//gmp.girl_height_gain_chart_0_24(child);\n" +
                "\n" +
                "//Height Gain chart 24-60 Months. Uncoment bellow line for example\n" +
                "//gmp.girl_height_gain_chart_24_60(child);\n" +
                "</script>\n" +
                "\n" +
                "</body>\n" +
                "</html> ";

//
//        return "<html>\n" +
//                "\n" +
//                "<head>\n" +
//                "    <style>\n" +
//                "        .svg-container {\n" +
//                "            width: 700px;\n" +
//                "            height: 500px;\n" +
//                "            resize: both;\n" +
//                "            overflow: auto;\n" +
//                "            border: 1px dashed #aaa;\n" +
//                "            }\n" +
//                "\n" +
//                "            svg {\n" +
//                "            width: 100%;\n" +
//                "            height: 100%;\n" +
//                "            }\n" +
//                "\n" +
//                "        .bangla-text {\n" +
//                "            font-size: 12px;\n" +
//                "        }\n" +
//                "\n" +
//                "        .base-point{\n" +
//                "           fill: rgb(200, 0, 255);\n" +
//                "        }\n" +
//                "\n" +
//                "    </style>\n" +
//                "\n" +
//                "    \n" +
//                "</head>\n" +
//                "<body>\n" +
//                "\n" +
//                "<div class=\"svg-container\">\n" +
//                "    <svg viewBox=\"0 0 700 500\" id=\"svg-container\">\n" +
//                "       \n" +
//                "    </svg>\n" +
//                "</div>\n" +
//                "\n" +
//                "\n" +
//                "\n" +
//                "\n" +
//                "<script type=\"text/javascript\" src=\"gmp.min_1.js\" ></script>\n" +
//                "\n" +
//                "<script>\n" +
//                "    var child = [\n" +builder+
////                "                {x:0  , y: 2.9 },\n" +
////                "                {x:1  , y: 3 },\n" +
////                "                {x:2 , y: 3.1},\n" +
////                "                {x:3  , y: 3.2 },\n" +
////                "                {x:4  , y: 3.5 },\n" +
////                "                {x:5  , y: 3.8},\n" +
////                "                {x:6  , y: 4},\n" +
////                "                {x:7  , y: 4.3},\n" +
////                "                {x:8  , y: 4.5},\n" +
////                "                {x:9  , y: 5},\n" +
////                "                {x:10  , y: 5.1},\n" +
////                "                {x:11  , y: 5.5},\n" +
////                "                {x:12  , y: 6},\n" +
////                "                {x:13  , y: 6.1},\n" +
////                "                {x:14  , y: 6.3},\n" +
////                "                {x:15  , y: 6.5},\n" +
////                "                {x:16  , y: 6.7},\n" +
////                "                {x:17  , y: 7},\n" +
////                "                {x:18  , y: 7.2},\n" +
////                "                {x:19  , y: 7.4}\n" +
//                "            ];\n" +
//                "\n" +
//                "\n" +
//                "gmp = new GMPChart(\"svg-container\");\n" +
//                "\n" +
//                "gmp.draw_chart(child);\n" +
//                "</script>\n" +
//                "\n" +
//                "</body>\n" +
//                "</html> \n";
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
