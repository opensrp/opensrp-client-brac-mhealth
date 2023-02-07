package org.smartregister.unicef.dghs.model;

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
    private boolean isSelected = true;
    private boolean considerChange = false;
    private boolean isEmpty = false;

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    public boolean isConsiderChange() {
        return considerChange;
    }

    public void setConsiderChange(boolean considerChange) {
        this.considerChange = considerChange;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

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

    @Override
    public String toString() {
        return "Payment{" +
                "serviceType='" + serviceType + '\'' +
                '}';
    }
}
