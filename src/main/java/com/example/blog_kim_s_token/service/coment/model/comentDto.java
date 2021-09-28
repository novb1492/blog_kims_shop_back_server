package com.example.blog_kim_s_token.service.coment.model;

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
@Table(name = "coment")
@Entity
public class comentDto {
    
    @Id
    @Column(name="cid",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cid;
    
    @Column(name="coment",nullable = false,length = 100)
    private String coment;

    @Column(name="cemail",nullable = false)
    private String cemail;

    @Column(name="cbid",nullable = false)
    private int cbid;

    @Column(name="c_created")
    @CreationTimestamp
    private Timestamp c_created;
}
