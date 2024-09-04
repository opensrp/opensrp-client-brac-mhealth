package org.smartregister.unicef.mis.service;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.domain.Response;
import org.smartregister.service.HTTPAgent;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.model.SbkCenter;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SBKCenterDownloadService {
    private static final String LOCATION_FETCH = "/rest/event/getSbkCenter?";
    private static final String TAG = "StockFetchIntentService";
    public static final String LAST_SYNC_TIME = "sbk_last_sync_time";
    public void downloadSBKCenter(){
        downloadAndParseSBKCenters().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        Log.v("SBK_CENTER","onNext>>"+aBoolean);

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        Log.v("SBK_CENTER","onComplete>>");
                    }
                });
    }
    private Observable<Boolean> downloadAndParseSBKCenters(){
        return  Observable.create(e->{
            JSONArray sbkList = getSbkList();
            if(sbkList!=null){
                long timestamp = 0;
                for(int i=0;i<sbkList.length();i++){
                    try{
                        JSONObject object = sbkList.getJSONObject(i);
                        SbkCenter sbkCenter = new Gson().fromJson(object.toString(), SbkCenter.class);
                        if(sbkCenter!=null){
                            HnppApplication.getSbkRepository().addOrUpdate(sbkCenter);
                            timestamp = sbkCenter.ServerVersion;
                        }
                    }catch (Exception exception){

                    }

                }
                if(sbkList.length()>0){
                    CoreLibrary.getInstance().context().allSharedPreferences().savePreference(LAST_SYNC_TIME,timestamp+"");
                }

            }
            e.onNext(true);
            e.onComplete();
          }
        );


    }
    private JSONArray getSbkList(){
        try{
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            String baseUrl = CoreLibrary.getInstance().context().
                    configuration().dristhiBaseURL();
            String endString = "/";
            if (baseUrl.endsWith(endString)) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
            }
            String userName = CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
            if(TextUtils.isEmpty(userName)){
                return null;
            }
            String lastSynTime = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(LAST_SYNC_TIME);
            if(TextUtils.isEmpty(lastSynTime)){
                lastSynTime ="0";
            }
            //testing
            String url = baseUrl + LOCATION_FETCH + "lastServerVersion="+lastSynTime;
            Log.v("SBK_CENTER","getLocationList>>url:"+url);
            Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(LOCATION_FETCH + " not returned data");
            }
            JSONObject jsonObject = new JSONObject((String)resp.payload());
            return jsonObject.getJSONArray("sbk-center-list");

        }catch (Exception e){
            e.printStackTrace();

        }
        return null;

    }
}
