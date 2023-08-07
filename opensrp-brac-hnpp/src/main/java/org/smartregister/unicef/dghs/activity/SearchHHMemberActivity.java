package org.smartregister.unicef.dghs.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.adapter.SearchHHMemberAdapter;
import org.smartregister.unicef.dghs.contract.SearchHHMemberContract;
import org.smartregister.unicef.dghs.model.HHMemberProperty;
import org.smartregister.unicef.dghs.presenter.SearchHHMemberPresenter;
import org.smartregister.unicef.dghs.utils.FilterDialog;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;

public class SearchHHMemberActivity extends SecuredActivity implements View.OnClickListener, SearchHHMemberContract.View {

    private static String EXTRA_TYPE_FOR = "extra_type";
    private static String EXTRA_VILLAGE_NAME= "extra_village";
    private static String EXTRA_CLUSTER_NAME= "extra_cluster";
    public static String EXTRA_INTENT_DATA = "extra_data";

    private EditText editTextSearch;
    private TextView villageNameTxt,clusterNameTxt;
    private ProgressBar progressBar;

    private String searchType,villageName,clusterName;
    SearchHHMemberPresenter presenter;
    private RecyclerView recyclerView;
    SearchHHMemberAdapter searchHHMemberAdapter;
    ArrayList<HHMemberProperty> previousList = new ArrayList<>();
    ImageView crossBtn;
    public static void startSearchActivity(Activity activity,String villageName, String clusterName, String searchType, ArrayList<HHMemberProperty> memberPropertyArrayList, int resultCode){
        Intent intent = new Intent(activity,SearchHHMemberActivity.class);
        intent.putExtra(EXTRA_TYPE_FOR,searchType);
        intent.putExtra(EXTRA_VILLAGE_NAME,villageName);
        intent.putExtra(EXTRA_CLUSTER_NAME,clusterName);
        intent.putExtra(EXTRA_INTENT_DATA,memberPropertyArrayList);
        activity.startActivityForResult(intent,resultCode);
    }


    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_search_hh);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        editTextSearch = findViewById(R.id.search_edit_text);
        //editTextSearch.setVisibility(View.GONE);
        villageNameTxt = findViewById(R.id.village_name_filter);
        clusterNameTxt = findViewById(R.id.cluster_filter);
        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recycler_view);
        CustomFontTextView title = findViewById(R.id.textview_detail_two);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        crossBtn = findViewById(R.id.cross_btn);
        crossBtn.setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.add_btn).setOnClickListener(this);
        searchType = getIntent().getStringExtra(EXTRA_TYPE_FOR);
        previousList = (ArrayList<HHMemberProperty> )getIntent().getSerializableExtra(EXTRA_INTENT_DATA);
        villageName = getIntent().getStringExtra(EXTRA_VILLAGE_NAME);
        clusterName = getIntent().getStringExtra(EXTRA_CLUSTER_NAME);
        if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.HH.toString())){
            title.setText(R.string.search_house);
            findViewById(R.id.add_btn).setVisibility(View.GONE);
        }else{
            title.setText(R.string.search_member);
        }
        SearchHHMemberAdapter.selectedId.clear();
        presenter = new SearchHHMemberPresenter(this);
        if(previousList.size() > 0){
            presenter.updatePreviousSelectedList(previousList);
        }
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!TextUtils.isEmpty(s.toString())){
                    crossBtn.setVisibility(View.VISIBLE);
                }else{
                    crossBtn.setVisibility(View.GONE);
                }
                if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.HH.toString())){
                    presenter.searchHH(s.toString());
                }else if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.ADO.toString())){
                    presenter.searchAdo(s.toString());
                }
                else if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.WOMEN.toString())){
                    presenter.searchWomen(s.toString());
                }
                else if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.CHILD.toString())){
                    presenter.searchChild(s.toString());
                }
                else if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.NCD.toString())
                        || searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.ADULT.toString())){
                    presenter.searchNcd(s.toString());
                }

            }
        });
        showFilterData();

    }

    @Override
    protected void onResumption() {

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cross_btn:
                editTextSearch.setText("");
                break;
            case R.id.add_btn:
                sendData();
                break;
            case R.id.backBtn:
                finish();
                break;
        }


    }
    private void sendData(){
        Intent intent = new Intent();
        intent.putExtra(EXTRA_INTENT_DATA,presenter.getSelectedList());
        setResult(RESULT_OK, intent);
        finish();
    }
    private void showFilterData(){
//
//        new FilterDialog().showDialog(this, new FilterDialog.OnFilterDialogFilter() {
//            @Override
//            public void onDialogPress(String ssName, String villageName, String cluster, String gender) {
                villageNameTxt.setText(villageName);
                clusterNameTxt.setText(HnppConstants.getClusterNameFromValue(clusterName));
                if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.HH.toString())){
                    presenter.fetchHH(villageName,clusterName);
                }else if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.ADO.toString())){
                    presenter.fetchAdo(villageName,clusterName);
                }else if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.WOMEN.toString())){
                    presenter.fetchWomen(villageName,clusterName);
                }else if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.CHILD.toString())){
                    presenter.fetchChild(villageName,clusterName);
                }else if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.NCD.toString())){
                    presenter.fetchNcd(villageName,clusterName);
                }else if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.ADULT.toString())){
                    presenter.fetchAdult(villageName,clusterName);
                }

//
//            }
//        }, !searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.HH.toString()));
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
    public void updateAdapter(ArrayList<HHMemberProperty> list) {

        searchHHMemberAdapter = new SearchHHMemberAdapter(this, new SearchHHMemberAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, HHMemberProperty content,boolean isNeedToAd) {
                presenter.updateList(content,isNeedToAd);
                searchHHMemberAdapter.notifyDataSetChanged();
            }

            @Override
            public void onClickHH(int position, HHMemberProperty content) {
                presenter.updateHH(content);
                sendData();
            }

            @Override
            public void onRemove(int position, HHMemberProperty content) {

            }
        },searchType);
        searchHHMemberAdapter.setData(list,previousList);
        recyclerView.setAdapter(searchHHMemberAdapter);

    }

    @Override
    public Context getContext() {
        return this;
    }
}
