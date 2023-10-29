//package org.smartregister.unicef.dghs.lookup;
//
//import android.content.Context;
//import android.util.Log;
//import android.view.View;
//import android.widget.ImageView;
//
//import com.rengwuxian.materialedittext.MaterialEditText;
//import com.vijay.jsonwizard.fragments.JsonFormFragment;
//import com.vijay.jsonwizard.widgets.EditTextFactory;
//
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class HnppEditTextFactory extends EditTextFactory {
//    @Override
//    protected void attachLayout(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, MaterialEditText editText, ImageView editButton) throws Exception {
//        super.attachLayout(stepName, context, formFragment, jsonObject, editText, editButton);
//        if (jsonObject.has("look_up") && jsonObject.get("look_up").toString().equalsIgnoreCase(Boolean.TRUE.toString())) {
//            String entityId = jsonObject.getString("key");
//            Log.v("attachLayout","entityId>>"+entityId);
//            if (jsonObject.has("entity_id")) {
//                entityId = jsonObject.getString("entity_id");
//            }
//
//            Map<String, List<View>> lookupMap = formFragment.getLookUpMap();
//            List<View> lookUpViews = new ArrayList<>();
//            if (lookupMap.containsKey(entityId)) {
//                lookUpViews = lookupMap.get(entityId);
//            }
//
//            if (!lookUpViews.contains(editText)) {
//                lookUpViews.add(editText);
//            }
//            lookupMap.put(entityId, lookUpViews);
//
//            editText.addTextChangedListener(new LookUpTextWatcher(formFragment, editText, entityId));
//            editText.setTag(com.vijay.jsonwizard.R.id.after_look_up, false);
//        }
//
//    }
//}
