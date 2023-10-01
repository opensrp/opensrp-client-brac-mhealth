package org.smartregister.unicef.dghs.activity;

import static org.smartregister.unicef.dghs.activity.HnppFamilyOtherMemberProfileActivity.IS_COMES_IDENTITY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.contract.SearchDetailsContract;
import org.smartregister.unicef.dghs.interactor.SearchDetailsInteractor;
import org.smartregister.unicef.dghs.model.GlobalSearchResult;
import org.smartregister.unicef.dghs.utils.GlobalSearchContentData;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.OtherVaccineContentData;
import org.smartregister.view.activity.SecuredActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class SearchActivity extends SecuredActivity implements SearchDetailsContract.InteractorCallBack {

    EditText vaccineNameTv,brnTv;
    TextView dobTv;
    @Override
    protected void onCreation() {

        setContentView(R.layout.activity_search_view);
        vaccineNameTv = findViewById(R.id.vaccine_name);
        dobTv = findViewById(R.id.dob_tv);
        brnTv = findViewById(R.id.brn_number);
        findViewById(R.id.shr_search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processOtherVaccine();
            }
        });
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.dob_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(dobTv);
            }
        });
        findViewById(R.id.date_picker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(dobTv);
            }
        });


//        processResult("camp,HPV,11111111111111111,2023-09-04,loy loy,ffg,fgfg");
    }

    @Override
    protected void onResumption() {

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private String vaccineName,dob;
    private void processOtherVaccine(){
        vaccineName = vaccineNameTv.getText().toString();
        String brn = brnTv.getText().toString();
        dob = dobTv.getText().toString();
        if(TextUtils.isEmpty(brn)||TextUtils.isEmpty(dob)||TextUtils.isEmpty(vaccineName)){
            Toast.makeText(this,"Please input all field",Toast.LENGTH_LONG).show();
            return;
        }
        if(!HnppConstants.isConnectedToInternet(this)){
            HnppConstants.checkNetworkConnection(this);
            return;
        }
        OtherVaccineContentData otherVaccineContentData = new OtherVaccineContentData();
        otherVaccineContentData.brn = brn;
        otherVaccineContentData.vaccine_name = vaccineName;
        otherVaccineContentData.dob = dob;
        String date = HnppConstants.DDMMYY.format(System.currentTimeMillis());
        otherVaccineContentData.date = date;
        globalSearchContentData = new GlobalSearchContentData();
        globalSearchContentData.setMigrationType(HnppConstants.MIGRATION_TYPE.OTHER_VACCINE.name());
        globalSearchContentData.setOtherVaccineContentData(otherVaccineContentData);
        if(!TextUtils.isEmpty(otherVaccineContentData.brn) && !TextUtils.isEmpty(otherVaccineContentData.dob)){
            SearchDetailsContract.Interactor interactor = new SearchDetailsInteractor(new AppExecutors());
            showProgressDialog("Searching....");
            interactor.fetchOtherVaccineData(otherVaccineContentData,this);

        }else{
            Toast.makeText(this,"BRN not found",Toast.LENGTH_SHORT).show();
            finish();
        }

    }
    @Override
    public void onUpdateOtherVaccine(OtherVaccineContentData otherVaccineContentData) {
        hideProgressDialog();
        otherVaccineContentData.vaccine_name = vaccineName;
        String date = HnppConstants.YYMMDD.format(System.currentTimeMillis());
        otherVaccineContentData.date = date;
        otherVaccineContentData.dob = dob;
        if(otherVaccineContentData!=null)showDetailsDialog(otherVaccineContentData);
    }
    private void showDetailsDialog(OtherVaccineContentData content){
        String buttonName= getString(R.string.other_vaccine_button,content.vaccine_name);
        StringBuilder builder = new StringBuilder();
        String name = content.firstNameBN;//+" "+content.lastName;
        builder.append(this.getString(R.string.name,name)+"\n");
        builder.append(this.getString(R.string.father_name,content.fatherName)+"\n");
        builder.append(this.getString(R.string.mother_name,content.motherName)+"\n");
        builder.append(this.getString(R.string.dob, content.dob)+"\n");
        builder.append(this.getString(R.string.bid,content.brn));
        if(TextUtils.isEmpty(content.brn)){
            Toast.makeText(this,"No result found",Toast.LENGTH_LONG).show();
            return;
        }
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_qrscan);
        TextView textViewTitle = dialog.findViewById(R.id.text_tv);
        TextView titleTxt = dialog.findViewById(R.id.title_tv);
        titleTxt.setText(buttonName);
        TextView vaccineDateTxt = dialog.findViewById(R.id.vaccine_date_tv);
        vaccineDateTxt.setText(content.date);
        dialog.findViewById(R.id.date_picker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(vaccineDateTxt);
            }
        });
        textViewTitle.setText(builder.toString());
        dialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                content.date = vaccineDateTxt.getText().toString();
                saveOtherVaccineInfo(content);
                finish();
            }
        });
        dialog.show();

    }

    private void showDatePicker(TextView vaccineDateTxt) {
        String[] yyMMdd = HnppConstants.YYMMDD.format(System.currentTimeMillis()).split("-");
        DatePickerDialog fromDialog = new DatePickerDialog(this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int yr, int mnt, int dayOfMonth) {

                String fromDate = yr + "-" + HnppConstants.addZeroForMonth((mnt+1)+"")+"-"+HnppConstants.addZeroForMonth(dayOfMonth+"");
                vaccineDateTxt.setText(fromDate);
            }
        },Integer.parseInt(yyMMdd[0]),Integer.parseInt(yyMMdd[1]),Integer.parseInt(yyMMdd[2]));
        fromDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        fromDialog.show();
    }

    private GlobalSearchContentData globalSearchContentData;
    private GlobalSearchResult globalSearchResult;
    private void startGlobalSearchWithQR(){
        SearchDetailsContract.Interactor interactor = new SearchDetailsInteractor(new AppExecutors());
        showProgressDialog("Searching....");
        interactor.fetchData(globalSearchContentData,this);

    }
    private ProgressDialog dialog;

    private void showProgressDialog(String text) {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setMessage(text);
            dialog.setCancelable(false);
            dialog.show();
        }

    }

    private void hideProgressDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onUpdateList(ArrayList<Client> list) {
        hideProgressDialog();
        if(list.size()>0) {
            Client client = list.get(0);
            saveClientAndEvent(client);
        }else{
            Toast.makeText(this,"No result found",Toast.LENGTH_LONG).show();
            finish();
        }
    }
