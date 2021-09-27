package com.example.blog_kim_s_token.model.article;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface articleDao extends JpaRepository<articleDto,Integer> {
  
    @Query(value = "select * from article order by bid desc limit ?,?",nativeQuery = true)
    Optional<List<articleDto>>findALLOrderByDescBidLimiteNative(int first,int end);

    @Query(value = "select * from article where title=%?% order by bid desc limit ?,?",nativeQuery = true)
    Optional<List<articleDto>>findByTitleOrderByDescBidLimiteNative(String title,int first,int end);

    @Query(value = "select count(*) from article",nativeQuery = true)
    int countAllNative();
}
