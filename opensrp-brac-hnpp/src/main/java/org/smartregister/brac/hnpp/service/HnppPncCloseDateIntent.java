package org.smartregister.brac.hnpp.service;

import org.smartregister.brac.hnpp.sync.intent.HnppPncCloseDateIntentFlv;
import org.smartregister.chw.core.intent.CoreChwPncCloseDateIntent;

public class HnppPncCloseDateIntent extends CoreChwPncCloseDateIntent {


    public HnppPncCloseDateIntent() {
        super(new HnppPncCloseDateIntentFlv());
    }
}
