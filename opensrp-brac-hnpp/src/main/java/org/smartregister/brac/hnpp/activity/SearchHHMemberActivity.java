package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.SearchHHMemberAdapter;
import org.smartregister.brac.hnpp.contract.SearchHHMemberContract;
import org.smartregister.brac.hnpp.model.HHMemberProperty;
import org.smartregister.brac.hnpp.presenter.SearchHHMemberPresenter;
import org.smartregister.brac.hnpp.utils.FilterDialog;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;

public class SearchHHMemberActivity extends SecuredActivity implements View.OnClickListener, SearchHHMemberContract.View {

    private static String EXTRA_TYPE_FOR = "extra_type";
    public static String EXTRA_INTENT_DATA = "extra_data";

    private EditText editTextSearch;
    private TextView villageNameTxt,clusterNameTxt;
    private ProgressBar progressBar;

    private String searchType;
    SearchHHMemberPresenter presenter;
    private RecyclerView recyclerView;
    SearchHHMemberAdapter searchHHMemberAdapter;
    public static void startSearchActivity(Activity activity, String searchType, ArrayList<HHMemberProperty> memberPropertyArrayList, int resultCode){
        Intent intent = new Intent(activity,SearchHHMemberActivity.class);
        intent.putExtra(EXTRA_TYPE_FOR,searchType);
        intent.putExtra(EXTRA_INTENT_DATA,memberPropertyArrayList);
        activity.startActivityForResult(intent,resultCode);
    }


    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_search_hh);
        editTextSearch = findViewById(R.id.search_edit_text);
        villageNameTxt = findViewById(R.id.village_name_filter);
        clusterNameTxt = findViewById(R.id.cluster_filter);
        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recycler_view);
        CustomFontTextView title = findViewById(R.id.textview_detail_two);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.filter_btn).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.add_btn).setOnClickListener(this);
        searchType = getIntent().getStringExtra(EXTRA_TYPE_FOR);
        if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.HH.toString())){
            title.setText("খানা খুজুন");
        }else{
            title.setText("সদস্য খুজুন");
        }
        presenter = new SearchHHMemberPresenter(this);
        showFilterDialog();

    }

    @Override
    protected void onResumption() {

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
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
    private void showFilterDialog(){
        new FilterDialog().showDialog(this, new FilterDialog.OnFilterDialogFilter() {
            @Override
            public void onDialogPress(String ssName, String villageName, String cluster, String gender) {
                villageNameTxt.setText(villageName);
                clusterNameTxt.setText(cluster);
                if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.HH.toString())){
                    presenter.fetchHH(villageName,cluster);
                }else if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.ADO.toString())){
                    presenter.fetchAdo(villageName,cluster);
                }else if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.WOMEN.toString())){
                    presenter.fetchWomen(villageName,cluster);
                }else if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.CHILD.toString())){
                    presenter.fetchChild(villageName,cluster);
                }else if(searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.NCD.toString())){
                    presenter.fetchNcd(villageName,cluster);
                }


            }
        }, !searchType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.HH.toString()));
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
            public void onClick(int position, HHMemberProperty content) {
                presenter.updateList(content);
                searchHHMemberAdapter.notifyDataSetChanged();
            }

            @Override
            public void onClickHH(int position, HHMemberProperty content) {
                presenter.updateHH(content);
                sendData();
            }
        },searchType);
        searchHHMemberAdapter.setData(list);
        recyclerView.setAdapter(searchHHMemberAdapter);

    }

    @Override
    public Context getContext() {
        return this;
    }
}
