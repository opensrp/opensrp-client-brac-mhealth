package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.SearchHHMemberAdapter;
import org.smartregister.brac.hnpp.contract.ForumDetailsContract;
import org.smartregister.brac.hnpp.model.ForumDetails;
import org.smartregister.brac.hnpp.model.HHMemberProperty;
import org.smartregister.brac.hnpp.model.SearchHHMemberModel;
import org.smartregister.brac.hnpp.presenter.ForumDetailsPresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;

import static org.smartregister.brac.hnpp.activity.SearchHHMemberActivity.EXTRA_INTENT_DATA;

public class ForumDetailsActivity extends SecuredActivity implements View.OnClickListener, ForumDetailsContract.View {
    private static int RESULT_CODE_HH = 11122;
    private static int RESULT_CODE_MEMBER =22222;
    private static String EXTRA_COMES_FROM = "comes_from";
    private static String EXTRA_TITLE = "extra_title";

    private CustomFontTextView textViewForumName,textViewKhanaName;
    private EditText editTextNoOfParticipants;
    private String fromType;
    private RecyclerView recyclerView;
    private ForumDetailsPresenter presenter;
    private ProgressBar progressBar;

    public static void startDetailsActivity(Activity activity, String comesFrom, String title){
        Intent intent  = new Intent(activity,ForumDetailsActivity.class);
        intent.putExtra(EXTRA_COMES_FROM,comesFrom);
        intent.putExtra(EXTRA_TITLE,title);
        activity.startActivity(intent);
    }




    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_forum_details);
        textViewForumName = findViewById(R.id.forum_name);
        textViewKhanaName = findViewById(R.id.khana_name);
        editTextNoOfParticipants = findViewById(R.id.no_of_perticipant);
        fromType = getIntent().getStringExtra(EXTRA_COMES_FROM);
        textViewForumName.setText(getIntent().getStringExtra(EXTRA_TITLE));
        findViewById(R.id.search_khana).setOnClickListener(this);
        findViewById(R.id.add_member_btn).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.submit_btn).setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        presenter = new ForumDetailsPresenter(this);
    }

    @Override
    protected void onResumption() {

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.submit_btn:
                ForumDetails forumDetails = getForumDetails();
                if(forumDetails !=null){
                    presenter.processChildForumEvent(forumDetails);
                }

                break;

            case R.id.backBtn:
                finish();
                break;
            case R.id.search_khana:
                SearchHHMemberActivity.startSearchActivity(this, HnppConstants.SEARCH_TYPE.HH.toString(),new ArrayList<>(),RESULT_CODE_HH);
                break;
            case R.id.add_member_btn:
                SearchHHMemberActivity.startSearchActivity(this,fromType,hhMemberPropertyArrayList,RESULT_CODE_MEMBER);

                break;
        }

    }
    private ForumDetails getForumDetails(){
        if(TextUtils.isEmpty(editTextNoOfParticipants.getText().toString())){
            editTextNoOfParticipants.setError("অংশগ্রহণকারী কত জন ছিল?");
            return null;
        }
        if(hhMemberProperty == null){
            Toast.makeText(this,"স্থান সিলেক্ট করুন",Toast.LENGTH_SHORT).show();
            return null;
        }
        if(hhMemberPropertyArrayList.size()==0){
            Toast.makeText(this,"অংশগ্রহণকারী সিলেক্ট করুন",Toast.LENGTH_SHORT).show();
            return null;
        }
        int participant = Integer.parseInt(editTextNoOfParticipants.getText().toString());
        if(participant!=hhMemberPropertyArrayList.size()){
            Toast.makeText(this,"অংশগ্রহণকারীর সংখ্যার সাথে মিলেনাই",Toast.LENGTH_SHORT).show();
            return null;
        }
        ForumDetails forumDetails = new ForumDetails();
        forumDetails.forumName = textViewForumName.getText().toString();
        forumDetails.forumType = fromType;
        forumDetails.noOfParticipant = editTextNoOfParticipants.getText().toString();
        forumDetails.participants = hhMemberPropertyArrayList;
        forumDetails.place = hhMemberProperty;

        return forumDetails;
    }
    ArrayList<HHMemberProperty> hhMemberPropertyArrayList = new ArrayList<>();
    HHMemberProperty hhMemberProperty;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && requestCode == RESULT_CODE_HH){
            ArrayList<HHMemberProperty> hhMemberPropertyArrayList = (ArrayList<HHMemberProperty>) data.getSerializableExtra(EXTRA_INTENT_DATA);
            if(hhMemberPropertyArrayList.size()>0){
                hhMemberProperty = hhMemberPropertyArrayList.get(0);
                String name = hhMemberProperty.getName();
                String id = hhMemberProperty.getId();
                textViewKhanaName.setText("খানা : "+name +" \n"+"আইডি: "+id);

            }
        }else if(resultCode == RESULT_OK && requestCode == RESULT_CODE_MEMBER){
            hhMemberPropertyArrayList = (ArrayList<HHMemberProperty>) data.getSerializableExtra(EXTRA_INTENT_DATA);
            SearchHHMemberAdapter adapter = new SearchHHMemberAdapter(this, new SearchHHMemberAdapter.OnClickAdapter() {
                @Override
                public void onClick(int position, HHMemberProperty content) {

                }

                @Override
                public void onClickHH(int position, HHMemberProperty content) {

                }
            },"");
            adapter.setData(hhMemberPropertyArrayList,new ArrayList<>());
            recyclerView.setAdapter(adapter);

        }
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
    public void showSuccessMessage(String message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
        finish();

    }

    @Override
    public void showFailedMessage(String message) {

    }

    @Override
    public Context getContext() {
        return null;
    }
}
