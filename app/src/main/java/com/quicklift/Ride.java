package com.quicklift;

/**
 * Created by pandey on 11/2/18.
 */

public class Ride {
    String customerid,source,destination,amount,time,driver,status,cancelledby,discount,cancel_charge,paymode,parking,seat;
    String waiting,timing;

    public Ride() {

    }

    public String getStatus() {
        return status;
    }

    public String getWaiting() {
        return waiting;
    }

    public void setWaiting(String waiting) {
        this.waiting = waiting;
    }

    public String getTiming() {
        return timing;
    }

    public void setTiming(String timing) {
        this.timing = timing;
    }

    public String getCancelledby() {
        return cancelledby;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getParking() {
        return parking;
    }

    public void setParking(String parking) {
        this.parking = parking;
    }

    public String getCancel_charge() {
        return cancel_charge;
    }

    public void setCancel_charge(String cancel_charge) {
        this.cancel_charge = cancel_charge;
    }

    public String getPaymode() {
        return paymode;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public void setPaymode(String paymode) {
        this.paymode = paymode;
    }

    public void setCancelledby(String cancelledby) {
        this.cancelledby = cancelledby;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerid() {
        return customerid;
    }

    public void setCustomerid(String customerid) {
        this.customerid = customerid;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }
}
