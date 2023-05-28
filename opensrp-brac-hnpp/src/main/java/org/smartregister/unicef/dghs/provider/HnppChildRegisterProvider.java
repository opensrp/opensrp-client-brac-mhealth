package org.smartregister.unicef.dghs.provider;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;

import org.apache.commons.lang3.text.WordUtils;
import org.smartregister.growthmonitoring.domain.ZScore;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.utils.GrowthUtil;
import org.smartregister.unicef.dghs.utils.HnppDBUtils;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.chw.core.holders.RegisterViewHolder;
import org.smartregister.chw.core.provider.CoreChildRegisterProvider;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.util.OpenSRPImageLoader;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.Set;

/**
 * Created by keyman on 13/11/2018.
 */

public class HnppChildRegisterProvider extends CoreChildRegisterProvider {

    private Set<org.smartregister.configurableviews.model.View> visibleColumns;
    private View.OnClickListener onClickListener;
    protected Context context;
    private CommonRepository commonRepository;

    public HnppChildRegisterProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, visibleColumns, onClickListener, paginationClickListener);
        this.visibleColumns = visibleColumns;
        this.onClickListener = onClickListener;
        this.context = context;
        this.commonRepository = commonRepository;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, client, viewHolder);
            populateLastColumn(pc, viewHolder);

            return;
        }
    }
    public void populatePatientColumn(CommonPersonObjectClient pc, SmartRegisterClient client, RegisterViewHolder viewHolder) {

        String motherEntityId = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.MOTHER_ENTITY_ID, true);
        String relationId = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.RELATIONAL_ID, true);
        String motherName = Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.CHILD_MOTHER_NAME, true);

        motherName = HnppDBUtils.getMotherName(motherEntityId,relationId,motherName);
        String parentName = context.getResources().getString(org.smartregister.chw.core.R.string.care_giver_initials, motherName);
        String firstName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String middleName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true);
        String lastName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
        String childName = org.smartregister.util.Utils.getName(firstName, middleName + " " + lastName);
        String houseHoldHead = Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.HOUSE_HOLD_NAME, true);
        String dobString = Utils.getDuration(Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false));
        viewHolder.textViewChildName.setText(context.getResources().getString(org.smartregister.chw.core.R.string.age, WordUtils.capitalize(Utils.getTranslatedDate(dobString, context))));

        String muacStatus = Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.MUAC_STATUS, true);
        String heightStatus = Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.HEIGHT_STATUS, true);
        String weightStatus = Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.WEIGHT_STATUS, true);
        viewHolder.childStatusImage.setColorFilter(ContextCompat.getColor(context,getChildStatusColor(getChildStatus(muacStatus,weightStatus,heightStatus))));

        fillValue(viewHolder.textViewParentName, WordUtils.capitalize(childName));

        String weightValue = Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.CHILD_WEIGHT, true);
        String heightValue = Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.CHILD_HEIGHT, true);
        String vaccineName = Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.LAST_VACCINE_NAME, true);
        String vaccineDate = Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.LAST_VACCINE_DATE, true);
        StringBuilder builder = new StringBuilder();
        if(!TextUtils.isEmpty(weightValue)){
            builder.append("W:"+weightValue+" kg ");
            builder.append("\n");
        }
        if(!TextUtils.isEmpty(heightValue)){
            builder.append("H:"+heightValue+" cm");
        }
        viewHolder.textViewWeight.setText(builder.toString());
        StringBuilder builder2 = new StringBuilder();
        if(!TextUtils.isEmpty(vaccineName)){
            builder2.append(vaccineName);
        }
        if(!TextUtils.isEmpty(vaccineDate)){
            builder2.append(context.getString(R.string.given_at)+vaccineDate);
        }
        viewHolder.textViewLastVaccine.setText(builder2.toString());
        viewHolder.profileImage.setVisibility(View.VISIBLE);
        viewHolder.profileImage.setImageResource(org.smartregister.family.R.mipmap.ic_child);

        viewHolder.textViewAddressGender.setTextColor(ContextCompat.getColor(context, android.R.color.black));

        setAddressAndGender(pc, viewHolder);

        addButtonClickListeners(client, viewHolder);
        String baseEntityId = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);

        if(HnppDBUtils.childRisk(baseEntityId)){
            viewHolder.riskView.setVisibility(View.VISIBLE);
        }else{
            viewHolder.riskView.setVisibility(View.GONE);
        }

    }
    private int getChildStatusColor(String child_status) {
        return ZScore.getZscoreColorByText(child_status);
    }
    private String getChildStatus(String muac_status, String weight_status, String height_status){
        String finalStatus = GrowthUtil.getOverallChildStatus(muac_status,weight_status,height_status);
        return finalStatus;

    }

    protected void populateLastColumn(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
      //  Utils.startAsyncTask(new UpdateLastAsyncTask(context, commonRepository, viewHolder, pc.entityId(), onClickListener), null);
    }

    @Override
    public void setAddressAndGender(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
        String address = Utils.getValue(pc.getColumnmaps(),  HnppConstants.KEY.BLOCK_NAME, true);

        String gender = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.GENDER, true);
        fillValue(viewHolder.textViewAddressGender, gender + " \u00B7 " + address);
        if(gender.equals("F")) {
            viewHolder.profileImage.setTag(org.smartregister.R.id.entity_id, pc.entityId());
            DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(pc.entityId(), OpenSRPImageLoader.getStaticImageListener(viewHolder.profileImage, R.drawable.child_girl_infant, R.drawable.child_girl_infant));
        }
        else {
            viewHolder.profileImage.setTag(org.smartregister.R.id.entity_id, pc.entityId());
            DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(pc.entityId(), OpenSRPImageLoader.getStaticImageListener(viewHolder.profileImage, R.drawable.child_boy_infant, R.drawable.child_boy_infant));
        }
    }

    @Override
    public void addButtonClickListeners(SmartRegisterClient client, RegisterViewHolder viewHolder) {
        viewHolder.dueButtonLayout.setVisibility(View.GONE);
        viewHolder.goToProfileLayout.setVisibility(View.GONE);

        View patient = viewHolder.childColumn;
        attachPatientOnclickListener(patient, client);

        View goToProfileImage = viewHolder.goToProfileImage;
        attachPatientOnclickListener(goToProfileImage, client);

        View goToProfileLayout = viewHolder.goToProfileLayout;
        attachPatientOnclickListener(goToProfileLayout, client);

    }

}
