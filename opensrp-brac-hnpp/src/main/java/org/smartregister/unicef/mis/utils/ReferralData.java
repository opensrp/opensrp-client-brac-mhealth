package org.smartregister.unicef.mis.utils;

import java.io.Serializable;

public class ReferralData implements Serializable {
    public String baseEntityId;
    public String referralId;
    public String referralCause;
    public String referralPlace;
    public long referralDate;
    public String referralEvent;
    public int referralStatus;
}
