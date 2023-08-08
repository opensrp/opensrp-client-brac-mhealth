package org.smartregister.unicef.dghs.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.repository.DistrictListRepository;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.Collection;

import static com.vijay.jsonwizard.utils.FormUtils.getFieldJSONObject;

public class IndividualProfileRemoveJsonFormFragment extends JsonWizardFormFragment {
    private int districtIndex = -1;
    private boolean isManuallyPressed = false;

    public static IndividualProfileRemoveJsonFormFragment getFormFragment(String stepName) {
        IndividualProfileRemoveJsonFormFragment jsonFormFragment = new IndividualProfileRemoveJsonFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString("stepName", stepName);
        jsonFormFragment.setArguments(bundle);
        return jsonFormFragment;
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        super.onItemSelected(parent, view, position, id);
        if (position != -1 && parent instanceof MaterialSpinner) {
            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(getString(R.string.district_only))) {
                districtIndex = position;
                if(isManuallyPressed){
                    processUpazilaList(position);
                }
            }
            //  hideKeyBoard();
        }


    }
    ArrayList<String>upazillalist = new ArrayList<>();

    ArrayList<String>districtList = new ArrayList<>();
    DistrictListRepository districtListRepository;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        districtListRepository = new DistrictListRepository(HnppApplication.getInstance().getRepository());

        Collection<View> formDataViews = getJsonApi().getFormDataViews();
        for (View v : formDataViews) {
            if (v instanceof MaterialSpinner) {
                if (((MaterialSpinner) v).getFloatingLabelText().toString().equalsIgnoreCase(getString(R.string.district_only))) {
                    districtList = districtListRepository.getDistrictNames();
                    ((MaterialSpinner) v).setEnabled(true);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), com.vijay.jsonwizard.R.layout.native_form_simple_list_item_1, districtList);
                    ((MaterialSpinner) v).setAdapter(adapter);
                    ((MaterialSpinner) v).setOnItemSelectedListener(this);
                    break;
                }
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isManuallyPressed = true;
                processUpazilaList(-1);

            }
        }, 1000);
    }

    public void processUpazilaList(final int index) {


        Utils.startAsyncTask(new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                upazillalist.clear();
                if(districtListRepository!=null&&index!=-1){
                    upazillalist = districtListRepository.getUpazilaFromDistrict(districtList.get(index));
                }


                //UpazilaList from districtList


                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());
                for (int i = 0; i < formdataviews.size(); i++) {
                    if (formdataviews.get(i) instanceof MaterialSpinner) {
                        if (!TextUtils.isEmpty(((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText()) &&
                                (((MaterialSpinner) formdataviews.get(i)).getFloatingLabelText().toString().trim()
                                        .equalsIgnoreCase(getString(R.string.upazila_city_corp)))) {


                            MaterialSpinner spinner = (MaterialSpinner) formdataviews.get(i);
                            spinner.setEnabled(true);
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), com.vijay.jsonwizard.R.layout.native_form_simple_list_item_1, upazillalist);
                            spinner.setAdapter(adapter);
                            spinner.setSelection(0, true);
                            spinner.setOnItemSelectedListener(IndividualProfileRemoveJsonFormFragment.this);
                            break;
                        }
                    }
                }
            }
        }, null);
    }
}
