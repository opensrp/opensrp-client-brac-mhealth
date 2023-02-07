package org.smartregister.unicef.dghs.utils;

public class StockDetailsData extends DashBoardData{
    private int monthStartBalance;
    private int newPackage;
    private int sell;
    private long endBalance;

    public int getMonthStartBalance() {
        return monthStartBalance;
    }

    public void setMonthStartBalance(int monthStartBalance) {
        this.monthStartBalance = monthStartBalance;
    }

    public int getNewPackage() {
        return newPackage;
    }

    public void setNewPackage(int newPackage) {
        this.newPackage = newPackage;
    }

    public int getSell() {
        return sell;
    }

    public void setSell(int sell) {
        this.sell = sell;
    }

    public long getEndBalance() {
        return endBalance;
    }

    public void setEndBalance(long endBalance) {
        this.endBalance = endBalance;
    }
}