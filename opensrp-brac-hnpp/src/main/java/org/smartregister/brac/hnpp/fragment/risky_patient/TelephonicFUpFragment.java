package org.smartregister.brac.hnpp.fragment.risky_patient;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.RoutinFUpListAdapter;
import org.smartregister.brac.hnpp.adapter.TelephonicFUpListAdapter;
import org.smartregister.brac.hnpp.contract.TelephonicFUpContract;
import org.smartregister.brac.hnpp.model.AncFollowUpModel;
import org.smartregister.brac.hnpp.presenter.SpecialFUpPresenter;
import org.smartregister.brac.hnpp.presenter.TelephonicFUpPresenter;

import java.util.ArrayList;


/**
 * A routine f/up fragment
 */
public class TelephonicFUpFragment extends Fragment implements TelephonicFUpContract.View {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final int REQUEST_PHONE_CALL = 101;
    TelephonicFUpPresenter presenter;
    RecyclerView recyclerView;
    ProgressBar progressBar;

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
        recyclerView = root.findViewById(R.id.telephonyFollowUpListRv);
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
        presenter = new TelephonicFUpPresenter(this);
        showProgressBar();
        ArrayList<AncFollowUpModel> list =  presenter.fetchData();
        TelephonicFUpListAdapter adapter = new TelephonicFUpListAdapter(getActivity(), new TelephonicFUpListAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, AncFollowUpModel content) {
                launchCall(content);
            }
        });
        adapter.setData(list);
        recyclerView.setAdapter(adapter);
        hideProgressBar();
    }

    private void launchCall(AncFollowUpModel content) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
        }
        else
        {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:"+content.memberPhoneNum));
            startActivity(intent);
        }


    }

    @Override
    public TelephonicFUpContract.Presenter getPresenter() {
        return presenter;
    }
}