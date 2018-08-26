package com.quicklift;

/**
 * Created by adarsh on 5/1/18.
 */

public class Data {
    double st_lat,st_lng,en_lat,en_lng,d_lat,d_lng;
    String customer_id,source,destination,price,otp,seat,offer_upto="0",offer_value="0",offer_disc="0",paymode="Cash",cancel_charge="0",veh_type,parking_price="0";
    String waitcharge="0",triptime="0",timecharge="0",waittime="0",version="5",request_time="",offer_code="";
    Integer accept=0;

    public Data() {
    }

    public String getOffer_value() {
        return offer_value;
    }

    public void setOffer_value(String offer_value) {
        this.offer_value = offer_value;
    }

    public String getOffer_code() {
        return offer_code;
    }

    public void setOffer_code(String offer_code) {
        this.offer_code = offer_code;
    }

    public String getRequest_time() {
        return request_time;
    }

    public void setRequest_time(String request_time) {
        this.request_time = request_time;
    }

    public String getOtp() {
        return otp;
    }

    public String getWaitcharge() {
        return waitcharge;
    }

    public String getTriptime() {
        return triptime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getWaittime() {
        return waittime;
    }

    public void setWaittime(String waittime) {
        this.waittime = waittime;
    }

    public String getTimecharge() {
        return timecharge;
    }

    public void setTimecharge(String timecharge) {
        this.timecharge = timecharge;
    }

    public void setTriptime(String triptime) {
        this.triptime = triptime;
    }

    public void setWaitcharge(String waitcharge) {
        this.waitcharge = waitcharge;
    }

    public String getParking_price() {
        return parking_price;
    }

    public void setParking_price(String parking_price) {
        this.parking_price = parking_price;
    }

    public String getOffer_upto() {
        return offer_upto;
    }

    public void setOffer_upto(String offer_upto) {
        this.offer_upto = offer_upto;
    }

    public String getOffer_disc() {
        return offer_disc;
    }

    public void setOffer_disc(String offer_disc) {
        this.offer_disc = offer_disc;
    }

    public String getPaymode() {
        return paymode;
    }

    public void setPaymode(String paymode) {
        this.paymode = paymode;
    }

    public String getCancel_charge() {
        return cancel_charge;
    }

    public void setCancel_charge(String cancel_charge) {
        this.cancel_charge = cancel_charge;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public String getSource() {
        return source;
    }

    public String getVeh_type() {
        return veh_type;
    }

    public void setVeh_type(String veh_type) {
        this.veh_type = veh_type;
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Integer getAccept() {
        return accept;
    }

    public void setAccept(Integer accept) {
        this.accept = accept;
    }

    public double getSt_lat() {
        return st_lat;
    }

    public double getD_lat() {
        return d_lat;
    }

    public void setD_lat(double d_lat) {
        this.d_lat = d_lat;
    }

    public double getD_lng() {
        return d_lng;
    }

    public void setD_lng(double d_lng) {
        this.d_lng = d_lng;
    }

    public void setSt_lat(double st_lat) {
        this.st_lat = st_lat;
    }

    public double getSt_lng() {
        return st_lng;
    }

    public void setSt_lng(double st_lng) {
        this.st_lng = st_lng;
    }

    public double getEn_lat() {
        return en_lat;
    }

    public void setEn_lat(double en_lat) {
        this.en_lat = en_lat;
    }

    public double getEn_lng() {
        return en_lng;
    }

    public void setEn_lng(double en_lng) {
        this.en_lng = en_lng;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }
}
