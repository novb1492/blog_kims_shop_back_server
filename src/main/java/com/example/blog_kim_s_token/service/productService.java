package com.example.blog_kim_s_token.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.example.blog_kim_s_token.enums.priceEnums;
import com.example.blog_kim_s_token.model.product.productDao;
import com.example.blog_kim_s_token.model.product.productDto;
import com.nimbusds.jose.shaded.json.JSONObject;

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
        optional.orElseThrow(()->new IllegalArgumentException("존재하지 않는 상품입니다"));
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
            productDto productDto=productDao.findByProductName(productName).orElseThrow(()->new IllegalArgumentException("존재하지 않는 제품입니다"));
            int originCount=productDto.getCount();
            int newCount=originCount-count;
            if(newCount<=0){
                throw new RuntimeException("재고가 없습니다");
            }
            productDto.setCount(originCount-count);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("minusProductCount error"+ e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    public JSONObject getItems(HttpServletRequest request) {
        System.out.println("getItems");
        try {
            String bigKind=request.getParameter("bigkind");
            String kind=request.getParameter("kind");
            List<productDto>productDtos=productDao.findByBigKindAndKindNative(bigKind, kind).orElseThrow(()->new IllegalArgumentException("존재하지 않는 카테고리입니다"));
            JSONObject jsonObject=new JSONObject();
            List<JSONObject>jsonObjects=new ArrayList<>();
            for(productDto p: productDtos){
                JSONObject data=new JSONObject();
                data.put("productName",p.getProductName());
                data.put("count",p.getCount());
                data.put("price", p.getPrice());
                jsonObjects.add(data);
            }
            jsonObject.put("itmes", jsonObjects);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("getItems error"+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
