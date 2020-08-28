package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.interactor.HnppAncHomeVisitInteractor;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.anc.activity.BaseAncHomeVisitActivity;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.presenter.BaseAncHomeVisitPresenter;
import org.smartregister.chw.anc.util.Constants;

import java.util.Map;

import static org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT;

public class HnppHomeVisitActivity extends BaseAncHomeVisitActivity {
    private static boolean sIsIdentify,sNeedVerified,sIsVerify;
    private static String sNotVerifyText;
    public static void startMe(Activity activity, MemberObject memberObject, Boolean isEditMode ) {
        Intent intent = new Intent(activity, HnppHomeVisitActivity.class);
        intent.putExtra(MEMBER_PROFILE_OBJECT, memberObject);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.EDIT_MODE, isEditMode);
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_HOME_VISIT);
    }

    public static void startMe(Activity activity, MemberObject memberObject, Boolean isEditMode,boolean isIdentify,boolean needVerified,
                               boolean isVerify, String notVerifyText ) {
        Intent intent = new Intent(activity, HnppHomeVisitActivity.class);
        intent.putExtra(MEMBER_PROFILE_OBJECT, memberObject);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.EDIT_MODE, isEditMode);
        sIsIdentify = isIdentify;
        sNeedVerified = needVerified;
        sIsVerify = isVerify;
        sNotVerifyText = notVerifyText;
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_HOME_VISIT);
    }

    @Override
    public void setUpView() {
        super.setUpView();
        TextView textView = findViewById(R.id.customFontTextViewSubmit);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
        if(!HnppConstants.isReleaseBuild()){
            findViewById(R.id.app_bar).setBackgroundResource(R.color.test_app_color);

        }else{
            findViewById(R.id.app_bar).setBackgroundResource(org.smartregister.family.R.color.customAppThemeBlue);

        }

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setMessage(R.string.form_back_confirm_dialog_message)
                .setTitle(R.string.form_back_confirm_dialog_title).setCancelable(false)
                .setPositiveButton(R.string.yes_button_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                }).setNegativeButton(R.string.no_button_label, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close) {
            onBackPressed();
        } else if (v.getId() == R.id.customFontTextViewSubmit) {
            submitVisit();
        }
    }
    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = new Intent(this, HnppAncJsonFormActivity.class);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
        Form form = new Form();
        form.setWizard(false);
        if(!HnppConstants.isReleaseBuild()){
            form.setActionBarBackground(R.color.test_app_color);

        }else{
            form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

        }

        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

        startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }
    @Override
    protected void registerPresenter() {
        presenter = new BaseAncHomeVisitPresenter(memberObject, this, new HnppAncHomeVisitInteractor(sIsIdentify,sNeedVerified,sIsVerify,sNotVerifyText));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}