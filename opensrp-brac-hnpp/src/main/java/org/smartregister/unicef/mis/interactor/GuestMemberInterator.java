package org.smartregister.unicef.mis.interactor;

import static java.text.MessageFormat.format;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.domain.Response;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.contract.GuestMemberContract;
import org.smartregister.unicef.mis.model.GuestMemberModel;
import org.smartregister.unicef.mis.utils.GuestMemberData;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;
import java.util.Iterator;

public class GuestMemberInterator implements GuestMemberContract.Interactor {
    private static final String OOC_EVENT = "/rest/client/ooc?";
    private AppExecutors appExecutors;
    private GuestMemberModel model;

    public GuestMemberInterator(AppExecutors appExecutors, GuestMemberModel model){
        this.appExecutors = appExecutors;
        this.model = model;
    }

    @Override
    public ArrayList<GuestMemberData> getAllGuestMemberList() {
        return model.getData();
    }

    @Override
    public void updateSHRIdFromServer(Context context, GuestMemberContract.InteractorCallBack callBack)  {
        ArrayList<String> ids = model.getIdsWithoutSHRIds();
        if(ids.size()>0){
            try{
            JSONObject request = new JSONObject();
            request.put(AllConstants.KEY.CLIENTS,ids);
            String jsonPayload = request.toString();
            String baseUrl = CoreLibrary.getInstance().context().
                    configuration().dristhiBaseURL();
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
            }
            Log.v("GUEST_MEMBER","jsonPayload:"+jsonPayload);

            Response<String> response = CoreLibrary.getInstance().context().getHttpAgent()
                    .post(format("{0}/{1}", baseUrl, OOC_EVENT),
                            jsonPayload);
            if (response.isFailure()) {

                return;
            }
            //{"base_entity_1":"123","base_entity_2":"4234","base_entity_3":"4234"}


                Object jsonObj = new JSONTokener(response.payload()).nextValue();
                if(jsonObj instanceof JSONArray) {
                    Log.v("qqqqq","array>>>");

                } else {
                    Log.v("qqqqq","object>>>");

                }
                JSONObject jsonObject = new JSONObject(response.payload());

                for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                    String key = it.next();
                    String value = jsonObject.getString(key);

                    try{
                        SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
                        String sql = "update ec_guest_member set shr_id= '"+value+"' where " +
                                "base_entity_id = '"+key+"' ;";
                        database.execSQL(sql);
                    }catch(Exception e){
                        e.printStackTrace();

                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    @Override
    public void processAndSaveRegistration(String jsonString, GuestMemberContract.InteractorCallBack callBack, boolean isEdited) {
        Runnable runnable = () -> {
            Pair<Client, Event> processEventClient = model.processRegistration(jsonString);
            if(processEventClient != null){
                model.saveRegistration(processEventClient);
            }
            if(isEdited){
                appExecutors.mainThread().execute(callBack::successfullySaved);
            }else{
                appExecutors.mainThread().execute(() -> {
                   if(processEventClient != null) callBack.openRegisteredProfile(processEventClient.first.getBaseEntityId());
                });
            }

        };
        appExecutors.diskIO().execute(runnable);

    }

    @Override
    public void fetchData(Context context, GuestMemberContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            model.loadData();

            appExecutors.mainThread().execute(callBack::updateAdapter);
        };
        appExecutors.diskIO().execute(runnable);

    }
    @Override
    public void filterData(Context context,String query,String ssName, GuestMemberContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            model.filterData(query);

            appExecutors.mainThread().execute(callBack::updateAdapter);
        };
        appExecutors.diskIO().execute(runnable);

    }
}
