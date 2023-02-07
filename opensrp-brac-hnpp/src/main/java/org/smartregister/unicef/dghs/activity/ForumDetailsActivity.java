//package org.smartregister.unicef.dghs.activity;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v7.widget.AppCompatTextView;
//import android.support.v7.widget.RecyclerView;
//import android.text.Editable;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.view.Gravity;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.EditText;
//import android.widget.ProgressBar;
//import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import org.smartregister.unicef.dghs.R;
//import org.smartregister.unicef.dghs.adapter.SearchHHMemberAdapter;
//import org.smartregister.unicef.dghs.contract.ForumDetailsContract;
//import org.smartregister.unicef.dghs.location.GeoLocationHelper;
//import org.smartregister.unicef.dghs.location.GeoLocation;
//import org.smartregister.unicef.dghs.location.SSModel;
//import org.smartregister.unicef.dghs.model.ForumDetails;
//import org.smartregister.unicef.dghs.model.HHMemberProperty;
//import org.smartregister.unicef.dghs.presenter.ForumDetailsPresenter;
//import org.smartregister.unicef.dghs.utils.HnppConstants;
//import org.smartregister.view.activity.SecuredActivity;
//import org.smartregister.view.customcontrols.CustomFontTextView;
//
//import java.util.ArrayList;
//import java.util.Date;
//
//import static org.smartregister.unicef.dghs.activity.SearchHHMemberActivity.EXTRA_INTENT_DATA;
//
//public class ForumDetailsActivity extends SecuredActivity implements View.OnClickListener, ForumDetailsContract.View {
//    private static int RESULT_CODE_HH = 11122;
//    private static int RESULT_CODE_MEMBER =22222;
//    private static String EXTRA_COMES_FROM = "comes_from";
//    private static String EXTRA_TITLE = "extra_title";
//
//    protected CustomFontTextView textViewForumName,textViewKhanaName;
//    protected EditText editTextNoOfParticipants,editTextNoOfService,editTextAdoFood;
//    private String fromType;
//    protected RecyclerView recyclerView;
//    private ForumDetailsPresenter presenter;
//    private ProgressBar progressBar;
//    protected Spinner village_spinner;
//    protected Spinner cluster_spinner,ss_spinner;
//    protected int ssIndex,vIndex,cIndex;
//    private ArrayAdapter<String> villageSpinnerArrayAdapter;
//    private String mSelectedVillageName,ssName;
//    private String mSelectedClasterName;
//    protected TextView textViewEmptySSName,textViewEmptyVillage,textViewEmptyClaster;
//
//    public static void startDetailsActivity(Activity activity, String comesFrom, String title){
//        Intent intent  = new Intent(activity,ForumDetailsActivity.class);
//        intent.putExtra(EXTRA_COMES_FROM,comesFrom);
//        intent.putExtra(EXTRA_TITLE,title);
//        activity.startActivity(intent);
//    }
//
//    @Override
//    protected void onCreation() {
//        setContentView(R.layout.activity_forum_details);
//        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
//        textViewForumName = findViewById(R.id.forum_name);
//        textViewKhanaName = findViewById(R.id.khana_name);
//        textViewEmptySSName = findViewById(R.id.ss_empty);
//        textViewEmptyVillage = findViewById(R.id.village_empty);
//        textViewEmptyClaster = findViewById(R.id.claster_empty);
//        editTextNoOfParticipants = findViewById(R.id.no_of_perticipant);
//        fromType = getIntent().getStringExtra(EXTRA_COMES_FROM);
//        textViewForumName.setText(getIntent().getStringExtra(EXTRA_TITLE));
//        findViewById(R.id.search_khana).setOnClickListener(this);
//        findViewById(R.id.add_member_btn).setOnClickListener(this);
//        findViewById(R.id.backBtn).setOnClickListener(this);
//        findViewById(R.id.submit_btn).setOnClickListener(this);
//        recyclerView = findViewById(R.id.recycler_view);
//        editTextNoOfService = findViewById(R.id.no_of_service_taken);
//        editTextAdoFood = findViewById(R.id.no_of_ado_taken);
//        progressBar = findViewById(R.id.progress_bar);
//        presenter = new ForumDetailsPresenter(this);
//        village_spinner = findViewById(R.id.village_filter_spinner);
//        cluster_spinner = findViewById(R.id.klaster_filter_spinner);
//        ss_spinner = findViewById(R.id.ss_filter_spinner);
//        onIntentDataSet();
//    }
//    protected void resetHHAndMember(){
//        if(hhMemberProperty!=null){
//            hhMemberProperty = new HHMemberProperty();
//            textViewKhanaName.setText("");
//        }
//        if(hhMemberPropertyArrayList.size()>0){
//            hhMemberPropertyArrayList.clear();
//            if(adapter!=null){
//                adapter.setData(hhMemberPropertyArrayList,new ArrayList<>());
//                adapter.notifyDataSetChanged();
//            }
//
//        }
//    }
//    protected void onIntentDataSet(){
//        if(!TextUtils.isEmpty(fromType) && fromType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.ADO.toString())){
//            findViewById(R.id.noOfAdoPanel).setVisibility(View.VISIBLE);
//        }
//        generateSpinner();
//    }
//    private void generateSpinner(){
//        editTextNoOfParticipants.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if(!TextUtils.isEmpty(s.toString()) && Integer.parseInt(s.toString())>50){
//                    editTextNoOfParticipants.setError("ফোরামে অংশ গ্রহনকারীর সংখ্যা ৫০ জন এর বেশি হবে না");
//                }
//
//            }
//        });
//        ArrayList<String> ssSpinnerArray = new ArrayList<>();
//
//        ArrayList<String> villageSpinnerArray = new ArrayList<>();
//
//        ArrayList<SSModel> ssLocationForms = GeoLocationHelper.getInstance().getSsModels();
//        for (SSModel ssModel : ssLocationForms) {
//            ssSpinnerArray.add(ssModel.username);
//        }
//
//        ArrayAdapter<String> ssSpinnerArrayAdapter = new ArrayAdapter<String>
//                (this, android.R.layout.simple_spinner_item,
//                        ssSpinnerArray){
//            @Override
//            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//                convertView = super.getDropDownView(position, convertView,
//                        parent);
//
//                AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
//                appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
//                appCompatTextView.setHeight(100);
//
//                return convertView;
//            }
//        };
//
//        villageSpinnerArrayAdapter = new ArrayAdapter<String>
//                (this, android.R.layout.simple_spinner_item,
//                        villageSpinnerArray){
//            @Override
//            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//                convertView = super.getDropDownView(position, convertView,
//                        parent);
//                AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
//                appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
//                appCompatTextView.setHeight(100);
//                return convertView;
//            }
//        };
//
//        ArrayAdapter<String> clusterSpinnerArrayAdapter = new ArrayAdapter<String>
//                (this, android.R.layout.simple_spinner_item,
//                        HnppConstants.getClasterSpinnerArray()){
//            @Override
//            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//                convertView = super.getDropDownView(position, convertView,
//                        parent);
//                AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
//                appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
//                appCompatTextView.setHeight(100);
//
//                return convertView;
//            }
//        };
//        village_spinner.setAdapter(villageSpinnerArrayAdapter);
//        cluster_spinner.setAdapter(clusterSpinnerArrayAdapter);
//        ss_spinner.setAdapter(ssSpinnerArrayAdapter);
//        ss_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position != -1) {
//                    ssIndex = position;
//                    ssName = ssSpinnerArray.get(position);
//                    villageSpinnerArray.clear();
//                    ArrayList<GeoLocation> ssLocations = GeoLocationHelper.getInstance().getSsModels().get(position).locations;
//                    for (GeoLocation geoLocation1 : ssLocations) {
//                        villageSpinnerArray.add(geoLocation1.village.name.trim());
//                    }
//                    villageSpinnerArrayAdapter = new ArrayAdapter<String>
//                            (ForumDetailsActivity.this, android.R.layout.simple_spinner_item,
//                                    villageSpinnerArray);
//                    village_spinner.setAdapter(villageSpinnerArrayAdapter);
//                    resetHHAndMember();
//                    //villageSpinnerArrayAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        village_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position != -1) {
//                    vIndex = position;
//                    mSelectedVillageName = villageSpinnerArray.get(position);
//                    resetHHAndMember();
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        cluster_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position != -1) {
//                    cIndex = position;
//                    mSelectedClasterName = HnppConstants.getClasterNames().get(HnppConstants.getClasterSpinnerArray().get(position));
//                resetHHAndMember();
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//    }
//
//    @Override
//    protected void onResumption() {
//
//    }
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.submit_btn:
//                ForumDetails forumDetails = getForumDetails();
//                if(forumDetails !=null){
//                    v.setAlpha(0.3f);
//                    v.setEnabled(false);
//                    if(fromType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.CHILD.toString())){
//                        presenter.processChildForumEvent(forumDetails);
//                    }else if(fromType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.WOMEN.toString())){
//                        presenter.processWomenForum(forumDetails);
//                    }else if(fromType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.ADO.toString())){
//                        presenter.processAdoForum(forumDetails);
//                    }else if(fromType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.NCD.toString())){
//                        presenter.processNcdForum(forumDetails);
//                    }
//                    else if(fromType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.ADULT.toString())){
//                        presenter.processAdultForum(forumDetails);
//                    }
//
//                }
//
//                break;
//
//            case R.id.backBtn:
//                finish();
//                break;
//            case R.id.search_khana:
//                SearchHHMemberActivity.startSearchActivity(this,mSelectedVillageName,mSelectedClasterName, HnppConstants.SEARCH_TYPE.HH.toString(),new ArrayList<>(),RESULT_CODE_HH);
//                break;
//            case R.id.add_member_btn:
//                SearchHHMemberActivity.startSearchActivity(this,mSelectedVillageName,mSelectedClasterName,fromType,hhMemberPropertyArrayList,RESULT_CODE_MEMBER);
//
//                break;
//            default:
//                break;
//        }
//
//    }
//    private ForumDetails getForumDetails(){
//        if(TextUtils.isEmpty(editTextNoOfParticipants.getText().toString())){
//            editTextNoOfParticipants.setError("অংশগ্রহণকারী কত জন ছিল?");
//            return null;
//        }
//        if(!TextUtils.isEmpty(editTextNoOfParticipants.getText().toString()) &&
//                Integer.parseInt(editTextNoOfParticipants.getText().toString())>50){
//            editTextNoOfParticipants.setError("ফোরামে অংশ গ্রহনকারীর সংখ্যা ৫০ জন এর বেশি হবে না");
//            return null;
//        }
//        if(hhMemberProperty == null){
//            Toast.makeText(this,"স্থান সিলেক্ট করুন",Toast.LENGTH_SHORT).show();
//            return null;
//        }
//        if(hhMemberPropertyArrayList.size()==0){
//            Toast.makeText(this,"সদস্য যোগ করুন",Toast.LENGTH_SHORT).show();
//            return null;
//        }
//        int participant = Integer.parseInt(editTextNoOfParticipants.getText().toString());
//        if(participant<hhMemberPropertyArrayList.size()){
//            Toast.makeText(this,"ফোরামে অংশগ্রহণকারীর সংখ্যা সেবা গ্রহণকারীর চেয়ে বেশি হবে",Toast.LENGTH_SHORT).show();
//            return null;
//        }
//        if(TextUtils.isEmpty(editTextNoOfService.getText().toString())){
//            editTextNoOfService.setError("কতজন সেবা নিয়েছে?");
//            return null;
//        }
//        if(Integer.parseInt(editTextNoOfService.getText().toString())>Integer.parseInt(editTextNoOfParticipants.getText().toString())){
//            editTextNoOfService.setError("অংশগ্রহণকারীর সংখ্যার সমান অথবা কম হবে ");
//            return null;
//        }
//        if(fromType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.ADO.toString()) &&
//                TextUtils.isEmpty(editTextAdoFood.getText().toString())){
//            editTextAdoFood.setError("কত জন কিশোরী খাবার খেয়েছে?");
//            return null;
//        }
//        ForumDetails forumDetails = new ForumDetails();
//        forumDetails.forumName = textViewForumName.getText().toString();
//        forumDetails.forumType = getForumType();
//        forumDetails.noOfParticipant = editTextNoOfParticipants.getText().toString();
//        forumDetails.participants = hhMemberPropertyArrayList;
//        forumDetails.place = hhMemberProperty;
//        forumDetails.villageName = mSelectedVillageName;
//        forumDetails.ssName = ssName;
//        forumDetails.clusterName = mSelectedClasterName;
//        forumDetails.sIndex = ssIndex;
//        forumDetails.cIndex = cIndex;
//        forumDetails.forumDate = new Date().toString();
//        forumDetails.noOfServiceTaken = editTextNoOfService.getText().toString();
//        if(!TextUtils.isEmpty(editTextAdoFood.getText().toString())){
//            forumDetails.noOfAdoTakeFiveFood =  editTextAdoFood.getText().toString();
//        }
//
//        return forumDetails;
//    }
//
//    private String getForumType() {
//        if(fromType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.CHILD.toString())){
//            return HnppConstants.EVENT_TYPE.FORUM_CHILD;
//        }
//        if(fromType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.ADO.toString())){
//            return HnppConstants.EVENT_TYPE.FORUM_ADO;
//        }
//        if(fromType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.WOMEN.toString())){
//            return HnppConstants.EVENT_TYPE.FORUM_WOMEN;
//        }
//        if(fromType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.NCD.toString())){
//            return HnppConstants.EVENT_TYPE.FORUM_NCD;
//        }
//        if(fromType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.ADULT.toString())){
//            return HnppConstants.EVENT_TYPE.FORUM_ADULT;
//        }
//        return "";
//    }
//
//    ArrayList<HHMemberProperty> hhMemberPropertyArrayList = new ArrayList<>();
//    HHMemberProperty hhMemberProperty;
//    SearchHHMemberAdapter adapter;
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(resultCode == RESULT_OK && requestCode == RESULT_CODE_HH){
//            ArrayList<HHMemberProperty> hhMemberPropertyArrayList = (ArrayList<HHMemberProperty>) data.getSerializableExtra(EXTRA_INTENT_DATA);
//            if(hhMemberPropertyArrayList.size()>0){
//                hhMemberProperty = hhMemberPropertyArrayList.get(0);
//                String name = hhMemberProperty.getName();
//                String id = hhMemberProperty.getId();
//                textViewKhanaName.setText("খানা : "+name +" \n"+"আইডি: "+id);
//
//            }
//        }else if(resultCode == RESULT_OK && requestCode == RESULT_CODE_MEMBER){
//            hhMemberPropertyArrayList.clear();
//            hhMemberPropertyArrayList = (ArrayList<HHMemberProperty>) data.getSerializableExtra(EXTRA_INTENT_DATA);
//             adapter= new SearchHHMemberAdapter(this, new SearchHHMemberAdapter.OnClickAdapter() {
//                @Override
//                public void onClick(int position, HHMemberProperty content, boolean isAd) {
//
//                }
//
//                @Override
//                public void onClickHH(int position, HHMemberProperty content) {
//
//                }
//
//                @Override
//                public void onRemove(int position, HHMemberProperty content) {
//                    hhMemberPropertyArrayList.remove(position);
//                    adapter.setData(hhMemberPropertyArrayList,new ArrayList<>());
//                    adapter.notifyDataSetChanged();
//                }
//            },"");
//            adapter.setData(hhMemberPropertyArrayList,new ArrayList<>());
//            recyclerView.setAdapter(adapter);
//
//        }
//    }
//
//    @Override
//    public void showProgressBar() {
//        findViewById(R.id.submit_btn).setAlpha(0.3f);
//        findViewById(R.id.submit_btn).setEnabled(false);
//        progressBar.setVisibility(View.VISIBLE);
//    }
//
//    @Override
//    public void hideProgressBar() {
//        findViewById(R.id.submit_btn).setAlpha(1.0f);
//        findViewById(R.id.submit_btn).setEnabled(true);
//        progressBar.setVisibility(View.GONE);
//    }
//
//    @Override
//    public void showSuccessMessage(String message) {
//        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
//        finish();
//
//    }
//
//    @Override
//    public void showFailedMessage(String message) {
//        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public Context getContext() {
//        return this;
//    }
//}
