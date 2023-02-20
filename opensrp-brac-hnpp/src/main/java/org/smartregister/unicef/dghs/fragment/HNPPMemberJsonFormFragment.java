package org.smartregister.unicef.dghs.fragment;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;
import com.vijay.jsonwizard.presenters.JsonFormFragmentPresenter;
import com.vijay.jsonwizard.viewstates.JsonFormFragmentViewState;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.interactor.HnppJsonFormInteractor;
import org.smartregister.unicef.dghs.widget.HnppFingerPrintFactory;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static com.vijay.jsonwizard.utils.FormUtils.getFieldJSONObject;

public class HNPPMemberJsonFormFragment extends JsonWizardFormFragment {
    public HNPPMemberJsonFormFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected JsonFormFragmentPresenter createPresenter() {
        return new JsonFormFragmentPresenter(this, HnppJsonFormInteractor.getInstance());
    }

    public static HNPPMemberJsonFormFragment getFormFragment(String stepName) {
        HNPPMemberJsonFormFragment jsonFormFragment = new HNPPMemberJsonFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString("stepName", stepName);
        jsonFormFragment.setArguments(bundle);
        return jsonFormFragment;
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        super.onItemSelected(parent, view, position, id);
        if(position == -1)
            return;
        if (parent instanceof MaterialSpinner) {
            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase("খানা প্রধান এই সদস্যের কি হয়?")) {
                processHouseholdName(position);
            }
        }
       // hideKeyBoard();
    }
    private String family_first_name = "",family_last_name = "",phone_no = "";
    @SuppressLint("StaticFieldLeak")
    private void processHouseholdName(final int position){
        Utils.startAsyncTask(new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] objects) {
                JSONObject formObject = getJsonApi().getmJSONObject();
                if(StringUtils.isEmpty(family_first_name)){

                    if (formObject.has("first_name")) {
                        try {
                            family_first_name = formObject.getString("first_name");
                        } catch (JSONException e) {

                        }
                    }

                }
                if(StringUtils.isEmpty(family_last_name)){

                    if (formObject.has("last_name")) {
                        try {
                            family_last_name = formObject.getString("last_name");
                        } catch (JSONException e) {

                        }
                    }

                }
                if(StringUtils.isEmpty(phone_no)){

                    if (formObject.has("phone_no")) {
                        try {
                            phone_no = formObject.getString("phone_no");
                        } catch (JSONException e) {

                        }
                    }

                }


                return null;
            }


            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if(TextUtils.isEmpty(family_first_name) &&TextUtils.isEmpty(family_last_name) && TextUtils.isEmpty(phone_no)) return;
                    Collection<View> formdataviews =  getJsonApi().getFormDataViews();
                    MaterialEditText first_name_view = null;
                MaterialEditText last_name_view = null;
                MaterialEditText phone_number_view = null;
                Iterator<View> iterator = formdataviews.iterator();

                // while loop
                while (iterator.hasNext()) {
                     View field_view = iterator.next();
                    if (field_view instanceof MaterialEditText) {
                        if (((MaterialEditText) field_view).getFloatingLabelText()!=null&&((MaterialEditText) field_view).getFloatingLabelText().toString().trim().equalsIgnoreCase("নামের প্রথম অংশ (ইংরেজীতে)")) {
                            first_name_view = ((MaterialEditText) field_view);
                        }
                        else if (((MaterialEditText) field_view).getFloatingLabelText()!=null&&((MaterialEditText) field_view).getFloatingLabelText().toString().trim().equalsIgnoreCase("নামের শেষ অংশ (ইংরেজীতে)")) {
                            last_name_view = ((MaterialEditText) field_view);
                        }
                        else if (((MaterialEditText) field_view).getFloatingLabelText()!=null&&((MaterialEditText) field_view).getFloatingLabelText().toString().trim().equalsIgnoreCase("মোবাইল নম্বর")) {
                            phone_number_view = ((MaterialEditText) field_view);
                        }

                    }
                }

                if(first_name_view!=null){

                    if(position == 0){
                        first_name_view.setText(family_first_name);
                        first_name_view.setEnabled(false);
                    }else{
                        first_name_view.setEnabled(true);
                    }

//                        else if(first_name_view.getText().toString().equalsIgnoreCase(family_name)){
//                            first_name_view.setText("");
//                        }
                }
                if(last_name_view!=null){

                    if(position == 0){
                        last_name_view.setText(family_last_name);
                        last_name_view.setEnabled(false);
                    }else{
                        last_name_view.setEnabled(true);
                    }

//                        else if(first_name_view.getText().toString().equalsIgnoreCase(family_name)){
//                            first_name_view.setText("");
//                        }
                }
                if(phone_number_view!=null){

                    if(position == 0){
                        phone_number_view.setText(phone_no);
                        if(!phone_no.isEmpty())phone_number_view.setEnabled(false);
                    }else{
                        phone_number_view.setEnabled(true);
                    }

//                        if(!phone_no.equals("0")&& phone_number_view.getText().toString().equalsIgnoreCase(phone_no)){
//                        phone_number_view.setText("");
//                    }
                }

            }
        },null);
    }
    @Override
    protected JsonFormFragmentViewState createViewState() {
        return super.createViewState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == com.vijay.jsonwizard.R.id.action_save){
           try{
               HnppFingerPrintFactory.showFingerPrintErrorMessage();
           }catch (Exception e){
               e.printStackTrace();
           }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public JSONObject getStep(String stepName) {
        return super.getStep(stepName);
    }

    public void updateGuid(String guid){
            try {
                JSONObject guIdField = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "gu_id");
                guIdField.put("value",guid);
                JSONObject fingerPrint = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "finger_print");
                fingerPrint.put("image_path", guid);
                JSONObject fingerPrintValue = getFieldJSONObject(getStep("step1").getJSONArray("fields"), "finger_print");
                fingerPrintValue.put("value",guid);
                ArrayList<View> formdataviews = new ArrayList<>(getJsonApi().getFormDataViews());

                ImageView imageView = null;
                for(View view : formdataviews){
                    if (view instanceof ImageView) {
                        imageView = (ImageView) view;
                        String key = (String) imageView.getTag(com.vijay.jsonwizard.R.id.key);
                        if (key.equals("finger_print")) {
                            imageView.setImageResource(R.drawable.fingerprint_given);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            imageView.setTag(R.id.imagePath, guid);
                            HnppFingerPrintFactory.updateButton(guid);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

}
