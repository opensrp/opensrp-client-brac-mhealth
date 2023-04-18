package org.smartregister.unicef.dghs.provider;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.utils.FormApplicability;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.chw.core.provider.CoreMemberRegisterProvider;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.fragment.BaseFamilyRegisterFragment;
import org.smartregister.family.provider.FamilyMemberRegisterProvider;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.util.AssetHandler;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.customcontrols.FontVariant;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

public class HNPPMemberRegisterProvider extends CoreMemberRegisterProvider {
    private final LayoutInflater inflater;
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;

    private View.OnClickListener onClickListener;
    private View.OnClickListener paginationClickListener;
    private String familyHead;
    private String primaryCaregiver;
    private Context context;
    private CommonRepository commonRepository;
    private ImageRenderHelper imageRenderHelper;
    protected AsyncTask<Void, Void, Void> updateAsyncTask;

    public HNPPMemberRegisterProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener, String familyHead, String primaryCaregiver) {
        super(context, commonRepository, visibleColumns, onClickListener, paginationClickListener, familyHead, primaryCaregiver);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.visibleColumns = visibleColumns;
        this.familyHead = familyHead;
        this.primaryCaregiver = primaryCaregiver;
        this.onClickListener = onClickListener;
        this.paginationClickListener = paginationClickListener;
        this.imageRenderHelper = new ImageRenderHelper(context);
        this.context = context;
        this.commonRepository = commonRepository;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {

        CommonPersonObjectClient pc = (CommonPersonObjectClient)client;
        if (this.visibleColumns.isEmpty()) {
            this.populatePatientColumn(pc, client, viewHolder);
//            this.populateIdentifierColumn(pc, viewHolder);
        }
       // super.getView(cursor, client, viewHolder);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewHolder.profile.getLayoutParams();
        layoutParams.width = context.getResources().getDimensionPixelSize(org.smartregister.chw.core.R.dimen.member_profile_pic_width);
        layoutParams.height = context.getResources().getDimensionPixelSize(org.smartregister.chw.core.R.dimen.member_profile_pic_width);
        viewHolder.profile.setLayoutParams(layoutParams);
        viewHolder.patientNameAge.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize(org.smartregister.chw.core.R.dimen.member_profile_list_title_size));

        viewHolder.statusLayout.setVisibility(View.GONE);
        viewHolder.status.setVisibility(View.GONE);
        if (updateAsyncTask != null) {
            Utils.startAsyncTask(updateAsyncTask, null);
        }

       // String entityType = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.ENTITY_TYPE, false);
//        if (CoreConstants.TABLE_NAME.CHILD.equals(entityType)) {
////            Utils.startAsyncTask(new UpdateAsyncTask(viewHolder, pc), null);
//        }
    }
    private void populatePatientColumn(CommonPersonObjectClient pc, SmartRegisterClient client, final FamilyMemberRegisterProvider.RegisterViewHolder viewHolder) {
        String baseEntityId = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);
        String firstName = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String middleName = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true);
        String lastName = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
        String patientName = org.smartregister.family.util.Utils.getName(firstName, middleName, lastName);
        String entityType = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.ENTITY_TYPE, false);
        String relation_with_household_head = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.RELATION_WITH_HOUSEHOLD, false);
        relation_with_household_head = HnppConstants.getRelationWithHouseholdHead(relation_with_household_head);
        String dob = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false);
        String guId = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(),  HnppConstants.KEY.GU_ID, false);
        String dobString = org.smartregister.family.util.Utils.getDuration(dob);
        String yearSub =  dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : "0";

        dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString;
        String dod = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOD, false);
        String dateRemoved = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DATE_REMOVED, false);

        if(!TextUtils.isEmpty(yearSub) && Integer.parseInt(yearSub) >=5){
            entityType ="";
        }

        viewHolder.gender.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        if (StringUtils.isNotBlank(dod) ) {
            dobString = org.smartregister.family.util.Utils.getDuration(dod, dob);
            patientName = patientName + "\n" + org.smartregister.family.util.Utils.getTranslatedDate(dobString, this.context) + " " + this.context.getString(org.smartregister.family.R.string.deceased_brackets);
            viewHolder.patientNameAge.setFontVariant(FontVariant.REGULAR);
            viewHolder.patientNameAge.setTextColor(-7829368);
            viewHolder.patientNameAge.setTypeface(viewHolder.patientNameAge.getTypeface(), Typeface.ITALIC);
            viewHolder.gender.setTextColor(-7829368);
            viewHolder.gender.setTypeface(viewHolder.patientNameAge.getTypeface(), Typeface.ITALIC);
            viewHolder.profile.setImageResource(org.smartregister.family.util.Utils.getMemberProfileImageResourceIDentifier(entityType));
            viewHolder.nextArrow.setVisibility(View.GONE);
        } else if(StringUtils.isNotBlank(dateRemoved) || dateRemoved.equalsIgnoreCase("1")){
            dobString = org.smartregister.family.util.Utils.getDuration(dob);
            patientName = patientName + "\n" + org.smartregister.family.util.Utils.getTranslatedDate(dobString, this.context) + " " + this.context.getString(R.string.migrated_brackets);
            viewHolder.patientNameAge.setFontVariant(FontVariant.REGULAR);
            viewHolder.patientNameAge.setTextColor(-7829368);
            viewHolder.patientNameAge.setTypeface(viewHolder.patientNameAge.getTypeface(), Typeface.ITALIC);
            viewHolder.gender.setTextColor(-7829368);
            viewHolder.gender.setTypeface(viewHolder.patientNameAge.getTypeface(), Typeface.ITALIC);
            viewHolder.profile.setImageResource(org.smartregister.family.util.Utils.getMemberProfileImageResourceIDentifier(entityType));
            viewHolder.nextArrow.setVisibility(View.GONE);
        }
        else {

            viewHolder.patientNameAge.setFontVariant(FontVariant.REGULAR);
            viewHolder.patientNameAge.setTextColor(-16777216);
            viewHolder.patientNameAge.setTypeface(viewHolder.patientNameAge.getTypeface(), Typeface.NORMAL);
            this.imageRenderHelper.refreshProfileImage(pc.getCaseId(), viewHolder.profile, org.smartregister.family.util.Utils.getMemberProfileImageResourceIDentifier(entityType));
            viewHolder.nextArrow.setVisibility(View.VISIBLE);
        }
        ((TextView)viewHolder.patientNameAge).setSingleLine(true);
        ((TextView)viewHolder.gender).setSingleLine(false);
        fillValue(viewHolder.patientNameAge, patientName);
        String gender_key = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), "gender", true);
        String maritalStatus  = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), "marital_status", false);

        String gender = "";
        if (gender_key.equalsIgnoreCase("M")) {
            gender = "পুরুষ";
        } else if (gender_key.equalsIgnoreCase("F")) {
            gender = "নারী";
        }
        String relationAge = context.getString(R.string.relation_with_member_and_head,relation_with_household_head) + "<br>বয়সঃ " + org.smartregister.family.util.Utils.getTranslatedDate(dobString, this.context)+", "+gender;

        if(!TextUtils.isEmpty(guId)){
            if(guId.equalsIgnoreCase(HnppConstants.TEST_GU_ID)){
                relationAge = relationAge.concat("<br>"+this.context.getString(R.string.finger_print_without));
            }else{
                relationAge = relationAge.concat("<br>"+this.context.getString(R.string.finger_print_added));
            }

        }
        viewHolder.gender.setText(Html.fromHtml(relationAge));


        viewHolder.nextArrowColumn.setOnClickListener(new View.OnClickListener() {
            public void onClick(android.view.View v) {
                viewHolder.nextArrow.performClick();
            }
        });
        viewHolder.primaryCaregiver.setOnClickListener(new View.OnClickListener() {
            public void onClick(android.view.View v) {
                viewHolder.primaryCaregiver.performClick();
            }
        });
        viewHolder.profile.setOnClickListener(new View.OnClickListener() {
            public void onClick(android.view.View v) {
                viewHolder.patientColumn.performClick();
            }
        });
        viewHolder.registerColumns.setOnClickListener(new View.OnClickListener() {
            public void onClick(android.view.View v) {
                viewHolder.patientColumn.performClick();
            }
        });
        if (StringUtils.isNotBlank(dod) || StringUtils.isNotBlank(dateRemoved)) {
            android.view.View patient = viewHolder.patientColumn;
            patient.setClickable(false);
            android.view.View nextArrow = viewHolder.nextArrow;
            nextArrow.setClickable(false);

        }else{
            android.view.View patient = viewHolder.patientColumn;
            attachPatientOnclickListener(patient, client);
            android.view.View nextArrow = viewHolder.nextArrow;
            attachNextArrowOnclickListener(nextArrow, client);
            android.view.View addChild = viewHolder.primaryCaregiver;
            attachAddChildOnclickListener(addChild, client);
        }
       if(gender_key.equalsIgnoreCase("F")){
           int age = FormApplicability.getAge(pc);
           if (updateAsyncTask == null) {
               new UpdateAsyncTask(viewHolder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,baseEntityId,age+"");
           }
           if(FormApplicability.isElco(age) && !TextUtils.isEmpty(maritalStatus) && maritalStatus.equalsIgnoreCase("Married")){
               viewHolder.primaryCaregiver.setVisibility(View.VISIBLE);
               viewHolder.primaryCaregiver.setText(Html.fromHtml(this.context.getString(R.string.add_child)));
           }


       }


    }
    private void attachNextArrowOnclickListener(android.view.View view, SmartRegisterClient client) {
        view.setOnClickListener(this.onClickListener);
        view.setTag(client);
        view.setTag(org.smartregister.family.R.id.VIEW_ID, "click_next_arrow");
    }
    private void attachAddChildOnclickListener(android.view.View view, SmartRegisterClient client) {
        view.setOnClickListener(this.onClickListener);
        view.setTag(client);
        view.setTag(org.smartregister.family.R.id.VIEW_ID, "click_add_child");
    }
    private void attachPatientOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(org.smartregister.family.R.id.VIEW_ID, BaseFamilyRegisterFragment.CLICK_VIEW_NORMAL);
    }

    private void attachDosageOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(org.smartregister.family.R.id.VIEW_ID, BaseFamilyRegisterFragment.CLICK_VIEW_DOSAGE_STATUS);
    }
    private void populateIdentifierColumn(CommonPersonObjectClient pc, FamilyMemberRegisterProvider.RegisterViewHolder viewHolder) {
        String uniqueId = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), "unique_id", false);
        String baseEntityId = pc.getCaseId();
        if (StringUtils.isNotBlank(baseEntityId)) {
            if (baseEntityId.equals(familyHead)) {
                viewHolder.familyHead.setVisibility(View.VISIBLE);
            } else {
                viewHolder.familyHead.setVisibility(View.GONE);
            }

            if (baseEntityId.equals(primaryCaregiver)) {
                viewHolder.primaryCaregiver.setVisibility(View.VISIBLE);
            } else {
                viewHolder.primaryCaregiver.setVisibility(View.GONE);
            }
        }

    }
    private class UpdateAsyncTask extends AsyncTask<String, Boolean, Boolean> {
        private final FamilyMemberRegisterProvider.RegisterViewHolder viewHolder;

        private UpdateAsyncTask(FamilyMemberRegisterProvider.RegisterViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean isPragnent = FormApplicability.isPragnent(params[0],Integer.parseInt(params[1]));

            return isPragnent;
        }

        @Override
        protected void onPostExecute(Boolean param) {
            updatePragnencyIcons(viewHolder, param);
        }
    }

    private void updatePragnencyIcons(RegisterViewHolder viewHolder, Boolean param) {
        if(param!=null && param.booleanValue() == true){
            viewHolder.nextArrow.setVisibility(View.VISIBLE);
            viewHolder.nextArrow.setImageResource(R.mipmap.ic_anc_pink);
        }else{
            viewHolder.nextArrow.setVisibility(View.GONE);
        }

    }

}
