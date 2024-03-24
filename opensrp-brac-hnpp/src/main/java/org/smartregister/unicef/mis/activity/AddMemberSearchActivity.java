package org.smartregister.unicef.mis.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.adapter.SearchMigrationAdapter;
import org.smartregister.unicef.mis.contract.MigrationContract;
import org.smartregister.unicef.mis.contract.SearchDetailsContract;
import org.smartregister.unicef.mis.holder.SearchMigrationViewHolder;
import org.smartregister.unicef.mis.interactor.MigrationInteractor;
import org.smartregister.unicef.mis.job.HnppSyncIntentServiceJob;
import org.smartregister.unicef.mis.model.GlobalSearchResult;
import org.smartregister.unicef.mis.presenter.SearchDetailsPresenter;
import org.smartregister.unicef.mis.utils.GlobalSearchContentData;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppDBUtils;
import org.smartregister.unicef.mis.utils.HouseHoldInfo;
import org.smartregister.view.activity.SecuredActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AddMemberSearchActivity extends SecuredActivity implements View.OnClickListener, SearchDetailsContract.View {
    public static final String EXTRA_SEARCH_RESULT = "extra_result";
    public static final int REQUEST_CODE = 566;
    public static final String EXTRA_IS_CHILD= "extra_is_child";
    protected RecyclerView recyclerView;
    private ProgressBar progressBar;
    protected Spinner idTypeSpinner;
    protected EditText idNumberEditText,dobEditText;
    protected EditText shrIdEditText;
    protected int day, month, year;
    Calendar calendar;
    private SearchDetailsPresenter presenter;
    private SearchMigrationAdapter adapter;
    private boolean isChildForm;

    public static void startMigrationFilterActivity(Activity activity,boolean isChildForm){
        Intent intent = new Intent(activity, AddMemberSearchActivity.class);
        intent.putExtra(EXTRA_IS_CHILD,isChildForm);
        activity.startActivityForResult(intent,REQUEST_CODE);
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.add_member_search_view);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        idNumberEditText = findViewById(R.id.id_card_number);
        dobEditText = findViewById(R.id.dobEditText);
        shrIdEditText  = findViewById(R.id.shr_number);
//        shrIdEditText.setText("98000322738");
//        idNumberEditText.setText("20006715879510392");
//        dobEditText.setText("2000-01-01");
        idTypeSpinner = findViewById(R.id.id_type_spinner);
        findViewById(R.id.offline_btn).setOnClickListener(this);
        findViewById(R.id.ok_btn).setOnClickListener(this);
        findViewById(R.id.id_less_btn).setOnClickListener(this);
        findViewById(R.id.by_shr_search_btn).setOnClickListener(this);
        findViewById(R.id.shr_search_btn).setOnClickListener(this);
        findViewById(R.id.by_address_search_btn).setOnClickListener(this);
        findViewById(R.id.showCalenderBtn).setOnClickListener(this);
        findViewById(R.id.search_btn).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progress_bar);
        isChildForm = getIntent().getBooleanExtra(EXTRA_IS_CHILD,false);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        if(isChildForm){
            findViewById(R.id.id_type_ll).setVisibility(View.GONE);
        }

        presenter = new SearchDetailsPresenter(this);

        if (!HnppConstants.isConnectedToInternet(this)) {
            findViewById(R.id.offline_view).setVisibility(View.VISIBLE);
            findViewById(R.id.online_view).setVisibility(View.GONE);
        }else{
            findViewById(R.id.offline_view).setVisibility(View.GONE);
            findViewById(R.id.online_view).setVisibility(View.VISIBLE);
        }

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.offline_btn:
            case R.id.id_less_btn:
                Intent intent = new Intent();
                intent.putExtra("offline_reg", true);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.by_address_search_btn:
                findViewById(R.id.filter_by_shr).setVisibility(View.GONE);
                findViewById(R.id.filter_by_div).setVisibility(View.VISIBLE);
                break;
            case R.id.by_shr_search_btn:
                findViewById(R.id.filter_by_shr).setVisibility(View.VISIBLE);
                findViewById(R.id.filter_by_div).setVisibility(View.GONE);

                break;
            case R.id.backBtn:
            case R.id.ok_btn:
                finish();
                break;
            case R.id.search_btn: {
                if (!HnppConstants.isConnectedToInternet(this)) {
                    HnppConstants.checkNetworkConnection(this);
                    return;
                }
                if(!isChildForm && (idTypeSpinner.getSelectedItemPosition()==0 || TextUtils.isEmpty(idNumberEditText.getText()))){
                    idNumberEditText.setError("Enter id type");
                    return;
                }
                GlobalSearchContentData searchContentData = new GlobalSearchContentData();
                searchContentData.setIdType(idTypeSpinner.getSelectedItem().toString());
                if(isChildForm)searchContentData.setIdType("brid");
                searchContentData.setId(idNumberEditText.getText().toString());
                searchContentData.setDob(dobEditText.getText().toString());
                presenter.fetchAddMemberSearchData(searchContentData);
            }
                break;
            case R.id.shr_search_btn: {
                if(!HnppConstants.isConnectedToInternet(this)){
                    HnppConstants.checkNetworkConnection(this);
                    return;
                }
                if (TextUtils.isEmpty(shrIdEditText.getText())) {
                    shrIdEditText.setError("Enter SHR Id");
                    return;
                }
                GlobalSearchContentData searchContentData = new GlobalSearchContentData();
                searchContentData.setShrId(shrIdEditText.getText().toString());
                presenter.fetchAddMemberSearchData(searchContentData);
            }
            break;
            case R.id.showCalenderBtn:
                DatePickerDialog fromDialog = new DatePickerDialog(this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int yr, int mnt, int dayOfMonth) {

                        day = dayOfMonth;
                        month = mnt +1;
                        year = yr;

                        String fromDate = yr + "-" + HnppConstants.addZeroForMonth((mnt+1)+"")+"-"+HnppConstants.addZeroForMonth(dayOfMonth+"");
                        dobEditText.setText(fromDate);
                    }
                },year,(month-1),day);
                fromDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                fromDialog.show();
                break;
        }
    }
    @Override
    protected void onResumption() {

    }

    @Override
    public SearchDetailsContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void updateAdapter() {
        if(presenter.getMemberList().size()>0){
            Client content = presenter.getMemberList().get(0);
            if(content !=null && content.getBirthdate()!=null){
                showDetailsDialog(content);
            }else{
                Toast.makeText(this, R.string.no_member_found,Toast.LENGTH_LONG).show();
            }

        }else{
            Toast.makeText(this, R.string.no_member_found,Toast.LENGTH_LONG).show();
        }

//        adapter = new SearchMigrationAdapter(this, new SearchMigrationAdapter.OnClickAdapter() {
//            @Override
//            public void onItemClick(SearchMigrationViewHolder viewHolder, int adapterPosition, Client content) {
//                showDetailsDialog(content);
//            }
//
//            @Override
//            public void onClick(SearchMigrationViewHolder viewHolder, int adapterPosition, Client content) {
//
//            }
//
//        });
//        adapter.setData(presenter.getMemberList());
//        recyclerView.setVisibility(View.VISIBLE);
//        recyclerView.setAdapter(adapter);
//        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
//                DividerItemDecoration.VERTICAL));
    }
    private void sendDataToActivity(Client content){
        Intent intent = new Intent();
        intent.putExtra("idType", idTypeSpinner.getSelectedItem().toString());
        intent.putExtra("hid", shrIdEditText.getText().toString());
        intent.putExtra("firstName", content.getFirstName());
        intent.putExtra("dob", HnppConstants.DDMMYY.format(content.getBirthdate()));
        intent.putExtra("gender", content.getGender());
        intent.putExtra("baseEntityId", content.getBaseEntityId());
        intent.putExtra("nationalId", content.getAttribute("nationalId")==null?"":content.getAttribute("nationalId").toString());
        intent.putExtra("birthRegistrationID", content.getAttribute("birthRegistrationID")==null?"":content.getAttribute("birthRegistrationID").toString());
        intent.putExtra("father_name_english", content.getAttribute("father_name_english")==null?"":content.getAttribute("father_name_english").toString());
        intent.putExtra("mother_name_english", content.getAttribute("mother_name_english")==null?"":content.getAttribute("mother_name_english").toString());
        intent.putExtra("Mobile_Number",content.getAttribute("Mobile_Number")==null?"":content.getAttribute("Mobile_Number").toString());
        intent.putExtra("shr_id", content.getIdentifier("shr_id"));
        setResult(RESULT_OK, intent);
        finish();
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


    @Override
    public Context getContext() {
        return this;
    }

    private void showDetailsDialog(Client content){
        Dialog dialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.migration_member_details_dialog);
        TextView textViewName = dialog.findViewById(R.id.name_TV);
        TextView textViewVillage = dialog.findViewById(R.id.village_TV);
        TextView textViewPhoneNo = dialog.findViewById(R.id.phone_no_TV);
        StringBuilder nameBuilder = new StringBuilder();
        if(content.getFirstName()!=null){
            nameBuilder.append(content.getFirstName());
        }
//        if(content.getLastName() !=null){
//            nameBuilder.append(" ");
//            nameBuilder.append(content.getLastName());
//        }
            textViewName.setText(this.getString(R.string.name,nameBuilder.toString()));
            textViewName.append("\n");
            textViewName.append(this.getString(R.string.father_name,content.getAttribute("father_name_english")==null?"":content.getAttribute("father_name_english")));
            textViewName.append("\n");
            textViewName.append(this.getString(R.string.mother_name,content.getAttribute("mother_name_english")==null?"":content.getAttribute("mother_name_english")));
            if(content.getAddresses()!=null && content.getAddresses().size()>0){
                textViewVillage.setText(content.getAddresses().get(0).getCityVillage());
                textViewVillage.append(",");
                textViewVillage.append(content.getAddresses().get(0).getStateProvince());
                textViewVillage.append(",");
                textViewVillage.append(content.getAddresses().get(0).getCountyDistrict());
            }else{
                textViewVillage.setVisibility(View.GONE);
            }

            StringBuilder builder = new StringBuilder();
            builder.append(this.getString(R.string.dob, HnppConstants.DDMMYY.format(content.getBirthdate()))+"\n");
            builder.append(this.getString(R.string.gender_postfix, HnppConstants.getGender(content.getGender()))+"\n");
            builder.append(this.getString(R.string.phone_no,content.getAttribute("Mobile_Number")==null?"":content.getAttribute("Mobile_Number"))+"\n");
            builder.append(this.getString(R.string.nid,content.getAttribute("nationalId")==null?"":content.getAttribute("nationalId"))+"\n");
            builder.append(this.getString(R.string.bid,content.getAttribute("birthRegistrationID")==null?"":content.getAttribute("birthRegistrationID")));
            textViewPhoneNo.setVisibility(View.VISIBLE);
            textViewPhoneNo.setText(builder.toString());
        ((TextView) dialog.findViewById(R.id.migration_btn)).setText(R.string.registration_korun);
        dialog.findViewById(R.id.cross_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.migration_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                sendDataToActivity(content);

            }
        });
        dialog.show();

    }


}