package com.example.blog_kim_s_token.model.reservation;

import java.sql.Timestamp;

public interface getClientInter {
    //reservation
    int getId();
    String getSeat();
    Timestamp getCreated();
    Timestamp getDate_and_time();
    String getPayment_id();
    ///product
    int getPrice();
    ///vbank
    String getVid();
    Timestamp getVexpire_dt();
    String getVfn_nm();
    String getVtl_acnt_no();
    String getVtrd_amt();
    String getVbankstatus();
    //CARD
    String getCid();
    String getCfn_nm();
    Timestamp getC_created();




}
