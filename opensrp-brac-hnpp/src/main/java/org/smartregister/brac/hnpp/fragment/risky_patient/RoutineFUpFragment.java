package org.smartregister.brac.hnpp.fragment.risky_patient;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.RoutinFUpListAdapter;
import org.smartregister.brac.hnpp.contract.RoutinFUpContract;
import org.smartregister.brac.hnpp.model.FollowUpModel;
import org.smartregister.brac.hnpp.presenter.BkashStatusPresenter;
import org.smartregister.brac.hnpp.presenter.RoutinFUpPresenter;

import java.util.ArrayList;


/**
 * A routine f/up fragment
 */
public class RoutineFUpFragment extends Fragment implements RoutinFUpContract.View {
    RoutinFUpPresenter presenter;
    RecyclerView recyclerView;

    private static final String ARG_SECTION_NUMBER = "section_number";

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
        View root = inflater.inflate(R.layout.fragment_routin_f_up, container, false);
        recyclerView = root.findViewById(R.id.routinFollowUpListRv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        initializePresenter();
        return root;
    }

    @Override
    public void showProgressBar() {

    }

    @Override
    public void hideProgressBar() {

    }

    @Override
    public void updateView() {

    }

    @Override
    public void initializePresenter() {
        presenter = new RoutinFUpPresenter(this);
        ArrayList<FollowUpModel> list =  presenter.fetchRoutinFUp();
        RoutinFUpListAdapter adapter = new RoutinFUpListAdapter(getActivity(), new RoutinFUpListAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, FollowUpModel content) {

            }
        });
        adapter.setData(list);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public RoutinFUpContract.Presenter getPresenter() {
        return presenter;
    }
}