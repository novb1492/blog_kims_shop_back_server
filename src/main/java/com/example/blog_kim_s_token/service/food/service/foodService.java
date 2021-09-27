package com.example.blog_kim_s_token.service.food.service;

import java.util.List;

import com.example.blog_kim_s_token.model.payment.getHashInfor;
import com.example.blog_kim_s_token.model.payment.reseponseSettleDto;
import com.example.blog_kim_s_token.service.productService;
import com.example.blog_kim_s_token.service.food.model.foodDao;
import com.example.blog_kim_s_token.service.food.model.foodDto;
import com.example.blog_kim_s_token.service.food.model.tempFoodDao;
import com.example.blog_kim_s_token.service.food.model.tempFoodDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class foodService {
    @Autowired
    private foodDao foodDao;
    @Autowired
    private tempFoodDao tempFoodDao;
    @Autowired
    private productService productService;
    
    public void insertTemp(getHashInfor getHashInfor,String email,String name,String mchtTrdNo) {
        System.out.println("insertTemp");
        try {
            String[][] productNameAndCount =getHashInfor.getProductNameAndCount();
            for(String[] s:productNameAndCount ){
                tempFoodDto dto=tempFoodDto.builder()
                                            .fname(name)
                                            .tf_count(Integer.parseInt(s[1]))
                                            .tfemail(email)
                                            .tfood_name(s[0])
                                            .tfpaymentid(mchtTrdNo)
                                            .build();
                                            tempFoodDao.save(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("insertTemp error"+e.getMessage());
            throw new RuntimeException("음식 임시 테이블 저장실패");
        }
    }
    public void tempToMain(reseponseSettleDto reseponseSettleDto) {
        System.out.println("insertMain");
        try {
            List<tempFoodDto> tempFoodDtos=tempFoodDao.findByTfpaymentid(reseponseSettleDto.getMchtTrdNo()).orElseThrow(()->new IllegalArgumentException("임시 음식테이블에서 찾을 수없습니다"));
            for(tempFoodDto f:tempFoodDtos){
                productService.minusProductCount(f.getTfood_name(), f.getTf_count());
                foodDto dto=foodDto.builder()
                                    .f_count(f.getTf_count())
                                    .femail(f.getTfemail())
                                    .fname(f.getFname())
                                    .food_name(f.getTfood_name())
                                    .fpayment_id(f.getTfpaymentid())
                                    .build();
                                    foodDao.save(dto);
            }
        }catch (RuntimeException e) {
            e.printStackTrace();
            System.out.println("insertMain error "+e.getMessage());
            throw new RuntimeException(e.getMessage());
         }catch (Exception e) {
           e.printStackTrace();
           System.out.println("insertMain error "+e.getMessage());
           throw new RuntimeException("음식테이블 저장 실패");
        }
    }
}
