package com.example.blog_kim_s_token.service.payment;

import com.example.blog_kim_s_token.service.payment.model.tempPaid.tempPaidDao;
import com.example.blog_kim_s_token.service.payment.model.tempPaid.tempPaidDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class tempService {

    @Autowired
    private tempPaidDao tempPaidDao;

    public void insert(String email,String mchtTrdNo,String price) {
        System.out.println("insert");
        tempPaidDto dto=tempPaidDto.builder()
                        .tpemail(email)
                        .tpaymentid(mchtTrdNo)
                        .tpprice(price)
                        .build();
        tempPaidDao.save(dto);
    }
    public tempPaidDto selectByMchtTrdNo(String mchtTrdNo) {
        System.out.println("selectByMchtTrdNo");
        return tempPaidDao.findByTpaymentid(mchtTrdNo).orElseThrow(()->new RuntimeException("결제 요청 정보가 없습니다"));
    }
    public void deleteByMchtTrdNo(String mchtTrdNo) {
        System.out.println("deleteByMchtTrdNo");
        tempPaidDao.deleteByTpaymentid(mchtTrdNo);
    }
}
