package org.smartregister.unicef.dghs.interactor;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;

import org.smartregister.unicef.dghs.widget.HnppDatePickerFactory;
import org.smartregister.unicef.dghs.widget.HnppFingerPrintFactory;
import org.smartregister.unicef.dghs.widget.HnppSectionFactory;
import org.smartregister.unicef.dghs.widget.HnppSpinnerFactory;

public class HnppJsonFormInteractor extends JsonFormInteractor {

    private static final JsonFormInteractor INSTANCE = new HnppJsonFormInteractor();
    private HnppJsonFormInteractor(){
        super();
    }

    @Override
    protected void registerWidgets() {
        super.registerWidgets();
        map.put(JsonFormConstants.DATE_PICKER, new HnppDatePickerFactory());
        map.put(JsonFormConstants.SECTION_LABEL, new HnppSectionFactory());
        map.put(JsonFormConstants.SPINNER, new HnppSpinnerFactory());
        map.put(JsonFormConstants.FINGER_PRINT,new HnppFingerPrintFactory());
    }

    public static JsonFormInteractor getInstance() {
        return INSTANCE;
    }
}
