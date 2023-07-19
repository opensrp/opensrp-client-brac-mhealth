package org.smartregister.unicef.dghs.provider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.task.UpdateBornChildCountTask;
import org.smartregister.unicef.dghs.task.UpdatePncLastServiceInfoTask;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppDBUtils;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.customcontrols.CustomFontTextView;

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

    @SuppressLint("SetTextI18n")
    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder1) {
        super.getView(cursor, client, new HnppPncRegisterViewHolder(viewHolder1.itemView));
        HnppPncRegisterViewHolder viewHolder = (HnppPncRegisterViewHolder) viewHolder1;

        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        String baseEntityId = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);

        String mobileNo = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.PHONE_NUMBER, true);


        String serialNo = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.SERIAL_NO, true);
        if(serialNo.isEmpty() || serialNo.equalsIgnoreCase("H")){
            serialNo="";
        }
        if(!TextUtils.isEmpty(serialNo)){
            viewHolder.patientNameAndAge.setText(viewHolder.patientNameAndAge.getText()+", "+context.getString(R.string.serial_no,serialNo));

        }
        String deliveryDate = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.DELIVERY_DATE, true);
        if (!TextUtils.isEmpty(deliveryDate))
            viewHolder.pncDay.setText(context.getString(R.string.delivery_date,deliveryDate));
        viewHolder.dueButton.setText(mobileNo);
        viewHolder.dueButton.setTag(mobileNo);
        viewHolder.dueButton.setOnClickListener(onClickListener);
        viewHolder.addChildBtn.setTag(pc);
        viewHolder.addChildBtn.setOnClickListener(onClickListener);
//        org.smartregister.family.util.Utils.startAsyncTask(new UpdatePncLastServiceInfoTask(context,viewHolder, pc.entityId()), null);

        // org.smartregister.family.util.Utils.startAsyncTask(new UpdateBornChildCountTask(context,viewHolder, pc.entityId()), null);

        if(HnppDBUtils.isRisk(baseEntityId, HnppConstants.EVENT_TYPE.PNC_REGISTRATION)){
            viewHolder.riskView.setVisibility(View.VISIBLE);
        }else{
            viewHolder.riskView.setVisibility(View.GONE);
        }
    }
    public class HnppPncRegisterViewHolder extends PncRegisterProvider.RegisterViewHolder{
        public TextView riskView,eddView;
        public Button addChildBtn;
        public HnppPncRegisterViewHolder(View itemView) {
            super(itemView);
            riskView = itemView.findViewById(R.id.risk_view);
            eddView = itemView.findViewById(R.id.edd_view);
            addChildBtn = itemView.findViewById(R.id.add_child_button);
        }
    }


}
