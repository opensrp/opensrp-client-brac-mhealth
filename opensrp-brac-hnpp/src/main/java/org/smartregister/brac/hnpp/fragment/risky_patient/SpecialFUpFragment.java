package org.smartregister.brac.hnpp.fragment.risky_patient;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;


/**
 * A routine f/up fragment
 */
public class SpecialFUpFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static SpecialFUpFragment newInstance(int index) {
        SpecialFUpFragment fragment = new SpecialFUpFragment();
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
        View root = inflater.inflate(R.layout.fragment_special_f_up, container, false);
        return root;
    }

}