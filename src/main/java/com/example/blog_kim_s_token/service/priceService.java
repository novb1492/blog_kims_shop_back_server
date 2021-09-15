package com.example.blog_kim_s_token.service;

import java.util.Map;
import java.util.Optional;

import javax.management.RuntimeErrorException;

import com.example.blog_kim_s_token.enums.priceEnums;
import com.example.blog_kim_s_token.model.product.getPriceDto;
import com.example.blog_kim_s_token.model.product.productDao;
import com.example.blog_kim_s_token.model.product.productDto;

import com.nimbusds.jose.shaded.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class priceService {
    @Autowired
    private productDao productDao;

    private final int errorPrice=0;
    private final int continuePrice=100;
    
    public productDto selectProduct(String productName) {
        Optional<productDto> optional=productDao.findByProductName(productName);
        optional.orElseThrow(()->new IllegalAccessError("존재하지 않는 상품입니다"));
        return optional.get();
    }
    public int responeTotalprice(String  productName,int count) {
        System.out.println("responeTotalprice");
        productDto productDto=selectProduct(productName);
        priceEnums priceEnums=confrimProduct(productDto, count);
        
        if(priceEnums.gettotalPrice()==errorPrice){
            throw new RuntimeException(priceEnums.getMessege());
        }
        return getTotalPrice(productDto.getPrice(),count);
    }
    private priceEnums confrimProduct(productDto productDto,int count) {
        System.out.println("confrimProduct");
        String messege=null;
        String selectEnum="failConfrimPrice";
        int enumPrice=errorPrice;
        if(count<=0){
            System.out.println("요청 수량이 0임"); 
            messege="요청 수량이 1보다 작습니다";
        }else if(productDto==null){
            System.out.println("존재하지 않는 상품"); 
            messege="존재하지 않는 상품입니다";
        }else if(productDto.getCount()<=0){
            System.out.println("재고가 0"); 
            messege="품절되었습니다";
        }else{
            System.out.println("재고검사 수량검사 상품조회 통과");
            selectEnum="sucConfrimPrice";
            enumPrice=continuePrice;
        }
        priceEnums.valueOf(selectEnum).setMessege(messege);
        priceEnums.valueOf(selectEnum).setPrice(enumPrice);
        return priceEnums.valueOf(selectEnum);
    }
    private int getTotalPrice(int  price, int count) {
        System.out.println("getTotalPrice");
        return price*count;
    }
    public int getTotalPrice(String  productName, int count) {
        System.out.println("getTotalPrice");
        productDto productDto=selectProduct(productName);
        return getTotalPrice(productDto.getPrice(),count);
    }
}
