package org.smartregister.brac.hnpp.sync.intent;

import org.smartregister.brac.hnpp.job.HnppPncCloseJob;
import org.smartregister.brac.hnpp.service.HnppPncCloseDateIntent;
import org.smartregister.chw.core.intent.CoreChwPncCloseDateIntent;

public class HnppPncCloseDateIntentFlv implements CoreChwPncCloseDateIntent.Flavor {
    @Override
    public int getNumberOfDays() {
        return 41;
    }
}
