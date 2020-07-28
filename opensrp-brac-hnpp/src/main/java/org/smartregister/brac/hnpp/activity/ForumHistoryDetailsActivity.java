package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.SearchHHMemberAdapter;
import org.smartregister.brac.hnpp.model.ForumDetails;
import org.smartregister.brac.hnpp.model.HHMemberProperty;
import org.smartregister.brac.hnpp.utils.HnppConstants;

import java.util.ArrayList;

public class ForumHistoryDetailsActivity extends ForumDetailsActivity {

    private static final String EXTRA_FORUM_DEAILS = "extra_forum_details";
    public static void startForumHistoryDetailsActivity(Activity activity, ForumDetails forumDetails){

        Intent intent = new Intent(activity, ForumHistoryDetailsActivity.class);
        intent.putExtra(EXTRA_FORUM_DEAILS,forumDetails);
        activity.startActivity(intent);
    }

    @Override
    protected void onIntentDataSet() {
        super.onIntentDataSet();
        viewDisable();
        ForumDetails forumDetails =(ForumDetails) getIntent().getSerializableExtra(EXTRA_FORUM_DEAILS);
        if(forumDetails!=null){
            hhMemberProperty = forumDetails.place;
            textViewForumName.setText(forumDetails.forumName);
            String name = hhMemberProperty.getName();
            String id = hhMemberProperty.getId();
            textViewKhanaName.setText("খানা : "+name +" \n"+"আইডি: "+id);
            editTextNoOfParticipants.setText(forumDetails.noOfParticipant);
            if(forumDetails.forumType.equalsIgnoreCase(HnppConstants.SEARCH_TYPE.ADO.toString())){
                editTextAdoFood.setVisibility(View.VISIBLE);
                editTextAdoFood.setText(forumDetails.noOfAdoTakeFiveFood);
            }
            editTextNoOfService.setText(forumDetails.noOfServiceTaken);
            ss_spinner.setSelection(forumDetails.sIndex);
            village_spinner.setSelection(forumDetails.vIndex);
            cluster_spinner.setSelection(forumDetails.cIndex);
            ss_spinner.setEnabled(false);
            village_spinner.setEnabled(false);
            cluster_spinner.setEnabled(false);
            hhMemberPropertyArrayList = forumDetails.participants;
            SearchHHMemberAdapter adapter = new SearchHHMemberAdapter(this, new SearchHHMemberAdapter.OnClickAdapter() {
                @Override
                public void onClick(int position, HHMemberProperty content) {

                }

                @Override
                public void onClickHH(int position, HHMemberProperty content) {

                }

                @Override
                public void onRemove(int position, HHMemberProperty content) {

                }
            },"");
            adapter.setData(hhMemberPropertyArrayList,new ArrayList<>());
            recyclerView.setAdapter(adapter);


        }
    }
    private void viewDisable(){
        findViewById(R.id.search_khana).setEnabled(false);
        findViewById(R.id.add_member_btn).setEnabled(false);
        findViewById(R.id.submit_btn).setEnabled(false);
        editTextNoOfParticipants.setEnabled(false);
        editTextNoOfService.setEnabled(false);
        editTextAdoFood.setEnabled(false);
    }
}
