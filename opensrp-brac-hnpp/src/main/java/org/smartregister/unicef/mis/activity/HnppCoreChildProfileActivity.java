package org.smartregister.unicef.mis.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.chw.core.contract.CoreChildProfileContract;
import org.smartregister.chw.core.contract.CoreChildRegisterContract;
import org.smartregister.chw.core.model.CoreChildProfileModel;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Task;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.listener.OnClickFloatingMenu;
import org.smartregister.unicef.mis.presenter.HnppChildProfilePresenter;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.view.activity.BaseProfileActivity;
import static org.smartregister.unicef.mis.utils.HnppConstants.MEMBER_ID_SUFFIX;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;
import java.util.Set;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class HnppCoreChildProfileActivity extends BaseProfileActivity implements CoreChildProfileContract.View, CoreChildRegisterContract.InteractorCallBack  {

    protected String houseHoldId = "";

    public String childBaseEntityId;
    public boolean isComesFromFamily = false;
    public String lastVisitDay;
    public OnClickFloatingMenu onClickFloatingMenu;
    public Handler handler = new Handler();

    public RelativeLayout layoutFamilyHasRow;
    protected TextView textViewParentName, textViewLastVisit, textViewMedicalHistory;
    protected CircleImageView imageViewProfile;
    protected View recordVisitPanel;
    protected MemberObject memberObject;
    private boolean appBarTitleIsShown = true;
    private int appBarLayoutScrollRange = -1;
    protected TextView textViewTitle,textViewId;
    protected TextView textViewChildName, textViewGender, textViewAddress, textViewRecord, textViewVisitNot, tvEdit;
    private RelativeLayout layoutNotRecordView, layoutLastVisitRow, layoutMostDueOverdue;
    private RelativeLayout layoutRecordButtonDone;
    private LinearLayout layoutRecordView;
    private View viewLastVisitRow, viewMostDueRow, viewFamilyRow;
    private TextView textViewNotVisitMonth, textViewUndo, textViewNameDue, textViewFamilyHas;
    private ImageView imageViewCross;
    protected String gender;
    public static void startMe(Activity activity, String houseHoldId, boolean isComesFromFamily, MemberObject memberObject, Class<?> cls) {
        Intent intent = new Intent(activity, cls);
        intent.putExtra(HnppConstants.KEY.HOUSE_HOLD_ID,houseHoldId);
        intent.putExtra(CoreConstants.INTENT_KEY.IS_COMES_FROM_FAMILY, isComesFromFamily);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT, memberObject);
        activity.startActivity(intent);
    }
    @Override
    protected void onCreation() {
        setContentView(org.smartregister.chw.core.R.layout.activity_child_profile);
        Toolbar toolbar = findViewById(org.smartregister.chw.core.R.id.collapsing_toolbar);
        textViewTitle = toolbar.findViewById(org.smartregister.chw.core.R.id.toolbar_title);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            memberObject = (MemberObject) getIntent().getSerializableExtra(org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT);
            childBaseEntityId = memberObject.getBaseEntityId();
            isComesFromFamily = getIntent().getBooleanExtra(CoreConstants.INTENT_KEY.IS_COMES_FROM_FAMILY, false);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            final Drawable upArrow = getResources().getDrawable(org.smartregister.chw.core.R.drawable.ic_arrow_back_white_24dp);
//            upArrow.setColorFilter(getResources().getColor(org.smartregister.chw.core.R.color.text_blue), PorterDuff.Mode.SRC_ATOP);
            actionBar.setHomeAsUpIndicator(upArrow);
            actionBar.setDisplayShowTitleEnabled(false);
            //toolbar.setNavigationIcon(R.drawable.ic_toolbar);
            toolbar.setTitle("");
            toolbar.setSubtitle("");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        textViewTitle.setOnClickListener(v -> onBackPressed());
        appBarLayout = findViewById(org.smartregister.chw.core.R.id.collapsing_toolbar_appbarlayout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBarLayout.setOutlineProvider(null);
        }
        imageRenderHelper = new ImageRenderHelper(this);
        houseHoldId = getIntent().getStringExtra(HnppConstants.KEY.HOUSE_HOLD_ID);
    }
    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == org.smartregister.chw.core.R.id.textview_visit_not) {
            presenter().updateVisitNotDone(System.currentTimeMillis());
            tvEdit.setVisibility(View.GONE);
        } else if (i == org.smartregister.chw.core.R.id.textview_undo) {
            presenter().updateVisitNotDone(0);
        }
    }
    @Override
    protected void initializePresenter() {
        childBaseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.BASE_ENTITY_ID);
        isComesFromFamily = getIntent().getBooleanExtra(CoreConstants.INTENT_KEY.IS_COMES_FROM_FAMILY, false);
        String familyName = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_NAME);
        if (familyName == null) {
            familyName = "";
        }

        if (presenter == null) {
            presenter = new HnppChildProfilePresenter(this, new CoreChildProfileModel(familyName), childBaseEntityId,houseHoldId);
        }

        fetchProfileData();
    }
    @Override
    protected void setupViews() {
        textViewParentName = findViewById(org.smartregister.chw.core.R.id.textview_parent_name);
        textViewChildName = findViewById(org.smartregister.chw.core.R.id.textview_name_age);
        textViewGender = findViewById(org.smartregister.chw.core.R.id.textview_gender);
        textViewAddress = findViewById(org.smartregister.chw.core.R.id.textview_address);
        textViewId = findViewById(org.smartregister.chw.core.R.id.textview_id);
        tvEdit = findViewById(org.smartregister.chw.core.R.id.textview_edit);
        imageViewProfile = findViewById(org.smartregister.chw.core.R.id.imageview_profile);
        recordVisitPanel = findViewById(org.smartregister.chw.core.R.id.record_visit_panel);
        textViewRecord = findViewById(org.smartregister.chw.core.R.id.textview_record_visit);
        textViewVisitNot = findViewById(org.smartregister.chw.core.R.id.textview_visit_not);
        textViewNotVisitMonth = findViewById(org.smartregister.chw.core.R.id.textview_not_visit_this_month);
        textViewLastVisit = findViewById(org.smartregister.chw.core.R.id.textview_last_vist_day);
        textViewUndo = findViewById(org.smartregister.chw.core.R.id.textview_undo);
        imageViewCross = findViewById(org.smartregister.chw.core.R.id.cross_image);
        layoutRecordView = findViewById(org.smartregister.chw.core.R.id.record_visit_bar);
        layoutNotRecordView = findViewById(org.smartregister.chw.core.R.id.record_visit_status_bar);
        layoutLastVisitRow = findViewById(org.smartregister.chw.core.R.id.last_visit_row);
        textViewMedicalHistory = findViewById(org.smartregister.chw.core.R.id.text_view_medical_hstory);
        layoutMostDueOverdue = findViewById(org.smartregister.chw.core.R.id.most_due_overdue_row);
        textViewNameDue = findViewById(org.smartregister.chw.core.R.id.textview_name_due);
        layoutFamilyHasRow = findViewById(org.smartregister.chw.core.R.id.family_has_row);
        textViewFamilyHas = findViewById(org.smartregister.chw.core.R.id.textview_family_has);
        layoutRecordButtonDone = findViewById(org.smartregister.chw.core.R.id.record_visit_done_bar);
        viewLastVisitRow = findViewById(org.smartregister.chw.core.R.id.view_last_visit_row);
        viewMostDueRow = findViewById(org.smartregister.chw.core.R.id.view_most_due_overdue_row);
        viewFamilyRow = findViewById(org.smartregister.chw.core.R.id.view_family_row);
        textViewRecord.setOnClickListener(this);
        textViewVisitNot.setOnClickListener(this);
        textViewUndo.setOnClickListener(this);
        imageViewCross.setOnClickListener(this);
        layoutLastVisitRow.setOnClickListener(this);
        layoutMostDueOverdue.setOnClickListener(this);
        layoutFamilyHasRow.setOnClickListener(this);
        layoutRecordButtonDone.setOnClickListener(this);
    }

    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        return null;
    }
    @Override
    protected void fetchProfileData() {
        presenter().fetchProfileData();
        //updateImmunizationData();
    }
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

        if (appBarLayoutScrollRange == -1) {
            appBarLayoutScrollRange = appBarLayout.getTotalScrollRange();
        }
        if (appBarLayoutScrollRange + verticalOffset == 0) {

            textViewTitle.setText(patientName);
            appBarTitleIsShown = true;
        } else if (appBarTitleIsShown) {
            setUpToolbar();
            appBarTitleIsShown = false;
        }

    }

    public void setUpToolbar() {
        if (isComesFromFamily) {
            textViewTitle.setText(getString(org.smartregister.chw.core.R.string.return_to_family_members));
        } else {
            textViewTitle.setText(checkIfStartedFromReferrals() ? getString(org.smartregister.chw.core.R.string.return_to_task_details) : getString(org.smartregister.chw.core.R.string.return_to_all_children));
        }

    }
    private boolean checkIfStartedFromReferrals() {
        return false;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(org.smartregister.chw.core.R.menu.other_member_menu, menu);
        menu.findItem(org.smartregister.chw.core.R.id.action_anc_registration).setVisible(false);
        menu.findItem(org.smartregister.chw.core.R.id.action_malaria_followup_visit).setVisible(false);
        menu.findItem(org.smartregister.chw.core.R.id.action_sick_child_follow_up).setVisible(false);
        menu.findItem(org.smartregister.chw.core.R.id.action_malaria_diagnosis).setVisible(false);
        menu.findItem(org.smartregister.chw.core.R.id.action_remove_member).setVisible(false);
        return true;
    }
    /**
     * update immunization data and commonpersonobject for child as data may be updated
     * from childhomevisitfragment screen and need at medical history/upcoming service data.
     * need postdelay to update the client map
     */
    public void updateImmunizationData() {
        handler.postDelayed(() -> {
            layoutMostDueOverdue.setVisibility(View.GONE);
            viewMostDueRow.setVisibility(View.GONE);
            presenter().fetchVisitStatus(childBaseEntityId);
            presenter().fetchUpcomingServiceAndFamilyDue(childBaseEntityId);
            presenter().updateChildCommonPerson(childBaseEntityId);
        }, 100);
    }

    /**
     * By this method it'll process the event client at home visit in background. After finish
     * it'll update the child client because for edit it's need the vaccine card,illness,birthcert.
     */
    public void processBackgroundEvent() {
        layoutMostDueOverdue.setVisibility(View.GONE);
        viewMostDueRow.setVisibility(View.GONE);
        presenter().fetchVisitStatus(childBaseEntityId);
        presenter().fetchUpcomingServiceAndFamilyDue(childBaseEntityId);
        presenter().updateChildCommonPerson(childBaseEntityId);
        presenter().processBackGroundEvent();
    }
    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        Form form = new Form();
        form.setActionBarBackground(org.smartregister.chw.core.R.color.family_actionbar);
        form.setWizard(false);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    @Override
    public void refreshProfile(FetchStatus fetchStatus) {
        if (fetchStatus.equals(FetchStatus.fetched)) {
            handler.postDelayed(() -> presenter().fetchProfileData(), 100);
        }
    }

    @Override
    public void displayShortToast(int resourceId) {
        super.displayToast(resourceId);
    }

    @Override
    public void setProfileImage(String baseEntityId) {
        int defaultImage = org.smartregister.chw.core.R.drawable.rowavatar_child;// gender.equalsIgnoreCase(Gender.MALE.toString()) ? R.drawable.row_boy : R.drawable.row_girl;
        imageRenderHelper.refreshProfileImage(baseEntityId, imageViewProfile, defaultImage);
    }

    @Override
    public void setParentName(String parentName) {
        textViewParentName.setText(parentName);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void setGender(String gender) {
        this.gender = gender;
        textViewGender.setText(gender+""+textViewParentName.getText().toString());
        updateTopBar();
    }
    protected void updateTopBar() {
        if (gender.equalsIgnoreCase(getString(R.string.male))) {
            imageViewProfile.setBorderColor(getResources().getColor(org.smartregister.chw.core.R.color.light_blue));
        } else if (gender.equalsIgnoreCase(getString(R.string.female))) {
            imageViewProfile.setBorderColor(getResources().getColor(org.smartregister.chw.core.R.color.light_pink));
        }
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void setId(String id) {
        //if(!StringUtils.isEmpty(id)&&id.length()>=MEMBER_ID_SUFFIX)
//        textViewId.setText("ID:"+id.substring(id.length() - MEMBER_ID_SUFFIX));
        textViewId.setText("ID:"+id);

    }
    @Override
    public void setAddress(String address) {
        textViewAddress.setText(address);
    }


    @Override
    public void setProfileName(String fullName) {
        patientName = fullName;
        textViewChildName.setText(fullName);
    }

    @Override
    public void setAge(String age) {
        textViewChildName.append(", " + age);
    }

    @Override
    public void setVisitButtonDueStatus() {
        openVisitButtonView();
        textViewRecord.setBackgroundResource(org.smartregister.chw.core.R.drawable.record_btn_selector_due);
        textViewRecord.setTextColor(getResources().getColor(org.smartregister.chw.core.R.color.white));
    }

    private void openVisitButtonView() {
        layoutNotRecordView.setVisibility(View.GONE);
        layoutRecordButtonDone.setVisibility(View.GONE);
        layoutRecordView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setVisitButtonOverdueStatus() {
        openVisitButtonView();
        textViewRecord.setBackgroundResource(org.smartregister.chw.core.R.drawable.record_btn_selector_overdue);
        textViewRecord.setTextColor(getResources().getColor(org.smartregister.chw.core.R.color.white));
    }

    @Override
    public void setVisitNotDoneThisMonth() {
        openVisitMonthView();
        textViewNotVisitMonth.setText(getString(org.smartregister.chw.core.R.string.not_visiting_this_month));
        textViewUndo.setText(getString(org.smartregister.chw.core.R.string.undo));
        textViewUndo.setVisibility(View.VISIBLE);
        imageViewCross.setImageResource(org.smartregister.chw.core.R.drawable.activityrow_notvisited);
    }

    @Override
    public void setLastVisitRowView(String days) {
        lastVisitDay = days;
        if (TextUtils.isEmpty(days)) {
            layoutLastVisitRow.setVisibility(View.GONE);
            viewLastVisitRow.setVisibility(View.GONE);
        } else {
            layoutLastVisitRow.setVisibility(View.VISIBLE);
            textViewLastVisit.setText(getString(org.smartregister.chw.core.R.string.last_visit_40_days_ago, days));
            viewLastVisitRow.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void setServiceNameDue(String serviceName, String dueDate) {
        if (!TextUtils.isEmpty(serviceName)) {
            layoutMostDueOverdue.setVisibility(View.VISIBLE);
            viewMostDueRow.setVisibility(View.VISIBLE);
            textViewNameDue.setText(CoreChildUtils.fromHtml(getString(org.smartregister.chw.core.R.string.vaccine_service_due, serviceName, dueDate)));
        } else {
            layoutMostDueOverdue.setVisibility(View.GONE);
            viewMostDueRow.setVisibility(View.GONE);
        }
    }

    @Override
    public void setServiceNameOverDue(String serviceName, String dueDate) {
        layoutMostDueOverdue.setVisibility(View.VISIBLE);
        viewMostDueRow.setVisibility(View.VISIBLE);
        textViewNameDue.setText(CoreChildUtils.fromHtml(getString(org.smartregister.chw.core.R.string.vaccine_service_overdue, serviceName, dueDate)));
    }

    @Override
    public void setServiceNameUpcoming(String serviceName, String dueDate) {
        layoutMostDueOverdue.setVisibility(View.VISIBLE);
        viewMostDueRow.setVisibility(View.VISIBLE);
        textViewNameDue.setText(CoreChildUtils.fromHtml(getString(org.smartregister.chw.core.R.string.vaccine_service_upcoming, serviceName, dueDate)));

    }

    @Override
    public void setVisitLessTwentyFourView(String monthName) {
        textViewNotVisitMonth.setText(getString(org.smartregister.chw.core.R.string.visit_month, monthName));
        textViewUndo.setText(getString(org.smartregister.chw.core.R.string.edit));
        textViewUndo.setVisibility(View.GONE);
        imageViewCross.setImageResource(org.smartregister.chw.core.R.drawable.activityrow_visited);
        openVisitMonthView();
    }

    @Override
    public void setVisitAboveTwentyFourView() {
        textViewVisitNot.setVisibility(View.GONE);
        openVisitRecordDoneView();
        textViewRecord.setBackgroundResource(org.smartregister.chw.core.R.drawable.record_btn_selector_above_twentyfr);
        textViewRecord.setTextColor(getResources().getColor(org.smartregister.chw.core.R.color.light_grey_text));
    }

    private void openVisitRecordDoneView() {
        layoutRecordButtonDone.setVisibility(View.VISIBLE);
        layoutNotRecordView.setVisibility(View.GONE);
        layoutRecordView.setVisibility(View.GONE);
    }

    @Override
    public void setFamilyHasNothingDue() {
        layoutFamilyHasRow.setVisibility(View.VISIBLE);
        viewFamilyRow.setVisibility(View.VISIBLE);
        textViewFamilyHas.setText(getString(org.smartregister.chw.core.R.string.family_has_nothing_due));
    }

    @Override
    public void setFamilyHasServiceDue() {
        layoutFamilyHasRow.setVisibility(View.VISIBLE);
        viewFamilyRow.setVisibility(View.VISIBLE);
        textViewFamilyHas.setText(getString(org.smartregister.chw.core.R.string.family_has_services_due));
    }

    @Override
    public void setFamilyHasServiceOverdue() {
        layoutFamilyHasRow.setVisibility(View.VISIBLE);
        viewFamilyRow.setVisibility(View.VISIBLE);
        textViewFamilyHas.setText(CoreChildUtils.fromHtml(getString(org.smartregister.chw.core.R.string.family_has_service_overdue)));
    }

    @Override
    public CoreChildProfileContract.Presenter presenter() {
        return (CoreChildProfileContract.Presenter) presenter;
    }

    @Override
    public void updateHasPhone(boolean hasPhone) {
        //// TODO: 15/08/19
    }

    @Override
    public void enableEdit(boolean enable) {
        if (enable) {
            tvEdit.setVisibility(View.VISIBLE);
            tvEdit.setOnClickListener(this);
        } else {
            tvEdit.setVisibility(View.GONE);
            tvEdit.setOnClickListener(null);
        }
    }

    @Override
    public void hideProgressBar() {
    }

    @Override
    public void openVisitMonthView() {
        layoutNotRecordView.setVisibility(View.VISIBLE);
        layoutRecordButtonDone.setVisibility(View.GONE);
        layoutRecordView.setVisibility(View.GONE);

    }

    @Override
    public void showUndoVisitNotDoneView() {
        presenter().fetchVisitStatus(childBaseEntityId);
    }

    @Override
    public void updateAfterBackgroundProcessed() {
        presenter().updateChildCommonPerson(childBaseEntityId);
    }

    @Override
    public void setClientTasks(Set<Task> taskList) {
        //// TODO: 06/08/19
    }

    @Override
    public void onNoUniqueId() {
        //TODO
        Timber.d("onNoUniqueId unimplemented");
    }

    @Override
    public void onUniqueIdFetched(Triple<String, String, String> triple, String entityId, String familyId) {

    }

    @Override
    public void onRegistrationSaved(boolean isEdit, String baseEntityId) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (i == org.smartregister.chw.core.R.id.action_registration) {
            ((HnppChildProfilePresenter) presenter()).startFormForEdit(getResources().getString(org.smartregister.chw.core.R.string.edit_child_form_title),
                    ((HnppChildProfilePresenter) presenter()).getChildClient());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK) return;
        switch (requestCode){
            case CoreConstants.ProfileActivityResults.CHANGE_COMPLETED:
                finish();
                break;
            case JsonFormUtils.REQUEST_CODE_GET_JSON:
                try {
                    String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                    JSONObject form = new JSONObject(jsonString);
                    if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(CoreConstants.EventType.UPDATE_CHILD_REGISTRATION)) {
                        presenter().updateChildProfile(jsonString);
                    } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(CoreConstants.EventType.CHILD_REFERRAL)) {
                        presenter().createSickChildEvent(Utils.getAllSharedPreferences(), jsonString);
                    }
                } catch (Exception e) {
                    Timber.e(e, "CoreChildProfileActivity --> onActivityResult");
                }
                break;
            case org.smartregister.chw.anc.util.Constants.REQUEST_CODE_HOME_VISIT:
                updateImmunizationData();
                break;
            default:
                break;
        }
    }

    @Override
    public void errorOccured(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }
}