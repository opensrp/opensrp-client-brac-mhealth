

package org.smartregister.unicef.dghs.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.domain.Alert;
import org.smartregister.domain.Response;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.service.HTTPAgent;
import org.smartregister.unicef.dghs.BuildConfig;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.OtherVaccineContentData;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OtherVaccineDueIntentService extends IntentService {
    public OtherVaccineDueIntentService() {
        super("OtherVaccineDueIntentService");
    }
    private final String TEST_BASE="362243b3-9c00-45eb-8fc5-778ff08db909-pros";
    public static final String ADD_URL = "rest/api/vaccination/sync";

    @Override
    protected void onHandleIntent(Intent intent) {
        processUnSyncData(0);

    }
    public static void processUnSyncData(int count){
        ArrayList<OtherVaccineContentData> vaccineContentData = HnppApplication.getOtherVaccineRepository().getUnSyncData();

        ArrayList<String> list = new ArrayList<>();

        for(OtherVaccineContentData otherVaccineContentData: vaccineContentData){
            String json = JsonFormUtils.gson.toJson(otherVaccineContentData);
            list.add(json);
        }
        if(list.size()==0) return;
        try{
            JSONObject request = new JSONObject();
            request.put("vaccines",list);
            String jsonPayload = request.toString();
            String add_url =  MessageFormat.format("{0}{1}",
                    BuildConfig.citizen_url,
                    ADD_URL);
            Log.v("OTHER_VACCINE","jsonPayload>>>"+jsonPayload);
            jsonPayload = jsonPayload.replace("\\","").replace("\"[","[").replace("]\"","]");
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            Log.v("OTHER_VACCINE","jsonPayload after replace>>>"+jsonPayload);
            Response<String> response = httpAgent.postWithBasicAuthInfo(add_url, jsonPayload,BuildConfig.AUTH_USER,BuildConfig.AUTH_PASS);
            if (response.isFailure()) {
                HnppConstants.appendLog("SYNC_URL", "pushECToServer:response response.isFailure");
                return;
            }
            HnppConstants.appendLog("SYNC_URL", "pushECToServer:response comes"+response.payload());
            for (OtherVaccineContentData contentData: vaccineContentData){
                HnppApplication.getOtherVaccineRepository().updateOtherVaccineStatus(contentData);
            }
            if (count < CoreLibrary.getInstance().getSyncConfiguration().getSyncMaxRetries()) {
                int newCount = count + 1;
                processUnSyncData(newCount);
            }else{
                Log.v("SYNC_URL","done");
            }

//{"timestamp":"2023-09-04T14:40:53.495+00:00","status":500,"error":"Internal Server Error","trace":"org.springframework.security.web.firewall.RequestRejectedException: The request was rejected because the URL contained a potentially malicious String \"//\"\n\tat org.springframework.security.web.firewall.StrictHttpFirewall.rejectedBlocklistedUrls(StrictHttpFirewall.java:535)\n\tat org.springframework.security.web.firewall.StrictHttpFirewall.getFirewalledRequest(StrictHttpFirewall.java:505)\n\tat org.springframework.security.web.FilterChainProxy.doFilterInternal(FilterChainProxy.java:196)\n\tat org.springframework.security.web.FilterChainProxy.doFilter(FilterChainProxy.java:183)\n\tat org.springframework.web.filter.DelegatingFilterProxy.invokeDelegate(DelegatingFilterProxy.java:354)\n\tat org.springframework.web.filter.DelegatingFilterProxy.doFilter(DelegatingFilterProxy.java:267)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\n\tat org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:117)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\n\tat org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:117)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\n\tat org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:117)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\n\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:197)\n\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:97)\n\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:541)\n\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:135)\n\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92)\n\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:78)\n\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:360)\n\tat org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:399)\n\tat org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:65)\n\tat org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:890)\n\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1743)\n\tat org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191)\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659)\n\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)\n\tat java.lang.Thread.run(Thread.java:748)\n","message":"The request was rejected because the URL contained a potentially malicious String \"//\"","path":"//rest/api/vaccination/sync"}

        }catch (Exception e){

        }
    }
    private void broadcastStatus(String message){
        try{
            Intent broadcastIntent = new Intent("VACCINE_OTHER");
            broadcastIntent.putExtra("EXTRA_OTHER_UPDATE", message);
            sendBroadcast(broadcastIntent);
        }catch (Exception e){

        }

    }

}
