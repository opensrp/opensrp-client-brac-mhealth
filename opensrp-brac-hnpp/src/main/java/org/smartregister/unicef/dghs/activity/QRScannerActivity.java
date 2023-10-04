package org.smartregister.unicef.dghs.activity;

import static org.smartregister.unicef.dghs.activity.HnppFamilyOtherMemberProfileActivity.IS_COMES_IDENTITY;
import static org.smartregister.unicef.dghs.utils.HnppConstants.showDialogWithAction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
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
import org.smartregister.unicef.dghs.job.GlobalLocationFetchJob;
import org.smartregister.unicef.dghs.model.GlobalSearchResult;
import org.smartregister.unicef.dghs.utils.GlobalSearchContentData;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppDBUtils;
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


public class QRScannerActivity extends SecuredActivity implements ZXingScannerView.ResultHandler,SearchDetailsContract.InteractorCallBack {

    private static final int CAMERA_PERMISSION_REQUEST = 123;

    private ZXingScannerView scannerView;
    @Override
    protected void onCreation() {

        scannerView = new ZXingScannerView(this);
        List<BarcodeFormat> barcodeFormats = new ArrayList<>();
        barcodeFormats.add(BarcodeFormat.QR_CODE);
        scannerView.setFormats(barcodeFormats);
        // this paramter will make your HUAWEI phone works great!
        //scannerView.setAspectTolerance(0.5f);
        setContentView(scannerView);
        if (ContextCompat.checkSelfPermission(QRScannerActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanner();
        } else {
            requestCameraPermission();
        }
//        processResult("camp,HPV,11111111111111111,2023-09-04,loy loy,ffg,fgfg");
    }

    @Override
    protected void onResumption() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanner();
        } else {
            requestCameraPermission();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void startScanner() {
        if (scannerView!=null){
            scannerView.setResultHandler(this);
            scannerView.startCamera();
        }

    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(QRScannerActivity.this,
                new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanner();
            } else {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processResult(String scanResult){
        Log.v("SCANNER_RESULT","scannedData>>"+scanResult);

           String[] ss = scanResult.split(",");
        if (ss.length > 1) {
           //base_entity,registrationId,divisionId,districtId,dob,gender,http://unicef-ha.mpower-social.com/opensrp-dashboard/epi-card.html,
            //other vaccine camp,HPV,brn,dob
            if(ss[0].equalsIgnoreCase("camp")){
                processOtherVaccine(ss);
                return;
            }
            String baseEntityId = ss[0];
            String shrId =ss[1];
            boolean isShr = ss[1].length() == 11;
            String divId = ss[2];
            String disId = ss[3];
            String dob = ss[4];
            String gender = ss[5];
            CommonPersonObjectClient client = clientObject(baseEntityId);//HnppDBUtils.getCommonPersonByBaseEntityId(baseEntityId);
            if (client != null) {
                String dobString = Utils.getDuration(Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false));
                Integer yearOfBirth = CoreChildUtils.dobStringToYear(dobString);
                if (yearOfBirth != null && yearOfBirth > 5) {
                    String DOD = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOD, false);
                    if (StringUtils.isEmpty(DOD)) {
                        Intent intent = new Intent(this, HnppFamilyOtherMemberProfileActivity.class);
                        intent.putExtra(IS_COMES_IDENTITY, false);
                        intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, client.getCaseId());
                        intent.putExtra(CoreConstants.INTENT_KEY.CHILD_COMMON_PERSON, client);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    String DOD = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOD, false);
                    if (StringUtils.isEmpty(DOD)) {
                        Intent intent = new Intent(this, HnppChildProfileActivity.class);

                        intent.putExtra(CoreConstants.INTENT_KEY.IS_COMES_FROM_FAMILY, true);
                        intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, client.getCaseId());
                        intent.putExtra(org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT, new MemberObject(client));
                        startActivity(intent);
                        finish();
                    }
                }

            }else{
                if(!HnppConstants.isConnectedToInternet(this)){
                    HnppConstants.checkNetworkConnection(this);
                    return;
                }
                //go to global searchresult with data
                globalSearchContentData = new GlobalSearchContentData();
                globalSearchContentData.setId(isShr?"shr_id=":"unique_id="+""+shrId);
                if(isShr)globalSearchContentData.setShrId(shrId);
                globalSearchContentData.setMigrationType(HnppConstants.MIGRATION_TYPE.Member.name());
                globalSearchContentData.setDivisionId(divId);
                globalSearchContentData.setDistrictId(disId);
                globalSearchContentData.setGender(gender);
                if(!TextUtils.isEmpty(dob))globalSearchContentData.setDob(dob);
                if(globalSearchContentData.getDistrictId()!=null && globalSearchContentData.getDivisionId()!=null){
                    startGlobalSearchWithQR();
                }else{
                    Toast.makeText(this,"Id not found",Toast.LENGTH_SHORT).show();
                    finish();
                }

            }
        }else{
            Toast.makeText(this,"ScanResult not valid, please try again",Toast.LENGTH_LONG).show();
            finish();
        }
    }
    private String vaccineName,dob;
    private void processOtherVaccine(String[] result){
        vaccineName = result[1];
        String brn = result[2];
        dob = result[3];
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
            finish();
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
        String[] yyMMdd = vaccineDateTxt.getText().toString().split("-");
        DatePickerDialog fromDialog = new DatePickerDialog(this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int yr, int mnt, int dayOfMonth) {

                String fromDate = yr + "-" + HnppConstants.addZeroForMonth((mnt+1)+"")+"-"+HnppConstants.addZeroForMonth(dayOfMonth+"");
                vaccineDateTxt.setText(fromDate);
            }
        },Integer.parseInt(yyMMdd[0]),Integer.parseInt(yyMMdd[1]),Integer.parseInt(yyMMdd[2]));
        fromDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        fromDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 259200000L);
        fromDialog.show();
    }

    @Override
    public void handleResult(Result result) {
        processResult(result.getText());
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
        if (scannerView!=null)scannerView.stopCamera();
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
                GlobalSearchMemberProfileActivity.startGlobalMemberProfileActivity(QRScannerActivity.this, baseClient);
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
        if (scannerView!=null)scannerView.stopCamera();
    }



}

