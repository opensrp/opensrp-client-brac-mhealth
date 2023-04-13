package org.smartregister.unicef.dghs.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;
import org.smartregister.Context;
import org.smartregister.unicef.dghs.HnppApplication;

public class HnppAncJsonFormFragment extends JsonWizardFormFragment {

    public static HnppAncJsonFormFragment getFormFragment(String stepName) {
        HnppAncJsonFormFragment jsonFormFragment = new HnppAncJsonFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString("stepName", stepName);
        jsonFormFragment.setArguments(bundle);
        return jsonFormFragment;
    }
    public Context context() {
        return HnppApplication.getInstance().getContext();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(com.vijay.jsonwizard.R.id.action_save).setVisible(true);

    }

}
