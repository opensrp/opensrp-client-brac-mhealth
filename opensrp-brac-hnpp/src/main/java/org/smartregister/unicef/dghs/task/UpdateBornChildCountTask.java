package org.smartregister.unicef.dghs.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.dao.VisitDao;
import java.text.SimpleDateFormat;
import java.util.Locale;

import provider.PncRegisterProvider;

public class UpdateBornChildCountTask extends AsyncTask<Void, Void, Void> {
    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private final PncRegisterProvider.RegisterViewHolder viewHolder;
    private final String baseEntityId;
    String totalServiceCount;

    public SimpleDateFormat DDMMYY = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    public UpdateBornChildCountTask(Context context, PncRegisterProvider.RegisterViewHolder viewHolder, String baseEntityId) {
        this.context = context;
        this.viewHolder = viewHolder;
        this.baseEntityId = baseEntityId;
    }

    @Override
    protected Void doInBackground(Void... params) {
        //commonPersonObject = commonRepository.findByBaseEntityId(baseEntityId);

        totalServiceCount = VisitDao.getNoOfBornChild(baseEntityId);

        return null;

    }

    @Override
    protected void onPostExecute(Void param) {
        setVisitButtonOverdueStatus(context, viewHolder.dueButton, totalServiceCount);

    }


    private void setVisitButtonOverdueStatus(Context context, Button dueButton, String totalServiceCount) {
        if(TextUtils.isEmpty(totalServiceCount)){
            viewHolder.dueButton.setVisibility(View.GONE);
        }else{
            viewHolder.dueButton.setVisibility(View.VISIBLE);
            dueButton.setText(context.getString(R.string.total_unregister_child,totalServiceCount));
            dueButton.setBackgroundResource(org.smartregister.chw.core.R.drawable.blue_btn_selector);
        }

    }


}
