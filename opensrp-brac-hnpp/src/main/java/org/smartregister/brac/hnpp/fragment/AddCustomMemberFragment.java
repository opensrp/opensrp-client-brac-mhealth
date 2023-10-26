package org.smartregister.brac.hnpp.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.smartregister.brac.hnpp.activity.HouseHoldVisitActivity;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.utils.CoreConstants;

public class AddCustomMemberFragment extends DialogFragment implements View.OnClickListener {

    public static final String DIALOG_TAG = "add_member_dialog";

    private Context context;

    public static AddCustomMemberFragment newInstance() {
        return new AddCustomMemberFragment();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_NoActionBar);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        // without a handler, the window sizes itself correctly
        // but the keyboard does not show up
        new Handler().post(() -> getDialog().getWindow().setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_add_member, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.close).setOnClickListener(this);
        view.findViewById(R.id.layout_add_child_under_five).setOnClickListener(this);
        view.findViewById(R.id.layout_add_other_family_member).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            int i = v.getId();
            if (i == R.id.close) {
                dismiss();
            } else if (i == R.id.layout_add_child_under_five) {
                ((HouseHoldVisitActivity) context).startChildForm(CoreConstants.JSON_FORM.getChildRegister(), "", "", "");
                dismiss();
            } else if (i == R.id.layout_add_other_family_member) {
                ((HouseHoldVisitActivity) context).startFormActivity(CoreConstants.JSON_FORM.getFamilyMemberRegister(), null, null);
                dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}