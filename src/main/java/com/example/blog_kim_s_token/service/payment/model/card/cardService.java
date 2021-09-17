package com.example.blog_kim_s_token.service.payment.model.card;

import java.util.Map;

import com.example.blog_kim_s_token.model.payment.reseponseSettleDto;
import com.example.blog_kim_s_token.model.reservation.getClientInter;
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
    public JSONObject makecancelBody(reseponseSettleDto reseponseSettleDto) {
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
    public void updateCardPay(int newPrice,String cid) {
        System.out.println("updateCardPay");
        try {
            int id=Integer.parseInt(cid);
            if(newPrice>0){
                System.out.println("환불 잔액"+newPrice);
                cardDto cardDto=cardDao.findById(id).orElseThrow(()->new IllegalAccessException("카드 내역이 존재 하지 않습니다"));
                cardDto.setCtrdAmt(Integer.toString(newPrice));
                return;
            }
            System.out.println("환불 잔액 0"+newPrice);
            cardDao.deleteById(id);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.out.println("updateCardPay error"+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("updateCardPay error"+e.getMessage());
            throw new RuntimeException("카드 내역 변경 실패");
        }
        
    }
    public reseponseSettleDto getClientInterToDto(getClientInter getClientInter) {
        System.out.println("getClientInterToDto");
        reseponseSettleDto dto=new reseponseSettleDto();
        dto.setMchtTrdNo(getClientInter.getCmcht_trd_no());
        dto.setTrdAmt(getClientInter.getPrice());
        dto.setMchtId(getClientInter.getCmcht_id());
        dto.setTrdNo(getClientInter.getCtrd_no());
        return dto;
    }
}
