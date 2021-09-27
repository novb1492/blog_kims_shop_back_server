package com.example.blog_kim_s_token.model.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface productDao extends JpaRepository<productDto,Integer> {
    Optional<productDto> findByProductName(String productName);

    @Query(value = "select *from product where big_kind=? and kind=? ",nativeQuery = true)
    Optional<List<productDto>> findByBigKindAndKindNative(String bigKind,String kind);
}
