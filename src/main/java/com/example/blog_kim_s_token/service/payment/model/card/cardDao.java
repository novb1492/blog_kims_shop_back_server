package com.example.blog_kim_s_token.service.payment.model.card;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface cardDao extends JpaRepository<cardDto,Integer> {
    Optional<cardDto> findByCmchtId(String cmcht_trd_no);
}
