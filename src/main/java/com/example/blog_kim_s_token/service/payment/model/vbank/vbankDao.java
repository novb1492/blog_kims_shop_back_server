package com.example.blog_kim_s_token.service.payment.model.vbank;

import java.sql.Timestamp;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface vbankDao extends JpaRepository<insertvbankDto,Integer> {
    Optional<insertvbankDto>findByVmchtTrdNo(String mchtTrdNo);

    @Modifying
    @Transactional
    @Query(value = "update vbank v set v.vcncl_ord=?,v.vtrd_amt=? where v.vid=?",nativeQuery = true)
    void updateVbankcnclOrdAndPriceNative(int cnclOrd,int vtrd_amt ,int vid);


    @Modifying
    @Transactional
    @Query(value = "update vbank v set v.vtl_acnt_no=?,v.vtrd_amt=?,v.vmcht_trd_no=?,v.vtrd_no =? where v.vid=?",nativeQuery = true)
    void updateVbankvtl_acnt_noAndvmcht_trd_noAndPriceNative(String vtl_acnt_no,int vtrd_amt,String vmcht_trd_no,String vtrd_no  ,int vid);

    @Modifying
    @Transactional
    @Query(value = "delete r,v,f from reservation r left join vbank v on r.payment_id=v.vmcht_trd_no left join food f on v.vmcht_trd_no=f.fpayment_id where v.vbankstatus='ready' and v.vexpire_dt<?",nativeQuery = true)
    void deleteJoinReservationReady(Timestamp today);
}
