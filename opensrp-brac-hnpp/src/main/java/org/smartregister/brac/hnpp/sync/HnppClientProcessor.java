package org.smartregister.brac.hnpp.sync;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.core.sync.ChwClientProcessor;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.domain.db.Event;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.jsonmapping.ClientClassification;
import org.smartregister.domain.jsonmapping.Table;
import org.smartregister.immunization.service.intent.RecurringIntentService;
import org.smartregister.immunization.service.intent.VaccineIntentService;

public class HnppClientProcessor extends ChwClientProcessor {
    public HnppClientProcessor(Context context) {
        super(context);
    }

    @Override
    protected void processEvents(ClientClassification clientClassification, Table vaccineTable, Table serviceTable, EventClient eventClient, Event event, String eventType) throws Exception {
        switch (eventType) {
            case HnppConstants.EVENT_TYPE.ELCO:
            case HnppConstants.EVENT_TYPE.MEMBER_REFERRAL:
            case HnppConstants.EVENT_TYPE.GIRL_PACKAGE:
            case HnppConstants.EVENT_TYPE.WOMEN_PACKAGE:
            case HnppConstants.EVENT_TYPE.NCD_PACKAGE:
            case HnppConstants.EVENT_TYPE.IYCF_PACKAGE:
                if (eventClient.getEvent() == null) {
                    return;
                }
                processVisitEvent(eventClient);
                processEvent(eventClient.getEvent(), eventClient.getClient(), clientClassification);
                break;
                default:
                    super.processEvents(clientClassification,vaccineTable,serviceTable,eventClient,event,eventType);
                    break;


        }
    }

    @Override
    protected void processAncHomeVisit(EventClient baseEvent, SQLiteDatabase database, String parentEventType) {
        super.processAncHomeVisit(baseEvent, database, parentEventType);
        //start log table job
        VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
    }
}
