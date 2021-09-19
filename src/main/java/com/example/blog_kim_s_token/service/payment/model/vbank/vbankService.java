package com.example.blog_kim_s_token.service.payment.model.vbank;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.blog_kim_s_token.enums.aboutPayEnums;
import com.example.blog_kim_s_token.model.payment.reseponseSettleDto;
import com.example.blog_kim_s_token.model.reservation.getClientInter;
import com.example.blog_kim_s_token.model.user.userDto;
import com.example.blog_kim_s_token.service.userService;
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
    @Autowired
    private userService userService;

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
                                .vcnclOrd(0)///취소 요청 횟수
                                .vbankstatus(aboutPayEnums.statusReady.getString())
                                .build();
            vbankDao.save(dto);
                                
                                
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("insertVbank error"+e.getMessage());
            throw new RuntimeException("가상 계좌 정보 저장 실패");
        }
    }
    public JSONObject makeCancleAccountBody(reseponseSettleDto reseponseSettleDto) {
        try {
            Map<String,String>map=utillService.getTrdDtTrdTm();
            String pktHash=requestcancleString(reseponseSettleDto.getMchtTrdNo(),reseponseSettleDto.getTrdAmt(), reseponseSettleDto.getMchtId(),map.get("trdDt"),map.get("trdTm"),"0");
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
    public JSONObject makeCancleBody(reseponseSettleDto reseponseSettleDto) {
        System.out.println("makeCancleBody");
        try {
            Map<String,String>map=utillService.getTrdDtTrdTm();
            String pktHash=requestcancleString(reseponseSettleDto.getMchtTrdNo(),reseponseSettleDto.getCnclAmt(), reseponseSettleDto.getMchtId(),map.get("trdDt"),map.get("trdTm"));
            JSONObject body=new JSONObject();
            JSONObject params=new JSONObject();
            JSONObject data=new JSONObject();
            params.put("mchtId", reseponseSettleDto.getMchtId());
            params.put("ver", "0A17");
            params.put("method", "VA");
            params.put("bizType", "C0");
            params.put("encCd", "23");
            params.put("mchtTrdNo", reseponseSettleDto.getMchtTrdNo());
            params.put("trdDt", map.get("trdDt"));
            params.put("trdTm", map.get("trdTm"));
            data.put("pktHash", sha256.encrypt(pktHash));
            data.put("orgTrdNo", reseponseSettleDto.getTrdNo());
            data.put("crcCd","KRW");
            data.put("cnclOrd",reseponseSettleDto.getCnclOrd());
            data.put("cnclAmt",aes256.encrypt(reseponseSettleDto.getCnclAmt()));
            data.put("refundBankCd",reseponseSettleDto.getRefundBankCd());
            data.put("refundAcntNo",aes256.encrypt(reseponseSettleDto.getRefundAcntNo()));
            data.put("refundDpstrNm",reseponseSettleDto.getUserName());
            body.put("params", params);
            body.put("data", data);
        return body;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    private String requestcancleString(String mchtTrdNo,String price,String mchtId,String trdDt,String trdTm) {
        System.out.println("requestcancleString");
        return  String.format("%s%s%s%s%s%s",trdDt,trdTm,mchtId,mchtTrdNo,price,"ST1009281328226982205"); 
    }
    private String requestcancleString(String mchtTrdNo,String price,String mchtId,String trdDt,String trdTm,String zero) {
        System.out.println("requestcancleString zero");
        return String.format("%s%s%s%s%s%s",trdDt,trdTm,mchtId,mchtTrdNo,zero,"ST1009281328226982205"); 
    }
    @Transactional
    public void okVank(reseponseSettleDto reseponseSettleDto) {
        System.out.println("okVank");
            String trdDtm=reseponseSettleDto.getTrdDtm();
            trdDtm=trdDtm.substring(0, 4)+"-"+trdDtm.substring(4, 6)+"-"+trdDtm.substring(6, 8)+" "+trdDtm.substring(8,10)+":"+trdDtm.substring(10,12)+":"+trdDtm.substring(12,14);
            System.out.println(trdDtm+" 날짜");
            insertvbankDto insertvbankDto=vbankDao.findByVmchtTrdNo(reseponseSettleDto.getMchtTrdNo()).orElseThrow(()->new IllegalAccessError("vbank에 내역이 존재 하지 않습니다"));
            if(insertvbankDto.getVbankstatus().equals(aboutPayEnums.statusReady.getString())){
                System.out.println("vbank 입금확인처리 ");
                insertvbankDto.setVbankstatus(aboutPayEnums.statusPaid.getString()); 
                insertvbankDto.setVtrdDtm(Timestamp.valueOf(trdDtm));
            }
       
    }
    public void updateVBankPay(int minusPrice,String vid,reseponseSettleDto reseponseSettleDto) {
        System.out.println("updateVBankPay");
        try {
            int id=Integer.parseInt(vid);
            System.out.println(id+" 아이디");
            insertvbankDto insertvbankDto=vbankDao.findById(id).orElseThrow(()->new IllegalAccessException("vbank 내역이 존재 하지 않습니다"));
            int cnclOrd=insertvbankDto.getVcnclOrd();
            String vbankStatus=insertvbankDto.getVbankstatus();//입금여부
            int originPrice=Integer.parseInt(insertvbankDto.getVtrdAmt());
            System.out.println(cnclOrd+"환불횟수"+insertvbankDto.toString());
            cnclOrd+=1;
            if(minusPrice<originPrice){
                System.out.println("환불 잔액"+minusPrice);
                insertvbankDto.setVcnclOrd(cnclOrd);
                insertvbankDto.setVtrdAmt(Integer.toString(originPrice-minusPrice));
            }else{
                System.out.println("환불 잔액 0"+minusPrice);
                vbankDao.deleteById(id);
            }
            System.out.println(cnclOrd);
            reseponseSettleDto.setCnclOrd(cnclOrd);
            reseponseSettleDto.setRefundBankCd(insertvbankDto.getVfnCd());
            reseponseSettleDto.setRefundAcntNo(insertvbankDto.getVtlAcntNo());
            reseponseSettleDto.setVbankStatus(vbankStatus);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.out.println("updateCardPay error"+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("updateCardPay error"+e.getMessage());
            throw new RuntimeException("vbank 내역 변경 실패");
        }
    }
    public void requqestCanclePaidVbank(List<getClientInter>vbankPaids) {
        System.out.println("requqestCanclePaidVbank");
        int vbankPaidsSize=vbankPaids.size();
        int minusPrice=0;
        int nextMinusPrice=0;
        List<reseponseSettleDto>requests=new ArrayList<>();
        for(int i=0;i<vbankPaidsSize;i++){
            if(i==0){
                System.out.println("결제된 vbank 제일 처음분류 ");
                minusPrice+=Integer.parseInt(vbankPaids.get(i).getPrice());
                if(i==vbankPaidsSize-1){
                   makeReseponseSettleDto( minusPrice, vbankPaids.get(i),requests);
                }
            }else if(vbankPaids.get(i).getVmcht_trd_no().equals(vbankPaids.get(i-1).getVmcht_trd_no())){
                System.out.println("이전번호와 일치함");
                minusPrice+=Integer.parseInt(vbankPaids.get(i).getPrice());
                if(i==vbankPaidsSize-1){
                    makeReseponseSettleDto( minusPrice, vbankPaids.get(i),requests);
                }
            }else if(!vbankPaids.get(i).getVmcht_trd_no().equals(vbankPaids.get(i-1).getVmcht_trd_no())){
                System.out.println("이전번호와 일치하지 않음");
                nextMinusPrice=Integer.parseInt(vbankPaids.get(i).getPrice());
                makeReseponseSettleDto( minusPrice, vbankPaids.get(i-1),requests);
                if(i==vbankPaidsSize-1){
                    minusPrice=nextMinusPrice;
                    makeReseponseSettleDto( minusPrice, vbankPaids.get(i),requests);
                }
                minusPrice=nextMinusPrice;
            }
        }
        for(reseponseSettleDto r: requests){
            System.out.println("vbank 취소요청");
            paymentService.requestCanclePaidVbank(r);
        }
    }
    private void makeReseponseSettleDto(int minusPrice,getClientInter vbanks,List<reseponseSettleDto>requests) {
        System.out.println("makeReseponseSettleDto");
        reseponseSettleDto reseponseSettleDto=new reseponseSettleDto();
        updateVBankPay(minusPrice, vbanks.getVid(),reseponseSettleDto);
        getClientInterToDto(vbanks,reseponseSettleDto,minusPrice);
        requests.add(reseponseSettleDto);
    }
    public void getClientInterToDto(getClientInter getClientInter,reseponseSettleDto reseponseSettleDto,int minusPrice) {
        System.out.println("getClientInterToDto");
        userDto userDto=userService.sendUserDto();
        reseponseSettleDto.setUserName(userDto.getName());
        reseponseSettleDto.setMchtTrdNo(getClientInter.getVmcht_trd_no());
        reseponseSettleDto.setCnclAmt(Integer.toString(minusPrice));
        reseponseSettleDto.setMchtId(getClientInter.getVmcht_id());
        reseponseSettleDto.setTrdNo(getClientInter.getVtrd_no());
    }
    public void reGetAccount(List<getClientInter>vbankReadys) {
        System.out.println("reGetAccount");
        int vbankReadysSize=vbankReadys.size();
        int minusPrice=0;
        int nextMinusPrice=0;
        List<reseponseSettleDto>requests=new ArrayList<>();
        for(int i=0;i<vbankReadysSize;i++){
            if(i==0){
                System.out.println("결제된 vbank 제일 처음분류 ");
                minusPrice+=Integer.parseInt(vbankReadys.get(i).getPrice());
                if(i==vbankReadysSize-1){
                   makeReseponseSettleDto( minusPrice, vbankReadys.get(i),requests);
                }
            }else if(vbankReadys.get(i).getVmcht_trd_no().equals(vbankReadys.get(i-1).getVmcht_trd_no())){
                System.out.println("이전번호와 일치함");
                minusPrice+=Integer.parseInt(vbankReadys.get(i).getPrice());
                if(i==vbankReadysSize-1){
                    makeReseponseSettleDto( minusPrice, vbankReadys.get(i),requests);
                }
            }else if(!vbankReadys.get(i).getVmcht_trd_no().equals(vbankReadys.get(i-1).getVmcht_trd_no())){
                System.out.println("이전번호와 일치하지 않음");
                nextMinusPrice=Integer.parseInt(vbankReadys.get(i).getPrice());
                makeReseponseSettleDto( minusPrice, vbankReadys.get(i-1),requests);
                if(i==vbankReadysSize-1){
                    minusPrice=nextMinusPrice;
                    makeReseponseSettleDto( minusPrice, vbankReadys.get(i),requests);
                }
                minusPrice=nextMinusPrice;
            }
        }
        for(reseponseSettleDto r: requests){
            System.out.println("vbank 취소요청");
            paymentService.requestCanclePaidVbank(r);
        }
    }

}
