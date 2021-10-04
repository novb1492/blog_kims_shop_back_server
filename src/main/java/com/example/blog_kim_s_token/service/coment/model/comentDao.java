package com.example.blog_kim_s_token.service.coment.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface comentDao extends JpaRepository<comentDto,Integer> {
    
    @Query(value = "select *,(select count(*)from coment where cbid=?)totalcount from coment where cbid=? order by cid desc limit ?,?",nativeQuery = true)
    Optional<List<getComentInter>>findByBidNative(int bid,int bid2,int first,int pagesize);
}
