package org.smartregister.brac.hnpp.provider;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;

import org.apache.commons.lang3.text.WordUtils;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.chw.core.holders.RegisterViewHolder;
import org.smartregister.chw.core.provider.CoreChildRegisterProvider;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;
import java.util.Set;

/**
 * Created by keyman on 13/11/2018.
 */

public class HnppAllMemberRegisterProvider extends CoreChildRegisterProvider {

    private Set<org.smartregister.configurableviews.model.View> visibleColumns;
    protected Context context;

    public HnppAllMemberRegisterProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, visibleColumns, onClickListener, paginationClickListener);
        this.visibleColumns = visibleColumns;
        this.context = context;
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


        String firstName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String middleName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true);
        String lastName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
        String childName = org.smartregister.util.Utils.getName(firstName, middleName + " " + lastName);

        //fillValue(viewHolder.textViewChildName, WordUtils.capitalize(childName));
        String houseHoldHead = Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.HOUSE_HOLD_NAME, true);
        viewHolder.textViewChildName.setText(context.getString(R.string.house_hold_head_name,houseHoldHead));
        String serialNo = Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.SERIAL_NO, true);
        if(serialNo.isEmpty() || serialNo.equalsIgnoreCase("H")){
            serialNo="";
        }
        if(!TextUtils.isEmpty(serialNo)){
            viewHolder.textViewChildName.setText(viewHolder.textViewChildName.getText()+", "+context.getString(R.string.serial_no,serialNo));

        }
        String dobString = Utils.getDuration(Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false));
        //dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString;
        fillValue(viewHolder.textViewParentName, WordUtils.capitalize(childName) + " " + context.getResources().getString(org.smartregister.chw.core.R.string.age, WordUtils.capitalize(Utils.getTranslatedDate(dobString, context))));
        String ssName = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.SS_NAME, true);
        if(!TextUtils.isEmpty(ssName)){
            viewHolder.textViewParentName.setText(viewHolder.textViewParentName.getText()+", "+context.getString(R.string.ss_name,ssName));

        }

        setAddressAndGender(pc, viewHolder);
        String entityType = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.ENTITY_TYPE, false);

        String yearSub =  dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : "0";
        if(!TextUtils.isEmpty(yearSub) && Integer.parseInt(yearSub) >=5){
            entityType ="";
        }
        viewHolder.profileImage.setVisibility(View.VISIBLE);
        viewHolder.profileImage.setImageResource(org.smartregister.family.util.Utils.getMemberProfileImageResourceIDentifier(entityType));
        viewHolder.textViewAddressGender.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        addButtonClickListeners(client, viewHolder);
        String baseEntityId = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);
        if(HnppDBUtils.isRiskAll(baseEntityId)){
            viewHolder.riskView.setVisibility(View.VISIBLE);
        }else{
            viewHolder.riskView.setVisibility(View.GONE);
        }

    }

    protected void populateLastColumn(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
        viewHolder.dueButtonLayout.setVisibility(View.GONE);
        // Utils.startAsyncTask(new UpdateElcoServiceTask(context, viewHolder, pc.entityId()), null);
    }

    @Override
    public void setAddressAndGender(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
        String address = Utils.getValue(pc.getColumnmaps(), HnppConstants.KEY.VILLAGE_NAME, true);
        String gender = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.GENDER, true);
        fillValue(viewHolder.textViewAddressGender, gender + " \u00B7 " + address);
    }

    @Override
    public void addButtonClickListeners(SmartRegisterClient client, RegisterViewHolder viewHolder) {
        //viewHolder.dueButtonLayout.setVisibility(View.GONE);
        viewHolder.goToProfileLayout.setVisibility(View.GONE);

        View patient = viewHolder.childColumn;
        attachPatientOnclickListener(patient, client);

        View goToProfileImage = viewHolder.goToProfileImage;
        attachPatientOnclickListener(goToProfileImage, client);

        View goToProfileLayout = viewHolder.goToProfileLayout;
        attachPatientOnclickListener(goToProfileLayout, client);

    }




}
