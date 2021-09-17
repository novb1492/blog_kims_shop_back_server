package com.example.blog_kim_s_token.service.payment.model.card;

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
@Table(name = "card")
@Entity
public class cardDto {
    @Id
    @Column(name="cid",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cid;

    @Column(name = "cmchtId",nullable = false)
    private String cmchtId;

    @Column(name = "cmethod",nullable = false)
    private String cmethod;

    @Column(name = "cmchtTrdNo",nullable = false)
    private String cmchtTrdNo;

    @Column(name = "CcnclOrd")
    private int CcnclOrd;

    @Column(name = "ctrdNo",nullable = false)
    private String ctrdNo;

    @Column(name = "ctrdAmt",nullable = false)
    private String ctrdAmt;

    @Column(name = "cfnNm",nullable = false)
    private String cfnNm;

    @Column(name = "cCreated")
    @CreationTimestamp
    private Timestamp created;    
}
