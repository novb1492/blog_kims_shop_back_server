package com.example.blog_kim_s_token.service.payment.model.vbank;

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
@Table(name = "vbank")
@Entity
public class insertvbankDto {
    
    @Id
    @Column(name="vid",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int vid;

    @Column(name = "vmchtId",nullable = false)
    private String vmchtId;

    
    @Column(name = "vmethod",nullable = false)
    private String vmethod;

    @Column(name = "vmchtTrdNo",nullable = false)
    private String vmchtTrdNo;

    @Column(name = "vtrdNo",nullable = false)
    private String vtrdNo;

    @Column(name = "vtrdAmt",nullable = false)
    private String vtrdAmt;

    @Column(name = "vtlAcntNo",nullable = false)
    private String vtlAcntNo;

    @Column(name = "vfnNm",nullable = false)
    private String vfnNm;

    @Column(name = "vfnCd",nullable = false)
    private String vfnCd; 

    @Column(name = "vexpireDt",nullable = false)
    private Timestamp vexpireDt;

    @Column(name = "created")
    @CreationTimestamp
    private Timestamp created;   
}
