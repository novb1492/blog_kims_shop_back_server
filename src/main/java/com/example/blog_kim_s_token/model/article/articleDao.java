package com.example.blog_kim_s_token.model.article;

import java.util.List;
import java.util.Optional;

import com.example.blog_kim_s_token.service.aritcle.getAllArticleinter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface articleDao extends JpaRepository<articleDto,Integer> {
  
    @Query(value = "select a.* ,(select count(*) from article)totalcount from article a order by a.bid desc limit ?,?",nativeQuery = true)
    Optional<List<getAllArticleinter>>findALLOrderByDescBidLimiteNative(int first,int end);

    @Query(value = "select *,(select count(*) from article)totalcount from article a where a.title like %?% order by a.bid desc limit ?,?",nativeQuery = true)
    Optional<List<getAllArticleinter>>findByTitleOrderByDescBidLimiteNative(String title,int first,int end);


}
