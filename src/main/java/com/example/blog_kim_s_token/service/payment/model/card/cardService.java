package com.example.blog_kim_s_token.service.payment.model.card;

import com.example.blog_kim_s_token.model.payment.reseponseSettleDto;
import com.example.blog_kim_s_token.service.hash.aes256;
import com.example.blog_kim_s_token.service.hash.sha256;
import com.example.blog_kim_s_token.service.payment.paymentService;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class cardService {

    @Autowired
    private paymentService paymentService;
    @Autowired
    private sha256 sha256;
    @Autowired
    private cardDao cardDao;
    
    public void insertCard(reseponseSettleDto reseponseSettleDto) {
        System.out.println("insertCard");
        try {
            cardDto dto=cardDto.builder()
                                .cfnNm(reseponseSettleDto.getFnNm())
                                .cmchtId(reseponseSettleDto.getMchtId())
                                .cmchtTrdNo(reseponseSettleDto.getMchtTrdNo())
                                .cmethod(reseponseSettleDto.getMethod())
                                .ctrdAmt(reseponseSettleDto.getTrdAmt())
                                .ctrdNo(reseponseSettleDto.getTrdNo())
                                .build();
                                cardDao.save(dto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public JSONObject makeBody(reseponseSettleDto reseponseSettleDto) {
        String pktHash=paymentService.requestcancleString(reseponseSettleDto.getMchtTrdNo(),reseponseSettleDto.getTrdAmt(), reseponseSettleDto.getMchtId());
        System.out.println(reseponseSettleDto.getTrdAmt());
        JSONObject body=new JSONObject();
        JSONObject body3=new JSONObject();
        JSONObject body4=new JSONObject();
        body3.put("mchtId", reseponseSettleDto.getMchtId());
        body3.put("ver", "0A17");
        body3.put("method", "CA");
        body3.put("bizType", "C0");
        body3.put("encCd", "23");
        body3.put("mchtTrdNo", reseponseSettleDto.getMchtTrdNo());
        body3.put("trdDt", "20210914");
        body3.put("trdTm", "210500");
        body4.put("pktHash", sha256.encrypt(pktHash));
        body4.put("orgTrdNo", reseponseSettleDto.getTrdNo());
        body4.put("crcCd", "KRW");
        body4.put("cnclAmt", aes256.encrypt(reseponseSettleDto.getTrdAmt()));
        body.put("params", body3);
        body.put("data", body4);
        return body;
    }
}
