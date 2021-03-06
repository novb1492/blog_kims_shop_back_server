package com.example.blog_kim_s_token.model.reservation;

import java.sql.Timestamp;

public interface getClientInter {
    //총개수
    int getTotalpage();
    //reservation
    String getId();
    String getSeat();
    Timestamp getCreated();
    Timestamp getDate_and_time();
    String getPayment_id();
    ///product
    String getPrice();
    ///vbank
    String getVid();
    Timestamp getVexpire_dt();
    String getVfn_nm();
    String getVtl_acnt_no();
    String getVtrd_amt();
    String getVbankstatus();
    String getVmcht_trd_no();
    String getVmcht_id();
    String getVtrd_no();
    String getVfn_cd();
    Timestamp getVtrd_dtm();
    int getVcncl_ord();
    //CARD
    String getCid();
    String getCfn_nm();
    String getCmcht_trd_no();
    Timestamp getC_created();
    int getCtrd_amt();
    String getCmcht_id();
    String getCtrd_no();
    int getCcncl_ord();
    //kakaopay
    int getKid();
    String getKtid();
    Timestamp getK_created(); 
    int getKtotal_amount();
    ///일반상품 (아직 없음)




}
