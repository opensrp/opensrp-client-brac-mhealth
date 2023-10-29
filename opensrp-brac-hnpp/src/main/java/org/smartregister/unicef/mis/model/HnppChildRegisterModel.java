package org.smartregister.unicef.mis.model;

import android.text.TextUtils;
import android.util.Pair;

import org.json.JSONObject;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppJsonFormUtils;
import org.smartregister.chw.core.model.CoreChildRegisterModel;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.family.util.Utils;

import static org.smartregister.unicef.mis.utils.HnppJsonFormUtils.makeReadOnlyFields;

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
        JSONObject form = HnppJsonFormUtils.getJsonObject(formName);;
        if (form == null || TextUtils.isEmpty(familyBaseEntityId)) {
            return null;
        }
        if(HnppConstants.isPALogin()){
            makeReadOnlyFields(form);
        }
        HnppJsonFormUtils.updateFormWithBlockInfo(form,familyBaseEntityId);
        HnppJsonFormUtils.updateFormWithMemberId(form,houseHoldId,familyBaseEntityId);
        return HnppJsonFormUtils.updateChildFormWithMetaData(form, houseHoldId,familyBaseEntityId);
    }

}
