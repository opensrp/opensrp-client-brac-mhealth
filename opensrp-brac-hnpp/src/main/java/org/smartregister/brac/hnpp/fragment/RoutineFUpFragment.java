package org.smartregister.brac.hnpp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.ViewModelProvider;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.ui.main.PageViewModel;


/**
 * A routine f/up fragment
 */
public class RoutineFUpFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    public static RoutineFUpFragment newInstance(int index) {
        RoutineFUpFragment fragment = new RoutineFUpFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_risky_patient, container, false);
        final TextView textView = root.findViewById(R.id.section_label);
        textView.setText("test");
        return root;
    }

}