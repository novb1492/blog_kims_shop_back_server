package com.example.blog_kim_s_token.service.food.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface foodDao extends JpaRepository<foodDto,Integer> {
    
}
