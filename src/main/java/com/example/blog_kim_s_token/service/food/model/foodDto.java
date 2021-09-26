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
@Table(name = "food")
@Entity
public class foodDto {
    @Id
    @Column(name="fid",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int fid;

    @Column(name="femail",nullable = false)
    private String femail;

    @Column(name = "fname",nullable = false)
    private String fname;

    @Column(name = "food_name",nullable = false)
    private String food_name;

    @Column(name="f_count",nullable = false)
    private int f_count;

    @Column(name="fpayment_id",nullable = false)
    private String fpayment_id;

    @Column(name="fcreated")
    @CreationTimestamp  
    private Timestamp fcreated;
}
