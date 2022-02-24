package org.smartregister.brac.hnpp.model;

import org.joda.time.DateTime;
import org.smartregister.domain.db.Client;
import org.smartregister.domain.db.Event;

public class InvalidDataModel {
    public String baseEntityId;
    public String firstName;
    public String formSubmissionId;
    public String address;
    public String eventType;
    public String errorCause;
    public String unique_id;
    public Client client;
    public Event event;
    public DateTime date;
    public long serverVersion;
}
