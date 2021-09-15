package com.example.blog_kim_s_token.model.payment;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class getHashInfor {

    @NotBlank(message = "제품명이 없습니다")
    private String productName;

    @Min(value = 0,message = "수량이 0보다 작습니다")
    private int count;

    @NotBlank(message = "mchtId가 공백입니다")
    private String mchtId;

    @NotBlank(message = "method가 공백입니다")
    private String method;

    @NotBlank(message = "kind가 공백입니다")
    private String kind;
    //결제정보만들때 쓰는것
    private String seat;
    private int month;
    private int date;
    private int year;
    private String status;
    private List<Integer> times;
    private String totalPrice;
    //응답할때 쓰는것
    private String mchtTrdNo;
    private String requestDate;
    private String requestTime;
    
    

}
