package com.example.blog_kim_s_token.service.payment.model.vbank;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Map;

import com.example.blog_kim_s_token.enums.aboutPayEnums;
import com.example.blog_kim_s_token.model.payment.reseponseSettleDto;
import com.example.blog_kim_s_token.service.utillService;
import com.example.blog_kim_s_token.service.hash.aes256;
import com.example.blog_kim_s_token.service.hash.sha256;
import com.example.blog_kim_s_token.service.payment.paymentService;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class vbankService {
    @Autowired
    private paymentService paymentService;
    @Autowired
    private vbankDao vbankDao;

    public void insertVbank(reseponseSettleDto reseponseSettleDto) {
        System.out.println("insertVbank");

        SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            String  expire = newDtFormat.format(dtFormat.parse(reseponseSettleDto.getExpireDt()));
            insertvbankDto dto=insertvbankDto.builder()
                                .vexpireDt(Timestamp.valueOf(expire))
                                .vfnCd(reseponseSettleDto.getFnCd())
                                .vfnNm(reseponseSettleDto.getFnNm())
                                .vmchtId(reseponseSettleDto.getMchtId())
                                .vmchtTrdNo(reseponseSettleDto.getMchtTrdNo())
                                .vmethod(reseponseSettleDto.getMethod())
                                .vtlAcntNo(reseponseSettleDto.getVtlAcntNo())
                                .vtrdAmt(reseponseSettleDto.getTrdAmt())
                                .vtrdNo(reseponseSettleDto.getTrdNo())
                                .vbankstatus(aboutPayEnums.statusReady.getString())
                                .build();
            vbankDao.save(dto);
                                
                                
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("insertVbank error"+e.getMessage());
            throw new RuntimeException("가상 계좌 정보 저장 실패");
        }
    }
    public JSONObject makeBody(reseponseSettleDto reseponseSettleDto) {
        try {
            Map<String,String>map=utillService.getTrdDtTrdTm();
            String pktHash=paymentService.requestcancleString(reseponseSettleDto.getMchtTrdNo(),reseponseSettleDto.getTrdAmt(), reseponseSettleDto.getMchtId(),map.get("trdDt"),map.get("trdTm"));
            System.out.println(reseponseSettleDto.getVtlAcntNo());
            JSONObject body=new JSONObject();
            JSONObject params=new JSONObject();
            JSONObject data=new JSONObject();
            params.put("mchtId", reseponseSettleDto.getMchtId());
            params.put("ver", "0A17");
            params.put("method", "VA");
            params.put("bizType", "A2");
            params.put("encCd", "23");
            params.put("mchtTrdNo", reseponseSettleDto.getMchtTrdNo());
            params.put("trdDt", map.get("trdDt"));
            params.put("trdTm", map.get("trdTm"));
            data.put("pktHash", sha256.encrypt(pktHash));
            data.put("orgTrdNo", reseponseSettleDto.getTrdNo());
            data.put("vAcntNo", aes256.encrypt(reseponseSettleDto.getVtlAcntNo()));
            body.put("params", params);
            body.put("data", data);
        return body;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void okVank(reseponseSettleDto reseponseSettleDto) {
        System.out.println("okVank");
        try {
            insertvbankDto insertvbankDto=vbankDao.findByVmchtTrdNo(reseponseSettleDto.getMchtTrdNo()).orElseThrow(()->new IllegalAccessError("vbank에 내역이 존재 하지 않습니다"));
            insertvbankDto.setVbankstatus(aboutPayEnums.statusPaid.getString()); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
