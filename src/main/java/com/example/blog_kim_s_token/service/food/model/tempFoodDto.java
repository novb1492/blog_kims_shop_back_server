package com.example.blog_kim_s_token.service.food.model;

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
@Table(name = "tempfood")
@Entity
public class tempFoodDto {
    
    @Id
    @Column(name="tfid",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int tfid;

    @Column(name="tfemail",nullable = false)
    private String tfemail;

    @Column(name = "tfname",nullable = false)
    private String fname;

    @Column(name = "tfood_name",nullable = false)
    private String tfood_name;

    @Column(name="tf_count",nullable = false)
    private int tf_count;

    @Column(name="tfpayment_id",nullable = false)
    private String tfpaymentid;

    @Column(name="tfcreated")
    @CreationTimestamp  
    private Timestamp tfcreated;
}
