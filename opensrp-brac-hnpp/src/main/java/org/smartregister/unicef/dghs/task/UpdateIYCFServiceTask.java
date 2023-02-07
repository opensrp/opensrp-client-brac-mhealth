package org.smartregister.unicef.dghs.task;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.dao.VisitDao;
import org.smartregister.chw.core.holders.RegisterViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UpdateIYCFServiceTask extends AsyncTask<Void, Void, Void> {
    private final Context context;
    private final RegisterViewHolder viewHolder;
    private final String baseEntityId;
    String lastVisit,totalServiceCount;

    public static SimpleDateFormat DDMMYY = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    public UpdateIYCFServiceTask(Context context, RegisterViewHolder viewHolder, String baseEntityId) {
        this.context = context;
        this.viewHolder = viewHolder;
        this.baseEntityId = baseEntityId;
    }

    @Override
    protected Void doInBackground(Void... params) {

            String[] returnValue = VisitDao.getVisitInfo(baseEntityId, HnppConstants.EVENT_TYPE.IYCF_PACKAGE);
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
        viewHolder.dueButtonLayout.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(lastVisitDays)|| lastVisitDays.equalsIgnoreCase("null")) {
            dueButton.setText(context.getString(R.string.due_visit, "--","--"));
        } else {
            Date d = new Date(Long.parseLong(lastVisitDays));
            lastVisitDays = DDMMYY.format(d);
            dueButton.setText(context.getString(R.string.due_visit, lastVisitDays,totalServiceCount));
        }

        dueButton.setBackgroundResource(R.drawable.blue_btn_selector);
    }


}

