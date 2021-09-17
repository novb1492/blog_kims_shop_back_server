package com.example.blog_kim_s_token.service.payment.model.vbank;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface vbankDao extends JpaRepository<insertvbankDto,Integer> {
    Optional<insertvbankDto>findByVmchtTrdNo(String mchtTrdNo);

    @Modifying
    @Transactional
    @Query(value = "update vbank v set v.vcncl_ord=? where v.vid=?",nativeQuery = true)
    void updateVbankcnclOrdNative(int cnclOrd ,int vid);
}
