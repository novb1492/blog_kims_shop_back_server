package com.example.blog_kim_s_token.service.ApiServies.kakao.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface kakaopayDao extends JpaRepository<kakaopayDto,Integer> {
    void deleteByKtid(String ktid);
}
