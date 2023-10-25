package org.smartregister.brac.hnpp.fragment.risky_patient;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;


/**
 * A routine f/up fragment
 */
public class TelephonicFUpFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static TelephonicFUpFragment newInstance(int index) {
        TelephonicFUpFragment fragment = new TelephonicFUpFragment();
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
        View root = inflater.inflate(R.layout.fragment_telephonic_f_up, container, false);
        return root;
    }

}