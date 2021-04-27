package org.smartregister.chw.core.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.contract.FamilyProfileExtendedContract;
import org.smartregister.chw.core.custom_views.FamilyFloatingMenu;
import org.smartregister.chw.core.event.PermissionEvent;
import org.smartregister.chw.core.listener.FloatingMenuListener;
import org.smartregister.chw.core.presenter.CoreFamilyProfilePresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.activity.BaseFamilyProfileActivity;
import org.smartregister.family.fragment.BaseFamilyProfileMemberFragment;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.util.PermissionUtils;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public abstract class CoreFamilyProfileActivity extends BaseFamilyProfileActivity implements FamilyProfileExtendedContract.View {
    protected String familyBaseEntityId;
    protected String familyHead;
    protected String primaryCaregiver;
    protected String familyName;
    protected FamilyFloatingMenu familyFloatingMenu;

    @Override
    protected void setupViews() {
        super.setupViews();

        // Update profile border
        CircleImageView profileView = findViewById(R.id.imageview_profile);
        profileView.setBorderWidth(2);

        // add floating menu
        familyFloatingMenu = new FamilyFloatingMenu(this);
        LinearLayout.LayoutParams linearLayoutParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
        familyFloatingMenu.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
        addContentView(familyFloatingMenu, linearLayoutParams);
        familyFloatingMenu.setClickListener(
                FloatingMenuListener.getInstance(this, presenter().familyBaseEntityId())
        );
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        FloatingMenuListener.getInstance(this, presenter().familyBaseEntityId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem addMember = menu.findItem(R.id.add_member);
        if (addMember != null) {
            addMember.setVisible(false);
        }

        getMenuInflater().inflate(R.menu.profile_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int i = item.getItemId();
        if (i == R.id.action_family_details) {
            startFormForEdit();
        } else if (i == R.id.action_remove_member) {
            Intent frm_intent = new Intent(this, getFamilyRemoveMemberClass());
            frm_intent.putExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, getFamilyBaseEntityId());
            frm_intent.putExtra(Constants.INTENT_KEY.FAMILY_HEAD, familyHead);
            frm_intent.putExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER, primaryCaregiver);
            startActivityForResult(frm_intent, CoreConstants.ProfileActivityResults.CHANGE_COMPLETED);
        } else if (i == R.id.action_change_head) {
            Intent fh_intent = new Intent(this, getFamilyProfileMenuClass());
            fh_intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, getFamilyBaseEntityId());
            fh_intent.putExtra(CoreFamilyProfileMenuActivity.MENU, CoreConstants.MenuType.ChangeHead);
            startActivityForResult(fh_intent, CoreConstants.ProfileActivityResults.CHANGE_COMPLETED);
        } else if (i == R.id.action_change_care_giver) {
            Intent pc_intent = new Intent(this, getFamilyProfileMenuClass());
            pc_intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, getFamilyBaseEntityId());
            pc_intent.putExtra(CoreFamilyProfileMenuActivity.MENU, CoreConstants.MenuType.ChangePrimaryCare);
            startActivityForResult(pc_intent, CoreConstants.ProfileActivityResults.CHANGE_COMPLETED);
        } else {
            super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case JsonFormUtils.REQUEST_CODE_GET_JSON:
                    try {
                        String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                        Timber.d("JSONResult : %s", jsonString);

                        JSONObject form = new JSONObject(jsonString);
                        String encounter_type = form.getString(JsonFormUtils.ENCOUNTER_TYPE);
                       if (encounter_type.equals(CoreConstants.EventType.CHILD_REGISTRATION)) {

                            presenter().saveChildForm(jsonString, false);

                        }
                       else {
                           super.onActivityResult(requestCode, resultCode, data);
                       }
                    } catch (Exception e) {
                        Timber.e(e);
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    break;

            }
        }

    }

    @Override
    public void refreshMemberList(FetchStatus fetchStatus) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            for (int i = 0; i < adapter.getCount(); i++) {
                refreshList(adapter.getItem(i));
            }
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                for (int i = 0; i < adapter.getCount(); i++) {
                    refreshList(adapter.getItem(i));
                }
            });
        }
    }

    @Override
    public FamilyProfileExtendedContract.Presenter presenter() {
        return (CoreFamilyProfilePresenter) presenter;
    }

    protected void setPrimaryCaregiver(String caregiver) {
        if (StringUtils.isNotBlank(caregiver)) {
            this.primaryCaregiver = caregiver;
            getIntent().putExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER, caregiver);
        }
    }

    protected abstract void refreshPresenter();

    private void refreshMemberFragment(String careGiverID, String familyHeadID) {
        BaseFamilyProfileMemberFragment memberFragment = this.getProfileMemberFragment();
        if (memberFragment != null) {
            if (StringUtils.isNotBlank(careGiverID)) {
                memberFragment.setPrimaryCaregiver(careGiverID);
            }
            if (StringUtils.isNotBlank(familyHeadID)) {
                memberFragment.setFamilyHead(familyHeadID);
            }
            refreshMemberList(FetchStatus.fetched);
        }
    }

    protected void setFamilyHead(String head) {
        if (StringUtils.isNotBlank(head)) {
            this.familyHead = head;
            getIntent().putExtra(Constants.INTENT_KEY.FAMILY_HEAD, head);
        }
    }

    protected abstract void refreshList(Fragment item);

    public void startFormForEdit() {
        if (familyBaseEntityId != null) {
            presenter().fetchProfileData();
        }
    }

    protected abstract Class<? extends CoreFamilyRemoveMemberActivity> getFamilyRemoveMemberClass();

    public String getFamilyBaseEntityId() {
        return familyBaseEntityId;
    }

    protected abstract Class<? extends CoreFamilyProfileMenuActivity> getFamilyProfileMenuClass();

    @Override
    public void startChildForm(String formName, String entityId, String metadata, String currentLocationId) throws Exception {
        presenter().startChildForm(formName, entityId, metadata, currentLocationId);
    }

    @Override
    public void updateHasPhone(boolean hasPhone) {
        if (familyFloatingMenu != null) {
            familyFloatingMenu.reDraw(hasPhone);
        }
    }

    @Override
    protected void initializePresenter() {
        familyBaseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        familyHead = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_HEAD);
        primaryCaregiver = getIntent().getStringExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER);
        familyName = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_NAME);
    }

    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If request is cancelled, the result arrays are empty.
        if (requestCode == PermissionUtils.PHONE_STATE_PERMISSION_REQUEST_CODE) {
            boolean granted = (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED);
            if (granted) {
                PermissionEvent event = new PermissionEvent(requestCode, granted);
                EventBus.getDefault().post(event);
            } else {
                Toast.makeText(this, getText(R.string.allow_calls_denied), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void updateDueCount(final int dueCount) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> adapter.updateCount(Pair.create(1, dueCount)));
    }
}
