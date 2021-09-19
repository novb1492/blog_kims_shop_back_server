package com.example.blog_kim_s_token.service.payment.model.card;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface cardDao extends JpaRepository<cardDto,Integer> {
    Optional<cardDto> findByCmchtId(String cmcht_trd_no);

    @Modifying
    @Transactional
    @Query(value = "update card c set c.ctrd_amt=?,c.ccncl_ord=? where c.cid=?",nativeQuery = true)
    void updateCardPriceAndCountNative(String ctrd_amt,int ccncl_ord,int cid);
}
