package org.smartregister.unicef.dghs.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import org.smartregister.unicef.dghs.dao.VisitDao;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import provider.PncRegisterProvider;

public class UpdatePncLastServiceInfoTask extends AsyncTask<Void, Void, Void> {
    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private final PncRegisterProvider.RegisterViewHolder viewHolder;
    private final String baseEntityId;
    String lastVisit,totalServiceCount;

    public SimpleDateFormat DDMMYY = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    public UpdatePncLastServiceInfoTask(Context context, PncRegisterProvider.RegisterViewHolder viewHolder, String baseEntityId) {
        this.context = context;
        this.viewHolder = viewHolder;
        this.baseEntityId = baseEntityId;
    }

    @Override
    protected Void doInBackground(Void... params) {
        //commonPersonObject = commonRepository.findByBaseEntityId(baseEntityId);

        String[] returnValue = VisitDao.getVisitInfo(baseEntityId, HnppConstants.EVENT_TYPE.PNC_REGISTRATION,HnppConstants.EVENT_TYPE.PNC_REGISTRATION);
        if(returnValue.length>0){
            lastVisit = returnValue[1];
            totalServiceCount =  returnValue[0];
        }
        return null;

    }

    @Override
    protected void onPostExecute(Void param) {
        setVisitButtonOverdueStatus(context, viewHolder.dueButton, lastVisit+"",totalServiceCount);

    }


    private void setVisitButtonOverdueStatus(Context context, Button dueButton, String lastVisitDays, String totalServiceCount) {
        viewHolder.dueButton.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(lastVisitDays)|| lastVisitDays.equalsIgnoreCase("null")) {
            dueButton.setText(context.getString(org.smartregister.chw.core.R.string.due_visit, "--","--"));
        } else {
            Date d = new Date(Long.parseLong(lastVisitDays));
            lastVisitDays = DDMMYY.format(d);
            dueButton.setText(context.getString(org.smartregister.chw.core.R.string.due_visit, lastVisitDays,totalServiceCount));
        }

        dueButton.setBackgroundResource(org.smartregister.chw.core.R.drawable.blue_btn_selector);
    }


}