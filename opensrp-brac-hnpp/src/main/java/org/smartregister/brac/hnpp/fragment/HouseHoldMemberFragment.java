package org.smartregister.brac.hnpp.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;

public class HouseHoldMemberFragment extends Fragment {
    public static String TAG = "HouseHoldMemberFragment";

    public HouseHoldMemberFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_house_hold_member, container, false);
    }
}