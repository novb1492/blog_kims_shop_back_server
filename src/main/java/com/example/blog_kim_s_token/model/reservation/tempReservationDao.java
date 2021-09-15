package com.example.blog_kim_s_token.model.reservation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface tempReservationDao extends JpaRepository<tempReservationDto,Integer> {
    Optional<List<tempReservationDto>>findByTrPaymentid(String trpayment_id);
}
