package org.smartregister.unicef.mis.model;

import java.io.Serializable;

public class PaymentHistory implements Serializable {
    String paymentId;
    String paymentDate;
    String serviceType;
    String price;
    String status;
    long paymentTimestamp;
    int quantity;

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setPaymentTimestamp(long paymentTimestamp) {
        this.paymentTimestamp = paymentTimestamp;
    }

    public long getPaymentTimestamp() {
        return paymentTimestamp;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
