package com.example.blog_kim_s_token.service.ApiServies.kakao;







import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.managedblockchain.model.IllegalActionException;
import com.example.blog_kim_s_token.model.reservation.getClientInter;
import com.example.blog_kim_s_token.service.ApiServies.kakao.model.kakaopayDao;
import com.example.blog_kim_s_token.service.ApiServies.kakao.model.kakaopayDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


@Service
public class kakaopayService {

    @Autowired
    private kakaopayDao kakaopayDao;
    @Autowired
    private kakaoService kakaoService;

    @Value("${kakao.kakaoPay.cid}")
    private String cid;

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
    public void deleteByKtid(String ktid) {
        kakaopayDao.deleteByKtid(ktid);
    }
    public void requestCancleToKakaoPay(List<getClientInter>kakaopays) {
        System.out.println("requestCancleToKakaoPay");
        int kakaopaysSize=kakaopays.size();
        int minusPrice=0;
        int nextMinusPrice=0;
        List<MultiValueMap<String,Object>>requests=new ArrayList<>();
        for(int i=0;i<kakaopaysSize;i++){
            if(i==0){
                System.out.println("카카오페이 제일 처음분류 ");
                minusPrice+=Integer.parseInt(kakaopays.get(i).getPrice());
                if(i==kakaopaysSize-1){
                   makeToMap(minusPrice, kakaopays.get(i),requests);
                }
            }else if(kakaopays.get(i).getKtid().equals(kakaopays.get(i-1).getKtid())){
                System.out.println("이전번호와 일치함");
                minusPrice+=Integer.parseInt(kakaopays.get(i).getPrice());
                if(i==kakaopaysSize-1){
                    makeToMap( minusPrice, kakaopays.get(i),requests);
                }
            }else if(!kakaopays.get(i).getKtid().equals(kakaopays.get(i-1).getKtid())){
                System.out.println("이전번호와 일치하지 않음");
                nextMinusPrice=Integer.parseInt(kakaopays.get(i).getPrice());
                makeToMap( minusPrice, kakaopays.get(i-1),requests);
                if(i==kakaopaysSize-1){
                    minusPrice=nextMinusPrice;
                    makeToMap( minusPrice, kakaopays.get(i),requests);
                }
                minusPrice=nextMinusPrice;
            }
        }
        for(MultiValueMap<String,Object> r: requests){
            System.out.println("카카오페이 취소요청"+r.toString());
            kakaoService.cancleKakaopay(r);
        }
    }
    private void makeToMap(int minusPrice,getClientInter kakopay,List<MultiValueMap<String,Object>>requests) {
        System.out.println("requestCancleToKakaoPay");
        MultiValueMap<String,Object> body=new LinkedMultiValueMap<>();
        updateKakaopay(minusPrice,kakopay.getKid());
        makeMap(body, kakopay.getKtid(), minusPrice);
        requests.add(body);
    }
    private void updateKakaopay(int minusPrice,int id) {
        System.out.println("updateKakaopay");
        kakaopayDto kakaopayDto=kakaopayDao.findById(id).orElseThrow(()->new IllegalActionException("카카오페이 내역이 존재 하지 않습니다"));
        int originPrice=kakaopayDto.getKtotal_amount();
        if(minusPrice<originPrice){
            System.out.println("환불 잔액"+minusPrice);
            kakaopayDto.setKtotal_amount(originPrice-minusPrice);
        }else{
            System.out.println("환불 잔액 0");
            kakaopayDao.deleteById(id);
        }
    }
    private void makeMap(MultiValueMap<String,Object> body,String tid,int minusPrice) {
        System.out.println("makeMap");
        body.add("cid", cid);
        body.add("tid", tid);
        body.add("cancel_amount", minusPrice);
        body.add("cancel_tax_free_amount",0);
    }



}
