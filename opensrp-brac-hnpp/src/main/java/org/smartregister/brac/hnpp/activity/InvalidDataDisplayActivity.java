package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.InvalidDataAdapter;
import org.smartregister.brac.hnpp.model.InvalidDataModel;
import org.smartregister.domain.db.Client;
import org.smartregister.domain.db.Event;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;
import java.util.List;

public class InvalidDataDisplayActivity extends SecuredActivity {
    private static final String TYPE ="type";
    public static final int TYPE_CLIENT =1;
    public static final int TYPE_EVENT =2;
    private RecyclerView recyclerView;
    private TextView countTv;
    private AppExecutors appExecutors;
    private int type;
    private ArrayList<InvalidDataModel> invalidDataModels = new ArrayList<>();
    public static void startInvalidActivity(int type, Activity activity){
        Intent intent = new Intent(activity,InvalidDataDisplayActivity.class);
        intent.putExtra(TYPE,type);
        activity.startActivity(intent);
    }
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_invalid_data);
        recyclerView = findViewById(R.id.recycler_view);
        countTv = findViewById(R.id.count_tv);
        this.type = getIntent().getIntExtra(TYPE,0);
        if(type==TYPE_CLIENT){
            findViewById(R.id.event_view).setVisibility(View.GONE);
        }else if(type==TYPE_EVENT){
            findViewById(R.id.client_view).setVisibility(View.GONE);
        }
        appExecutors = new AppExecutors();
        showProgressBar(true);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        loadInvalidData();

    }
    private void loadInvalidData(){
        Runnable runnable = () -> {
            EventClientRepository eventClientRepository = HnppApplication.getHNPPInstance().getEventClientRepository();

            if(type == TYPE_CLIENT){
                List<Client> clientList = eventClientRepository.getInvalidClientList();
                for(Client client : clientList){
                    InvalidDataModel invalidDataModel = new InvalidDataModel();
                    invalidDataModel.baseEntityId = client.getBaseEntityId();
                    if(TextUtils.isEmpty(invalidDataModel.baseEntityId)){
                        invalidDataModel.errorCause = "Base entity id null";
                    }
                    invalidDataModel.address = client.getAddresses().toString();
                    if(client.getAddresses().size() == 0){
                        invalidDataModel.errorCause = "Address not found";
                    }
                    invalidDataModel.firstName = client.getFirstName();
                    if(client.getLastName()!=null && client.getLastName().equalsIgnoreCase("family")){
                        invalidDataModel.eventType = "Household";
                    }else{
                        invalidDataModel.eventType = "Member";
                    }
                    invalidDataModel.client = client;
                    invalidDataModel.date = client.getDateCreated();
                    invalidDataModel.unique_id = client.getIdentifier("opensrp_id");
                    invalidDataModels.add(invalidDataModel);
                }

            }

            else if(type == TYPE_EVENT){
                List<Event> eventList = eventClientRepository.getInvalidEventList();
                for(Event event : eventList){
                    InvalidDataModel invalidDataModel = new InvalidDataModel();
                    invalidDataModel.baseEntityId = event.getBaseEntityId();
                    if(TextUtils.isEmpty(invalidDataModel.baseEntityId)){
                        invalidDataModel.errorCause = "Base entity id null";
                    }
                    invalidDataModel.serverVersion = event.getServerVersion();
                    if(invalidDataModel.serverVersion ==0){
                        invalidDataModel.errorCause = "Server version null";
                    }
                    invalidDataModel.formSubmissionId = event.getFormSubmissionId();
                    if(TextUtils.isEmpty(invalidDataModel.formSubmissionId)){
                        invalidDataModel.errorCause = "formSubmissionId null";
                    }
                    invalidDataModel.eventType = event.getEventType();
                    invalidDataModel.event = event;
                    invalidDataModel.date = event.getEventDate();
                    invalidDataModels.add(invalidDataModel);
                }

            }
            appExecutors.mainThread().execute(this::updateAdapter);
        };

        appExecutors.diskIO().execute(runnable);
    }
    private void showProgressBar(boolean isVisible){
        findViewById(R.id.progress_bar).setVisibility(isVisible? View.VISIBLE:View.GONE);
    }
    private void updateAdapter(){
        showProgressBar(false);
        if(type==TYPE_CLIENT){
            countTv.setText("No of invalid client:"+invalidDataModels.size()+"");
        }else if(type==TYPE_EVENT){
            countTv.setText("No of invalid event:"+invalidDataModels.size()+"");
        }
        InvalidDataAdapter adapter = new InvalidDataAdapter(this, new InvalidDataAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, InvalidDataModel content) {

            }
        });
        adapter.setData(invalidDataModels);
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onResumption() {

    }
}
