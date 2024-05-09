package org.smartregister.unicef.mis.model;

import org.json.JSONObject;
import org.smartregister.chw.anc.model.BaseAncRegisterModel;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.unicef.mis.utils.HnppJsonFormUtils;

public class HNPPANCRegisterModel extends BaseAncRegisterModel {
    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        JSONObject jsonObject = HnppJsonFormUtils.getJsonObject(formName);
        JsonFormUtils.getRegistrationForm(jsonObject, entityId, currentLocationId);
        return jsonObject;
    }
}
