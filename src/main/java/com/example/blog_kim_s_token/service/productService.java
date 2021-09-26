package com.example.blog_kim_s_token.service;


import java.util.Optional;

import com.amazonaws.services.managedblockchain.model.IllegalActionException;
import com.example.blog_kim_s_token.enums.priceEnums;
import com.example.blog_kim_s_token.model.product.productDao;
import com.example.blog_kim_s_token.model.product.productDto;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class productService {
    @Autowired
    private productDao productDao;

    private final int errorPrice=0;
    private final int continuePrice=100;
    
    public productDto selectProduct(String productName) {
        Optional<productDto> optional=productDao.findByProductName(productName);
        optional.orElseThrow(()->new IllegalAccessError("존재하지 않는 상품입니다"));
        return optional.get();
    }
    public int responeTotalprice(String[][] productNameAndCount) {
        System.out.println("responeTotalprice");
        int totalPrice=0;
        int count=0;
        for(int i=0;i<productNameAndCount.length;i++){
                System.out.println(productNameAndCount[i][0]+" 상품이름");
                productDto productDto=selectProduct(productNameAndCount[i][0]);
                count=Integer.parseInt(productNameAndCount[i][1]);
                priceEnums priceEnums=confrimProduct(productDto,count);
                if(priceEnums.gettotalPrice()==errorPrice){
                    throw new RuntimeException(priceEnums.getMessege());
                }
                totalPrice+=getTotalPrice(productDto.getPrice(),count);
        }
        return totalPrice;
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
    public void minusProductCount(String productName,int count) {
        System.out.println("minusProductCount");
        try {
            productDto productDto=productDao.findByProductName(productName).orElseThrow(()->new IllegalAccessException("존재하지 않는 제품입니다"));
            int originCount=productDto.getCount();
            productDto.setCount(originCount-count);
        }catch(IllegalAccessException e){
            e.printStackTrace();
            System.out.println("minusProductCount error"+ e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("minusProductCount error"+ e.getMessage());
            throw new RuntimeException("재고가 부족합니다");
        }
    }

}
