package com.example.blog_kim_s_token.service.food.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface tempFoodDao extends JpaRepository<tempFoodDto,Integer> {
   Optional<List<tempFoodDto>> findByTfpaymentid(String mchtTrdNo);
}
