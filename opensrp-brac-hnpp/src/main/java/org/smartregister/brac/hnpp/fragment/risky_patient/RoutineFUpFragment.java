package org.smartregister.brac.hnpp.fragment.risky_patient;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.ProgressBar;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.RoutinFUpListAdapter;
import org.smartregister.brac.hnpp.contract.RoutinFUpContract;
import org.smartregister.brac.hnpp.model.AncFollowUpModel;
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
    ProgressBar progressBar;

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
        progressBar = root.findViewById(R.id.progress_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        initializePresenter();
        return root;
    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void updateView() {

    }

    @Override
    public void initializePresenter() {
        showProgressBar();
        presenter = new RoutinFUpPresenter(this);
        ArrayList<AncFollowUpModel> list =  presenter.fetchRoutinFUp();
        RoutinFUpListAdapter adapter = new RoutinFUpListAdapter(getActivity(), new RoutinFUpListAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, AncFollowUpModel content) {

            }
        });
        adapter.setData(list);
        recyclerView.setAdapter(adapter);
        hideProgressBar();
    }

    @Override
    public RoutinFUpContract.Presenter getPresenter() {
        return presenter;
    }
}