package org.smartregister.unicef.mis.activity;

import static org.smartregister.unicef.mis.utils.HnppConstants.showDialogWithAction;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.adapter.UpdateLocationAdapter;
import org.smartregister.unicef.mis.contract.UpdateLocationContract;
import org.smartregister.unicef.mis.location.UpdateLocationModel;
import org.smartregister.unicef.mis.presenter.UpdateLocationPresenter;
import org.smartregister.view.activity.SecuredActivity;

public class UpdateLocationActivity extends SecuredActivity implements  UpdateLocationContract.View{
    private static final String PAURASAVA = "paurasava";
    private static final String UNION = "union";
    private static final String OLDWARD = "oldward";
    private static final String WARD = "ward";
    private ProgressBar progressBar;
    private UpdateLocationPresenter presenter;
    private RecyclerView paurashavaRV,unionRV,oldWardRV,waldRV;
    UpdateLocationAdapter paurashavaAdapter,unionAdapter,oldWardAdapter,wardAdapter;
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_location_update);
        progressBar = findViewById(R.id.progressBar);
        paurashavaRV = findViewById(R.id.pourashova_rv);
        unionRV = findViewById(R.id.union_rv);
        oldWardRV = findViewById(R.id.old_ward_rv);
        waldRV = findViewById(R.id.ward_rv);
        findViewById(R.id.update_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder builder = new StringBuilder();
                builder.append("পৌরসভা");
                builder.append("\n --------------\n");
                for(String por:paurashavaAdapter.getLocationNameList()){
                    builder.append(por);
                    builder.append("\n");
                }
                builder.append("ইউনিয়ন/ জোন");
                builder.append("\n --------------\n");
                for(String por:unionAdapter.getLocationNameList()){
                    builder.append(por);
                    builder.append("\n");
                }
                builder.append("পুরাতন ওয়ার্ড");
                builder.append("\n --------------\n");
                for(String por:oldWardAdapter.getLocationNameList()){
                    builder.append(por);
                    builder.append("\n");
                }
                builder.append("নতুন ওয়ার্ড");
                builder.append("\n --------------\n");
                for(String por:wardAdapter.getLocationNameList()){
                    builder.append(por);
                    builder.append("\n");
                }
                showDialogWithAction(UpdateLocationActivity.this, "আপনার সিলেক্টেড লোকেশন কনফার্ম করুন ", builder.toString()
                        ,new Runnable() {
                    @Override
                    public void run() {
                        presenter.setSelectedMapList(PAURASAVA,paurashavaAdapter.getSelectedList());
                        presenter.setSelectedMapList(UNION,unionAdapter.getSelectedList());
                        presenter.setSelectedMapList(OLDWARD,oldWardAdapter.getSelectedList());
                        presenter.setSelectedMapList(WARD,wardAdapter.getSelectedList());
                        presenter.updateLocation();
                    }
                }, new Runnable() {
                            @Override
                            public void run() {

                            }
                        });


            }
        });
        findViewById(R.id.backBtn).setVisibility(View.GONE);
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        presenter = new UpdateLocationPresenter(this);
        assignAdapter();
        presenter.processPaurasava();

    }

    @Override
    public void onBackPressed() {
        new android.app.AlertDialog.Builder(this)
                .setMessage("আপনি লোকেশন আপডেট না করে বের হতে চাচ্ছেন?")
                .setCancelable(false)
                .setPositiveButton(R.string.yes_button_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        HnppApplication.getInstance().forceLogout();
                        finish();
                    }
                }).setNegativeButton(R.string.no_button_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
        //super.onBackPressed();
    }

    private void assignAdapter(){
        paurashavaAdapter = new UpdateLocationAdapter(new UpdateLocationAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, UpdateLocationModel content) {
                presenter.processUnion(content.id+"");
            }

            @Override
            public void unChecked(int position, UpdateLocationModel content) {
                presenter.removeUnion(content.id);
            }
        });
        unionAdapter = new UpdateLocationAdapter(new UpdateLocationAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, UpdateLocationModel content) {
                presenter.processOldWard(content.id+"");
            }
            @Override
            public void unChecked(int position, UpdateLocationModel content) {
                presenter.removeOldWard(content.id);
            }
        });
        oldWardAdapter = new UpdateLocationAdapter(new UpdateLocationAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, UpdateLocationModel content) {
                presenter.processWard(content.id+"");
            }
            @Override
            public void unChecked(int position, UpdateLocationModel content) {
                presenter.removeWard(content.id);
            }
        });
        wardAdapter = new UpdateLocationAdapter(new UpdateLocationAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, UpdateLocationModel content) {
                //presenter.processOldWard(content.id+"");
            }
            @Override
            public void unChecked(int position, UpdateLocationModel content) {
               // presenter.removeWard(content.id);
            }
        });
    }

    @Override
    protected void onResumption() {

    }
        private void showAlertDialog(boolean status) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(this, R.style.AlertDialog_AppCompat);
        downloadDialog.setTitle(status==true?"Successfully updated":"Fail to update");
        downloadDialog.setMessage(status==true?"Please login again":"Try again");
        if(status){
            downloadDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    HnppApplication.getInstance().forceLogout();
                    finish();


                }
            });
        }else{
            downloadDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });
        }


        downloadDialog.show();
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
    public void updatePaurashovaAdapter() {
        paurashavaAdapter.setData(presenter.getPaurashavaList());
        paurashavaRV.setAdapter(paurashavaAdapter);

    }

    @Override
    public void updateUnionAdapter() {

        unionAdapter.setData(presenter.getUnion());
        unionRV.setAdapter(unionAdapter);
    }

    @Override
    public void updateOldWardAdapter() {
        oldWardAdapter.setData(presenter.getOldWard());
        oldWardRV.setAdapter(oldWardAdapter);
    }

    @Override
    public void updateWardAdapter() {
        wardAdapter.setData(presenter.getWard());
        waldRV.setAdapter(wardAdapter);
    }

    @Override
    public void onBlockUpdated(boolean status) {
      showAlertDialog(status);

    }

    @Override
    public Context getContext() {
        return null;
    }

}
