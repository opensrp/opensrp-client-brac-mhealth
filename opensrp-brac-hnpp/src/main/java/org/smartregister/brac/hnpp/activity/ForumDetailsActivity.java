package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.SearchHHMemberAdapter;
import org.smartregister.brac.hnpp.model.HHMemberProperty;
import org.smartregister.brac.hnpp.model.SearchHHMemberModel;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;

import static org.smartregister.brac.hnpp.activity.SearchHHMemberActivity.EXTRA_INTENT_DATA;

public class ForumDetailsActivity extends SecuredActivity implements View.OnClickListener {
    private static int RESULT_CODE_HH = 11122;
    private static int RESULT_CODE_MEMBER =22222;
    private static String EXTRA_COMES_FROM = "comes_from";
    private static String EXTRA_TITLE = "extra_title";

    private CustomFontTextView textViewForumName,textViewKhanaName;
    private String fromType;
    private RecyclerView recyclerView;

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
        fromType = getIntent().getStringExtra(EXTRA_COMES_FROM);
        textViewForumName.setText(getIntent().getStringExtra(EXTRA_TITLE));
        findViewById(R.id.search_khana).setOnClickListener(this);
        findViewById(R.id.add_member_btn).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_view);
    }

    @Override
    protected void onResumption() {

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backBtn:
                finish();
                break;
            case R.id.search_khana:
                SearchHHMemberActivity.startSearchActivity(this, HnppConstants.SEARCH_TYPE.HH.toString(),new ArrayList<>(),RESULT_CODE_HH);
                break;
            case R.id.add_member_btn:
                SearchHHMemberActivity.startSearchActivity(this,fromType,new ArrayList<>(),RESULT_CODE_MEMBER);

                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && requestCode == RESULT_CODE_HH){
            ArrayList<HHMemberProperty> hhMemberPropertyArrayList = (ArrayList<HHMemberProperty>) data.getSerializableExtra(EXTRA_INTENT_DATA);
            if(hhMemberPropertyArrayList.size()>0){
                String name = hhMemberPropertyArrayList.get(0).getName();
                String id = hhMemberPropertyArrayList.get(0).getId();
                textViewKhanaName.setText("খানা : "+name +" \n"+"আইডি: "+id);

            }
        }else if(resultCode == RESULT_OK && requestCode == RESULT_CODE_MEMBER){
            ArrayList<HHMemberProperty> hhMemberPropertyArrayList = (ArrayList<HHMemberProperty>) data.getSerializableExtra(EXTRA_INTENT_DATA);
            SearchHHMemberAdapter adapter = new SearchHHMemberAdapter(this, new SearchHHMemberAdapter.OnClickAdapter() {
                @Override
                public void onClick(int position, HHMemberProperty content) {

                }

                @Override
                public void onClickHH(int position, HHMemberProperty content) {

                }
            },"");
            adapter.setData(hhMemberPropertyArrayList);
            recyclerView.setAdapter(adapter);

        }
    }
}
