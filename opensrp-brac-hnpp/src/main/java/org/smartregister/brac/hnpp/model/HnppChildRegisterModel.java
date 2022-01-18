package org.smartregister.brac.hnpp.model;

import android.util.Pair;

import org.json.JSONObject;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.core.model.CoreChildRegisterModel;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.family.util.Utils;

import java.util.ArrayList;

import static org.smartregister.brac.hnpp.utils.HnppJsonFormUtils.makeReadOnlyFields;

public class HnppChildRegisterModel extends CoreChildRegisterModel {
    private String houseHoldId;
    private String familyBaseEntityId;
    public HnppChildRegisterModel( String houseHoldId, String familyBaseEntityId) {
        this.houseHoldId = houseHoldId;
        this.familyBaseEntityId = familyBaseEntityId;
    }
    @Override
    public Pair<Client, Event> processRegistration(String jsonString) {
        return HnppJsonFormUtils.processChildRegistrationForm(Utils.context().allSharedPreferences(), jsonString);
    }

    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId, String familyID) throws Exception {
        JSONObject form = getFormUtils().getFormJson(formName);
        if (form == null) {
            return null;
        }
        if(HnppConstants.isPALogin()){
            makeReadOnlyFields(form);
        }
        ArrayList<String> womenList = HnppDBUtils.getAllWomenInHouseHold(familyBaseEntityId);
        HnppJsonFormUtils.updateFormWithMotherName(form,womenList,familyBaseEntityId);
        HnppJsonFormUtils.updateFormWithMemberId(form,houseHoldId,familyBaseEntityId);
        return HnppJsonFormUtils.updateChildFormWithMetaData(form, houseHoldId,familyBaseEntityId);
    }

}
