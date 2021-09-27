package com.example.blog_kim_s_token.model.article;

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
@Table(name = "article")
@Entity
public class articleDto {

    @Id
    @Column(name="bid",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bid;
    
    @Column(name="title",nullable = false,length = 50)
    private String title;

    @Column(name="textarea",nullable = false,length = 2000)
    private String textarea;

    @Column(name="bemail",nullable = false)
    private String bemail;

    @Column(name="bkind",nullable = false)
    private String bkind;

    @Column(name="bclicked",nullable = false)
    private int bclicked;

    @Column(name="bcreated",nullable = false)
    @CreationTimestamp
    private Timestamp bcreated;
}
