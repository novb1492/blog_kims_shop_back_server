package com.example.blog_kim_s_token.service.payment.model.card;

import java.util.Map;

import com.example.blog_kim_s_token.model.payment.reseponseSettleDto;
import com.example.blog_kim_s_token.service.utillService;
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
        Map<String,String>map=utillService.getTrdDtTrdTm();
        String pktHash=paymentService.requestcancleString(reseponseSettleDto.getMchtTrdNo(),reseponseSettleDto.getTrdAmt(), reseponseSettleDto.getMchtId(),map.get("trdDt"),map.get("trdTm"));
        System.out.println(reseponseSettleDto.getTrdAmt());
        JSONObject body=new JSONObject();
        JSONObject params=new JSONObject();
        JSONObject data=new JSONObject();
        params.put("mchtId", reseponseSettleDto.getMchtId());
        params.put("ver", "0A17");
        params.put("method", "CA");
        params.put("bizType", "C0");
        params.put("encCd", "23");
        params.put("mchtTrdNo", reseponseSettleDto.getMchtTrdNo());
        params.put("trdDt", map.get("trdDt"));
        params.put("trdTm",map.get("trdTm"));
        data.put("pktHash", sha256.encrypt(pktHash));
        data.put("orgTrdNo", reseponseSettleDto.getTrdNo());
        data.put("crcCd", "KRW");
        data.put("cnclAmt", aes256.encrypt(reseponseSettleDto.getTrdAmt()));
        body.put("params", params);
        body.put("data", data);
        return body;
    }
}
