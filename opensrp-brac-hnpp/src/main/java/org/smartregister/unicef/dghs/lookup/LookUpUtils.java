package org.smartregister.unicef.dghs.lookup;

import static android.view.View.VISIBLE;
import static org.smartregister.util.Utils.startAsyncTask;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.smartregister.Context;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.event.Listener;

import java.util.HashMap;
import java.util.List;

public class LookUpUtils {
    public static HashMap<String, String> lookUpTableHash = new HashMap<String, String>();
    static {
        lookUpTableHash.put("permanentAddress","ec_family");
    }
    public static void addressLookUp(final Context context, final EntityLookUp entityLookUp, final Listener<HashMap<CommonPersonObject, List<CommonPersonObject>>> listener, final ProgressBar progressBar, final String lookuptype){
        startAsyncTask(new AsyncTask<Void, Void, HashMap<CommonPersonObject, List<CommonPersonObject>>>() {
            @Override
            protected HashMap<CommonPersonObject, List<CommonPersonObject>> doInBackground(Void... params) {
                publishProgress();
                return lookUp(context, entityLookUp,lookuptype);
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                if (progressBar != null) {
                    progressBar.setVisibility(VISIBLE);
                }
            }

            @Override
            protected void onPostExecute(HashMap<CommonPersonObject, List<CommonPersonObject>> result) {
                listener.onEvent(result);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        }, null);
    }
    private static HashMap<CommonPersonObject, List<CommonPersonObject>> lookUp(Context context, EntityLookUp entityLookUp, String lookuptype){
        if (lookuptype.equals("permanent_address")) {
            return Jilla.getResultsAddress(entityLookUp.getMap().get("permanent_address"));
        }
        return new HashMap<>();
    }
}
