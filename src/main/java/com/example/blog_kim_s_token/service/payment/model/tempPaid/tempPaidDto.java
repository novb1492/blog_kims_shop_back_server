package com.example.blog_kim_s_token.service.payment.model.tempPaid;

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
@Table(name = "temporder")
@Entity
public class tempPaidDto {
    
    @Id
    @Column(name="tpid",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int tpid;

    @Column(name="tpaymentid",nullable = false)
    private String tpaymentid;

    @Column(name = "tpemail",nullable = false)
    private String tpemail;

    @Column(name = "tpprice",nullable = false)
    private String tpprice;

    @Column(name="tpcreated")
    @CreationTimestamp  
    private Timestamp trcreated;
}
