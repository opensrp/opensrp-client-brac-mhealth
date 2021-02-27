package org.smartregister.brac.hnpp.fragment;

import android.os.Bundle;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;

import java.util.Collection;

public class HnppFormViewFragment extends JsonWizardFormFragment {
    public static HnppFormViewFragment getFormFragment(String stepName) {
        HnppFormViewFragment jsonFormFragment = new HnppFormViewFragment();
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Collection<View> formDataViews = getJsonApi().getFormDataViews();
        for (View v : formDataViews) {
            if(v instanceof MaterialEditText) {
                if (((MaterialEditText) v).getFloatingLabelText() != null && (((MaterialEditText) v).getFloatingLabelText().toString()).equals("বি.এম.আই")) {
                    ((MaterialEditText) v).setEnabled(false);
                    break;
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(com.vijay.jsonwizard.R.id.action_save).setVisible(false);

    }
}
