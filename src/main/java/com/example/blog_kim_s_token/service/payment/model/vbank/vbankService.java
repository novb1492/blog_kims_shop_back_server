package com.example.blog_kim_s_token.service.payment.model.vbank;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
            String pktHash=paymentService.requestcancleString(reseponseSettleDto.getMchtTrdNo(),reseponseSettleDto.getTrdAmt(), reseponseSettleDto.getMchtId(),map.get("trdDt"),map.get("trdTm"));
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
    public JSONObject makeReAccountBody(reseponseSettleDto reseponseSettleDto) {
        try {
            Map<String,String>map=utillService.getTrdDtTrdTm();
            String pktHash=paymentService.requestcancleString(reseponseSettleDto.getMchtTrdNo(),reseponseSettleDto.getTrdAmt(), reseponseSettleDto.getMchtId(),map.get("trdDt"),map.get("trdTm"));
            JSONObject body=new JSONObject();
            JSONObject params=new JSONObject();
            JSONObject data=new JSONObject();
            params.put("mchtId", reseponseSettleDto.getMchtId());
            params.put("ver", "0A17");
            params.put("method", "VA");
            params.put("bizType", "A0");
            params.put("encCd", "23");
            params.put("mchtTrdNo", reseponseSettleDto.getMchtTrdNo());
            params.put("trdDt", map.get("trdDt"));
            params.put("trdTm", map.get("trdTm"));
            data.put("pktHash", sha256.encrypt(pktHash));
            data.put("bankCd", reseponseSettleDto.getFnCd());
            data.put("orgTrdNo", reseponseSettleDto.getTrdNo());
            data.put("vAcntNo", aes256.encrypt(reseponseSettleDto.getVtlAcntNo()));
            data.put("acntType", "1");
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
    public String requestcancleString(String mchtTrdNo,String price,String mchtId,String trdDt,String trdTm) {
        System.out.println("requestcancleString");
        String pain=String.format("%s%s%s%s%s%s",trdDt,trdTm,mchtId,mchtTrdNo,price,"ST1009281328226982205"); 
        return  pain;
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
    public void updateVBankPay(int newPrice,String vid,reseponseSettleDto reseponseSettleDto) {
        System.out.println("updateVBankPay");
        try {
            int id=Integer.parseInt(vid);
            System.out.println(id+" 아이디");
            insertvbankDto insertvbankDto=vbankDao.findById(id).orElseThrow(()->new IllegalAccessException("vbank 내역이 존재 하지 않습니다"));
            int cnclOrd=insertvbankDto.getVcnclOrd();
            System.out.println(cnclOrd+"환불횟수"+insertvbankDto.toString());
            cnclOrd+=1;
            if(newPrice>0){
                System.out.println("환불 잔액"+newPrice);
                insertvbankDto.setVcnclOrd(cnclOrd);
                insertvbankDto.setVtrdAmt(Integer.toString(newPrice));
                reseponseSettleDto.setVbankFlag("true");///금액이 남았다면 부분취소
            }else{
                System.out.println("환불 잔액 0"+newPrice);
                reseponseSettleDto.setVbankFlag("false");
                vbankDao.deleteById(id);
            }
            System.out.println(cnclOrd);
            reseponseSettleDto.setCnclOrd(cnclOrd);
            reseponseSettleDto.setRefundBankCd(insertvbankDto.getVfnCd());
            reseponseSettleDto.setRefundAcntNo(insertvbankDto.getVtlAcntNo());
            reseponseSettleDto.setVbankStatus(insertvbankDto.getVbankstatus());
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
    public void getClientInterToDto(getClientInter getClientInter,reseponseSettleDto reseponseSettleDto) {
        System.out.println("getClientInterToDto");
        userDto userDto=userService.sendUserDto();
        reseponseSettleDto.setUserName(userDto.getName());
        reseponseSettleDto.setMchtTrdNo(getClientInter.getVmcht_trd_no());
        reseponseSettleDto.setCnclAmt(getClientInter.getPrice());
        reseponseSettleDto.setMchtId(getClientInter.getVmcht_id());
        reseponseSettleDto.setTrdNo(getClientInter.getVtrd_no());
    }
}
