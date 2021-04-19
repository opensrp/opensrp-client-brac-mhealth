package org.smartregister.brac.hnpp.utils;

import java.io.Serializable;

public class PaymentDetails implements Serializable {
    private String serviceType;
    private String serviceCode;
    private int unitPrice;
    private int payFor;

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getPayFor() {
        return payFor;
    }

    public void setPayFor(int payFor) {
        this.payFor = payFor;
    }
}
