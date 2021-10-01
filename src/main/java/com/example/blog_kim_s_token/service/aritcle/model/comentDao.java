package com.example.blog_kim_s_token.service.aritcle.model;

import com.example.blog_kim_s_token.service.coment.model.comentDto;

import org.springframework.data.jpa.repository.JpaRepository;

public interface comentDao extends JpaRepository<comentDto,Integer> {
    
}
