package com.example.blog_kim_s_token.service.ApiServies.kakao.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "kakaopay")
@Entity
public class kakaopayDto {
    @Id
    @Column(name="kid",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int kid;

    @Column(name = "kcid",nullable = false)
    private String kcid;

    @Column(name = "ktid",nullable = false)
    private String ktid;

    @Column(name = "ktotal_amount",nullable = false)
    private int ktotal_amount;

    @Column(name = "kpartner_order_id",nullable = false)
    private String kpartner_order_id;

    
    @Column(name = "kpartner_user_id",nullable = false)
    private String kpartner_user_id;

    @Column(name = "ktax_free_amount",nullable = false)
    private String ktax_free_amount;

    @Column(name = "kCreated")
    @CreationTimestamp
    private Timestamp kcreated;    
}
