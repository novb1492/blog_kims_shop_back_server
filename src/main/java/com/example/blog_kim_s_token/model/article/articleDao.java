package com.example.blog_kim_s_token.model.article;

import java.util.List;
import java.util.Optional;

import com.example.blog_kim_s_token.service.aritcle.getAllArticleinter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;


public interface articleDao extends JpaRepository<articleDto,Integer> {
  
    @Query(value = "select a.* ,(select count(*) from article)totalcount from article a order by a.bid desc limit ?,?",nativeQuery = true)
    Optional<List<getAllArticleinter>>findALLOrderByDescBidLimiteNative(int first,int end);

    @Query(value = "select *,(select count(*) from article where title like %?%)totalcount from article a where a.title like %?% order by a.bid desc limit ?,?",nativeQuery = true)
    Optional<List<getAllArticleinter>>findByTitleOrderByDescBidLimiteNative(String title2,String title,int first,int end);

    @Query(value = "select a.*,c.*,(select count(*)from coment where cbid=?)totalcount from article a left join coment c on a.bid=c.cbid where a.bid=? order by c.cid desc limit ?,?",nativeQuery = true)
    Optional<List<getArticleInter>>findByBidJoinComment(int bid,int bid2,int first,int pagesize);

    @Modifying
    @Transactional
    @Query(value = "update article set bclicked=? where bid=?",nativeQuery = true)
    void plusClicked(int upClick,int bid);

}
