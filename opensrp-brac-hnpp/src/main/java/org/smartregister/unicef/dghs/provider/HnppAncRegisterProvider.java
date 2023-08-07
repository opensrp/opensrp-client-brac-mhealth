package org.smartregister.unicef.dghs.provider;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.unicef.dghs.activity.RiskyDataDisplayActivity;
import org.smartregister.unicef.dghs.task.UpdateAncLastServiceInfoTask;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppDBUtils;
import org.smartregister.chw.anc.fragment.BaseAncRegisterFragment;
import org.smartregister.chw.anc.provider.AncRegisterProvider;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.provider.ChwAncRegisterProvider;
import org.smartregister.unicef.dghs.R;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.unicef.dghs.utils.RiskyModel;
import org.smartregister.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Set;

public class HnppAncRegisterProvider extends ChwAncRegisterProvider {
    private final LayoutInflater inflater;
    private View.OnClickListener onClickListener;
    private Context context;
    private CommonRepository commonRepository;

    public HnppAncRegisterProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, commonRepository, visibleColumns, onClickListener, paginationClickListener);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.onClickListener = onClickListener;
        this.commonRepository = commonRepository;
        this.context = context;
    }
    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {

        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        populatePatientColumn(pc, client, viewHolder);
        populateLastColumn(pc,viewHolder);
    }

    @Override
    protected void populatePatientColumn(@NotNull CommonPersonObjectClient pc, SmartRegisterClient client, @NotNull final AncRegisterProvider.RegisterViewHolder viewHolder1) {
        HnppAncRegisterViewHolder viewHolder = (HnppAncRegisterViewHolder)viewHolder1;
        String fname = Utils.getName(
                Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true),
                Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true)
        );

        String patientName = Utils.getName(fname, Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true));

        // calculate LMP
        String dobString = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false);
        String lmpString = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_MENSTRUAL_PERIOD, false);
        if (StringUtils.isNotBlank(dobString) && StringUtils.isNotBlank(lmpString)) {
            int age = Years.yearsBetween(new DateTime(dobString), new DateTime()).getYears();

            String gaLocation = MessageFormat.format("{0}: {1} {2} {3}",
                    context.getString(R.string.gestation_age_initial),
                    NCUtils.gestationAgeString(lmpString, context, false),
                    context.getString(R.string.abbrv_weeks),
                    context.getString(R.string.interpunct));

            String patientNameAge = MessageFormat.format("{0},{1}: {2}", patientName,context.getString(R.string.boyos), age);
            viewHolder.patientName.setText(patientNameAge);
            viewHolder.patientAge.setText(gaLocation);

        }
        String serialNo = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.SERIAL_NO, true);
        if(serialNo.isEmpty() || serialNo.equalsIgnoreCase("H")){
            serialNo="";
        }
        if(!TextUtils.isEmpty(serialNo)){
            viewHolder.patientName.setText(viewHolder.patientName.getText()+", "+context.getString(R.string.serial_no,serialNo));

        }
        String ssName = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.BLOCK_NAME, true);
        String mobileNo = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.PHONE_NUMBER, true);
        if (!TextUtils.isEmpty(ssName))viewHolder.patientAge.append(context.getString(R.string.ss_name,ssName));
        viewHolder.villageTown.setText(Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.VILLAGE_NAME, true));

        // add patient listener
        viewHolder.patientColumn.setOnClickListener(onClickListener);
        viewHolder.patientColumn.setTag(client);
        viewHolder.patientColumn.setTag(R.id.VIEW_ID, BaseAncRegisterFragment.CLICK_VIEW_NORMAL);


        // add due listener
        viewHolder.dueButton.setOnClickListener(onClickListener);
        viewHolder.dueButton.setText(mobileNo);
        viewHolder.dueButton.setTag(mobileNo);
        viewHolder.dueButton.setTag(R.id.VIEW_ID, BaseAncRegisterFragment.CLICK_VIEW_DOSAGE_STATUS);

        viewHolder.registerColumns.setOnClickListener(v -> viewHolder.patientColumn.performClick());
        viewHolder.dueWrapper.setOnClickListener(v -> viewHolder.dueButton.performClick());
        String baseEntityId = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);
        if(HnppDBUtils.isAncRisk(baseEntityId)){
            viewHolder.riskView.setVisibility(View.VISIBLE);
        }else{
            viewHolder.riskView.setVisibility(View.GONE);
        }
        if(HnppConstants.isEddImportant(lmpString)){
            viewHolder.eddView.setVisibility(View.VISIBLE);
        }else{
            viewHolder.eddView.setVisibility(View.GONE);
        }

        viewHolder.riskView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RiskyDataDisplayActivity.class);
                intent.putExtra(RiskyDataDisplayActivity.BASE_ENTITY_ID,baseEntityId);
                context.startActivity(intent);
                //openRiskFactorDialog(baseEntityId);
            }
        });
    }

    private void openRiskFactorDialog(String baseEntityId){
        ArrayList<RiskyModel> riskyModels = HnppApplication.getRiskDetailsRepository().getRiskyKeyByEntityId(baseEntityId);
        StringBuilder builder = new StringBuilder();
        for (RiskyModel riskyModel:riskyModels) {
            String[] fs= riskyModel.riskyKey.split(",");
            if(fs.length>0){
                for (String key:fs) {
                    builder.append(HnppConstants.riskeyFactorMapping.get(key));
                    builder.append(":");
                    builder.append(riskyModel.riskyValue);
                    builder.append("\n");
                }
            }else{
                builder.append(HnppConstants.riskeyFactorMapping.get(riskyModel.riskyKey));
                builder.append(":");
                builder.append(riskyModel.riskyValue);
                builder.append("\n");
            }

        }

        Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        TextView message = dialog.findViewById(R.id.text_tv);
        titleTv.setText(R.string.risk_causes);
        message.setText(builder.toString());
        Button ok_btn = dialog.findViewById(R.id.ok_btn);
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private void populateLastColumn(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
        //org.smartregister.family.util.Utils.startAsyncTask(new UpdateAncLastServiceInfoTask(context,viewHolder, pc.entityId()), null);
    }
    @Override
    public AncRegisterProvider.RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.anc_register_list_row, parent, false);
        return new HnppAncRegisterViewHolder(view);
    }
    public class HnppAncRegisterViewHolder extends AncRegisterProvider.RegisterViewHolder{
        public TextView riskView,eddView;
        public HnppAncRegisterViewHolder(View itemView) {
            super(itemView);
            riskView = itemView.findViewById(R.id.risk_view);
            eddView = itemView.findViewById(R.id.edd_view);
        }
    }

}
