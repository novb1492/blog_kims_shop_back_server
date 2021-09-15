package com.example.blog_kim_s_token.service.payment.model.vbank;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface vbankDao extends JpaRepository<insertvbankDto,Integer> {
    Optional<insertvbankDto>findByVmchtTrdNo(String mchtTrdNo);
}