//    @SuppressLint("SimpleDateFormat")
//    private void saveClientAndEvent(Client baseClient){
//
//        try{
//            if(baseClient == null) return;
//            if(globalSearchContentData.getMigrationType().equalsIgnoreCase(HnppConstants.MIGRATION_TYPE.HH.name())) {
//                List<String> ids = new ArrayList<>();
//                ids.add(globalSearchContentData.getFamilyBaseEntityId());
//                ids.add(globalSearchContentData.getFamilyBaseEntityId());
//                baseClient.getRelationships().put("family",ids);
//            }
//            JSONObject clientJson = new JSONObject(JsonFormUtils.gson.toJson(baseClient));
//
//            getSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);
//
//            for (Event baseEvent: globalSearchResult.events) {
//                if(baseEvent.getBaseEntityId().equalsIgnoreCase(baseClient.getBaseEntityId())){
//                    JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
//                    getSyncHelper().addEvent(baseEvent.getBaseEntityId(), eventJson);
//                }
//            }
//
//            long lastSyncTimeStamp = getAllSharedPreferences().fetchLastUpdatedAtDate(0);
//            Date lastSyncDate = new Date(lastSyncTimeStamp);
//            getClientProcessorForJava().processClient(getSyncHelper().getEvents(lastSyncDate, BaseRepository.TYPE_Unprocessed));
//            getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
//                ContentValues values = new ContentValues();
//                values.put(org.smartregister.chw.anc.util.DBConstants.KEY.DATE_REMOVED, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
//                values.put("is_closed", 1);
//                HnppApplication.getInstance().getRepository().getWritableDatabase().update(CoreConstants.TABLE_NAME.FAMILY_MEMBER, values,
//                        org.smartregister.chw.anc.util.DBConstants.KEY.BASE_ENTITY_ID + " = ?  ", new String[]{baseClient.getBaseEntityId()});
//
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                GlobalSearchMemberProfileActivity.startGlobalMemberProfileActivity(QRScannerActivity.this, baseClient);
//                finish();
//            }
//        },1000);
//
//    }
    private void saveClientAndEvent(Client baseClient){
        AppExecutors appExecutors = new AppExecutors();
        @SuppressLint("SimpleDateFormat") Runnable runnable = () -> {
            try{
                if(baseClient == null) return;
                if(globalSearchContentData.getMigrationType().equalsIgnoreCase(HnppConstants.MIGRATION_TYPE.HH.name())) {
                    List<String> ids = new ArrayList<>();
                    ids.add(globalSearchContentData.getFamilyBaseEntityId());
                    ids.add(globalSearchContentData.getFamilyBaseEntityId());
                    baseClient.getRelationships().put("family",ids);
                    String previousProviderId = baseClient.getAttribute("provider_id")+"";
                    Log.v("GLOBAL_SEARCH","previousProviderId>>"+previousProviderId);
                    if(!TextUtils.isEmpty(previousProviderId) && !previousProviderId.equalsIgnoreCase("null")){
                        Map<String,String> identifiers =  baseClient.getIdentifiers();
                        if(identifiers ==null) identifiers = new HashMap<>();
                        identifiers.put("previous_provider", previousProviderId);
                        identifiers.put("is_migrated", "true");
                        baseClient.setIdentifiers(identifiers);
                    }

                }
                JSONObject clientJson = new JSONObject(JsonFormUtils.gson.toJson(baseClient));

                getSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);

                for (Event baseEvent: globalSearchResult.events) {
                    if(baseEvent.getEventType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION)
                            || baseEvent.getEventType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_UPDATE_REGISTRATION)){
                        for (Obs observation:baseEvent.getObs()) {
                            if(Objects.equals(observation.getFieldCode(), "age_calculated")){
                                int age = Integer.parseInt(observation.getValues().get(0)+"");
                                if(age>5){
                                    baseEvent.setEventType(HnppConstants.EVENT_TYPE.FAMILY_MEMBER_REGISTRATION);
                                }else{
                                    baseEvent.setEventType(HnppConstants.EVENT_TYPE.CHILD_REGISTRATION);
                                }
                                break;
                            }
                        }
                    }
                    if(baseEvent.getBaseEntityId().equalsIgnoreCase(baseClient.getBaseEntityId())){
                        JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
                        getSyncHelper().addEvent(baseEvent.getBaseEntityId(), eventJson);
                    }
                }

                long lastSyncTimeStamp = getAllSharedPreferences().fetchLastUpdatedAtDate(0);
                Date lastSyncDate = new Date(lastSyncTimeStamp);
                getClientProcessorForJava().processClient(getSyncHelper().getEvents(lastSyncDate, BaseRepository.TYPE_Unprocessed));
                getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
                if(globalSearchContentData.getMigrationType().equalsIgnoreCase(HnppConstants.MIGRATION_TYPE.Member.name())) {
                    ContentValues values = new ContentValues();
                    values.put(org.smartregister.chw.anc.util.DBConstants.KEY.DATE_REMOVED, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                    values.put("is_closed", 1);
                    HnppApplication.getInstance().getRepository().getWritableDatabase().update(CoreConstants.TABLE_NAME.FAMILY_MEMBER, values,
                            org.smartregister.chw.anc.util.DBConstants.KEY.BASE_ENTITY_ID + " = ?  ", new String[]{baseClient.getBaseEntityId()});
                }
                Log.v("FAMILY_IDS","saveClientAndEvent>>"+globalSearchContentData.getFamilyBaseEntityId());

            } catch (Exception e) {
                e.printStackTrace();
            }
            appExecutors.mainThread().execute(() -> {
                GlobalSearchMemberProfileActivity.startGlobalMemberProfileActivity(SearchActivity.this, baseClient);
                finish();
            });
        };
        appExecutors.diskIO().execute(runnable);
