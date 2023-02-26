package org.smartregister.unicef.dghs.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.rey.material.widget.CheckBox;
import com.rey.material.widget.TextView;
import org.smartregister.CoreLibrary;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.contract.BlockUpdateContract;
import org.smartregister.unicef.dghs.presenter.BlockUpdatePresenter;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.Node;
import org.smartregister.view.activity.SecuredActivity;
import java.util.ArrayList;

public class BlockUpdateActivity extends SecuredActivity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener, BlockUpdateContract.View {

    ProgressBar progressBar;
    private BlockUpdatePresenter presenter;
    private LinearLayout blocklistView;
    @Override
    protected void onCreation() {
        setContentView(R.layout.blockselectiondialog);
        blocklistView = findViewById(R.id.blockslist);
        progressBar = findViewById(R.id.progress_bar);
        findViewById(R.id.submitblocks).setOnClickListener(this);
        presenter = new BlockUpdatePresenter(this);
        presenter.getBlock();
    }

    @Override
    protected void onResumption() {

    }
    public void submit(String selectedLocation) {
        CoreLibrary.getInstance().context().allSharedPreferences().savePreference(HnppConstants.LOCATION_UPDATED,selectedLocation+"");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        presenter.getNodeView().get(view).vaild = isChecked;
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
        for(int i=0;i<presenter.getNodeList().size();i++){
            TextView wardView = new TextView(this);
            wardView.setTypeface(Typeface.DEFAULT_BOLD);
            wardView.setTextColor(getResources().getColor(android.R.color.black));
            wardView.setText(presenter.getNodeList().get(i).name);
            blocklistView.addView(wardView);
            presenter.getNodeView().put(wardView,presenter.getNodeList().get(i));
            ArrayList<Node>blocks = presenter.getNodeList().get(i).nodes;
            for(int k=0;k<blocks.size();k++){
                CheckBox block = new CheckBox(this);
                block.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                block.setText(blocks.get(k).name);
                block.setChecked(blocks.get(k).vaild);
                block.setOnCheckedChangeListener(this);
                blocklistView.addView(block);
                presenter.getNodeView().put(block,blocks.get(k));
            }
        }
    }

    @Override
    public void onBlockUpdated() {
        showAlertDialog();
    }

    @Override
    public Context getContext() {
        return this;
    }

    public void submitSelection(){
        String BLOCKS = "";
        //make wards valid if block in a ward is valid
        for(int i=0;i<presenter.getNodeList().size();i++) {
            Node ward = presenter.getNodeList().get(i);
            ward.vaild = false;
        }
        for(int i=0;i<presenter.getNodeList().size();i++){
            Node ward = presenter.getNodeList().get(i);
            for(int k=0;k<ward.nodes.size();k++){
                if(ward.nodes.get(k).vaild){
                    BLOCKS = BLOCKS + ward.nodes.get(k).name + ",";
//                    ward.vaild = ward.nodes.get(k).vaild;
                }
            }
        }
        BLOCKS = BLOCKS.substring(0,BLOCKS.length()-1);

        final String fblocks = BLOCKS.replaceAll(" ","%20");
        presenter.updateBlock(fblocks);
    }

    private void showAlertDialog() {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(BlockUpdateActivity.this, R.style.AlertDialog_AppCompat);
        downloadDialog.setTitle(" ব্লক আপডেট করা হয়েছে !!!");
        downloadDialog.setMessage("অনুগ্রহ করে আবার লগইন করুন ");
        downloadDialog.setPositiveButton("ঠিক আছে", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                HnppApplication.getInstance().forceLogout();
                finish();
            }
        });
        downloadDialog.show();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.submitblocks:
                submitSelection();

                break;
        }
    }

}
