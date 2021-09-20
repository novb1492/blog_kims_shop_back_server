package com.example.blog_kim_s_token.service.payment.model.card;

import java.util.ArrayList;
import java.util.List;
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
                                .CcnclOrd(0)
                                .build();
                                cardDao.save(dto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public JSONObject makecancelBody(reseponseSettleDto reseponseSettleDto) {
        Map<String,String>map=utillService.getTrdDtTrdTm();
        String pktHash=requestcancleString(reseponseSettleDto.getMchtTrdNo(),reseponseSettleDto.getTrdAmt(), reseponseSettleDto.getMchtId(),map.get("trdDt"),map.get("trdTm"));
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
        data.put("cnclOrd", reseponseSettleDto.getCnclOrd());
        data.put("pktHash", sha256.encrypt(pktHash));
        data.put("orgTrdNo", reseponseSettleDto.getTrdNo());
        data.put("crcCd", "KRW");
        data.put("cnclAmt", aes256.encrypt(reseponseSettleDto.getTrdAmt()));
        body.put("params", params);
        body.put("data", data);
       
        return body;
    }
    public void  updateCardPay(int newPrice,String cid,getClientInter cards,reseponseSettleDto reseponseSettleDto) {
        System.out.println("updateCardPay");
        try {
            int id=Integer.parseInt(cards.getCid());
            int cnclOrd=cards.getCcncl_ord();
            int originPrice=cards.getCtrd_amt();
            cnclOrd+=1;
            if(newPrice<originPrice){
                System.out.println("환불 잔액"+newPrice);
                cardDao.updateCardPriceAndCountNative(Integer.toString(originPrice-newPrice), cnclOrd, id);
            }else{
                System.out.println("환불 잔액 0");
                cardDao.deleteById(id);
            }
            System.out.println(cnclOrd+" 취소회차");
            reseponseSettleDto.setCnclOrd(cnclOrd); 
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("updateCardPay error"+e.getMessage());
            throw new RuntimeException("카드 내역 변경 실패");
        }
        
    }
    public void getClientInterToDto(getClientInter getClientInter,reseponseSettleDto reseponseSettleDto,int minusPrice) {
        System.out.println("getClientInterToDto");
        reseponseSettleDto.setMchtTrdNo(getClientInter.getCmcht_trd_no());
        reseponseSettleDto.setTrdAmt(Integer.toString(minusPrice));
        reseponseSettleDto.setMchtId(getClientInter.getCmcht_id());
        reseponseSettleDto.setTrdNo(getClientInter.getCtrd_no());
    }
    public void requestCancleCard(List<getClientInter>cards) {
        System.out.println("requestCancleCard");
        int cardsSize=cards.size();
        int minusPrice=0;
        int nextMinusPrice=0;
        List<reseponseSettleDto>requests=new ArrayList<>();
        for(int i=0;i<cardsSize;i++){
            if(i==0){
                System.out.println("카드 결제 제일 처음분류 ");
                minusPrice+=Integer.parseInt(cards.get(i).getPrice());
                if(i==cardsSize-1){
                   makeReseponseSettleDto( minusPrice, cards.get(i),requests);
                }
            }else if(cards.get(i).getCmcht_trd_no().equals(cards.get(i-1).getCmcht_trd_no())){
                System.out.println("이전번호와 일치함");
                minusPrice+=Integer.parseInt(cards.get(i).getPrice());
                if(i==cardsSize-1){
                    makeReseponseSettleDto( minusPrice, cards.get(i),requests);
                }
            }else if(!cards.get(i).getCmcht_trd_no().equals(cards.get(i-1).getCmcht_trd_no())){
                System.out.println("이전번호와 일치하지 않음");
                nextMinusPrice=Integer.parseInt(cards.get(i).getPrice());
                makeReseponseSettleDto( minusPrice, cards.get(i-1),requests);
                if(i==cardsSize-1){
                    minusPrice=nextMinusPrice;
                    makeReseponseSettleDto( minusPrice, cards.get(i),requests);
                }
                minusPrice=nextMinusPrice;
            }
        }
       for(reseponseSettleDto r: requests){
            System.out.println("카드 결제 취소요청");
            JSONObject body=makecancelBody(r);
            paymentService.requestCancleCard(body);
        }
    }
    private void makeReseponseSettleDto(int minusPrice,getClientInter cards,List<reseponseSettleDto>requests) {
        System.out.println("makeReseponseSettleDto");
        reseponseSettleDto reseponseSettleDto=new reseponseSettleDto();
        updateCardPay(minusPrice, cards.getCid(),cards,reseponseSettleDto);
        getClientInterToDto(cards,reseponseSettleDto,minusPrice);
        requests.add(reseponseSettleDto);
    }
    private String requestcancleString(String mchtTrdNo,String price,String mchtId,String trdDt,String trdTm) {
        System.out.println("requestcancleString");
        return  String.format("%s%s%s%s%s%s",trdDt,trdTm,mchtId,mchtTrdNo,price,"ST1009281328226982205"); 
    }
}
