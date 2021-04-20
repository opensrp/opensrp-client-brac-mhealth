package org.smartregister.brac.hnpp.model;

import java.io.Serializable;

public class Payment implements Serializable {
    int transactionId;
    String serviceType;
    String serviceCode;
    int unitPrice;
    int quantity;
    int total;
    private int payFor;
    private int totalInitialAmount;

    public void setTotalInitialAmount(int totalInitialAmount) {
        this.totalInitialAmount = totalInitialAmount;
    }

    public int getTotalInitialAmount() {
        return totalInitialAmount;
    }

    public void setPayFor(int payFor) {
        this.payFor = payFor;
    }

    public int getPayFor() {
        return payFor;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
