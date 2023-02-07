package org.smartregister.unicef.dghs.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;
import com.vijay.jsonwizard.presenters.JsonFormFragmentPresenter;
import com.vijay.jsonwizard.viewstates.JsonFormFragmentViewState;

import org.json.JSONObject;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.interactor.HnppJsonFormInteractor;

public class COVIDJsonFormFragment extends JsonWizardFormFragment {
    public COVIDJsonFormFragment() {
        super();
    }

    public static COVIDJsonFormFragment getFormFragment(String stepName) {
        COVIDJsonFormFragment jsonFormFragment = new COVIDJsonFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString("stepName", stepName);
        jsonFormFragment.setArguments(bundle);
        return jsonFormFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected JsonFormFragmentPresenter createPresenter() {
        return new JsonFormFragmentPresenter(this, HnppJsonFormInteractor.getInstance());
    }

    @Override
    protected JsonFormFragmentViewState createViewState() {
        return super.createViewState();
    }
    private int ssIndex = -1;
    private int villageIndex = -1;
    private boolean isManuallyPressed = false;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isManuallyPressed = true;
            }
        }, 1000);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        super.onItemSelected(parent, view, position, id);
        if (position != -1 && parent instanceof MaterialSpinner) {
            if (((MaterialSpinner) parent).getFloatingLabelText().toString().equalsIgnoreCase(view.getContext().getResources().getString(R.string.ss_name_form_field))) {
                ssIndex = position;
            }
        }
    }

    @Override
    public JSONObject getStep(String stepName) {
        return super.getStep(stepName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }


}
