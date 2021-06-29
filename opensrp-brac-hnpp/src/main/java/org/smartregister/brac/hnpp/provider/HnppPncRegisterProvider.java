package org.smartregister.brac.hnpp.provider;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.task.UpdatePncLastServiceInfoTask;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.Set;

import provider.PncRegisterProvider;

public class HnppPncRegisterProvider extends PncRegisterProvider {

    private Context context;
    private View.OnClickListener onClickListener;

    public HnppPncRegisterProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, commonRepository, visibleColumns, onClickListener, paginationClickListener);
        this.context = context;
        this.onClickListener = onClickListener;
    }

    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.pnc_register_list_row, parent, false);
        return new HnppPncRegisterViewHolder(view);
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder1) {
        super.getView(cursor, client, new HnppPncRegisterViewHolder(viewHolder1.itemView));
        HnppPncRegisterViewHolder viewHolder = (HnppPncRegisterViewHolder) viewHolder1;

        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        String baseEntityId = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);



        String serialNo = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.SERIAL_NO, true);
        if(serialNo.isEmpty() || serialNo.equalsIgnoreCase("H")){
            serialNo="";
        }
        if(!TextUtils.isEmpty(serialNo)){
            viewHolder.patientNameAndAge.setText(viewHolder.patientNameAndAge.getText()+", "+context.getString(R.string.serial_no,serialNo));

        }
        String ssName = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.SS_NAME, true);
        if (!TextUtils.isEmpty(ssName))viewHolder.pncDay.append(context.getString(R.string.ss_name,ssName));
        viewHolder.dueButton.setVisibility(View.GONE);
        viewHolder.dueButton.setOnClickListener(null);
        org.smartregister.family.util.Utils.startAsyncTask(new UpdatePncLastServiceInfoTask(context,viewHolder, pc.entityId()), null);

        if(HnppDBUtils.isRisk(baseEntityId, HnppConstants.EVENT_TYPE.PNC_REGISTRATION) ||
                HnppDBUtils.isRisk(baseEntityId, HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour)||
                HnppDBUtils.isRisk(baseEntityId, HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour)){
            viewHolder.riskView.setVisibility(View.VISIBLE);
        }else{
            viewHolder.riskView.setVisibility(View.GONE);
        }
    }
    public class HnppPncRegisterViewHolder extends PncRegisterProvider.RegisterViewHolder{
        public TextView riskView,eddView;
        public HnppPncRegisterViewHolder(View itemView) {
            super(itemView);
            riskView = itemView.findViewById(R.id.risk_view);
            eddView = itemView.findViewById(R.id.edd_view);
        }
    }


}
