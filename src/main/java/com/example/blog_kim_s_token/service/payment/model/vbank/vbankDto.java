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
public class vbankDto {
    
    @Id
    @Column(name="vid",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int vid;

    @Column(name = "vorgTrdNo",nullable = false,unique = true)
    private String vorgTrdNo;

    @Column(name = "vemail",nullable = false)
    private String vemail;

    @Column(name = "vname",nullable = false)
    private String vname;

    @Column(name = "vbanktrdAmt",nullable = false)
    private int vbanktrdAmt;

    @Column(name = "vstatus",nullable = false)
    private String vstatus;

    @Column(name = "bank",nullable = false)
    private String bank;

    @Column(name = "bankCode",nullable = false)
    private String bankCode;

    @Column(name = "vkind",nullable = false)
    private String vkind;

    @Column(name = "bankNum",nullable = false)
    private String bankNum;


    @Column(name = "vmchtTrdNo",nullable = false)
    private String vmchtTrdNo;

    @Column(name = "vtrdNo",nullable = false)
    private String vtrdNo;

    @Column(name = "vendDate",nullable = false)
    private Timestamp vendDate; 

    @Column(name = "created")
    @CreationTimestamp
    private Timestamp created;   
}
