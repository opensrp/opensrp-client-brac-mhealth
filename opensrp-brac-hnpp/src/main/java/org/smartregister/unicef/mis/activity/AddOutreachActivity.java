package org.smartregister.unicef.mis.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.vision.L;

import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.listener.OnPostDataWithGps;
import org.smartregister.unicef.mis.location.BlockLocation;
import org.smartregister.unicef.mis.location.HALocationHelper;
import org.smartregister.unicef.mis.location.WardLocation;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.OutreachContentData;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;

public class AddOutreachActivity extends SecuredActivity implements View.OnClickListener {
    private static final String PUT_EXTRA_DATA ="outreach_put_extra";

    protected Spinner unionSpinner, oldWardSpinner, newWardSpinner, blockSpinner, centerTypeSpinner;
    protected EditText outreachNameTxt, addressTxt, mobileTxt, latitudeTxt, longitudeTxt;
    ArrayList<WardLocation> unionList, oldWardList, newWardList;
    ArrayList<BlockLocation> blocks;
    OutreachContentData outreachContentData;
    public static  void startAddOutreachActivity(Activity activity, OutreachContentData outreachContentData){
        Intent intent = new Intent(activity,AddOutreachActivity.class);
        intent.putExtra(PUT_EXTRA_DATA,outreachContentData);
        activity.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_add_outreach);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar_layout));
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.manual_btn).setOnClickListener(this);
        findViewById(R.id.auto_btn).setOnClickListener(this);
        findViewById(R.id.submit_btn).setOnClickListener(this);
        initUi();
        outreachContentData = (OutreachContentData) getIntent().getSerializableExtra(PUT_EXTRA_DATA);

    }

    @SuppressLint("SetTextI18n")
    private void populatedUI() {
        unionSpinner.setSelection(((ArrayAdapter<String>)unionSpinner.getAdapter()).getPosition(outreachContentData.unionName));
        unionSpinner.setEnabled(false);
        oldWardSpinner.setSelection(((ArrayAdapter<String>)oldWardSpinner.getAdapter()).getPosition(outreachContentData.oldWardName));
        oldWardSpinner.setEnabled(false);
        newWardSpinner.setSelection(((ArrayAdapter<String>)newWardSpinner.getAdapter()).getPosition(outreachContentData.newWardName));
        newWardSpinner.setEnabled(false);
        blockSpinner.setSelection(((ArrayAdapter<String>)blockSpinner.getAdapter()).getPosition(outreachContentData.blockName));
        blockSpinner.setEnabled(false);
        centerTypeSpinner.setSelection(((ArrayAdapter<String>)centerTypeSpinner.getAdapter()).getPosition(outreachContentData.centerType));
        outreachNameTxt.setText(outreachContentData.outreachName);
        addressTxt.setText(outreachContentData.address);
        mobileTxt.setText(outreachContentData.mobile);
        latitudeTxt.setText(outreachContentData.latitude+"");
        longitudeTxt.setText(outreachContentData.longitude+"");
    }

    private void initUi() {
        unionSpinner = findViewById(R.id.union_spinner);
        oldWardSpinner = findViewById(R.id.old_ward_spinner);
        newWardSpinner = findViewById(R.id.new_ward_spinner);
        blockSpinner = findViewById(R.id.block_spinner);
        centerTypeSpinner = findViewById(R.id.center_type_spinner);
        outreachNameTxt = findViewById(R.id.outreach_name);
        addressTxt = findViewById(R.id.address);
        mobileTxt = findViewById(R.id.mobile);
        latitudeTxt = findViewById(R.id.latitude_edit);
        longitudeTxt = findViewById(R.id.longitude_edit);
        unionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                loadOldWard(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        oldWardSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                loadNewWard(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        newWardSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                loadBlock(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        blockSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedBlockId = blocks.get(position).block.id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    int selectedUnionId,selectedBlockId;
    private void loadOldWard(int position) {
        selectedUnionId = unionList.get(position).ward.id;
        oldWardList = HnppApplication.getHALocationRepository().getOldWardByUnionId(selectedUnionId);
        ArrayList<String> oldWardSpinnerArray = new ArrayList<>();
        for (WardLocation wardLocation : oldWardList) {
            oldWardSpinnerArray.add(wardLocation.ward.name);
        }
        ArrayAdapter<String> oldWardSpinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                oldWardSpinnerArray);
        oldWardSpinner.setAdapter(oldWardSpinnerArrayAdapter);
    }
    int selectedOldWardId;
    private void loadNewWard(int position) {
        selectedOldWardId = oldWardList.get(position).ward.id;
        newWardList = HnppApplication.getHALocationRepository().getAllWardByOldWardId(selectedOldWardId);
        ArrayList<String> newWardSpinnerArray = new ArrayList<>();
        for (WardLocation wardLocation : newWardList) {
            newWardSpinnerArray.add(wardLocation.ward.name);
        }
        ArrayAdapter<String> newWardSpinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                newWardSpinnerArray);
        newWardSpinner.setAdapter(newWardSpinnerArrayAdapter);
    }
    int selectedNewWardId;
    private void loadBlock(int position) {
        selectedNewWardId = newWardList.get(position).ward.id;
        blocks = HnppApplication.getHALocationRepository().getOnlyBlockLocationByWardId(selectedNewWardId + "");

        ArrayList<String> blockSpinnerArray = new ArrayList<>();
        for (BlockLocation wardLocation : blocks) {
            blockSpinnerArray.add(wardLocation.block.name);
        }
        ArrayAdapter<String> blockSpinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                blockSpinnerArray);
        blockSpinner.setAdapter(blockSpinnerArrayAdapter);
    }

    private void getGPSLocation() {
                HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onPost(double latitude, double longitude) {
                latitudeTxt.setText(latitude+"");
                latitudeTxt.setEnabled(false);
                longitudeTxt.setText(longitude+"");
                longitudeTxt.setEnabled(false);

                }
            });
    }

    @Override
    protected void onResumption() {
        loadLocation();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(outreachContentData!=null){
                    populatedUI();
                }
            }
        },1000);

    }
    private void loadLocation(){
        ArrayList<String> unionSpinnerArray = new ArrayList<>();
        unionList = HALocationHelper.getInstance().getUnionList();

        for (WardLocation ssModel : unionList) {
            unionSpinnerArray.add(ssModel.ward.name);
        }
        ArrayAdapter<String> unionSpinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        unionSpinnerArray);
        unionSpinner.setAdapter(unionSpinnerArrayAdapter);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.manual_btn:
                latitudeTxt.setEnabled(true);
                longitudeTxt.setEnabled(true);
                break;
            case R.id.auto_btn:
                getGPSLocation();
                break;
            case R.id.backBtn:
                finish();
                break;
            case R.id.submit_btn:
                if(TextUtils.isEmpty(outreachNameTxt.getText())){
                    outreachNameTxt.setError("Outreach name empty");
                    return;
                }
                if(outreachContentData ==null){
                    outreachContentData = new OutreachContentData();
                }
                outreachContentData.unionName = unionSpinner.getSelectedItem().toString();
                outreachContentData.unionId = selectedUnionId;
                outreachContentData.oldWardName = oldWardSpinner.getSelectedItem().toString();
                outreachContentData.oldWardId = selectedOldWardId;
                outreachContentData.newWardName = newWardSpinner.getSelectedItem().toString();
                outreachContentData.newWardId = selectedNewWardId;
                outreachContentData.blockId = selectedBlockId;
                outreachContentData.blockName = blockSpinner.getSelectedItem().toString();
                outreachContentData.outreachName = outreachNameTxt.getText().toString();
                outreachContentData.outreachId = outreachContentData.unionId+"-"+outreachContentData.oldWardId+"-"+outreachContentData.newWardId+"-"+outreachContentData.blockId;
                outreachContentData.mobile = mobileTxt.getText().toString();
                outreachContentData.address = addressTxt.getText().toString();
                outreachContentData.centerType = centerTypeSpinner.getSelectedItem().toString();
                outreachContentData.latitude = getDoubleValue(latitudeTxt.getText().toString());
                outreachContentData.longitude = getDoubleValue(longitudeTxt.getText().toString());
                boolean isInserted = HnppApplication.getOutreachRepository().addAndUpdateOutreach(outreachContentData);
                if(isInserted){
                    finish();
                }else {
                    Toast.makeText(this,"Failed to add or update", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
    private double getDoubleValue(String value){
        try{
            return Double.parseDouble(value);
        }catch (NumberFormatException ne){
            return 0.0;
        }
    }
}