package org.smartregister.unicef.mis.utils;

import static org.smartregister.unicef.mis.utils.HnppConstants.KEY.IS_URBAN;
import static org.smartregister.unicef.mis.utils.HnppConstants.KEY.LAST_SYNC_HPV;
import static org.smartregister.unicef.mis.utils.HnppConstants.KEY.LAST_VACCINE_DATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.domain.Response;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.service.HTTPAgent;
import org.smartregister.unicef.mis.BuildConfig;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.activity.FamilyRegisterActivity;
import org.smartregister.unicef.mis.listener.OnGpsDataGenerateListener;
import org.smartregister.unicef.mis.listener.OnPostDataWithGps;
import org.smartregister.unicef.mis.activity.TermAndConditionWebView;
import org.smartregister.unicef.mis.model.Notification;
import org.smartregister.unicef.mis.task.GenerateGPSTask;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.LangUtils;
import org.smartregister.view.activity.BaseProfileActivity;
import org.smartregister.view.activity.SecuredActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Observable;

public class HnppConstants extends CoreConstants {
    public static final String LOCATION_UPDATED = "location_updated";
    public static boolean IS_MANDATORY_GPS = true;
    public static int GPS_ATTEMPT_COUNT = 0;
    public static final int DEFAULT_GPS_ATTEMPT = 1;
    public static final String ACTION_STOCK_COME = "ACTION_STOCK_COME";
    public static final String ACTION_STOCK_END = "ACTION_STOCK_END";
    public static final String ACTION_EDD = "ACTION_EDD";
    public static final String ACTION_LOCATION_UPDATE = "ACTION_LOCATION_UPDATE";
    public static final String EXTRA_STOCK_COME = "EXTRA_STOCK_COME";
    public static final String EXTRA_STOCK_END = "EXTRA_STOCK_END";
    public static final String EXTRA_EDD = "EXTRA_EDD";
    public static final long AUTO_SYNC_DEFAULT_TIME = 60 * 60 * 1000;//1 hr
    public static final long SIX_HOUR = 6*60*60*1000;//6 hr
    public static final long TWENTY_FOUR_HOUR = 23*60*60*1000;//6 hr
    public static final long STOCK_END_DEFAULT_TIME = 6*60*60*1000;//6 hr
    public static final long INVALID_CALL_DEFAULT_TIME = 30*60*1000;//30 mint
    public static final long EDD_DEFAULT_TIME = 6*60*60*1000;//6 hr
    public static final long SURVEY_HISTORY_DEFAULT_TIME = 12*60*60*1000;//6 hr
    public static final String TEST_GU_ID = "test";
    public static final float VERIFY_THRESHOLD = 20;
    public static final String MODULE_ID_TRAINING = "TRAINING";
    public static final int MEMBER_ID_SUFFIX = 11;
    public static final int HOUSE_HOLD_ID_SUFFIX = 9;
    public static final int GUEST_MEMBER_ID_SUFFIX = 5;
    public static final String IS_RELEASE = "is_release_build";
    public static final String IS_DEVICE_VERIFY = "is_device_verify";
    public static final String DEVICE_IMEI = "device_imei";
    public static boolean isSortByLastVisit = false;
    public static boolean isViewRefresh = false;
    public static final String KEY_IS_SAME_MONTH = "is_same_month";
    public static final String KEY_NEED_TO_OPEN = "need_to_open_drawer";
    public static HashMap<String,Boolean> GMPMessage = new HashMap<>();
    public static final String GLOBAL_SEARCH = "global_search";
    public static SimpleDateFormat DDMMYY = new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault());
    public static SimpleDateFormat HHMM = new SimpleDateFormat("HH:mm:ss",Locale.getDefault());
    public static SimpleDateFormat YYYYMM = new SimpleDateFormat("yyyy-MM",Locale.getDefault());
    public static SimpleDateFormat YYMMDD = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());

    public static ArrayList<String> filterTypeList = new ArrayList<String>(
            Arrays.asList(
                    "Today",
                    "Yesterday",
                    "Tomorrow",
                    "This week",
                    "Last week",
                    "Next week",
                    "This month",
                    "Last month",
                    "Next month",
                    "From - To",
                    "Anytime"
            )
    );

    public static Observable<Boolean> postMicroPlanData(){
        return Observable.create(e->{
            try {

                processMicroPlanUnSyncData();
                e.onNext(true);//error
                e.onComplete();
            } catch (Exception ex) {
                ex.printStackTrace();
                e.onNext(false);//error
                e.onComplete();
            }
        });
    }
    private static void processMicroPlanUnSyncData(){
        String ADD_URL = "rest/event/microplan-save";
        ArrayList<MicroPlanEpiData> microPlanEpiDataArrayList = HnppApplication.getMicroPlanRepository().getUnSyncData();

        for(MicroPlanEpiData microPlanEpiData: microPlanEpiDataArrayList){

            try{
                JSONObject request = new JSONObject();
                request.put("outreach_info",JsonFormUtils.gson.toJson(microPlanEpiData.outreachContentData));
                request.put("center_details",JsonFormUtils.gson.toJson(microPlanEpiData.microPlanTypeData));
                request.put("distribution_data",JsonFormUtils.gson.toJson(microPlanEpiData.distributionData));
                request.put("session_plan",JsonFormUtils.gson.toJson(microPlanEpiData.sessionPlanData));//JsonFormUtils.gson.toJson(microPlanEpiData.sessionPlanData));
                request.put("worker_info",JsonFormUtils.gson.toJson(microPlanEpiData.workerData));
                request.put("supervisor_info",JsonFormUtils.gson.toJson(microPlanEpiData.superVisorData));
                request.put("status",microPlanEpiData.microPlanStatus);
                String jsonPayload = request.toString();
                String add_url =  MessageFormat.format("{0}{1}",
                        BuildConfig.opensrp_url_live,
                        ADD_URL);
                Log.v("MICROPLAN_ADD","add_url:"+add_url);

                jsonPayload = jsonPayload.replace("\\","").replace("\"{","{").replace("}\"","}");
               Log.v("MICROPLAN_ADD","jsonPayload>>"+jsonPayload);
                HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();

                Response<String> response = httpAgent.post(add_url, jsonPayload);
                if (response.isFailure() || response.isTimeoutError()) {
                    HnppConstants.appendLog("SYNC_URL", "message>>"+response.payload()+"status:"+response.status().displayValue());
                    return;
                }
                HnppConstants.appendLog("SYNC_URL", "pushECToServer:response comes"+response.payload());
                //{"error":[],"notFound":[]}
                JSONObject results = new JSONObject((String) response.payload());
                if(results.has("success")){
                    HnppApplication.getMicroPlanRepository().updateMicroPlanSyncStatus(microPlanEpiData);
                }



            }catch (Exception e){
                e.printStackTrace();

            }
        }

    }
    public static Observable<String> getAppVersionFromServer() {

        return  Observable.create(e->{
                    try {
                        String baseUrl = CoreLibrary.getInstance().context().
                                configuration().dristhiBaseURL();
                        // Create a URL for the desired page
                        String base_url = HnppApplication.getHNPPInstance().getString(R.string.opensrp_url).replace("opensrp/", "");
                        if (!StringUtils.isEmpty(baseUrl) && baseUrl.contains("opensrp")) {
                            base_url = baseUrl.replace("opensrp/", "");
                        }


                        URL url = new URL(base_url + "opt/multimedia/app-version.txt");
                        Log.v("VERSION_CODE","base_url:"+url);
                        // Read all the text returned by the server
                        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                        String str;
                        str = "";
                        String version_code ="";
                        while ((str = in.readLine()) != null) {
                            // str is one line of text; readLine() strips the newline character(s)
                            version_code += str;
                        }
                        in.close();
                        Log.v("VERSION_CODE","version_code:"+version_code);
                        e.onNext(version_code);//error
                        e.onComplete();
                    } catch (Exception ex){
                        ex.printStackTrace();
                        e.onNext("");//error
                        e.onComplete();
                    }

                }
        );

    }
    public static Observable<String> saveOtherVaccineData(OtherVaccineContentData contentData) {

        return  Observable.create(e->{
                    try {
                        HnppApplication.getOtherVaccineRepository().addOtherVaccine(contentData);
                        e.onNext("done");//error
                        e.onComplete();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        e.onNext("");//error
                        e.onComplete();
                    }

                }
        );

    }
    public static Observable<String> postOtherVaccineData() {

        return  Observable.create(e->{
                    try {

                        processOtherVaccineUnSyncData(0);

                        e.onNext("done");//error
                        e.onComplete();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        e.onNext("");//error
                        e.onComplete();
                    }

                }
        );

    }
    public static Observable<String> forseHPVVaccineData() {

        return  Observable.create(e->{
                    try {

                        forseSyncHpvData(0);
                        e.onNext("done");//error
                        e.onComplete();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        e.onNext("");//error
                        e.onComplete();
                    }

                }
        );

    }
    public static Observable<String> sendOtherVaccineSingleData(OtherVaccineContentData otherVaccineContentData) {

        return  Observable.create(e->{
                    try {

                        postOtherVaccineData(otherVaccineContentData);
                        e.onNext("done");//error
                        e.onComplete();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        e.onNext("");//error
                        e.onComplete();
                    }

                }
        );

    }
    private static void postOtherVaccineData(OtherVaccineContentData otherVaccineContentData){
        String ADD_URL = "rest/api/vaccination/sync";
        ArrayList<String> list = new ArrayList<>();
        String json = JsonFormUtils.gson.toJson(otherVaccineContentData);
        list.add(json);
        if(list.size()==0) return;
        try{
            JSONObject request = new JSONObject();
            request.put("vaccines",list);
            String jsonPayload = request.toString();
            //{"vaccines":[{"brn":"123456","dob":"2022-08-01","vaccineDate":"2023-01-01","vaccine_name":"HPV"},{"brn":"1234564","dob":"2022-08-01","vaccineDate":"2023-01-01","vaccine_name":"HPV"}]}
            String add_url =  MessageFormat.format("{0}{1}",
                    BuildConfig.citizen_url,
                    ADD_URL);
            jsonPayload = jsonPayload.replace("\\","").replace("\"[","[").replace("]\"","]");
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            HashMap<String,String> headers = new HashMap<>();
            headers.put("dd",BuildConfig.dd);
            Response<String> response = httpAgent.postWithHeaderAndJwtToken(add_url, jsonPayload,headers,BuildConfig.JWT_TOKEN);
            if (response.isFailure() || response.isTimeoutError()) {
                HnppApplication.getOtherVaccineRepository().addOtherVaccine(otherVaccineContentData);
                return;
            }
            //{"error":[],"notFound":[]}
            JSONObject results = new JSONObject((String) response.payload());
            if(results.has("error") && results.getJSONArray("error").length()==0){
                HnppApplication.getOtherVaccineRepository().addAndUpdateOtherVaccine(otherVaccineContentData);
            }else{
                HnppApplication.getOtherVaccineRepository().addOtherVaccine(otherVaccineContentData);
            }

        }catch (Exception e){
            e.printStackTrace();

        }
    }
    private static void processOtherVaccineUnSyncData(int count){
        String ADD_URL = "rest/api/vaccination/sync";
        ArrayList<OtherVaccineContentData> vaccineContentData = HnppApplication.getOtherVaccineRepository().getUnSyncData();
        ArrayList<String> list = new ArrayList<>();

        for(OtherVaccineContentData otherVaccineContentData: vaccineContentData){
            String json = JsonFormUtils.gson.toJson(otherVaccineContentData);
            list.add(json);
        }
        if(list.size()==0){
            CoreLibrary.getInstance().context().allSharedPreferences().savePreference(LAST_SYNC_HPV,System.currentTimeMillis()+"");

            return;
        }
        try{
            JSONObject request = new JSONObject();
            request.put("vaccines",list);
            String jsonPayload = request.toString();
            //{"vaccines":[{"brn":"123456","dob":"2022-08-01","vaccineDate":"2023-01-01","vaccine_name":"HPV"},{"brn":"1234564","dob":"2022-08-01","vaccineDate":"2023-01-01","vaccine_name":"HPV"}]}
            String add_url =  MessageFormat.format("{0}{1}",
                    BuildConfig.citizen_url,
                    ADD_URL);
            jsonPayload = jsonPayload.replace("\\","").replace("\"[","[").replace("]\"","]");
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            HashMap<String,String> headers = new HashMap<>();
            headers.put("dd",BuildConfig.dd);
            Response<String> response = httpAgent.postWithHeaderAndJwtToken(add_url, jsonPayload,headers,BuildConfig.JWT_TOKEN);
            if (response.isFailure() || response.isTimeoutError()) {
                return;
            }
            //{"error":[],"notFound":[]}
            JSONObject results = new JSONObject((String) response.payload());
            if(results.has("error")){
                for (OtherVaccineContentData contentData: vaccineContentData){
                    HnppApplication.getOtherVaccineRepository().updateOtherVaccineStatus(contentData);
                }
                if (count < CoreLibrary.getInstance().getSyncConfiguration().getSyncMaxRetries()) {
                    int newCount = count + 1;
                    processOtherVaccineUnSyncData(newCount);
                }else{
                    Log.v("SYNC_URL","done");
                }
            }


//{"timestamp":"2023-09-04T14:40:53.495+00:00","status":500,"error":"Internal Server Error","trace":"org.springframework.security.web.firewall.RequestRejectedException: The request was rejected because the URL contained a potentially malicious String \"//\"\n\tat org.springframework.security.web.firewall.StrictHttpFirewall.rejectedBlocklistedUrls(StrictHttpFirewall.java:535)\n\tat org.springframework.security.web.firewall.StrictHttpFirewall.getFirewalledRequest(StrictHttpFirewall.java:505)\n\tat org.springframework.security.web.FilterChainProxy.doFilterInternal(FilterChainProxy.java:196)\n\tat org.springframework.security.web.FilterChainProxy.doFilter(FilterChainProxy.java:183)\n\tat org.springframework.web.filter.DelegatingFilterProxy.invokeDelegate(DelegatingFilterProxy.java:354)\n\tat org.springframework.web.filter.DelegatingFilterProxy.doFilter(DelegatingFilterProxy.java:267)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\n\tat org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:117)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\n\tat org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:117)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\n\tat org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:117)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\n\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:197)\n\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:97)\n\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:541)\n\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:135)\n\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92)\n\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:78)\n\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:360)\n\tat org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:399)\n\tat org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:65)\n\tat org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:890)\n\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1743)\n\tat org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191)\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659)\n\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)\n\tat java.lang.Thread.run(Thread.java:748)\n","message":"The request was rejected because the URL contained a potentially malicious String \"//\"","path":"//rest/api/vaccination/sync"}

        }catch (Exception e){
            e.printStackTrace();

        }
    }
    private static void forseSyncHpvData(int count){
        String ADD_URL = "rest/api/vaccination/sync";
        ArrayList<OtherVaccineContentData> vaccineContentData = HnppApplication.getOtherVaccineRepository().getAllDataForForseSync(count);
        Log.v("OTHER_VACCINE","processUnSyncData>>"+vaccineContentData.size());
        ArrayList<String> list = new ArrayList<>();

        for(OtherVaccineContentData otherVaccineContentData: vaccineContentData){
            String json = JsonFormUtils.gson.toJson(otherVaccineContentData);
            list.add(json);
        }
        Log.v("OTHER_VACCINE","processUnSyncData>>"+list);
        if(list.size()==0) return;
        try{
            JSONObject request = new JSONObject();
            request.put("vaccines",list);
            String jsonPayload = request.toString();
            //{"vaccines":[{"brn":"123456","dob":"2022-08-01","vaccineDate":"2023-01-01","vaccine_name":"HPV"},{"brn":"1234564","dob":"2022-08-01","vaccineDate":"2023-01-01","vaccine_name":"HPV"}]}
            String add_url =  MessageFormat.format("{0}{1}",
                    BuildConfig.citizen_url,
                    ADD_URL);
            Log.v("OTHER_VACCINE","jsonPayload>>>"+jsonPayload);
            jsonPayload = jsonPayload.replace("\\","").replace("\"[","[").replace("]\"","]");
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            HashMap<String,String> headers = new HashMap<>();
            headers.put("dd",BuildConfig.dd);
            Log.v("OTHER_VACCINE","jsonPayload after replace>>>"+jsonPayload);
            Response<String> response = httpAgent.postWithHeaderAndJwtToken(add_url, jsonPayload,headers,BuildConfig.JWT_TOKEN);
            if (response.isFailure() || response.isTimeoutError()) {
                HnppConstants.appendLog("SYNC_URL", "message>>"+response.payload()+"status:"+response.status().displayValue());
                return;
            }
            HnppConstants.appendLog("SYNC_URL", "pushECToServer:response comes"+response.payload());
            //{"error":[],"notFound":[]}
            JSONObject results = new JSONObject((String) response.payload());
            if(results.has("error")){
                for (OtherVaccineContentData contentData: vaccineContentData){
                    HnppApplication.getOtherVaccineRepository().updateOtherVaccineStatus(contentData);
                }
                if (count < CoreLibrary.getInstance().getSyncConfiguration().getSyncMaxRetries()) {
                    int newCount = count + 29;
                    forseSyncHpvData(newCount);
                }else{
                    Log.v("SYNC_URL","done");
                }
            }


//{"timestamp":"2023-09-04T14:40:53.495+00:00","status":500,"error":"Internal Server Error","trace":"org.springframework.security.web.firewall.RequestRejectedException: The request was rejected because the URL contained a potentially malicious String \"//\"\n\tat org.springframework.security.web.firewall.StrictHttpFirewall.rejectedBlocklistedUrls(StrictHttpFirewall.java:535)\n\tat org.springframework.security.web.firewall.StrictHttpFirewall.getFirewalledRequest(StrictHttpFirewall.java:505)\n\tat org.springframework.security.web.FilterChainProxy.doFilterInternal(FilterChainProxy.java:196)\n\tat org.springframework.security.web.FilterChainProxy.doFilter(FilterChainProxy.java:183)\n\tat org.springframework.web.filter.DelegatingFilterProxy.invokeDelegate(DelegatingFilterProxy.java:354)\n\tat org.springframework.web.filter.DelegatingFilterProxy.doFilter(DelegatingFilterProxy.java:267)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\n\tat org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:117)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\n\tat org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:117)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\n\tat org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:117)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\n\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:197)\n\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:97)\n\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:541)\n\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:135)\n\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92)\n\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:78)\n\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:360)\n\tat org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:399)\n\tat org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:65)\n\tat org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:890)\n\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1743)\n\tat org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191)\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659)\n\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)\n\tat java.lang.Thread.run(Thread.java:748)\n","message":"The request was rejected because the URL contained a potentially malicious String \"//\"","path":"//rest/api/vaccination/sync"}

        }catch (Exception e){
            e.printStackTrace();

        }
    }
    public static Observable<Boolean> deleteLogFile() {

        return  Observable.create(e->{
                    try {
                        Context context= HnppApplication.getInstance().getApplicationContext();
                        String path = context.getExternalFilesDir(null) + "/hnpp_log";
                        File directory = new File(path);
                        File[] files = directory.listFiles();
                        if(files!=null){
                            for(int i = 0; i< files.length; i++){
                                if(files.length>3){
                                    if(i<3){
                                        File f = new File(directory + "/" + files[i].getName());
                                        boolean isDeleted = deleteDirectory(f.getAbsoluteFile());
                                        Log.v("LOG_FILE", " for delete FileName: isDeleted" + isDeleted + ":" + f.getAbsolutePath());

                                    }
                                }
                            }
                        }
                        e.onNext(true);//error
                        e.onComplete();
                    } catch (Exception ex) {
                        e.onNext(false);//error
                        e.onComplete();
                    }

                }
        );

    }

    static boolean deleteDirectory(File path) {
        if(path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return false;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    boolean wasSuccessful = file.delete();
                    if (wasSuccessful) {
                        Log.i("Deleted ", "successfully");
                    }
                }
            }
        }
        return(path.delete());
    }
    public static void appendLog(String TAG,String text) {
        try{
            Log.v(TAG,text);
//            Context context= HnppApplication.getInstance().getApplicationContext();
//            String saveText = TAG + new DateTime(System.currentTimeMillis())+" >>> "+ text;
//            Calendar calender = Calendar.getInstance();
//            int year = calender.get(Calendar.YEAR);
//            int month = calender.get(Calendar.MONTH)+1;
//            int day = calender.get(Calendar.DAY_OF_MONTH);
//            String fileNameDayWise = year+""+addZeroForDay(month+"")+""+addZeroForDay(day+"");
//
//            File f = new File(context.getExternalFilesDir(null) + "/hnpp_log/"+fileNameDayWise);
//            if (!f.exists()) {
//                f.mkdirs();
//            }
//            File logFile = new File(context.getExternalFilesDir(null) + "/hnpp_log/"+fileNameDayWise+"/"+"log.file");
//            if (!logFile.exists()) {
//                try {
//                    logFile.createNewFile();
//                } catch (IOException ee) {
//                    Log.e(TAG, ee.getMessage());
//                }
//            }
//            //BufferedWriter for performance, true to set append to file flag
//            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
//            buf.append(saveText);
//            buf.newLine();
//            buf.close();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static String getDatefromLongDate(long toMonth) {
        Calendar calendar = Calendar.getInstance();
        String date = calendar.get(Calendar.DAY_OF_MONTH)+"";
        String yymm = YYYYMM.format(new Date(toMonth==-1?System.currentTimeMillis():toMonth));
        String returnDate = yymm+"-"+date;
        Log.v("ANC_TRIMESTER","returnDate:"+returnDate);
        return returnDate;
    }



    public enum VisitType {DUE, OVERDUE, LESS_TWENTY_FOUR, VISIT_THIS_MONTH, NOT_VISIT_THIS_MONTH, EXPIRY, VISIT_DONE}
    public enum HomeVisitType {GREEN, YELLOW, RED, BROWN}
    public enum SEARCH_TYPE {HH, ADO, WOMEN, CHILD,NCD,ADULT}
    public enum MIGRATION_TYPE {HH, Member,OTHER_VACCINE}

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager conMgr = null;
        boolean connected = false;

        try {
            conMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            connected = (conMgr.getActiveNetworkInfo() != null
                    && conMgr.getActiveNetworkInfo().isAvailable() && conMgr
                    .getActiveNetworkInfo().isConnected());
            return connected;
        } catch (Exception e) {
            if (context == null) {
                context = HnppApplication.getHNPPInstance();
            }
            if (conMgr == null) {
                conMgr = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
            }
            if (conMgr != null) {
                connected = (conMgr.getActiveNetworkInfo() != null
                        && conMgr.getActiveNetworkInfo().isAvailable() && conMgr
                        .getActiveNetworkInfo().isConnected());
            }
            return connected;
        }
    }
    public static void checkNetworkConnection(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.no_internet_title);
        builder.setMessage(R.string.no_internet_msg);
        builder.setNegativeButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    public static void getGPSLocation(FamilyRegisterActivity activity,final OnPostDataWithGps onPostDataWithGps){


        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    11111);
            return ;
        }
        GenerateGPSTask task = new GenerateGPSTask(new OnGpsDataGenerateListener() {
            @Override
            public void showProgressBar(int message) {
                activity.showProgressDialog(message);
            }

            @Override
            public void hideProgress() {
                activity.hideProgressDialog();

            }

            @Override
            public void onGpsDataNotFound() {
                HnppConstants.showOneButtonDialog(activity, "", activity.getString(R.string.gps_not_found), new Runnable() {
                    @Override
                    public void run() {
                       if(!IS_MANDATORY_GPS) onPostDataWithGps.onPost(0.0,0.0);
                    }
                });
            }

            @Override
            public void onGpsData(double latitude, double longitude) {
                onPostDataWithGps.onPost(latitude,longitude);

            }
        },activity);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                task.updateUi();
            }
        },5000);


    }
    public static void getGPSLocation(BaseProfileActivity activity, OnPostDataWithGps onPostDataWithGps){


        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    11111);
            return ;
        }
        GenerateGPSTask task = new GenerateGPSTask(new OnGpsDataGenerateListener() {
            @Override
            public void showProgressBar(int message) {
                try{
                    activity.showProgressDialog(message);
                }catch (Exception e){

                }
            }

            @Override
            public void hideProgress() {
                try{
                    activity.hideProgressDialog();
                }catch (Exception e){

                }

            }

            @Override
            public void onGpsDataNotFound() {
                try{
                    HnppConstants.showOneButtonDialog(activity, "", activity.getString(R.string.gps_not_found), new Runnable() {
                        @Override
                        public void run() {
                            if(!IS_MANDATORY_GPS)onPostDataWithGps.onPost(0.0,0.0);
                        }
                    });
                }catch (Exception e){

                }
            }

            @Override
            public void onGpsData(double latitude, double longitude) {
                onPostDataWithGps.onPost(latitude,longitude);

            }
        },activity);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(task!=null) task.updateUi();
            }
        },5000);


    }
    public static void getGPSLocation(SecuredActivity activity, OnPostDataWithGps onPostDataWithGps){


        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    11111);
            return ;
        }
        GenerateGPSTask task = new GenerateGPSTask(new OnGpsDataGenerateListener() {
            @Override
            public void showProgressBar(int message) {
                Toast.makeText(activity,"GPS finding........",Toast.LENGTH_LONG).show();
            }

            @Override
            public void hideProgress() {
            }

            @Override
            public void onGpsDataNotFound() {
                try{
                    HnppConstants.showOneButtonDialog(activity, "", activity.getString(R.string.gps_not_found), new Runnable() {
                        @Override
                        public void run() {
                            if(!IS_MANDATORY_GPS)onPostDataWithGps.onPost(0.0,0.0);
                        }
                    });
                }catch (Exception e){

                }
            }

            @Override
            public void onGpsData(double latitude, double longitude) {
                onPostDataWithGps.onPost(latitude,longitude);

            }
        },activity);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(task!=null) task.updateUi();
            }
        },5000);


    }
    public static String addZeroForMonth(String month){
        if(TextUtils.isEmpty(month)) return "";
        if(month.length()==1) return "0"+month;
        return month;
    }
    public static String addZeroForDay(String day){
        if(TextUtils.isEmpty(day)) return "";
        if(day.length()==1) return "0"+day;
        return day;
    }
    public class ANC_REGISTER_COLUMNS {
        public static final String LAST_MENSTRUAL_PERIOD = "last_menstrual_period";
        public static final String EDD = "edd";
        public static final String NO_PREV_PREG = "no_prev_preg";
        public static final String NO_SURV_CHILDREN = "no_surv_children";
        public static final String HEIGHT = "height";
    }

    public static class TABLE_NAME {
        public static final String FAMILY = "ec_family";
        public static final String FAMILY_MEMBER = "ec_family_member";
        public static final String CHILD = "ec_child";
        public static final String ANC_PREGNANCY_OUTCOME = "ec_pregnancy_outcome";
        public static final String ANC_MEMBER = "ec_anc_register";
    }
    public class OTHER_SERVICE_TYPE{
        public static final int TYPE_WOMEN_PACKAGE = 1;
        public static final int TYPE_GIRL_PACKAGE = 2;
        public static final int TYPE_NCD = 3;
        public static final int TYPE_IYCF = 4;
        public static final int TYPE_EYE = 5;
        public static final int TYPE_BLOOD = 6;
        public static final int TYPE_REFERRAL = 7;
        public static final int TYPE_REFERRAL_FOLLOW_UP = 8;
    }

    public static String getPaymentIdFromUrl(String url){
        String paymentId = "";
        if(TextUtils.isEmpty(url)) return "";
        paymentId= url.substring(url.indexOf("paymentID")+10,url.indexOf("&"));
        return paymentId;
    }

    public static boolean isNeedToShowEDDPopup(){
        String lastEddTimeStr =  org.smartregister.Context.getInstance().allSharedPreferences().getPreference("LAST_EDD_TIME");
        if(TextUtils.isEmpty(lastEddTimeStr)){
            org.smartregister.Context.getInstance().allSharedPreferences().savePreference("LAST_EDD_TIME",System.currentTimeMillis()+"");
            return true;
        }
        long diff = System.currentTimeMillis() - Long.parseLong(lastEddTimeStr);
        if(diff > EDD_DEFAULT_TIME){
            org.smartregister.Context.getInstance().allSharedPreferences().savePreference("LAST_EDD_TIME",System.currentTimeMillis()+"");

            return true;
        }
        return false;
    }
    public static boolean isNeedToCallInvalidApi(){
        String lastInvalidTimeStr =  org.smartregister.Context.getInstance().allSharedPreferences().getPreference("INVALID_LAST_TIME");
        if(TextUtils.isEmpty(lastInvalidTimeStr)){
            org.smartregister.Context.getInstance().allSharedPreferences().savePreference("INVALID_LAST_TIME",System.currentTimeMillis()+"");
            return true;
        }
        long diff = System.currentTimeMillis() - Long.parseLong(lastInvalidTimeStr);
        Log.v("INVALID_REQ","diff:"+diff);
        if(diff > INVALID_CALL_DEFAULT_TIME){
            org.smartregister.Context.getInstance().allSharedPreferences().savePreference("INVALID_LAST_TIME",System.currentTimeMillis()+"");

            return true;
        }
        return false;
    }
    public static boolean isNeedToCallSurveyHistoryApi(){
        String surveyHistoryTimeStr =  org.smartregister.Context.getInstance().allSharedPreferences().getPreference("SURVEY_LAST_TIME");
        if(TextUtils.isEmpty(surveyHistoryTimeStr)){
            org.smartregister.Context.getInstance().allSharedPreferences().savePreference("SURVEY_LAST_TIME",System.currentTimeMillis()+"");
            return true;
        }
        long diff = System.currentTimeMillis() - Long.parseLong(surveyHistoryTimeStr);
        Log.v("SURVEY_HISTORY","diff:"+diff);
        if(diff > SURVEY_HISTORY_DEFAULT_TIME){
            org.smartregister.Context.getInstance().allSharedPreferences().savePreference("SURVEY_LAST_TIME",System.currentTimeMillis()+"");

            return true;
        }
        return false;
    }
    public static boolean isNeedToShowStockEndPopup(){
        String lastEddTimeStr =  org.smartregister.Context.getInstance().allSharedPreferences().getPreference("STOCK_END_TIME");
        if(TextUtils.isEmpty(lastEddTimeStr)){
            org.smartregister.Context.getInstance().allSharedPreferences().savePreference("STOCK_END_TIME",System.currentTimeMillis()+"");
            return true;
        }
        long diff = System.currentTimeMillis() - Long.parseLong(lastEddTimeStr);
        if(diff > STOCK_END_DEFAULT_TIME){
            org.smartregister.Context.getInstance().allSharedPreferences().savePreference("STOCK_END_TIME",System.currentTimeMillis()+"");

            return true;
        }
        return false;
    }
    public static boolean isNeedToAutoSync(){
        String lastEddTimeStr =  org.smartregister.Context.getInstance().allSharedPreferences().getPreference("AUTO_SYNC_TIME");
        if(TextUtils.isEmpty(lastEddTimeStr)){
            org.smartregister.Context.getInstance().allSharedPreferences().savePreference("AUTO_SYNC_TIME",System.currentTimeMillis()+"");
            return false;
        }
        long diff = System.currentTimeMillis() - Long.parseLong(lastEddTimeStr);
        Log.v("AUTO_SYNC","isNeedToAutoSync>>>>diff:"+diff);
        if(diff > AUTO_SYNC_DEFAULT_TIME){
            org.smartregister.Context.getInstance().allSharedPreferences().savePreference("AUTO_SYNC_TIME",System.currentTimeMillis()+"");

            return true;
        }
        return false;
    }
    public static void saveAutoSyncData(){
        org.smartregister.Context.getInstance().allSharedPreferences().savePreference("AUTO_SYNC_TIME",System.currentTimeMillis()+"");

    }
    public static long getFlexValue(int value) {
        int minutes = 5;

        if (value > 5) {

            minutes = (int) Math.ceil(value / 3);
        }

        return Math.max(minutes, 5);
    }
    public static void showSaveFormConfirmationDialog(Context context,String title, OnDialogOptionSelect onDialogOptionSelect){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.save_confirm_dialog);
        TextView textViewTitle = dialog.findViewById(R.id.condirm_text);
        textViewTitle.setText(title);
        dialog.findViewById(R.id.no_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onDialogOptionSelect.onClickNoButton();
            }
        });
        dialog.findViewById(R.id.yes_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onDialogOptionSelect.onClickYesButton();
            }
        });
        dialog.show();
    }
    public static void showDialogWithAction(Context context,String title, String text,Runnable okRunnable,Runnable cancelRunnable){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_two_button);
        TextView textViewTitle = dialog.findViewById(R.id.text_tv);
        TextView titleTxt = dialog.findViewById(R.id.title_tv);
        titleTxt.setText(title);
        textViewTitle.setText(text);
        dialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                cancelRunnable.run();
            }
        });
        dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                okRunnable.run();
            }
        });
        dialog.show();
    }
    public static void showTermConditionDialog(Context context,String title, String text,Runnable runnable){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_term_condition);
        TextView textViewTitle = dialog.findViewById(R.id.text_tv);
        TextView titleTxt = dialog.findViewById(R.id.title_tv);
        titleTxt.setText(title);
        textViewTitle.setText(text);
        CheckBox checkBox = dialog.findViewById(R.id.term_check);
        LinearLayout payBtn = dialog.findViewById(R.id.bkash_pay);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    payBtn.setAlpha(1.0f);
                    payBtn.setEnabled(true);
                }else{
                    payBtn.setAlpha(0.3f);
                    payBtn.setEnabled(false);
                }
            }
        });
        dialog.findViewById(R.id.term_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, TermAndConditionWebView.class));
            }
        });
        dialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                runnable.run();
            }
        });
        dialog.show();
    }
    @SuppressLint("SetTextI18n")
    public static void showButtonWithImageDialog(Context context, int type, String message, Runnable runnable){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_bkash_success);
        ImageView imageView = dialog.findViewById(R.id.image);
        TextView titleTxt = dialog.findViewById(R.id.title_tv);
        TextView statusTxt = dialog.findViewById(R.id.status_tv);
        TextView messageTxt = dialog.findViewById(R.id.text_tv);
        if(type ==1){
            titleTxt.setText(R.string.payment_succ);
            imageView.setImageResource(R.drawable.success);
            statusTxt.setText("Payment successfully");
            statusTxt.setTextColor(context.getResources().getColor(R.color.alert_complete_green));
            messageTxt.setText(message);
            messageTxt.setTextColor(context.getResources().getColor(R.color.alert_complete_green));
        }else if(type == 2){
            titleTxt.setText(R.string.payment_failed);
            imageView.setImageResource(R.drawable.failure);
            statusTxt.setTextColor(context.getResources().getColor(R.color.alert_urgent_red));
            statusTxt.setText("Payment failed");
            messageTxt.setText(message);
            messageTxt.setTextColor(context.getResources().getColor(R.color.alert_urgent_red));
        }else if(type == 3){
            titleTxt.setText(R.string.payment_cancel);
            imageView.setImageResource(R.drawable.cancel);
            statusTxt.setText("Payment cancel");
            statusTxt.setTextColor(context.getResources().getColor(R.color.alert_urgent_red));
            messageTxt.setText(message);
            messageTxt.setTextColor(context.getResources().getColor(R.color.alert_urgent_red));
        }


        dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                runnable.run();
            }
        });
        dialog.show();
    }
    public static void showOneButtonDialog(Context context,String title, String text){
        showOneButtonDialog(context,title,text,null);
    }
    public static void showOneButtonDialog(Context context,String title, String text,Runnable runnable){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView textViewTitle = dialog.findViewById(R.id.text_tv);
        TextView titleTxt = dialog.findViewById(R.id.title_tv);
        titleTxt.setText(title);
        textViewTitle.setText(text);
        dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
               if(runnable!=null) runnable.run();
            }
        });
        dialog.show();
    }
    public static void showDialog(Context context,String title, String text){
        Dialog dialog = new Dialog(context, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.new_stock_view);
        TextView textViewTitle = dialog.findViewById(R.id.stock_new_text);
        TextView titleTxt = dialog.findViewById(R.id.textview_detail_two);
        titleTxt.setText(title);
        textViewTitle.setText(text);
        dialog.findViewById(R.id.cross_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    public static void insertAtNotificationTable(String title, String details){
        long currentTime = System.currentTimeMillis();
        String notificationType = "In App";
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        String sendDate = calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH);

        Notification notification = new Notification();
        notification.setId((int) currentTime);
        notification.setTimestamp(currentTime);
        notification.setTitle(title);
        notification.setDetails(details);
        notification.setNotificationType(notificationType);
        notification.setHour(hour);
        notification.setMinute(minute);
        notification.setSendDate(sendDate);
        HnppApplication.getNotificationRepository().addOrUpdate(notification);
        Log.v("NOTIFICATION_JOB","insertAtNotificationTable:"+title);

    }
    public static boolean isEddImportant(String lmp){
        DateTime lmpDate = DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(lmp);

        LocalDate lastMenstrualPeriod = new LocalDate(lmpDate);
        LocalDate expectedDeliveryDate = lastMenstrualPeriod.plusDays(280);
        int dayDiff = Days.daysBetween(lastMenstrualPeriod, expectedDeliveryDate).getDays();
        return dayDiff <=30;
    }
    public static String getSchedulePncDate(String deliveryDateStr, int noOfPnc){
        DateTime ddDate = DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(deliveryDateStr);
        LocalDate deliveryDate = new LocalDate(ddDate);
        LocalDate scheduleDate;
        switch (noOfPnc){
            case 2: scheduleDate= deliveryDate.plusDays(3);
                break;
            case 3: scheduleDate= deliveryDate.plusDays(7);
                break;
            case 4: scheduleDate= deliveryDate.plusDays(28);
                break;
            default:
                scheduleDate= deliveryDate.plusDays(0);
                break;
        }
        return DateTimeFormat.forPattern("dd-MM-yyyy").print(scheduleDate);
    }
    public static String getScheduleNewPncDate(String dob,int noOfPnc){
        DateTime ddDate = DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(dob);
        LocalDate deliveryDate = new LocalDate(ddDate);
        LocalDate scheduleDate;
        switch (noOfPnc){
            case 1:
                scheduleDate= deliveryDate.plusDays(0);
                break;
            case 2: scheduleDate= deliveryDate.plusDays(3);
                break;
            case 3: scheduleDate= deliveryDate.plusDays(7);
                break;
            case 4: scheduleDate= deliveryDate.plusDays(28);
                break;
            default:
                scheduleDate= deliveryDate.plusDays(42);
                break;
        }
        return DateTimeFormat.forPattern("dd-MM-yyyy").print(scheduleDate);
    }
    public static String getScheduleKMCHomeDate(String baseEntityId,int noOfPnc){
        String identifiedDate = HnppDBUtils.getKMCIdentifiedDate(baseEntityId);
        DateTime ddDate = null;
        if(!TextUtils.isEmpty(identifiedDate)){
            Date date = new Date(Long.parseLong(identifiedDate));
            String dobFormate = HnppConstants.DDMMYY.format(date);
            ddDate = DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(dobFormate);
        }
        if(ddDate == null) return "";
        LocalDate deliveryDate = new LocalDate(ddDate);
        LocalDate scheduleDate;
        switch (noOfPnc){
            case 1:
                scheduleDate= deliveryDate.plusDays(0);
                break;
            case 2: scheduleDate= deliveryDate.plusDays(3);
                break;
            case 3: scheduleDate= deliveryDate.plusDays(7);
                break;
            case 4: scheduleDate= deliveryDate.plusDays(42);
                break;
            default:
                scheduleDate= deliveryDate.plusDays(42);
                break;
        }
        return DateTimeFormat.forPattern("dd-MM-yyyy").print(scheduleDate);
    }
    public static String getScheduleAncDate(String lmp, int noOfAnc){
        DateTime lmpDate = DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(lmp);

        LocalDate lastMenstrualPeriod = new LocalDate(lmpDate);
        LocalDate scheduleDate;
        switch (noOfAnc){
            case 2: scheduleDate= lastMenstrualPeriod.plusDays(140);
                break;
            case 3: scheduleDate= lastMenstrualPeriod.plusDays(182);
                break;
            case 4: scheduleDate= lastMenstrualPeriod.plusDays(210);
                break;
            case 5: scheduleDate= lastMenstrualPeriod.plusDays(238);
                break;
            case 6: scheduleDate= lastMenstrualPeriod.plusDays(252);
                break;
            case 7: scheduleDate= lastMenstrualPeriod.plusDays(266);
                break;
            case 8: scheduleDate= lastMenstrualPeriod.plusDays(280);
                break;
            default:
                scheduleDate= lastMenstrualPeriod.plusDays(84);
                break;

        }

        return  DateTimeFormat.forPattern("dd-MM-yyyy").print(scheduleDate);
    }
    public static String[] getPncTitle(int noOfPnc){
        String[] ancType = new String[2];
        switch (noOfPnc){
            case 1:
                ancType[0]=HnppApplication.appContext.getString(R.string.pnc_service_1);
                ancType[1] = "PNC -1";
                return ancType;
            case 2:
                ancType[0]=HnppApplication.appContext.getString(R.string.pnc_service_2);
                ancType[1] = "ANC -2";
                return ancType;
            case 3:
                ancType[0]=HnppApplication.appContext.getString(R.string.pnc_service_3);
                ancType[1] = "ANC -3";
                return ancType;
            case 4:
                ancType[0]=HnppApplication.appContext.getString(R.string.pnc_service_4);
                ancType[1] = "ANC -4";
                return ancType;
            case 5:
                ancType[0]=HnppApplication.appContext.getString(R.string.pnc_service_5);
                ancType[1] = "ANC -5";
                return ancType;
            default:
                ancType[0]=HnppApplication.appContext.getString(R.string.pnc_service);
                ancType[1] = "PNC";
                return ancType;
        }
    }
    public static String[] getNewBornPncTitle(int noOfPnc){
        String[] ancType = new String[2];
        switch (noOfPnc){
            case 1:
                ancType[0]=HnppApplication.appContext.getString(R.string.newborn_pnc_1);
                ancType[1] = "PNC -1";
                return ancType;
            case 2:
                ancType[0]=HnppApplication.appContext.getString(R.string.newborn_pnc_2);
                ancType[1] = "PNC -2";
                return ancType;
            case 3:
                ancType[0]=HnppApplication.appContext.getString(R.string.newborn_pnc_3);
                ancType[1] = "PNC -3";
                return ancType;
            case 4:
                ancType[0]=HnppApplication.appContext.getString(R.string.newborn_pnc_4);
                ancType[1] = "PNC -4";
                return ancType;
            default:
                ancType[0]=HnppApplication.appContext.getString(R.string.newborn_pnc);
                ancType[1] = "PNC";
                return ancType;
        }
    }
    public static String[] getKMCHomeFollowUpTitle(int noOfPnc){
        String[] ancType = new String[2];
        switch (noOfPnc){
            case 1:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_home_followup)+""+HnppApplication.appContext.getString(R.string.first_suffix);
                ancType[1] = "KMC -1";
                return ancType;
            case 2:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_home_followup)+""+HnppApplication.appContext.getString(R.string.second_suffix);
                ancType[1] = "PNC -2";
                return ancType;
            case 3:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_home_followup)+""+HnppApplication.appContext.getString(R.string.third_suffix);
                ancType[1] = "PNC -3";
                return ancType;
            case 4:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_home_followup)+""+HnppApplication.appContext.getString(R.string.fourth_suffix);
                ancType[1] = "PNC -4";
                return ancType;
            default:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_home_followup);
                ancType[1] = "PNC";
                return ancType;
        }
    }
    public static String[] getKMCHomeServiceTitle(int noOfPnc){
        String[] ancType = new String[2];
        switch (noOfPnc){
            case 1:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_service_home)+""+HnppApplication.appContext.getString(R.string.first_suffix);
                ancType[1] = "KMC -1";
                return ancType;
            case 2:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_service_home)+""+HnppApplication.appContext.getString(R.string.second_suffix);
                ancType[1] = "PNC -2";
                return ancType;
            case 3:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_service_home)+""+HnppApplication.appContext.getString(R.string.third_suffix);
                ancType[1] = "PNC -3";
                return ancType;
            case 4:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_service_home)+""+HnppApplication.appContext.getString(R.string.fourth_suffix);
                ancType[1] = "PNC -4";
                return ancType;
            default:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_service_home);
                ancType[1] = "PNC";
                return ancType;
        }
    }
    public static String[] getKMCHospitalServiceTitle(int noOfPnc){
        String[] ancType = new String[2];
        switch (noOfPnc){
            case 1:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_service_hospital)+""+HnppApplication.appContext.getString(R.string.first_suffix);
                ancType[1] = "KMC -1";
                return ancType;
            case 2:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_service_hospital)+""+HnppApplication.appContext.getString(R.string.second_suffix);
                ancType[1] = "PNC -2";
                return ancType;
            case 3:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_service_hospital)+""+HnppApplication.appContext.getString(R.string.third_suffix);
                ancType[1] = "PNC -3";
                return ancType;
            case 4:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_service_hospital)+""+HnppApplication.appContext.getString(R.string.fourth_suffix);
                ancType[1] = "PNC -4";
                return ancType;
            default:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_service_hospital);
                ancType[1] = "PNC";
                return ancType;
        }
    }
    public static String[] getKMCHospitalFollowupTitle(int noOfPnc){
        String[] ancType = new String[2];
        switch (noOfPnc){
            case 1:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_hospital_followup)+""+HnppApplication.appContext.getString(R.string.first_suffix);
                ancType[1] = "KMC -1";
                return ancType;
            case 2:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_hospital_followup)+""+HnppApplication.appContext.getString(R.string.second_suffix);
                ancType[1] = "PNC -2";
                return ancType;
            case 3:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_hospital_followup)+""+HnppApplication.appContext.getString(R.string.third_suffix);
                ancType[1] = "PNC -3";
                return ancType;
            case 4:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_hospital_followup)+""+HnppApplication.appContext.getString(R.string.fourth_suffix);
                ancType[1] = "PNC -4";
                return ancType;
            default:
                ancType[0]=HnppApplication.appContext.getString(R.string.kmc_hospital_followup);
                ancType[1] = "PNC";
                return ancType;
        }
    }
    public static String[] getScanuTitle(int noOfPnc){
        String[] ancType = new String[2];
        switch (noOfPnc){
            case 1:
                ancType[0]=HnppApplication.appContext.getString(R.string.scanu_followup)+""+HnppApplication.appContext.getString(R.string.first_suffix);
                ancType[1] = "KMC -1";
                return ancType;
            case 2:
                ancType[0]=HnppApplication.appContext.getString(R.string.scanu_followup)+""+HnppApplication.appContext.getString(R.string.second_suffix);
                ancType[1] = "PNC -2";
                return ancType;
            case 3:
                ancType[0]=HnppApplication.appContext.getString(R.string.scanu_followup)+""+HnppApplication.appContext.getString(R.string.third_suffix);
                ancType[1] = "PNC -3";
                return ancType;
            case 4:
                ancType[0]=HnppApplication.appContext.getString(R.string.scanu_followup)+""+HnppApplication.appContext.getString(R.string.fourth_suffix);
                ancType[1] = "PNC -4";
                return ancType;
            default:
                ancType[0]=HnppApplication.appContext.getString(R.string.scanu_followup);
                ancType[1] = "PNC";
                return ancType;
        }
    }
    public static String[] getAncTitle(int noOfAnc){
        String[] ancType = new String[2];
        switch (noOfAnc){
            case 0:
                ancType[0]=HnppApplication.appContext.getString(R.string.pregnancy_service);
                ancType[1] = "ANC";
                return ancType;
            case 1:
                ancType[0]=HnppApplication.appContext.getString(R.string.pregnancy_service_1);
                ancType[1] = "ANC -1";
                return ancType;
            case 2:
                ancType[0]=HnppApplication.appContext.getString(R.string.pregnancy_service_2);
                ancType[1] = "ANC -2";
                return ancType;
            case 3:
                ancType[0]=HnppApplication.appContext.getString(R.string.pregnancy_service_3);
                ancType[1] = "ANC -3";
                return ancType;
            case 4:
                ancType[0]=HnppApplication.appContext.getString(R.string.pregnancy_service_4);
                ancType[1] = "ANC -4";
                return ancType;
            case 5:
                ancType[0]=HnppApplication.appContext.getString(R.string.pregnancy_service_5);
                ancType[1] = "ANC -5";
                return ancType;
            case 6:
                ancType[0]=HnppApplication.appContext.getString(R.string.pregnancy_service_6);
                ancType[1] = "ANC -6";
                return ancType;
            case 7:
                ancType[0]=HnppApplication.appContext.getString(R.string.pregnancy_service_7);
                ancType[1] = "ANC -7";
                return ancType;
            case 8:
                ancType[0]=HnppApplication.appContext.getString(R.string.pregnancy_service_8);
                ancType[1] = "ANC -8";
                return ancType;
            default:
                ancType[0]=HnppApplication.appContext.getString(R.string.pregnancy_service_8_plus);
                ancType[1] = "ANC -8+";
                return ancType;
        }
    }
    public static String getTodayDate(){
        return DateTime.now().toString("dd-MM-yyyy");
    }
    public static String getToday(){
        return DateTime.now().toString("yyyy-MM-dd");
    }
    public static boolean isMissedSchedule(String dueVaccineDate){

        return !TextUtils.isEmpty(dueVaccineDate) && new LocalDate(dueVaccineDate).isBefore(new LocalDate(System.currentTimeMillis()));
    }
    @SuppressLint("SimpleDateFormat")
    public static boolean isMissedScheduleDDMMYYYY(String dueDate){
        Log.v("FILTER_LIST","isMissedScheduleDDMMYYYY:before>>>dueDate:"+dueDate);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        Calendar c = Calendar.getInstance();
        try {
            c.setTime(Objects.requireNonNull(sdf.parse(dueDate)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        dueDate = sdf1.format(c.getTime());
        Log.v("FILTER_LIST","isMissedScheduleDDMMYYYY:after>>>dueDate:"+dueDate);
        return !TextUtils.isEmpty(dueDate) && new LocalDate(dueDate).isBefore(new LocalDate(System.currentTimeMillis()));
    }
    public static String getYesterDay(){
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.minusDays(1);
        return DateTimeFormat.forPattern("yyyy-MM-dd").print(tomorrow);
    }
    public static String getTomorrowDay() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        return DateTimeFormat.forPattern("yyyy-MM-dd").print(tomorrow);
    }
    public static String[] getThisWeekDay(){
        String[] strings = new String[2];
        LocalDate today = LocalDate.now();
        LocalDate saterday = today;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            while (saterday.getDayOfWeek()!= DayOfWeek.SATURDAY.getValue()){
                saterday = saterday.minusDays(1);
                strings[0] = DateTimeFormat.forPattern("yyyy-MM-dd").print(saterday);
            }
        }
        LocalDate friday = today;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            while (friday.getDayOfWeek()!= DayOfWeek.FRIDAY.getValue()){
                friday = friday.plusDays(1);
                strings[1] = DateTimeFormat.forPattern("yyyy-MM-dd").print(friday);
            }
        }
        return strings;
    }

    public static String[] getLastWeekDay(){
        String[] strings = new String[2];
        LocalDate today = LocalDate.now().minusDays(7);
        LocalDate saterday = today;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            while (saterday.getDayOfWeek()!= DayOfWeek.SATURDAY.getValue()){
                saterday = saterday.minusDays(1);
                strings[0] = DateTimeFormat.forPattern("yyyy-MM-dd").print(saterday);
            }
        }
        LocalDate friday = today;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            while (friday.getDayOfWeek()!= DayOfWeek.FRIDAY.getValue()){
                friday = friday.plusDays(1);
                strings[1] = DateTimeFormat.forPattern("yyyy-MM-dd").print(friday);
            }
        }
        return strings;
    }

    public static String[] geNextWeekDay(){
        String[] strings = new String[2];
        LocalDate today = LocalDate.now().plusDays(7);
        LocalDate saterday = today;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            while (saterday.getDayOfWeek()!= DayOfWeek.SATURDAY.getValue()){
                saterday = saterday.minusDays(1);
                strings[0] = DateTimeFormat.forPattern("yyyy-MM-dd").print(saterday);
            }
        }
        LocalDate friday = today;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            while (friday.getDayOfWeek()!= DayOfWeek.FRIDAY.getValue()){
                friday = friday.plusDays(1);
                strings[1] = DateTimeFormat.forPattern("yyyy-MM-dd").print(friday);
            }
        }
        return strings;
    }

    public static String[] getThisMonth(){
        String[] months = new String[2];
        LocalDate startMonth = new LocalDate().withDayOfMonth(1);
        months[0] = DateTimeFormat.forPattern("yyyy-MM-dd").print(startMonth);
        LocalDate endMonth = new LocalDate().plusMonths(1).withDayOfMonth(1).minusDays(1);
        months[1] = DateTimeFormat.forPattern("yyyy-MM-dd").print(endMonth);
        return months;
    }

    public static String[] getLastMonth(){
        String[] months = new String[2];
        LocalDate startMonth = new LocalDate().minusMonths(1).withDayOfMonth(1);
        months[0] = DateTimeFormat.forPattern("yyyy-MM-dd").print(startMonth);
        LocalDate endMonth = new LocalDate().withDayOfMonth(1).minusDays(1);
        months[1] = DateTimeFormat.forPattern("yyyy-MM-dd").print(endMonth);
        return months;
    }

    public static String[] getNextMonth(){
        String[] months = new String[2];
        LocalDate startMonth = new LocalDate().plusMonths(1).withDayOfMonth(1);
        months[0] = DateTimeFormat.forPattern("yyyy-MM-dd").print(startMonth);
        LocalDate endMonth = startMonth.plusMonths(1).withDayOfMonth(1).minusDays(1);
        months[1] = DateTimeFormat.forPattern("yyyy-MM-dd").print(endMonth);
        return months;
    }

    public static boolean isWrongDate(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        if(year<2018) return true;
        return false;
    }

    public static String getHomeVisitStatus(long lastHomeVisit , String dateCreatedStr){

        LocalDate lastVisitDate = new LocalDate(lastHomeVisit);

        LocalDate dateCreated = new LocalDate(TextUtils.isEmpty(dateCreatedStr) ? System.currentTimeMillis(): new DateTime(dateCreatedStr).toLocalDate());

        LocalDate todayDate = new LocalDate();
        int monthDiff = getMonthsDifference((lastHomeVisit != 0 ? lastVisitDate : dateCreated), todayDate);
        if(monthDiff > 7) return HomeVisitType.BROWN.name();
        if(monthDiff > 5) return HomeVisitType.RED.name();
        if(monthDiff > 3) return HomeVisitType.YELLOW.name();
        return HomeVisitType.GREEN.name();

    }
    public static int getMonthsDifference(LocalDate date1, LocalDate date2) {
        return Months.monthsBetween(
                date1.withDayOfMonth(1),
                date2.withDayOfMonth(1)).getMonths();
    }

    public static boolean isExistSpecialCharacter(String filters) {
        if (!TextUtils.isEmpty(filters) && filters.contains("/")) {
            return true;
        }
        return false;
    }

    public static void updateAppBackground(View view) {
        if (!isReleaseBuild()) {
            view.setBackgroundColor(Color.parseColor("#B53737"));
        }
    }

    public static void updateAppBackgroundOnResume(View view) {
        if (!isReleaseBuild()) {
            view.setBackgroundColor(Color.parseColor("#B53737"));
        } else {
            view.setBackgroundColor(Color.parseColor("#F6F6F6"));
        }
    }

    public static ArrayList<String> getClasterSpinnerArray() {

        return new ArrayList<>(getClasterNames().keySet());
    }

    public static String getClusterNameFromValue(String value) {
        HashMap<String, String> keys = getClasterNames();
        for (String key : keys.keySet()) {
            if (keys.get(key).equalsIgnoreCase(value)) {
                return key;
            }
        }
        return "";
    }

    public static HashMap<String, String> getClasterNames() {
        LinkedHashMap<String, String> clusterArray = new LinkedHashMap<>();
        clusterArray.put("ক্লাস্টার ১", "1st_Cluster");
        clusterArray.put("ক্লাস্টার ২", "2nd_Cluster");
        clusterArray.put("ক্লাস্টার ৩", "3rd_Cluster");
        clusterArray.put("ক্লাস্টার ৪", "4th_Cluster");
        return clusterArray;
    }

    public static final class DrawerMenu {
        public static final String ELCO_CLIENT = "Elco Clients";
        public static final String ALL_MEMBER = "All member";
    }

    public static final class FORM_KEY {
        public static final String SS_INDEX = "ss_index";
        public static final String VILLAGE_INDEX = "village_index";
    }
    public static String getGender(String value){
        if(value.equalsIgnoreCase("F")){
            return HnppApplication.appContext.getString(R.string.woman);
        }
        if(value.equalsIgnoreCase("M")){
            return HnppApplication.appContext.getString(R.string.man);
        }
        return value;
    }

    public static String getTotalCount(int count) {
        String local = LangUtils.getLanguage(HnppApplication.getInstance().getApplicationContext());

        if(local.equals("bn")){
            char[] bn_numbers = "০১২৩৪৫৬৭৮৯".toCharArray();
            String c = String.valueOf(count);
            String number_to_return = "";
            for (char ch : c.toCharArray()) {

                number_to_return += bn_numbers[Integer.valueOf(ch) % Integer.valueOf('0')];
            }
            return number_to_return;
        }else {
            return  String.valueOf(count);
        }

    }
    public static boolean isPALogin(){
        String role = org.smartregister.Context.getInstance().allSharedPreferences().fetchRegisteredRole();
        if(TextUtils.isEmpty(role)) return false;
        return role.equalsIgnoreCase("PA");

    }
    public static boolean isVaccinator(){
        String role = org.smartregister.Context.getInstance().allSharedPreferences().fetchRegisteredRole();
        if(TextUtils.isEmpty(role)) return false;
        return role.equalsIgnoreCase("vaccinator");

    }
    public static boolean isUrbanUser(){
       String isUrban =  CoreLibrary.getInstance().context().allSharedPreferences().getPreference(IS_URBAN);
        return isUrban.equalsIgnoreCase("true");
    }
    public static boolean isDisabilityEnable(){
        return true;
//        String isUrban =  CoreLibrary.getInstance().context().allSharedPreferences().getPreference(DISABILITY_ENABLE);
//        return isUrban.equalsIgnoreCase("true");
    }
    public static boolean isReleaseBuild() {
//        if(BuildConfig.IS_TRAINING){
//            return false;
//        }
//        if(BuildConfig.DEBUG){
//            return false;
//        }
        return true;
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        String deviceId = "";
        try{
            deviceId = Settings.Secure.getString(context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        }catch (SecurityException se){
            se.printStackTrace();
        }

        return deviceId;
    }
    public static boolean isDeviceVerified(){
        AllSharedPreferences preferences = Utils.getAllSharedPreferences();
        String isDeviceVerif = preferences.getPreference(IS_DEVICE_VERIFY);
        if(!TextUtils.isEmpty(isDeviceVerif) && isDeviceVerif.equalsIgnoreCase("V")){
            return true;
        }
        return false;
    }
    public static String getDeviceImeiFromSharedPref(){
        String imei = Utils.getAllSharedPreferences().getPreference(DEVICE_IMEI);
        return TextUtils.isEmpty(imei)?"testimei":imei;
    }
    public static void updateLiveTest(String appMode){
        AllSharedPreferences preferences = Utils.getAllSharedPreferences();
        preferences.savePreference(IS_RELEASE,appMode);
    }
    public static void updateDeviceVerified(boolean isVerify, String deviceImei){
        AllSharedPreferences preferences = Utils.getAllSharedPreferences();
        preferences.savePreference(IS_DEVICE_VERIFY,isVerify?"V":"");
        preferences.savePreference(DEVICE_IMEI,deviceImei);
    }
    public static String getSimPrintsProjectId(){

        return isReleaseBuild()?BuildConfig.SIMPRINT_PROJECT_ID_RELEASE:BuildConfig.SIMPRINT_PROJECT_ID_TRAINING;
    }
    public static final class KEY {
        public static final String IS_URBAN = "is_urban";
        public static final String DISABILITY_ENABLE = "disability_enable";
        public static final String USER_ID = "user_id_location";
        public static final String TOTAL_MEMBER = "member_count";
        public static final String VILLAGE_NAME = "village_name";
        public static final String CLASTER = "claster";
        public static final String MODULE_ID = "module_id";
        public static final String IS_COMES_FROM_MIGRATION = "from_migration";
        public static final String RELATION_WITH_HOUSEHOLD = "relation_with_household_head";
        public static final String GU_ID = "gu_id";
        public static final String MARITAL_STATUS = "marital_status";
        public static final String HOUSE_HOLD_ID = "house_hold_id";
        public static final String HOUSE_HOLD_NAME = "house_hold_name";
        public static final String BLOCK_NAME = "block_name";
        public static final String WARD_NAME = "ward_name";
        public static final String WARD_ID = "ward_id";
        public static final String BLOCK_ID = "block_id";
        public static final String IS_RISK = "is_risk";
        public static final String NEXT_VISIT_DATE = "next_visit_date";
        public static final String EDD = "edd";
        public static final String SERIAL_NO = "serial_no";
        public static final String DELIVERY_DATE = "delivery_date";
        public static final String CHILD_MOTHER_NAME_REGISTERED = "mother_name_bangla";
        public static final String FATHER_NAME_ENGLISH = "father_name_english";
        public static final String FATHER_NAME_BANGLA = "father_name_bangla";
        public static final String CHILD_MOTHER_NAME = "mother_name_english";
        public static final String GENDER = "gender";
        public static final String NATIONAL_ID = "national_id";
        public static final String BIRTH_ID = "birth_id";
        public static final String IS_BITHDAY_KNOWN = "is_birthday_known";
        public static final String BLOOD_GROUP = "blood_group";
        public static final String SHR_ID = "shr_id";
        public static final String MOTHER_ID = "mother_id";
        public static final String LAST_HOME_VISIT = "last_home_visit";
        public static final String DATE_CREATED = "date_created";
        public static final String BIRTH_WEIGHT = "birth_weight";
        public static final String WEIGHT_STATUS = "weight_status";
        public static final String HEIGHT_STATUS = "height_status";
        public static final String MUAC_STATUS = "muac_status";
        public static final String LAST_VACCINE_NAME = "last_vaccine_name";
        public static final String LAST_VACCINE_DATE = "last_vaccine_date";
        public static final String LAST_SYNC_HPV = "last_sync_hpv";
        public static final String NEW_BORN_INFO = "new_born_info";
        public static final String CHILD_MUAC = "child_muac";
        public static final String CHILD_HEIGHT = "child_height";
        public static final String CHILD_WEIGHT = "child_weight";
        public static final String CHILD_STATUS = "child_status";
        public static final String DUE_VACCINE_NAME = "due_vaccine_name";
        public static final String DUE_VACCINE_DATE = "due_vaccine_date";
        public static final String DUE_VACCINE_WEEK = "due_vaccine_week";
        public static final String HAS_AEFI = "has_aefi";
        public static final String AEFI_VACCINE = "aefi_vaccines";
    }

    public static class IDENTIFIER {
        public static final String FAMILY_TEXT = "Family";

        public IDENTIFIER() {
        }
    }

    public static String getRelationWithHouseholdHead(String value){
        String relationshipObject = "{" +
                "  \"খানা প্রধান\": \"Household Head\"," +
                "  \"মা/আম্মা\": \"Mother\"," +
                "  \"বাবা/আব্বা\": \"Father\"," +
                "  \"পুত্র\": \"Son\"," +
                "  \"কন্যা\": \"Daughter\"," +
                "  \"পুত্রবধূ\": \"Daughter-in-law\"," +
                "  \"নাতি\": \"Grandson\"," +
                "  \"নাতনি\": \"Grand-Daughter\"," +
                "  \"স্ত্রী\": \"Wife\"," +
                "  \"স্বামী\": \"Husband\"," +
                "  \"পিতা\": \"Father\"," +
                "  \"মাতা\": \"Mother\"," +
                "  \"ভাই\": \"Brother\"," +
                "  \"বোন\": \"Brother\"," +
                "  \"ভাইপো\": \"Nephew\"," +
                "  \"ভাইঝি\": \"niece\"," +
                "  \"ভাগ্নে\": \"Nephew\"," +
                "  \"ভাগ্নি\": \"niece\"," +
                "  \"শ্যালক\": \"brother-in-law\"," +
                "  \"শ্যালিকা\": \"sister-in-law\"," +
                "  \"নাতি\": \"Grandson\"," +
                "  \"নাতনী\": \"GrandDaughter\"," +
                "  \"ছেলের বউ\": \"SonsWife\"," +
                "  \"মেয়ের স্বামী\": \"DaughtersHusband\"," +
                "  \"শ্বশুর\": \"Father in law\"," +
                "  \"শাশুড়ি\": \"Mother in law\"," +
                "  \"দাদা\": \"Grandpa\"," +
                "  \"দাদি\": \"Grandma\"," +
                "  \"নানা\": \"Grandfather\"," +
                "  \"নানী\": \"Grandmother\"," +
                "  \"দেবর\": \"brother-in-law\"," +
                "  \"জা\": \"sister-in-law\"," +
                "  \"ননদ\": \"Sister-in-law\"," +
                "  \"ভাইয়ের স্ত্রী\": \"Brother's wife\"," +
                "  \"ভগ্নিপতি\": \"sister-in-law\"," +
                "  \"জামাতা\": \"Son-in-law\"," +
                "  \"অতিথি\": \"Guest\"," +
                "  \"অন্যান্য\": \"Others\"," +
                "  \"অন্যান্য আত্মীয়\": \"other relatives\"," +
                "  \"অন্যান্য অনাত্মীয়\": \"other non-relative\"" +
                "}";
        return getKeyByValue(relationshipObject,value);
    }
    public class JSON_FORMS {
        public static final String  ANC_CARD_FORM = "anc_card_form";
        public static final String  IMMUNIZATION = "hv_immunization";
        public static final String  DANGER_SIGNS = "anc_hv_danger_signs";

        public static final String  ANC_FORM = "anc_registration";
        public static final String  ANC_VISIT_FORM = "anc_home_visit";
        public static final String  ANC_VISIT_FORM_FACILITY = "anc_home_visit_facility";
        public static final String  ANC_VISIT_FORM_OOC = "hnpp_anc_home_visit_ooc";
//        public static final String  GENERAL_DISEASE = "hnpp_anc_general_disease";
//        public static final String  PREGNANCY_HISTORY = "hnpp_anc_pregnancy_history";
        public static final String  PREGNANCY_OUTCOME = "anc_pregnancy_outcome";
        public static final String  PREGNANCY_OUTCOME_OOC = "hnpp_anc_pregnancy_outcome_ooc";
        public static final String  MEMBER_REFERRAL = "hnpp_member_referral";
        public static final String  MEMBER_REFERRAL_PA = "hnpp_member_referral_pa";
        public static final String  WOMEN_REFERRAL = "hnpp_women_referral";
        public static final String  CHILD_REFERRAL = "hnpp_child_referral";
        public static final String  ELCO = "elco_register";
        public static final String  PNC_FORM = "pnc_home_visit";
        public static final String  PNC_FORM_OOC = "hnpp_pnc_registration_ooc";
        public static final String  WOMEN_PACKAGE = "hnpp_women_package";
        public static final String  EYE_TEST = "eye_test";
        public static final String  BLOOD_TEST = "blood_test";
        public static final String  GIRL_PACKAGE = "hnpp_adolescent_package";
        public static final String  NCD_PACKAGE = "hnpp_ncd_package";
        public static final String  IYCF_PACKAGE = "hnpp_iycf_package";
        public static final String ENC_REGISTRATION = "hnpp_enc_child";
        public static final String HOME_VISIT_FAMILY = "hnpp_hh_visit";
        public static final String CHILD_PROFILE_VISIT= "child_profile_visit";
        public static final String MEMBER_PROFILE_VISIT= "member_profile_visit";
        public static final String REFERREL_FOLLOWUP = "hnpp_member_referral_followup";
        public static final String GMP_REFERREL_FOLLOWUP = "gmp_referral_followup";
        public static final String GMP_SESSION_INFO = "gmp_session_info";
        public static final String CHILD_FOLLOWUP = "child_followup";
        public static final String NEW_BORN_PNC_1_4 = "new_born_pnc_1_4";
        public static final String KMC_SERVICE_HOME = "kmc_service_home";
        public static final String KMC_HOME_FOLLOWUP = "kmc_home_followup";
        public static final String KMC_SERVICE_HOSPITAL = "kmc_service_hospital";
        public static final String KMC_HOSPITAL_FOLLOWUP = "kmc_hospital_followup";
        public static final String SCANU_FOLLOWUP = "scanu_service";
        public static final String AEFI_CHILD_ = "aefi_child";
        public static final String CHILD_DISEASE = "child_general_disease_survey";
        public static final String MEMBER_DISEASE = "general_disease_survey";
        public static final String CHILD_ECCD_2_3_MONTH = "child_eccd_2_3_months";
        public static final String CHILD_ECCD_4_6_MONTH = "child_eccd_4_6_months";
        public static final String CHILD_ECCD_7_9_MONTH = "child_eccd_7_9_months";
        public static final String CHILD_ECCD_10_12_MONTH = "child_eccd_10_12_months";
        public static final String CHILD_ECCD_18_MONTH = "child_eccd_18_months";
        public static final String CHILD_ECCD_24_MONTH = "child_eccd_24_months";
        public static final String CHILD_ECCD_36_MONTH = "child_eccd_36_months";
        public static final String CHILD_INFO_EBF12 = "child_info_ebf12";
        public static final String CHILD_INFO_7_24_MONTHS = "child_info_7_24_months";
        public static final String CHILD_INFO_25_MONTHS = "child_info_25_months";
        public static final String CORONA_INDIVIDUAL = "corona_individual";
        public static final String SS_FORM = "ss_form";
        public static final String GUEST_MEMBER_FORM = "guest_member_register";
        public static final String GUEST_MEMBER_DETAILS_FORM = "guest_member_details_register";

    }
    public class INDICATOR {
        public static final String FP_uses = "fp_user";
        public static final String FP_no_method_uses = "fp_no_method_user";
        public static final String ANC_OTHER_SOURCE = "anc_other_source";
        public static final String ANC_TT = "anc_tt";
        public static final String OUTCOME_TT = "is_tt_completed";
        public static final String FEEDING_UPTO_6_MONTH = "feeding_6_month";
        public static final String PP_PILL = "contraceptive_pill";
        public static final String PP_CONDOM = "condom";
        public static final String PP_IUD = "iud";
        public static final String PP_INJECTION = "injection";
        public static final String PP_Implant = "norplant";
        public static final String PP_Vesectomy = "vasectomy";
        public static final String PP_Tubectomy = "ligation";


    }

    public class EVENT_TYPE{
        public static final String ELCO = "ELCO Registration";
        public static final String MEMBER_REFERRAL = "Member Referral";
        public static final String WOMEN_REFERRAL = "Women Referral";
        public static final String CHILD_REFERRAL = "Child Referral";

//        public static final String ANC_PREGNANCY_HISTORY = "ANC Pregnancy History";
//        public static final String ANC_GENERAL_DISEASE = "ANC General Disease";
        public static final String ANC_HOME_VISIT= "ANC Home Visit";
        public static final String ANC_HOME_VISIT_FACILITY= "Facility_AncService";
        public static final String ANC_HOME_VISIT_OOC= "ANC Home Visit OOC";
        public static final String ANC_REGISTRATION = "ANC Registration";
        public static final String UPDATE_ANC_REGISTRATION = "Update ANC Registration";
        public static final String PNC_REGISTRATION = "PNC Home Visit";
        public static final String PNC_REGISTRATION_OOC = "PNC Home Visit OOC";
        public static final String WOMEN_PACKAGE = "Women package";
        public static final String GIRL_PACKAGE = "Adolescent package";
        public static final String NCD_PACKAGE = "NCD package";//pa
        public static final String EYE_TEST = "Eye test";//pa
        public static final String BLOOD_GROUP = "Blood group";//pa
        public static final String IYCF_PACKAGE = "IYCF package";
        public static final String ENC_REGISTRATION = "ENC Registration";
        public static final String HOME_VISIT_FAMILY = "HH visit";
        public static final String CHILD_PROFILE_VISIT = "Child profile visit";
        public static final String MEMBER_PROFILE_VISIT = "Member profile visit";
        public static final String VACCINATION = "Vaccination";
        public static final String SERVICES = "Recurring Service";
        public static final String PREGNANCY_OUTCOME = "Pregnancy Outcome";
        public static final String PREGNANCY_OUTCOME_OOC = "OOC Pregnancy Outcome";
        public static final String REFERREL_FOLLOWUP = "Member Referral Followup";
        public static final String GMP_REFERRAL = "Referral Clinic";
        public static final String GMP_COUNSELING = "GMP Counseling";
        public static final String GMP_REFERREL_FOLLOWUP = "GMP Referral Followup";
        public static final String GMP_SESSION_INFO = "GMP Session Info";
        public static final String CHILD_ECCD_2_3_MONTH = "Child ECCD Followup 2-3 months";
        public static final String CHILD_ECCD_4_6_MONTH = "Child ECCD Followup 4-6 months";
        public static final String CHILD_ECCD_7_9_MONTH = "Child ECCD Followup 7-9 months";
        public static final String CHILD_ECCD_10_12_MONTH = "Child ECCD Followup 10-12 months";
        public static final String CHILD_ECCD_18_MONTH = "Child ECCD Followup 18 months";
        public static final String CHILD_ECCD_24_MONTH = "Child ECCD Followup 24 months";
        public static final String CHILD_ECCD_36_MONTH = "Child ECCD Followup 36 months";
        public static final String CHILD_INFO_EBF12 = "Child Info EBF 1_2";
        public static final String CHILD_INFO_7_24_MONTHS = "Child Info 7-24 months";
        public static final String CHILD_INFO_25_MONTHS = "Child Info 25 Months";
        public static final String CHILD_FOLLOWUP = "Child Followup";
        public static final String NEW_BORN_PNC_1_4 = "New Born PNC 1-4";
        public static final String KMC_SERVICE_HOME = "KMC Service Home";
        public static final String KMC_HOME_FOLLOWUP = "KMC Home Followup";
        public static final String KMC_SERVICE_HOSPITAL = "KMC Service Hospital";
        public static final String KMC_HOSPITAL_FOLLOWUP = "KMC Hospital Followup";
        public static final String SCANU_FOLLOWUP = "SCANU Followup";
        public static final String AEFI_CHILD = "AEFI Child";
        public static final String CHILD_DISEASE = "Child General Disease Survey";
        public static final String MEMBER_DISEASE = "General Disease Survey";
        public static final String PNC_CHILD_REGISTRATION = "PNC Child Registration";
        public static final String UPDATE_CHILD_REGISTRATION = "Update Child Registration";
        public static final String FORUM_CHILD = "Child Forum";
        public static final String FORUM_ADO = "Adolescent Forum";
        public static final String FORUM_WOMEN = "WOMEN Forum";
        public static final String FORUM_NCD = "NCD Forum";
        public static final String FORUM_ADULT = "ADULT Forum";
        public static final String CORONA_INDIVIDUAL = "corona individual";
        public static final String SS_INFO = "SS Form";
        //for target
        public static final String METHOD_USER = "Methods Users";
        public static final String ADO_METHOD_USER = "Adolescent Methods Users";
        public static final String PREGNANCY_IDENTIFIED = "Pregnancy Identified";
        public static final String INSTITUTIONALIZES_DELIVERY = "Institutionalized Delivery";
        public static final String PREGNANCY_VISIT= "Pregnant Visit";
        public static final String CHILD_VISIT_0_6= "Child Visit(0-6 months)";
        public static final String CHILD_VISIT_7_24= "Child Visit(7-24 months)";
        public static final String CHILD_VISIT_18_36= "Child Visit(18-36 months)";
        public static final String CHILD_IMMUNIZATION_0_59= "Immunization(0-59 months)";
        public static final String AVG_ATTEND_ADO_FORUM= "Avg. Attendance (Adolescent Forum)";
        public static final String AVG_ATTEND_NCD_FORUM= "Avg. Attendance (NCD Forum)";
        public static final String AVG_ATTEND_ADULT_FORUM= "Avg Attendance (Adult Forum)";
        public static final String AVG_ATTEND_IYCF_FORUM= "Avg Attendance (IYCF Forum)";
        public static final String AVG_ATTEND_WOMEN_FORUM= "Avg Attendance (Women Forum)";

        //for PA target
        public static final String ADULT_FORUM_ATTENDANCE = "Avg. Attendance (Adult Forum)";
        public static final String ADULT_FORUM_SERVICE_TAKEN = "Adult Forum Service Taken";
        public static final String MARKED_PRESBYOPIA = "Marked as presbyopia";
        public static final String PRESBYOPIA_CORRECTION = "Presbyopia correction";
        public static final String ESTIMATE_DIABETES = "Estimate diabetes";
        public static final String ESTIMATE_HBP = "Estimate HBP";
        public static final String CATARACT_SURGERY_REFER = "Cataract surgery refer";
        public static final String CATARACT_SURGERY = "Cataract surgery";
        //public static final String NCD_BY_PA = "NCD by PA";

        //PA stock

        public static final String GLASS = "glass";
        public static final String SUN_GLASS = "Sun glass";
        public static final String SV_1 = "Sv 1";
        public static final String SV_1_5 = "Sv 1.5";
        public static final String SV_2 = "Sv 2";
        public static final String SV_2_5 = "Sv 2.5";
        public static final String SV_3 = "Sv 3";
        public static final String BF_1 = "Bf 1";
        public static final String BF_1_5 = "Bf 1.5";
        public static final String BF_2 = "Bf 2";
        public static final String BF_2_5 = "Bf 2.5";
        public static final String BF_3 = "Bf 3";

        //service
        public static final String ANC_SERVICE = "ANC Service";
        public static final String PNC_SERVICE = "PNC Service";
        public static final String ANC_PACKAGE = "ANC package";
        public static final String PNC_PACKAGE = "PNC package";
        public static final String GUEST_MEMBER_REGISTRATION = "OOC Member Registration";
        public static final String GUEST_MEMBER_UPDATE_REGISTRATION = "OOC Member Update Registration";
        public static final String FAMILY_MEMBER_REGISTRATION = "Family Member Registration";
        public static final String CHILD_REGISTRATION = "Child Registration";
    }
    public static final class SURVEY_KEY{
        public static final String USER_NAME = "user_name";
        public static final String USER_PASSWORD = "password_string";
        public static final String USER_FIRST_NAME = "first_name";
        public static String HH_TYPE = "hh";
        public static String MM_TYPE = "mm";
        public static String VIEW_MODE = "view";
        public static String TYPE_KEY = "type";
        public static String CHILD_TYPE = "child";
        public static String DATA = "data";
        public static String LAST_SYNC_TIME = "last_sync_time";
        public static String SK_LOCATION = "sklocation";
        public static String PACKAGE_NAME = "org.smartregister.unicef.dghs.survey";
        public static final String SURVEY_REQUEST_ACTION = "org.smartregister.unicef.dghs.survey.SURVEY_REQUEST";
        public static final String VIEW_REQUEST_ACTION = "org.smartregister.unicef.dghs.survey.VIEW_REQUEST";


    }
    public static JSONArray CAMP_TYPE_JSON_ARR
            = new JSONArray()
            .put( "Outreach 1")
            .put( "Outreach 2")
            .put( "Outreach 3")
            .put( "Outreach 4")
            .put( "Outreach 5")
            .put( "Outreach 6")
            .put( "Outreach 7")
            .put( "Outreach 8");
    public static JSONObject viewSurveyForm(String type,String formId, String uuid,String baseEntityId){
        JSONObject hhObject;
        try{
            Map<String,String> hhData = new HashMap<>();
            hhData.put("form_id",formId);
            hhData.put("type",type);
            hhData.put("uuid",uuid);
            hhData.put("base_entity_id",baseEntityId);
            hhObject = new JSONObject(hhData);
            Log.v("passToSurveyApp","populateMemberData>>"+hhObject);
            return hhObject;
        }catch (Exception e){

        }
        return null;
    }
    public static long getLongDateFormatForFromMonth(String year,String month){
        String dateFormate = year+"-"+HnppConstants.addZeroForMonth(month)+"-01";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
        long startDate = System.currentTimeMillis();
        try{
            Date date = format.parse(dateFormate);
            startDate = date.getTime()+SIX_HOUR;
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.v("LAST_BALANCE_STOCK","getLongDateFormatForFromMonth>>dateFormate:"+dateFormate+":startDate:"+startDate+":minus:"+(startDate-SIX_HOUR)+"");
        return startDate;
    }
    public static long getLongDateFormatForStock(String year,String month){
        String dateFormate = year+"-"+HnppConstants.addZeroForMonth(month)+"-01";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
        long startDate = System.currentTimeMillis();
        try{
            Date date = format.parse(dateFormate);
            startDate = date.getTime();
        }catch (Exception e){
            e.printStackTrace();
        }
        return startDate;
    }
    public static String getStringDateFormatForFromMonth(String year, String month){
        return  year+"-"+HnppConstants.addZeroForMonth(month)+"-01";
    }
    public static String getStringDateFormatForToMonth(String year, String month){
        return year+"-"+HnppConstants.addZeroForMonth(month)+"-"+getLastDateOfAMonth(month);
    }
    public static long getLongDateFormatForToMonth(String year,String month){
        String dateFormate = year+"-"+HnppConstants.addZeroForMonth(month)+"-"+getLastDateOfAMonth(month);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
        long startDate = System.currentTimeMillis();
        try{
            Date date = format.parse(dateFormate);
            startDate = date.getTime()+SIX_HOUR;
        }catch (Exception e){
            e.printStackTrace();
        }
        return startDate;
    }
    public static String getLastDateOfAMonth(String month){
        if (TextUtils.isEmpty(month)) return "";
        int m = Integer.parseInt(month);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH,m-1);
        int lastDate = c.getActualMaximum(Calendar.DATE);
        return HnppConstants.addZeroForMonth(lastDate+"");
    }
    public static long getLongDateFormate(String year,String month,String day){
        String dateFormate = year+"-"+HnppConstants.addZeroForMonth(month)+"-"+HnppConstants.addZeroForDay(day);

        Log.v("DAILY_TERGET","dateStr:"+dateFormate);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
        long startDate = System.currentTimeMillis();
        try{
            Date date = format.parse(dateFormate);
            startDate = date.getTime();
        }catch (Exception e){
            e.printStackTrace();
        }
        return startDate;
    }
    public static String getStringFormatedDate(String year,String month,String day){
        return   year+"-"+HnppConstants.addZeroForMonth(month)+"-"+HnppConstants.addZeroForDay(day);
    }
    @SuppressLint("SimpleDateFormat")
    public static long getLongFromDateFormat(String dateTime){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        long milliseconds =0;
        try{
            Date d = format.parse(dateTime);
            milliseconds  = d.getTime();
        }catch (Exception e){
            e.printStackTrace();
        }
        return milliseconds;
    }
    @SuppressLint("SimpleDateFormat")
    public static long getLongFromWeightDateFormat(String dateTime){
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        long milliseconds =0;
        try{
            Date d = format.parse(dateTime);
            milliseconds  = d.getTime();
        }catch (Exception e){
            e.printStackTrace();
        }
        return milliseconds;
    }
    @SuppressLint("SimpleDateFormat")
    public static String getDateFormateFromLong(long dateTime){
        Date date = new Date(dateTime);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = null;
        try{
            dateString = format.format(date);
        }catch (Exception e){
            e.printStackTrace();
        }
        return dateString;
    }
    @SuppressLint("SimpleDateFormat")
    public static String getDateWithHHMMFormateFromLong(long dateTime){
        Date date = new Date(dateTime);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        String dateString = null;
        try{
            dateString = format.format(date);
        }catch (Exception e){
            e.printStackTrace();
        }
        return dateString;
    }
    @SuppressLint("SimpleDateFormat")
    public static String getDDMMYYYYFormateFromLong(long dateTime){
        Date date = new Date(dateTime);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        String dateString = null;
        try{
            dateString = format.format(date);
        }catch (Exception e){
            e.printStackTrace();
        }
        return dateString;
    }
    public static final Map<String,String> genderMapping = ImmutableMap.<String,String> builder()
            .put("মহিলা","F")
            .put("পুরুষ","M")
            .put("অন্যান্য","O")
            .build();
    public static Map<String,String> getVaccineNameMapping(){
        Map<String,String> vaccineNameMapping = ImmutableMap.<String,String> builder()
                .put("bcg", HnppApplication.appContext.getString(R.string.bcg))
                .put("opv_0", HnppApplication.appContext.getString(R.string.polio_0))
                .put("opv_1",HnppApplication.appContext.getString(R.string.polio_1))
                .put("pcv_1",HnppApplication.appContext.getString(R.string.pcv_1))
                .put("penta_1",HnppApplication.appContext.getString(R.string.penta_1))
                .put("opv_2",HnppApplication.appContext.getString(R.string.polio_2))
                .put("pcv_2",HnppApplication.appContext.getString(R.string.pcv_2))
                .put("penta_2",HnppApplication.appContext.getString(R.string.penta_2))
                .put("opv_3",HnppApplication.appContext.getString(R.string.polio_3))
                .put("ipv",HnppApplication.appContext.getString(R.string.ipv))
                .put("pcv_3",HnppApplication.appContext.getString(R.string.pcv_3))
                .put("penta_3","পেন্টা-৩")
                .put("mr_1",HnppApplication.appContext.getString(R.string.mr_1))
                .put("mr_2",HnppApplication.appContext.getString(R.string.mr_2))
                .put("vitamin_a1",HnppApplication.appContext.getString(R.string.vitamin))
                .put("fipv_1",HnppApplication.appContext.getString(R.string.fipv_1))
                .put("fipv_2",HnppApplication.appContext.getString(R.string.fipv_2))
                .build();
        return vaccineNameMapping;
    }
    public static final Map<String,String> eventTypeFormNameMapping = ImmutableMap.<String,String> builder()
            .put(EVENT_TYPE.ANC_REGISTRATION,JSON_FORMS.ANC_FORM)
            .put(EVENT_TYPE.ANC_HOME_VISIT,JSON_FORMS.ANC_VISIT_FORM)
            .put(EVENT_TYPE.ANC_HOME_VISIT_FACILITY,JSON_FORMS.ANC_VISIT_FORM_FACILITY)
            .put(EVENT_TYPE.ELCO,JSON_FORMS.ELCO)
            .put(EVENT_TYPE.PNC_REGISTRATION,JSON_FORMS.PNC_FORM)
            .put(EVENT_TYPE.PNC_REGISTRATION_OOC,JSON_FORMS.PNC_FORM_OOC)
            .put(EVENT_TYPE.CHILD_ECCD_2_3_MONTH,JSON_FORMS.CHILD_ECCD_2_3_MONTH)
            .put(EVENT_TYPE.CHILD_ECCD_4_6_MONTH,JSON_FORMS.CHILD_ECCD_4_6_MONTH)
            .put(EVENT_TYPE.CHILD_ECCD_7_9_MONTH,JSON_FORMS.CHILD_ECCD_7_9_MONTH)
            .put(EVENT_TYPE.CHILD_ECCD_10_12_MONTH,JSON_FORMS.CHILD_ECCD_10_12_MONTH)
            .put(EVENT_TYPE.CHILD_ECCD_18_MONTH,JSON_FORMS.CHILD_ECCD_18_MONTH)
            .put(EVENT_TYPE.CHILD_ECCD_24_MONTH,JSON_FORMS.CHILD_ECCD_24_MONTH)
            .put(EVENT_TYPE.CHILD_ECCD_36_MONTH,JSON_FORMS.CHILD_ECCD_36_MONTH)
            .put(EVENT_TYPE.CHILD_INFO_7_24_MONTHS,JSON_FORMS.CHILD_INFO_7_24_MONTHS)
            .put(EVENT_TYPE.CHILD_INFO_25_MONTHS,JSON_FORMS.CHILD_INFO_25_MONTHS)
            .build();
    public static final Map<String,String> formNameEventTypeMapping = ImmutableMap.<String,String> builder()
            .put(JSON_FORMS.ANC_VISIT_FORM_FACILITY,EVENT_TYPE.ANC_HOME_VISIT_FACILITY)
            .put(JSON_FORMS.PNC_FORM,EVENT_TYPE.PNC_REGISTRATION)
            .put(JSON_FORMS.PNC_FORM_OOC,EVENT_TYPE.PNC_REGISTRATION_OOC)
            .put(JSON_FORMS.NCD_PACKAGE,EVENT_TYPE.NCD_PACKAGE)
            .put(JSON_FORMS.IYCF_PACKAGE,EVENT_TYPE.IYCF_PACKAGE)
            .put(JSON_FORMS.WOMEN_PACKAGE,EVENT_TYPE.WOMEN_PACKAGE)
            .put(JSON_FORMS.GIRL_PACKAGE,EVENT_TYPE.GIRL_PACKAGE)
            .build();
    public static final Map<String,String> guestEventTypeFormNameMapping = ImmutableMap.<String,String> builder()
            .put(EVENT_TYPE.ANC_REGISTRATION,JSON_FORMS.ANC_FORM)
            .put(EVENT_TYPE.ANC_HOME_VISIT,JSON_FORMS.ANC_VISIT_FORM)
            .put(EVENT_TYPE.PNC_REGISTRATION,JSON_FORMS.PNC_FORM)

            .build();
    public static final Map<String,Integer> iconMapping = ImmutableMap.<String,Integer> builder()
            .put("গর্ভবতী পরিচর্যা-১ম ত্রিমাসিক",R.mipmap.ic_anc_pink)
            .put("গর্ভকালীন সেবা- ২য় ত্রিমাসিক",R.mipmap.ic_anc_pink)
            .put("গর্ভকালীন সেবা- ৩য় ত্রিমাসিক",R.mipmap.ic_anc_pink)
            .put("শারীরিক সমস্যা",R.mipmap.ic_anc_pink)
            .put( "পূর্বের গর্ভের ইতিহাস",R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.PNC_REGISTRATION,R.drawable.sidemenu_pnc)
            .put(EVENT_TYPE.PNC_REGISTRATION_OOC,R.drawable.sidemenu_pnc)
            .put(EVENT_TYPE.PREGNANCY_OUTCOME,R.drawable.sidemenu_pnc)
//            .put(EVENT_TYPE.ANC_GENERAL_DISEASE,R.mipmap.ic_anc_pink)
//            .put(EVENT_TYPE.ANC_PREGNANCY_HISTORY,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.ANC_REGISTRATION,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.UPDATE_ANC_REGISTRATION,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.ELCO,R.drawable.ic_elco)
            .put(EventType.REMOVE_FAMILY,R.drawable.ic_remove)
            .put(EventType.REMOVE_MEMBER,R.drawable.ic_remove)
            .put(HnppConstants.EventType.FAMILY_REGISTRATION,R.drawable.ic_home)
            .put(HnppConstants.EventType.FAMILY_MEMBER_REGISTRATION,R.drawable.rowavatar_member)
            .put(HnppConstants.EventType.UPDATE_FAMILY_MEMBER_REGISTRATION,R.drawable.rowavatar_member)
            .put(HnppConstants.EventType.CHILD_REGISTRATION,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.MEMBER_REFERRAL,R.mipmap.ic_refer)
            .put(EVENT_TYPE.WOMEN_REFERRAL,R.mipmap.ic_refer)
            .put(EVENT_TYPE.CHILD_REFERRAL,R.mipmap.ic_refer)
            .put(EVENT_TYPE.WOMEN_PACKAGE,R.drawable.ic_women)
            .put(EVENT_TYPE.GIRL_PACKAGE, R.drawable.ic_adolescent)
            .put(EVENT_TYPE.NCD_PACKAGE,R.drawable.ic_muac)
            .put(EVENT_TYPE.BLOOD_GROUP,R.drawable.ic_blood)
            .put(EVENT_TYPE.EYE_TEST,R.drawable.ic_eye)
            .put(EVENT_TYPE.IYCF_PACKAGE, R.drawable.child_girl_infant)
            .put(EVENT_TYPE.ANC_HOME_VISIT,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.ANC_HOME_VISIT_FACILITY,R.mipmap.ic_anc_pink)
            .put(EVENT_TYPE.ENC_REGISTRATION,R.mipmap.ic_child)
            .put("Member referral",R.mipmap.ic_refer)
            .put(EVENT_TYPE.HOME_VISIT_FAMILY, R.mipmap.ic_icon_home)
            .put(EVENT_TYPE.CHILD_PROFILE_VISIT, R.drawable.rowavatar_child)
            .put(EVENT_TYPE.MEMBER_PROFILE_VISIT, R.drawable.rowavatar_member)
            .put(EventType.CHILD_HOME_VISIT, R.mipmap.ic_icon_home)
            .put(EVENT_TYPE.VACCINATION, R.drawable.ic_muac)
            .put(EVENT_TYPE.SERVICES, R.mipmap.form_vitamin)
            .put(EVENT_TYPE.REFERREL_FOLLOWUP,R.mipmap.ic_refer)
            .put(EVENT_TYPE.CHILD_FOLLOWUP,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.NEW_BORN_PNC_1_4,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.KMC_SERVICE_HOME,R.mipmap.kmc_icon)
            .put(EVENT_TYPE.KMC_HOME_FOLLOWUP,R.mipmap.kmc_icon)
            .put(EVENT_TYPE.KMC_SERVICE_HOSPITAL,R.mipmap.kmc_icon)
            .put(EVENT_TYPE.KMC_HOSPITAL_FOLLOWUP,R.mipmap.kmc_icon)
            .put(EVENT_TYPE.SCANU_FOLLOWUP,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.AEFI_CHILD,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.CHILD_DISEASE,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.MEMBER_DISEASE,R.drawable.rowavatar_member)
            .put(EVENT_TYPE.CHILD_ECCD_2_3_MONTH,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.CHILD_ECCD_4_6_MONTH,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.CHILD_ECCD_7_9_MONTH,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.CHILD_ECCD_10_12_MONTH,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.CHILD_ECCD_24_MONTH,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.CHILD_ECCD_18_MONTH,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.CHILD_ECCD_36_MONTH,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.CHILD_INFO_7_24_MONTHS,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.CHILD_INFO_25_MONTHS,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.PNC_CHILD_REGISTRATION,R.drawable.rowavatar_child)
            .put(EVENT_TYPE.UPDATE_CHILD_REGISTRATION,R.drawable.rowavatar_child)
            .put("Update Family Registration",R.mipmap.ic_icon_home)
            .put(EVENT_TYPE.CORONA_INDIVIDUAL,R.drawable.ic_virus)
            .put(EVENT_TYPE.FORUM_ADULT,R.drawable.ic_familiar)
            .put(EVENT_TYPE.FORUM_WOMEN,R.drawable.ic_women)
            .put(EVENT_TYPE.FORUM_ADO,R.drawable.ic_adolescent)
            .put(EVENT_TYPE.FORUM_CHILD,R.drawable.ic_child)
            .put(EVENT_TYPE.FORUM_NCD,R.drawable.ic_sugar_blood_level)
            .put("ANC",R.mipmap.ic_anc_pink)
            .put("pnc",R.drawable.sidemenu_pnc)
            .put(EVENT_TYPE.GLASS,R.drawable.ic_glasses)
            .put(EVENT_TYPE.SUN_GLASS,R.drawable.ic_sun_glasses)
            .put(EVENT_TYPE.SV_1,R.drawable.ic_glasses)
            .put(EVENT_TYPE.SV_1_5,R.drawable.ic_glasses)
            .put(EVENT_TYPE.SV_2,R.drawable.ic_glasses)
            .put(EVENT_TYPE.SV_2_5,R.drawable.ic_glasses)
            .put(EVENT_TYPE.SV_3,R.drawable.ic_glasses)
            .put(EVENT_TYPE.BF_1,R.drawable.ic_glasses)
            .put(EVENT_TYPE.BF_1_5,R.drawable.ic_glasses)
            .put(EVENT_TYPE.BF_2,R.drawable.ic_glasses)
            .put(EVENT_TYPE.BF_2_5,R.drawable.ic_glasses)
            .put(EVENT_TYPE.BF_3,R.drawable.ic_glasses)
            .build();
    //need to show the title at row/option
    public static Map<String,String> getVisitEventTypeMapping(){
        Map<String,String> visitEventTypeMapping = ImmutableMap.<String,String> builder()
                .put(EVENT_TYPE.ANC_REGISTRATION, HnppApplication.appContext.getString(R.string.pregnancy_reg))
//            .put(EVENT_TYPE.ANC_GENERAL_DISEASE,"শারীরিক সমস্যা")
//            .put(EVENT_TYPE.ANC_PREGNANCY_HISTORY,"পূর্বের গর্ভের ইতিহাস")
                .put(EVENT_TYPE.ELCO,HnppApplication.appContext.getString(R.string.elco_visit))
                .put(JSON_FORMS.ANC_FORM,HnppApplication.appContext.getString(R.string.anc_service))
                .put(EVENT_TYPE.ANC_HOME_VISIT,HnppApplication.appContext.getString(R.string.anc_service))
                .put(EVENT_TYPE.ANC_HOME_VISIT_FACILITY,HnppApplication.appContext.getString(R.string.anc_service))
//            .put(JSON_FORMS.GENERAL_DISEASE,"শারীরিক সমস্যা")
                .put(EVENT_TYPE.MEMBER_REFERRAL,HnppApplication.appContext.getString(R.string.referrel))
                .put(EVENT_TYPE.WOMEN_REFERRAL,HnppApplication.appContext.getString(R.string.referrel))
                .put(EVENT_TYPE.CHILD_REFERRAL,HnppApplication.appContext.getString(R.string.referrel))
                .put("Member referral",HnppApplication.appContext.getString(R.string.referrel))
                .put(EVENT_TYPE.GMP_REFERRAL,HnppApplication.appContext.getString(R.string.referrel))
//            .put( JSON_FORMS.PREGNANCY_HISTORY,"পূর্বের গর্ভের ইতিহাস")

                .put( EVENT_TYPE.PREGNANCY_OUTCOME,HnppApplication.appContext.getString(R.string.pregnancy_outcome))
                .put( EVENT_TYPE.PREGNANCY_OUTCOME_OOC,HnppApplication.appContext.getString(R.string.pregnancy_outcome))
                .put( JSON_FORMS.PNC_FORM,HnppApplication.appContext.getString(R.string.before_preg_service))
                .put( EVENT_TYPE.PNC_REGISTRATION_OOC,HnppApplication.appContext.getString(R.string.before_preg_visit_pnc))
                .put( EVENT_TYPE.PNC_REGISTRATION,HnppApplication.appContext.getString(R.string.before_preg_visit_pnc))
                .put(EVENT_TYPE.WOMEN_PACKAGE,HnppApplication.appContext.getString(R.string.woman_package))
                .put(EVENT_TYPE.GIRL_PACKAGE, HnppApplication.appContext.getString(R.string.girl_package))
                .put(EVENT_TYPE.NCD_PACKAGE, HnppApplication.appContext.getString(R.string.ncd_package))
                .put(EVENT_TYPE.BLOOD_GROUP,HnppApplication.appContext.getString(R.string.blood_group))
                .put(EVENT_TYPE.EYE_TEST,HnppApplication.appContext.getString(R.string.eye_test))
                .put(EVENT_TYPE.IYCF_PACKAGE, HnppApplication.appContext.getString(R.string.child_package))
                .put(EVENT_TYPE.ENC_REGISTRATION, HnppApplication.appContext.getString(R.string.newborn_package))
                .put(EVENT_TYPE.HOME_VISIT_FAMILY, HnppApplication.appContext.getString(R.string.household_profile_update))
                .put(EVENT_TYPE.CHILD_PROFILE_VISIT, HnppApplication.appContext.getString(R.string.child_profile_visit))
                .put(EVENT_TYPE.MEMBER_PROFILE_VISIT, HnppApplication.appContext.getString(R.string.member_profile_visit))
                .put(EVENT_TYPE.VACCINATION, HnppApplication.appContext.getString(R.string.vaccination))
                .put(EVENT_TYPE.SERVICES, HnppApplication.appContext.getString(R.string.vitamin_service))
                .put(EVENT_TYPE.REFERREL_FOLLOWUP,HnppApplication.appContext.getString(R.string.referrel_followup))
                .put(EVENT_TYPE.GMP_COUNSELING,HnppApplication.appContext.getString(R.string.gmp_counceling))
                .put(EVENT_TYPE.GMP_REFERREL_FOLLOWUP,HnppApplication.appContext.getString(R.string.referrel_followup))
                .put(EVENT_TYPE.GMP_SESSION_INFO,HnppApplication.appContext.getString(R.string.session_info))
                .put(EVENT_TYPE.CHILD_FOLLOWUP,HnppApplication.appContext.getString(R.string.child_followup))
                .put(EVENT_TYPE.NEW_BORN_PNC_1_4,HnppApplication.appContext.getString(R.string.newborn_pnc_1_4))
                .put(EVENT_TYPE.KMC_SERVICE_HOME,HnppApplication.appContext.getString(R.string.kmc_service_home))
                .put(EVENT_TYPE.KMC_HOME_FOLLOWUP,HnppApplication.appContext.getString(R.string.kmc_home_followup))
                .put(EVENT_TYPE.KMC_SERVICE_HOSPITAL,HnppApplication.appContext.getString(R.string.kmc_service_hospital))
                .put(EVENT_TYPE.KMC_HOSPITAL_FOLLOWUP,HnppApplication.appContext.getString(R.string.kmc_home_followup))
                .put(EVENT_TYPE.SCANU_FOLLOWUP,HnppApplication.appContext.getString(R.string.scanu_followup))
                .put(EVENT_TYPE.AEFI_CHILD, HnppApplication.appContext.getString(R.string.aifi_child))
                .put(EVENT_TYPE.CHILD_DISEASE, HnppApplication.appContext.getString(R.string.common_disease_info))
                .put(EVENT_TYPE.MEMBER_DISEASE, HnppApplication.appContext.getString(R.string.common_disease_info))
                .put(EVENT_TYPE.CHILD_ECCD_2_3_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_2_3))
                .put(EVENT_TYPE.CHILD_ECCD_4_6_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_4_6))
                .put(EVENT_TYPE.CHILD_ECCD_7_9_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_7_9))
                .put(EVENT_TYPE.CHILD_ECCD_10_12_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_10_12))
                .put(EVENT_TYPE.CHILD_ECCD_18_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_18))
                .put(EVENT_TYPE.CHILD_ECCD_24_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_24))
                .put(EVENT_TYPE.CHILD_ECCD_36_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_36))
                .put(EVENT_TYPE.CHILD_INFO_7_24_MONTHS,HnppApplication.appContext.getString(R.string.child_info))
                .put(EVENT_TYPE.CHILD_INFO_25_MONTHS,HnppApplication.appContext.getString(R.string.child_info))
                .put(EVENT_TYPE.PNC_CHILD_REGISTRATION,HnppApplication.appContext.getString(R.string.pnc_child_reg))
                .put(EVENT_TYPE.UPDATE_CHILD_REGISTRATION,HnppApplication.appContext.getString(R.string.child_reg_update))
                .put("Update Family Registration",HnppApplication.appContext.getString(R.string.house_reg_update))
                .put(EventType.REMOVE_FAMILY,HnppApplication.appContext.getString(R.string.house_remove))
                .put(EventType.REMOVE_MEMBER,HnppApplication.appContext.getString(R.string.member_remove))
                .put(EventType.REMOVE_CHILD,HnppApplication.appContext.getString(R.string.child_remove))
                .put(EVENT_TYPE.CORONA_INDIVIDUAL,HnppApplication.appContext.getString(R.string.corona_info))
                .put(EVENT_TYPE.SS_INFO,HnppApplication.appContext.getString(R.string.ss_info))
                .put(EVENT_TYPE.FORUM_ADO,HnppApplication.appContext.getString(R.string.girl_forum))
                .put(EVENT_TYPE.FORUM_WOMEN,HnppApplication.appContext.getString(R.string.woman_forum))
                .put(EVENT_TYPE.FORUM_CHILD,HnppApplication.appContext.getString(R.string.child_forum))
                .put(EVENT_TYPE.FORUM_NCD,HnppApplication.appContext.getString(R.string.common_forum))
                .put(EVENT_TYPE.FORUM_ADULT,HnppApplication.appContext.getString(R.string.adult_forum))
                .build();
        return visitEventTypeMapping;

    }
    //for dashboard poridorshon
    public static final Map<String,String> targetTypeMapping = ImmutableMap.<String,String> builder()

            .put(EVENT_TYPE.HOME_VISIT_FAMILY,HnppApplication.appContext.getString(R.string.house_profile_update))
            .put(EVENT_TYPE.CHILD_PROFILE_VISIT,HnppApplication.appContext.getString(R.string.house_member_survey))
            .put(EVENT_TYPE.MEMBER_PROFILE_VISIT,HnppApplication.appContext.getString(R.string.house_member_survey))
            .put(EVENT_TYPE.ELCO,HnppApplication.appContext.getString(R.string.elco))
            .put(EVENT_TYPE.METHOD_USER,HnppApplication.appContext.getString(R.string.method_user))
            .put(EVENT_TYPE.ADO_METHOD_USER,HnppApplication.appContext.getString(R.string.add_method_user))
            .put(EVENT_TYPE.PREGNANCY_IDENTIFIED,HnppApplication.appContext.getString(R.string.pregnancy_identified))
            .put(EVENT_TYPE.INSTITUTIONALIZES_DELIVERY,HnppApplication.appContext.getString(R.string.inst_delivery))
            .put(EVENT_TYPE.CHILD_VISIT_0_6,HnppApplication.appContext.getString(R.string.child_visit_0_6))
            .put(EVENT_TYPE.CHILD_VISIT_7_24,HnppApplication.appContext.getString(R.string.child_visit_7_24))
            .put(EVENT_TYPE.CHILD_VISIT_18_36,HnppApplication.appContext.getString(R.string.child_visit_18_36))
            .put(EVENT_TYPE.CHILD_IMMUNIZATION_0_59,HnppApplication.appContext.getString(R.string.child_imm_0_59))
            .put(EVENT_TYPE.FORUM_ADO,HnppApplication.appContext.getString(R.string.girl_forum))
            .put(EVENT_TYPE.FORUM_WOMEN,HnppApplication.appContext.getString(R.string.woman_forum))
            .put(EVENT_TYPE.FORUM_CHILD,HnppApplication.appContext.getString(R.string.child_forum))
            .put(EVENT_TYPE.FORUM_NCD,HnppApplication.appContext.getString(R.string.common_forum))
            .put(EVENT_TYPE.FORUM_ADULT,HnppApplication.appContext.getString(R.string.adult_forum))
            .put(EVENT_TYPE.PREGNANCY_OUTCOME,HnppApplication.appContext.getString(R.string.delivery))
            .put(EVENT_TYPE.GIRL_PACKAGE,HnppApplication.appContext.getString(R.string.girl_package))
            .put(EVENT_TYPE.WOMEN_PACKAGE,HnppApplication.appContext.getString(R.string.woman_package))
            .put(EVENT_TYPE.IYCF_PACKAGE,HnppApplication.appContext.getString(R.string.iycf_package))
            .put(EVENT_TYPE.NCD_PACKAGE,HnppApplication.appContext.getString(R.string.ncd_package))
            .put(EVENT_TYPE.ANC_SERVICE,HnppApplication.appContext.getString(R.string.anc_service2))
            .put(EVENT_TYPE.PNC_SERVICE,HnppApplication.appContext.getString(R.string.pnc_service_within_48))
            .put(EVENT_TYPE.ANC_PACKAGE,HnppApplication.appContext.getString(R.string.anc_package))
            .put(EVENT_TYPE.PNC_PACKAGE,HnppApplication.appContext.getString(R.string.pnc_within_48))
            .put(EVENT_TYPE.PNC_REGISTRATION,HnppApplication.appContext.getString(R.string.after_delivery_service))
            .put(EVENT_TYPE.AVG_ATTEND_ADULT_FORUM,HnppApplication.appContext.getString(R.string.number_of_attend))
            .put(EVENT_TYPE.ADULT_FORUM_ATTENDANCE,HnppApplication.appContext.getString(R.string.number_of_attend))
           // .put(EVENT_TYPE.NCD_BY_PA,"অসংক্রামক রোগের সেবা")
            .put(EVENT_TYPE.ADULT_FORUM_SERVICE_TAKEN,HnppApplication.appContext.getString(R.string.service_taken_num))
            .put(EVENT_TYPE.MARKED_PRESBYOPIA,HnppApplication.appContext.getString(R.string.marked_presbyopia))
            .put(EVENT_TYPE.PRESBYOPIA_CORRECTION,HnppApplication.appContext.getString(R.string.presbyopia_correction))
            .put(EVENT_TYPE.ESTIMATE_DIABETES,HnppApplication.appContext.getString(R.string.estimated_diabetes))
            .put(EVENT_TYPE.ESTIMATE_HBP,HnppApplication.appContext.getString(R.string.estimated_hbp))
            .put(EVENT_TYPE.CATARACT_SURGERY_REFER,HnppApplication.appContext.getString(R.string.cataract_surgery_refer))
            .put(EVENT_TYPE.CATARACT_SURGERY,HnppApplication.appContext.getString(R.string.cateract_surgery))
            .build();


    //for dashboard workSummery
    public static Map<String,String> getWorkSummeryTypeMapping(){
        Map<String,String> workSummeryTypeMapping = ImmutableMap.<String,String> builder()

                .put(EventType.FAMILY_REGISTRATION,HnppApplication.appContext.getString(R.string.household_reg))
                .put(EventType.FAMILY_MEMBER_REGISTRATION,HnppApplication.appContext.getString(R.string.member_reg))
                .put(EVENT_TYPE.HOME_VISIT_FAMILY,HnppApplication.appContext.getString(R.string.house_profile_update))
                .put(EVENT_TYPE.CHILD_PROFILE_VISIT,HnppApplication.appContext.getString(R.string.house_member_survey))
                .put(EVENT_TYPE.MEMBER_PROFILE_VISIT,HnppApplication.appContext.getString(R.string.house_member_survey))
                .put(EventType.UPDATE_FAMILY_MEMBER_REGISTRATION,HnppApplication.appContext.getString(R.string.member_reg))
                .put("ANC",HnppApplication.appContext.getString(R.string.anc_service_anc))
                .put("pnc",HnppApplication.appContext.getString(R.string.old_pnc_service))
                .put(EVENT_TYPE.ELCO,HnppApplication.appContext.getString(R.string.elco_visit))
                .put(EventType.CHILD_REGISTRATION,HnppApplication.appContext.getString(R.string.child_reg))
                .put(EVENT_TYPE.ANC_REGISTRATION,HnppApplication.appContext.getString(R.string.pregnancy_reg))
                .put(Constants.EVENT_TYPE.ANC_HOME_VISIT,HnppApplication.appContext.getString(R.string.preg_service_visit))

                .put(EVENT_TYPE.PREGNANCY_OUTCOME,HnppApplication.appContext.getString(R.string.delivery))
                .put(EVENT_TYPE.ENC_REGISTRATION, HnppApplication.appContext.getString(R.string.newborn_service))
                .put(EVENT_TYPE.CHILD_FOLLOWUP,HnppApplication.appContext.getString(R.string.child_followup))
                .put(EVENT_TYPE.NEW_BORN_PNC_1_4,HnppApplication.appContext.getString(R.string.newborn_pnc_1_4))
                .put(EVENT_TYPE.KMC_SERVICE_HOME,HnppApplication.appContext.getString(R.string.kmc_service_home))
                .put(EVENT_TYPE.KMC_HOME_FOLLOWUP,HnppApplication.appContext.getString(R.string.kmc_home_followup))
                .put(EVENT_TYPE.KMC_SERVICE_HOSPITAL,HnppApplication.appContext.getString(R.string.kmc_service_hospital))
                .put(EVENT_TYPE.KMC_HOSPITAL_FOLLOWUP,HnppApplication.appContext.getString(R.string.kmc_home_followup))
                .put(EVENT_TYPE.SCANU_FOLLOWUP,HnppApplication.appContext.getString(R.string.scanu_followup))
                .put(EVENT_TYPE.AEFI_CHILD,HnppApplication.appContext.getString(R.string.aefi_followup))
                .put(EVENT_TYPE.CHILD_DISEASE, HnppApplication.appContext.getString(R.string.common_disease_info))
                .put(EVENT_TYPE.MEMBER_DISEASE, HnppApplication.appContext.getString(R.string.common_disease_info))
                .put(EVENT_TYPE.CHILD_ECCD_2_3_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_2_3))
                .put(EVENT_TYPE.CHILD_ECCD_4_6_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_4_6))
                .put(EVENT_TYPE.CHILD_ECCD_7_9_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_7_9))
                .put(EVENT_TYPE.CHILD_ECCD_10_12_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_10_12))
                .put(EVENT_TYPE.CHILD_ECCD_18_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_18))
                .put(EVENT_TYPE.CHILD_ECCD_24_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_24))
                .put(EVENT_TYPE.CHILD_ECCD_36_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_36))
                .put(EVENT_TYPE.CHILD_INFO_7_24_MONTHS,HnppApplication.appContext.getString(R.string.child_info))
                .put(EVENT_TYPE.CHILD_INFO_25_MONTHS,HnppApplication.appContext.getString(R.string.child_info))
                .put(EVENT_TYPE.FORUM_ADO,HnppApplication.appContext.getString(R.string.girl_forum))
                .put(EVENT_TYPE.FORUM_WOMEN,HnppApplication.appContext.getString(R.string.woman_forum))
                .put(EVENT_TYPE.FORUM_CHILD,HnppApplication.appContext.getString(R.string.girl_forum))
                .put(EVENT_TYPE.FORUM_NCD,HnppApplication.appContext.getString(R.string.common_forum))
                .put(EVENT_TYPE.FORUM_ADULT,HnppApplication.appContext.getString(R.string.adult_forum))
                .put(EVENT_TYPE.WOMEN_PACKAGE,HnppApplication.appContext.getString(R.string.woman_package))
                .put(EVENT_TYPE.GIRL_PACKAGE, HnppApplication.appContext.getString(R.string.girl_package))
                .put(EVENT_TYPE.NCD_PACKAGE, HnppApplication.appContext.getString(R.string.ncd_package))
                .put(EVENT_TYPE.BLOOD_GROUP,HnppApplication.appContext.getString(R.string.blood_group))
                .put(EVENT_TYPE.EYE_TEST,HnppApplication.appContext.getString(R.string.eye_test))
                .put(EVENT_TYPE.GLASS,HnppApplication.appContext.getString(R.string.total_glass))
                .put(EVENT_TYPE.SUN_GLASS,HnppApplication.appContext.getString(R.string.sunglass))
                .put(EVENT_TYPE.SV_1,HnppApplication.appContext.getString(R.string.sv_1))
                .put(EVENT_TYPE.SV_1_5,HnppApplication.appContext.getString(R.string.sv_1_5))
                .put(EVENT_TYPE.SV_2,HnppApplication.appContext.getString(R.string.sv_2))
                .put(EVENT_TYPE.SV_2_5,HnppApplication.appContext.getString(R.string.sv_2_5))
                .put(EVENT_TYPE.SV_3,HnppApplication.appContext.getString(R.string.sv_3))
                .put(EVENT_TYPE.BF_1,HnppApplication.appContext.getString(R.string.bf_1))
                .put(EVENT_TYPE.BF_1_5,HnppApplication.appContext.getString(R.string.bf_1_5))
                .put(EVENT_TYPE.BF_2,HnppApplication.appContext.getString(R.string.bf_2))
                .put(EVENT_TYPE.BF_2_5,HnppApplication.appContext.getString(R.string.bf_2_5))
                .put(EVENT_TYPE.BF_3,HnppApplication.appContext.getString(R.string.bf_3))

                .put(EVENT_TYPE.IYCF_PACKAGE, HnppApplication.appContext.getString(R.string.child_package_iyocf))
                .put("familyplanning_method_known", HnppApplication.appContext.getString(R.string.family_planning_user))
                .put(EVENT_TYPE.ANC_SERVICE,HnppApplication.appContext.getString(R.string.anc_package))
                .put(EVENT_TYPE.PNC_SERVICE,HnppApplication.appContext.getString(R.string.pnc_within_48))
                .put(EVENT_TYPE.PNC_REGISTRATION,HnppApplication.appContext.getString(R.string.pnc_only))

                .build();
        return workSummeryTypeMapping;
    }
    //for dashboard countSummery
    public static final Map<String,String> countSummeryTypeMapping = ImmutableMap.<String,String> builder()

            .put(EventType.FAMILY_REGISTRATION,HnppApplication.appContext.getString(R.string.house_number))
            .put(EventType.FAMILY_MEMBER_REGISTRATION,HnppApplication.appContext.getString(R.string.number_of_member))
            .put(EventType.UPDATE_FAMILY_MEMBER_REGISTRATION,HnppApplication.appContext.getString(R.string.number_of_member))
            .put(EventType.CHILD_REGISTRATION,HnppApplication.appContext.getString(R.string.number_of_child))
            .put(EVENT_TYPE.ANC_REGISTRATION,HnppApplication.appContext.getString(R.string.pregnancy_reg))
            .put(EVENT_TYPE.HOME_VISIT_FAMILY,HnppApplication.appContext.getString(R.string.house_profile_update))
            .put(Constants.EVENT_TYPE.ANC_HOME_VISIT,HnppApplication.appContext.getString(R.string.pregnancy_service_visit))
            .put(EVENT_TYPE.PNC_REGISTRATION,HnppApplication.appContext.getString(R.string.pnc_only))
            .put(EVENT_TYPE.PREGNANCY_OUTCOME,HnppApplication.appContext.getString(R.string.delivery))
            .build();



    public static final Map<String,String> eventTypeMapping = ImmutableMap.<String,String> builder()
            .put(EventType.FAMILY_REGISTRATION,HnppApplication.appContext.getString(R.string.add_family))
            .put(EventType.FAMILY_MEMBER_REGISTRATION,HnppApplication.appContext.getString(R.string.member_reg))
            .put(EventType.UPDATE_FAMILY_MEMBER_REGISTRATION,HnppApplication.appContext.getString(R.string.member_update))
            .put(EventType.CHILD_REGISTRATION,HnppApplication.appContext.getString(R.string.child_reg))
            .put(EVENT_TYPE.MEMBER_REFERRAL,HnppApplication.appContext.getString(R.string.referrel))
            .put(EVENT_TYPE.WOMEN_REFERRAL,HnppApplication.appContext.getString(R.string.referrel))
            .put(EVENT_TYPE.CHILD_REFERRAL,HnppApplication.appContext.getString(R.string.referrel))
            .put(EVENT_TYPE.PREGNANCY_OUTCOME,HnppApplication.appContext.getString(R.string.delivery))
            .put("Member referral",HnppApplication.appContext.getString(R.string.referrel))
            .put(EVENT_TYPE.ELCO,HnppApplication.appContext.getString(R.string.elco_visit))
            .put(EVENT_TYPE.ANC_REGISTRATION,HnppApplication.appContext.getString(R.string.pregnancy_reg))
            .put(EVENT_TYPE.UPDATE_ANC_REGISTRATION,HnppApplication.appContext.getString(R.string.pregnancy_reg_update))
            .put(EVENT_TYPE.WOMEN_PACKAGE,HnppApplication.appContext.getString(R.string.woman_package))
            .put(EVENT_TYPE.GIRL_PACKAGE, HnppApplication.appContext.getString(R.string.girl_package))
            .put(EVENT_TYPE.NCD_PACKAGE,HnppApplication.appContext.getString(R.string.ncd_package))
            .put(EVENT_TYPE.BLOOD_GROUP,HnppApplication.appContext.getString(R.string.blood_group))
            .put(EVENT_TYPE.EYE_TEST,HnppApplication.appContext.getString(R.string.eye_test))
            .put(EVENT_TYPE.IYCF_PACKAGE, HnppApplication.appContext.getString(R.string.child_package_iyocf))
            .put(Constants.EVENT_TYPE.ANC_HOME_VISIT,HnppApplication.appContext.getString(R.string.pregnancy_service_visit))
            .put( EVENT_TYPE.PNC_REGISTRATION,HnppApplication.appContext.getString(R.string.pnc_only))
            .put(EVENT_TYPE.ENC_REGISTRATION, HnppApplication.appContext.getString(R.string.newborn_service))
            .put(EVENT_TYPE.HOME_VISIT_FAMILY,HnppApplication.appContext.getString(R.string.house_profile_update))
            .put(EVENT_TYPE.CHILD_PROFILE_VISIT, HnppApplication.appContext.getString(R.string.house_member_survey))
            .put(EVENT_TYPE.MEMBER_PROFILE_VISIT, HnppApplication.appContext.getString(R.string.house_member_survey))
            .put(EventType.CHILD_HOME_VISIT, HnppApplication.appContext.getString(R.string.child_home_visit))
            .put(EVENT_TYPE.VACCINATION, HnppApplication.appContext.getString(R.string.vaccination))
            .put(EVENT_TYPE.SERVICES, HnppApplication.appContext.getString(R.string.vitamin_service))
            .put(EVENT_TYPE.REFERREL_FOLLOWUP,HnppApplication.appContext.getString(R.string.referrel_followup))
            .put(EVENT_TYPE.GMP_COUNSELING,HnppApplication.appContext.getString(R.string.gmp_counceling))
            .put(EVENT_TYPE.GMP_REFERREL_FOLLOWUP,HnppApplication.appContext.getString(R.string.referrel_followup))
            .put(EVENT_TYPE.GMP_SESSION_INFO,HnppApplication.appContext.getString(R.string.session_info))
            .put(EVENT_TYPE.GMP_REFERRAL,HnppApplication.appContext.getString(R.string.referrel))
            .put(EVENT_TYPE.CHILD_FOLLOWUP,HnppApplication.appContext.getString(R.string.child_followup))
            .put(EVENT_TYPE.NEW_BORN_PNC_1_4,HnppApplication.appContext.getString(R.string.newborn_pnc))
            .put(EVENT_TYPE.KMC_SERVICE_HOME,HnppApplication.appContext.getString(R.string.kmc_service_home))
            .put(EVENT_TYPE.KMC_HOME_FOLLOWUP,HnppApplication.appContext.getString(R.string.kmc_home_followup))
            .put(EVENT_TYPE.KMC_SERVICE_HOSPITAL,HnppApplication.appContext.getString(R.string.kmc_service_hospital))
            .put(EVENT_TYPE.KMC_HOSPITAL_FOLLOWUP,HnppApplication.appContext.getString(R.string.kmc_home_followup))
            .put(EVENT_TYPE.SCANU_FOLLOWUP,HnppApplication.appContext.getString(R.string.scanu_followup))
            .put(EVENT_TYPE.AEFI_CHILD,HnppApplication.appContext.getString(R.string.aefi_followup))
            .put(EVENT_TYPE.CHILD_DISEASE, HnppApplication.appContext.getString(R.string.common_disease_info))
            .put(EVENT_TYPE.MEMBER_DISEASE, HnppApplication.appContext.getString(R.string.common_disease_info))
            .put(EVENT_TYPE.CHILD_ECCD_2_3_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_2_3))
            .put(EVENT_TYPE.CHILD_ECCD_4_6_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_4_6))
            .put(EVENT_TYPE.CHILD_ECCD_7_9_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_7_9))
            .put(EVENT_TYPE.CHILD_ECCD_10_12_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_10_12))
            .put(EVENT_TYPE.CHILD_ECCD_18_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_18))
            .put(EVENT_TYPE.CHILD_ECCD_24_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_24))
            .put(EVENT_TYPE.CHILD_ECCD_36_MONTH,HnppApplication.appContext.getString(R.string.child_eccd_36))
            .put(EVENT_TYPE.CHILD_INFO_7_24_MONTHS,HnppApplication.appContext.getString(R.string.child_info))
            .put(EVENT_TYPE.CHILD_INFO_25_MONTHS,HnppApplication.appContext.getString(R.string.child_info))
            .put(EVENT_TYPE.PNC_CHILD_REGISTRATION,HnppApplication.appContext.getString(R.string.pnc_child_reg))
            .put(EVENT_TYPE.UPDATE_CHILD_REGISTRATION,HnppApplication.appContext.getString(R.string.child_reg_update))
            .put("Update Family Registration",HnppApplication.appContext.getString(R.string.house_reg_update))
            .put(EventType.REMOVE_FAMILY,HnppApplication.appContext.getString(R.string.house_remove))
            .put(EventType.REMOVE_MEMBER,HnppApplication.appContext.getString(R.string.member_remove))
            .put(EventType.REMOVE_CHILD,HnppApplication.appContext.getString(R.string.child_remove))
            .put(EVENT_TYPE.CORONA_INDIVIDUAL,HnppApplication.appContext.getString(R.string.corona_info))
            .put(EVENT_TYPE.SS_INFO,HnppApplication.appContext.getString(R.string.ss_info))
            .put(EVENT_TYPE.FORUM_ADO,HnppApplication.appContext.getString(R.string.girl_forum))
            .put(EVENT_TYPE.FORUM_WOMEN,HnppApplication.appContext.getString(R.string.woman_forum))
            .put(EVENT_TYPE.FORUM_CHILD,HnppApplication.appContext.getString(R.string.child_forum))
            .put(EVENT_TYPE.FORUM_NCD,HnppApplication.appContext.getString(R.string.common_forum))
            .put(EVENT_TYPE.FORUM_ADULT,HnppApplication.appContext.getString(R.string.adult_forum))
            .put(EVENT_TYPE.ANC_SERVICE,HnppApplication.appContext.getString(R.string.anc_package))
            .put(EVENT_TYPE.PNC_SERVICE,HnppApplication.appContext.getString(R.string.pnc_within_48))
            .put("Guest Member Registration",HnppApplication.appContext.getString(R.string.guest_reg))
            .put("OOC Member Registration",HnppApplication.appContext.getString(R.string.guest_reg))
            .build();
    public static Map<String,String> getRiskeyFactorMapping(){
        Map<String,String> riskeyFactorMapping = ImmutableMap.<String,String> builder()
                .put("Bleeding_Through_Birth_Canal",HnppApplication.appContext.getString(R.string.bleeding_birth_canel))
                .put("High_Temperature_102_Degree_or_More",HnppApplication.appContext.getString(R.string.faver))
                .put("Convulsion",HnppApplication.appContext.getString(R.string.convulsion))
                .put("Weakness_Blurred_vision",HnppApplication.appContext.getString(R.string.blurred_vision))
                .put("high_blood_pressure",HnppApplication.appContext.getString(R.string.high_blood_pressure))
                .put("clinical_anemia",HnppApplication.appContext.getString(R.string.clinical_anemia))
                .put("pph",HnppApplication.appContext.getString(R.string.pph))
                .put("postpartum_eclampsia",HnppApplication.appContext.getString(R.string.postpartum_eclampsia))
                .put("puerperal_sepsis",HnppApplication.appContext.getString(R.string.puerpartum_sepsis))
                .put("vv_fistula_rv_fistula",HnppApplication.appContext.getString(R.string.vv_fistula))
                .put("perinal_tear",HnppApplication.appContext.getString(R.string.perinal_tear))
                .put("Fetal_Heart_Rate",HnppApplication.appContext.getString(R.string.fetal_heard_rate))
                .put("Hemoglobin_result",HnppApplication.appContext.getString(R.string.hemoglobin))
                .put("fbs_result",HnppApplication.appContext.getString(R.string.fasting))
                .put("rbs_result",HnppApplication.appContext.getString(R.string.rendom))
                .put("Urine_Albumin_result",HnppApplication.appContext.getString(R.string.urine_albumin))
                .put("Hb_tested_result",HnppApplication.appContext.getString(R.string.hb_test_result))
                .put("chipilis_tested_result",HnppApplication.appContext.getString(R.string.chiphilis))
                .put("hiv_tested_result",HnppApplication.appContext.getString(R.string.hiv))
                .put("ultra_sound_result",HnppApplication.appContext.getString(R.string.ultra_sounf_scan))
                .put("body_temp_fahrenheit",HnppApplication.appContext.getString(R.string.body_temp))
                .put("Denger_Signs_During_PNC",HnppApplication.appContext.getString(R.string.denger_sign_pnc))
                .build();
        return riskeyFactorMapping;
    }
    public static final Map<String,String> immunizationMapping = ImmutableMap.<String,String> builder()
            .put("PENTA 1",HnppApplication.appContext.getString(R.string.penta_1))
            .put("PENTA 2",HnppApplication.appContext.getString(R.string.penta_2))
            .put("PENTA 3",HnppApplication.appContext.getString(R.string.penta_3))
            .put("OPV 1",HnppApplication.appContext.getString(R.string.polio_1))
            .put("OPV 2",HnppApplication.appContext.getString(R.string.polio_2))
            .put("OPV 3",HnppApplication.appContext.getString(R.string.polio_3))
            .put("PCV 1",HnppApplication.appContext.getString(R.string.pcv_1))
            .put("PCV 2",HnppApplication.appContext.getString(R.string.pcv_2))
            .put("PCV 3",HnppApplication.appContext.getString(R.string.pcv_3))
            .put("BCG",HnppApplication.appContext.getString(R.string.bcg))
            .put("VITAMIN A1",HnppApplication.appContext.getString(R.string.vitamin_a))
            .build();
    public static final Map<String,String> referealResonMapping = ImmutableMap.<String,String> builder()
            .put("child_problems",HnppApplication.appContext.getString(R.string.child_problems))
            .put("pregnancy_problems",HnppApplication.appContext.getString(R.string.pregnancy_problems))
            .put("delivery_problems",HnppApplication.appContext.getString(R.string.delivery_problem))
            .put("pnc_problem",HnppApplication.appContext.getString(R.string.pnc_problem))
            .put("problems_eyes",HnppApplication.appContext.getString(R.string.eye_problem))
            .put("diabetes",HnppApplication.appContext.getString(R.string.diabetes))
            .put("high_blood_pressure",HnppApplication.appContext.getString(R.string.high_blood_pressure))
            .put("problems_with_birth_control",HnppApplication.appContext.getString(R.string.birth_control_problem))
            .put("cataract_problem",HnppApplication.appContext.getString(R.string.cataract_problem))
            .put("other",HnppApplication.appContext.getString(R.string.other))
            .build();
    public static final Map<String,String> referealPlaceMapping = ImmutableMap.<String,String> builder()
            .put("brac_maternity_center", HnppApplication.appContext.getString(R.string.brac_maternity_center))
            .put("union_health_center",HnppApplication.appContext.getString(R.string.union_health_center))
            .put("upozzila_health_complex",HnppApplication.appContext.getString(R.string.upazila_health_complex))
            .put("union_family_kollan_center",HnppApplication.appContext.getString(R.string.union_family_kollan_center))
            .put("union_health_and_family_kollan_center",HnppApplication.appContext.getString(R.string.union_health_and_family_kollan_center))
            .put("mother_child_kollan_center",HnppApplication.appContext.getString(R.string.mother_child_kollan_center))
            .put("center_hospital",HnppApplication.appContext.getString(R.string.center_hospital))
            .put("medical_collage_hospital",HnppApplication.appContext.getString(R.string.medical_collage_hospital))
            .put("private_clinic",HnppApplication.appContext.getString(R.string.private_clinic))
            .put("spacial_hospital",HnppApplication.appContext.getString(R.string.special_hospital))
            .put("other_option",HnppApplication.appContext.getString(R.string.other))
            .build();

    private static String getKeyByValue(String mapperObj, String value){
        try {
            JSONObject choiceObject = new JSONObject(mapperObj);
            for (int i = 0; i < choiceObject.names().length(); i++) {
                if (value.equalsIgnoreCase(choiceObject.getString(choiceObject.names().getString(i)))) {
                    value = choiceObject.names().getString(i);
                    return value;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return value;
    }
}

