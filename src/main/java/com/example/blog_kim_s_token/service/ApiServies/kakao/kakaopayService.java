package com.example.blog_kim_s_token.service.ApiServies.kakao;







import com.example.blog_kim_s_token.service.ApiServies.kakao.model.kakaopayDao;
import com.example.blog_kim_s_token.service.ApiServies.kakao.model.kakaopayDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class kakaopayService {

    @Autowired
    private kakaopayDao kakaopayDao;

    public void kakaopayInsert(String cid,String partner_order_id,String partner_user_id,String tax_free_amount,String tid,int total_amount) {
        System.out.println("kakaopayInsert");
        try {
            kakaopayDto dto=kakaopayDto.builder()
                                        .kcid(cid)
                                        .kpartner_order_id(partner_order_id)
                                        .kpartner_user_id(partner_user_id)
                                        .ktax_free_amount(tax_free_amount)
                                        .ktid(tid)
                                        .ktotal_amount(total_amount)
                                        .build();
            kakaopayDao.save(dto);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("kakaopayInsert error"+e.getMessage());
            throw new RuntimeException("카카오페이 저장 실패");
        }
    }

}
