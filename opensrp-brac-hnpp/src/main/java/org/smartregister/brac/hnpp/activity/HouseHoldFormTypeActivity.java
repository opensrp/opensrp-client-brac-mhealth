package org.smartregister.brac.hnpp.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.rey.material.widget.Button;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.listener.ContextListener;
import org.smartregister.chw.core.contract.FamilyProfileExtendedContract;
import org.smartregister.chw.core.fragment.AddMemberFragment;
import org.smartregister.chw.core.presenter.CoreFamilyProfilePresenter;
import org.smartregister.family.activity.BaseFamilyProfileActivity;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.util.Constants;

public class HouseHoldFormTypeActivity extends AppCompatActivity implements ContextListener {
    protected String familyBaseEntityId;
    protected String familyHead;
    protected String primaryCaregiver;
    protected String familyName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_hold_form_type);

        Button click = findViewById(R.id.click);

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //newBornRadio.setChecked(true);
                AddMemberFragment addmemberFragment = AddMemberFragment.newInstance();
                addmemberFragment.setContext(HouseHoldFormTypeActivity.this);
                addmemberFragment.show((((HouseHoldFormTypeActivity) getApplicationContext())).getFragmentManager(), AddMemberFragment.DIALOG_TAG);
            }
        });
    }

    @Override
    public void getContext(Context context) {
        Log.d("connnnn",""+context);
    }
}