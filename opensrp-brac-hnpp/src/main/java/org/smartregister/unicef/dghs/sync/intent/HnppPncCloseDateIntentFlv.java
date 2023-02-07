package org.smartregister.unicef.dghs.sync.intent;

import org.smartregister.chw.core.intent.CoreChwPncCloseDateIntent;

public class HnppPncCloseDateIntentFlv implements CoreChwPncCloseDateIntent.Flavor {
    @Override
    public int getNumberOfDays() {
        return 41;
    }
}
