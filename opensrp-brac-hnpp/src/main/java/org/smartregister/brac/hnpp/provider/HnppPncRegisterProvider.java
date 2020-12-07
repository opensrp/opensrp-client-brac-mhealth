package org.smartregister.brac.hnpp.provider;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.jeasy.rules.api.Rules;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.rule.PncVisitAlertRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.HomeVisitUtil;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import provider.PncRegisterProvider;
import timber.log.Timber;

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
        Utils.startAsyncTask(new UpdateAsyncTask(context, viewHolder, pc), null);
        if(HnppDBUtils.isRisk(baseEntityId, HnppConstants.EVENT_TYPE.PNC_REGISTRATION)){
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

    private void updateDueColumn(Context context, RegisterViewHolder viewHolder, PncVisitAlertRule pncVisitAlertRule) {
        viewHolder.dueButton.setVisibility(View.VISIBLE);
        if (pncVisitAlertRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.DUE)) {
            setVisitButtonDueStatus(context, pncVisitAlertRule.getVisitID(), viewHolder.dueButton);
        } else if (pncVisitAlertRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.OVERDUE)) {
            setVisitButtonOverdueStatus(context, pncVisitAlertRule.getVisitID(), viewHolder.dueButton);
        } else if (pncVisitAlertRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.VISIT_DONE)) {
            setVisitDone(context, viewHolder.dueButton);
        }
    }

    private void setVisitButtonDueStatus(Context context, String visitDue, Button dueButton) {
        dueButton.setTextColor(context.getResources().getColor(R.color.alert_in_progress_blue));
        dueButton.setText(context.getString(R.string.pnc_visit_day_due, visitDue));
        dueButton.setBackgroundResource(R.drawable.blue_btn_selector);
        dueButton.setOnClickListener(onClickListener);
    }

    private void setVisitButtonOverdueStatus(Context context, String visitDue, Button dueButton) {
        dueButton.setTextColor(context.getResources().getColor(R.color.white));
        dueButton.setText(context.getString(R.string.pnc_visit_day_overdue, visitDue));
        dueButton.setBackgroundResource(R.drawable.overdue_red_btn_selector);
        dueButton.setOnClickListener(onClickListener);
    }

    private void setVisitDone(Context context, Button dueButton) {
        dueButton.setTextColor(context.getResources().getColor(R.color.alert_complete_green));
        dueButton.setText(context.getString(R.string.visit_done));
        dueButton.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        dueButton.setOnClickListener(null);
    }

    private class UpdateAsyncTask extends AsyncTask<Void, Void, Void> {
        private final RegisterViewHolder viewHolder;
        private final CommonPersonObjectClient pc;
        private final Context context;

        private final Rules rules;
        private PncVisitAlertRule pncVisitAlertRule;

        private UpdateAsyncTask(Context context, RegisterViewHolder viewHolder, CommonPersonObjectClient pc) {
            this.context = context;
            this.viewHolder = viewHolder;
            this.pc = pc;
            this.rules = HnppApplication.getInstance().getRulesEngineHelper().rules(HnppConstants.RULE_FILE.PNC_HOME_VISIT);
        }

        @Override
        protected Void doInBackground(Void... params) {
            //map = getChildDetails(pc.getCaseId());
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            String baseEntityID = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);
            String dayPnc = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DELIVERY_DATE, true);
            Date deliveryDate = null;
            Date lastVisitDate = null;
            try {
                deliveryDate = sdf.parse(dayPnc);
            } catch (ParseException e) {
                Timber.e(e);
            }

            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(baseEntityID, org.smartregister.chw.anc.util.Constants.EVENT_TYPE.PNC_HOME_VISIT);
            if (lastVisit != null) {
                lastVisitDate = lastVisit.getDate();
            }

            pncVisitAlertRule = HomeVisitUtil.getPncVisitStatus(rules, lastVisitDate, deliveryDate);
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            // Update status column
            if (pncVisitAlertRule == null || StringUtils.isBlank(pncVisitAlertRule.getVisitID())) {
                return;
            }

            if (pncVisitAlertRule != null
                    && StringUtils.isNotBlank(pncVisitAlertRule.getVisitID())
                    && !pncVisitAlertRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.EXPIRED)
            ) {
                updateDueColumn(context, viewHolder, pncVisitAlertRule);
            }
        }
    }
}