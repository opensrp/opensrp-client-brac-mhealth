package org.smartregister.brac.hnpp.fragment.risky_patient;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.RoutinFUpListAdapter;
import org.smartregister.brac.hnpp.adapter.SpecialFUpListAdapter;
import org.smartregister.brac.hnpp.contract.SpecialFUpContract;
import org.smartregister.brac.hnpp.model.AncFollowUpModel;
import org.smartregister.brac.hnpp.presenter.RoutinFUpPresenter;
import org.smartregister.brac.hnpp.presenter.SpecialFUpPresenter;

import java.util.ArrayList;


/**
 * A routine f/up fragment
 */
public class SpecialFUpFragment extends Fragment implements SpecialFUpContract.View {

    private static final String ARG_SECTION_NUMBER = "section_number";
    SpecialFUpPresenter presenter;
    RecyclerView recyclerView;
    ProgressBar progressBar;

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
        recyclerView = root.findViewById(R.id.specialFollowUpListRv);
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
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void initializePresenter() {
        showProgressBar();
        presenter = new SpecialFUpPresenter(this);
        ArrayList<AncFollowUpModel> list =  presenter.fetchRoutinFUp();
        SpecialFUpListAdapter adapter = new SpecialFUpListAdapter(getActivity(), new SpecialFUpListAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, AncFollowUpModel content) {

            }
        });
        adapter.setData(list);
        recyclerView.setAdapter(adapter);
        hideProgressBar();
    }

    @Override
    public SpecialFUpContract.Presenter getPresenter() {
        return presenter;
    }
}