//

    }
    @Override
    public void setGlobalSearchResult(GlobalSearchResult globalSearchResult) {
        this.globalSearchResult = globalSearchResult;

    }


    private void saveOtherVaccineInfo(OtherVaccineContentData contentData){
        showProgressDialog("saving....");
        HnppConstants.saveOtherVaccineData(contentData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(String s) {
                        Log.v("OTHER_VACCINE","onNext>>s:"+s);
                        hideProgressDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v("OTHER_VACCINE",""+e);
                        hideProgressDialog();
                    }

                    @Override
                    public void onComplete() {
                        Log.v("OTHER_VACCINE","completed");
                        hideProgressDialog();
                    }
                });
        //OtherVaccineJob.scheduleJobImmediately(OtherVaccineJob.TAG);

    }
    public ECSyncHelper getSyncHelper() {
        return FamilyLibrary.getInstance().getEcSyncHelper();
    }


    public AllSharedPreferences getAllSharedPreferences() {
        return org.smartregister.chw.core.utils.Utils.context().allSharedPreferences();
    }

    public ClientProcessorForJava getClientProcessorForJava() {
        return FamilyLibrary.getInstance().getClientProcessorForJava();
    }
    private CommonPersonObjectClient clientObject(String baseEntityId) {
        CommonRepository commonRepository =Utils.context().commonrepository(Utils.metadata().familyMemberRegister.tableName);
        final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(baseEntityId);
        if(commonPersonObject==null) return null;
        final CommonPersonObjectClient client =
                new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
        client.setColumnmaps(commonPersonObject.getColumnmaps());
        return client;
    }
    @Override
    protected void onPause() {
        super.onPause();
    }



}

