package com.example.blog_kim_s_token.model.reservation;

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

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name="tempreservation")
@Entity
public class tempReservationDto {
    
    @Id
    @Column(name="trid",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int trid;

    @Column(name="trSeat",nullable = false)
    private String trSeat;

    @Column(name = "trEmail",nullable = false)
    private String trEmail;

    @Column(name = "trName",nullable = false)
    private String trName;

    @Column(name="trTime")
    private int trTime;

    @Column(name="trPaymentid")
    private String trPaymentid;

    @Column(name = "trStatus",nullable = false)
    private String trstatus;

    @Column(name="trDateAndTime")
    private Timestamp trDateAndTime;

    @Column(name="trCreated")
    @CreationTimestamp  
    private Timestamp trCreated;

}
