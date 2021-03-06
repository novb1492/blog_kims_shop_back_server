package com.example.blog_kim_s_token.service.payment.model.vbank;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import com.example.blog_kim_s_token.service.reservation.reservationService;
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
    @Autowired
    private reservationService reservationService;

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
                                .vcnclOrd(0)///?????? ?????? ??????
                                .vbankstatus(aboutPayEnums.statusReady.getString())
                                .build();
            vbankDao.save(dto);
                                
                                
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("insertVbank error"+e.getMessage());
            throw new RuntimeException("?????? ?????? ?????? ?????? ??????");
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
            System.out.println(trdDtm+" ??????");
            insertvbankDto insertvbankDto=vbankDao.findByVmchtTrdNo(reseponseSettleDto.getMchtTrdNo()).orElseThrow(()->new IllegalArgumentException("vbank??? ????????? ?????? ?????? ????????????"));
            if(insertvbankDto.getVbankstatus().equals(aboutPayEnums.statusReady.getString())){
                System.out.println("vbank ?????????????????? ");
                insertvbankDto.setVbankstatus(aboutPayEnums.statusPaid.getString()); 
                insertvbankDto.setVtrdDtm(Timestamp.valueOf(trdDtm));
            }
       
    }
    public void updateVBankPay(int minusPrice,reseponseSettleDto reseponseSettleDto,getClientInter vbanks) {
        System.out.println("updateVBankPay");
        try {
            int id=Integer.parseInt(vbanks.getVid());
            System.out.println(id+" ?????????");
            int cnclOrd=vbanks.getVcncl_ord();
            int originPrice=Integer.parseInt(vbanks.getVtrd_amt());
            System.out.println(cnclOrd+"????????????");
            cnclOrd+=1;
            if(minusPrice<originPrice){
                System.out.println("?????? ??????"+minusPrice);
                vbankDao.updateVbankcnclOrdAndPriceNative(cnclOrd, originPrice-minusPrice, id);
            }else{
                System.out.println("?????? ?????? 0"+minusPrice);
                vbankDao.deleteById(id);
            }
            System.out.println(cnclOrd);
            reseponseSettleDto.setCnclOrd(cnclOrd);
            reseponseSettleDto.setRefundBankCd(vbanks.getVfn_cd());
            reseponseSettleDto.setRefundAcntNo(vbanks.getVtl_acnt_no());
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("updateCardPay error"+e.getMessage());
            throw new RuntimeException("vbank ?????? ?????? ??????");
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
                System.out.println("????????? vbank ?????? ???????????? ");
                minusPrice+=Integer.parseInt(vbankPaids.get(i).getPrice());
                if(i==vbankPaidsSize-1){
                   makeReseponseSettleDto( minusPrice, vbankPaids.get(i),requests);
                }
            }else if(vbankPaids.get(i).getVmcht_trd_no().equals(vbankPaids.get(i-1).getVmcht_trd_no())){
                System.out.println("??????????????? ?????????");
                minusPrice+=Integer.parseInt(vbankPaids.get(i).getPrice());
                if(i==vbankPaidsSize-1){
                    makeReseponseSettleDto( minusPrice, vbankPaids.get(i),requests);
                }
            }else if(!vbankPaids.get(i).getVmcht_trd_no().equals(vbankPaids.get(i-1).getVmcht_trd_no())){
                System.out.println("??????????????? ???????????? ??????");
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
            System.out.println("vbank ????????????");
            paymentService.requestCanclePaidVbank(r);
        }
    }
    private void makeReseponseSettleDto(int minusPrice,getClientInter vbanks,List<reseponseSettleDto>requests) {
        System.out.println("makeReseponseSettleDto");
        reseponseSettleDto reseponseSettleDto=new reseponseSettleDto();
        updateVBankPay(minusPrice,reseponseSettleDto,vbanks);
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
        List<JSONObject>deleteRequests=new ArrayList<>();
        for(int i=0;i<vbankReadysSize;i++){
            if(i==0){
                System.out.println("????????? vbank ?????? ???????????? ");
                minusPrice+=Integer.parseInt(vbankReadys.get(i).getPrice());
                if(i==vbankReadysSize-1){
                    System.out.println("????????? ??????/???????????? ????????????");
                    deleteOrUpdate(vbankReadys.get(i), minusPrice,deleteRequests);
                }
            }else if(vbankReadys.get(i).getVmcht_trd_no().equals(vbankReadys.get(i-1).getVmcht_trd_no())){
                System.out.println("??????????????? ?????????");
                minusPrice+=Integer.parseInt(vbankReadys.get(i).getPrice());
                if(i==vbankReadysSize-1){
                    deleteOrUpdate(vbankReadys.get(i),minusPrice,deleteRequests);
                }
            }else if(!vbankReadys.get(i).getVmcht_trd_no().equals(vbankReadys.get(i-1).getVmcht_trd_no())){
                System.out.println("??????????????? ???????????? ??????");
                nextMinusPrice=Integer.parseInt(vbankReadys.get(i).getPrice());
                deleteOrUpdate(vbankReadys.get(i-1),minusPrice,deleteRequests);
                if(i==vbankReadysSize-1){
                    minusPrice=nextMinusPrice;
                    deleteOrUpdate(vbankReadys.get(i),minusPrice,deleteRequests);
                }
                minusPrice=nextMinusPrice;
            }
        }
        if(!deleteRequests.isEmpty()){
            System.out.println("???????????? ?????? ?????? ??????");
            for(JSONObject j:deleteRequests){
               paymentService.requestCancleAccount(j);
            }
            System.out.println("???????????? ?????? ?????? ??????");
        }
    }
    private void deleteOrUpdate(getClientInter getClientInter,int minusPrice,List<JSONObject>deleteRequests) {
        System.out.println("deleteOrUpdate");
        int newPrice=minusPrice(Integer.parseInt(getClientInter.getVtrd_amt()),minusPrice);
        System.out.println(newPrice+" ??? ?????? ?????? ??????");
            if(newPrice>0){
                System.out.println("???????????? ???????????? ????????????"+getClientInter.getVid());
                JSONObject body=makeGetReAccountBody(getClientInter,newPrice); 
                JSONObject response =paymentService.requestGetNewAccount(body);
                LinkedHashMap<String,Object>data=(LinkedHashMap<String, Object>)response.get("data");
                LinkedHashMap<String,Object>params=(LinkedHashMap<String, Object>)response.get("params");
                String vAcntNo=paymentService.aesToNomal((String)data.get("vAcntNo"));
                int trdAmt=Integer.parseInt(paymentService.aesToNomal((String)data.get("trdAmt")));
                String trdNo=(String)params.get("trdNo");
                String mchtTrdNo=(String)params.get("mchtTrdNo");
                int vid=Integer.parseInt(getClientInter.getVid());
                System.out.println(vAcntNo+" "+trdAmt+" "+trdNo+" "+vid+" ??????");
                vbankDao.updateVbankvtl_acnt_noAndvmcht_trd_noAndPriceNative(vAcntNo,trdAmt,mchtTrdNo,trdNo,vid);
                if(getClientInter.getSeat()!=null){
                    System.out.println("????????? ???????????? ???????????? ??????????????? ??????");
                    reservationService.updatenewpayment_id((String)params.get("mchtTrdNo"),getClientInter.getVmcht_trd_no());
                }else{
                    System.out.println("????????? ???????????? ???????????? ????????????????????? ??????");
                }
               
                
            }else if(newPrice==0){
                System.out.println("???????????? ?????? ?????? ????????????");
                vbankDao.deleteById(Integer.parseInt(getClientInter.getVid()));
                makeCancleAccountBody(getClientInter,deleteRequests);
            }
    }
    private void makeCancleAccountBody(getClientInter getClientInter,List<JSONObject>deleteRequests) {
        System.out.println("makeCancleAccountBody");
        try {
            Map<String,String>map=utillService.getTrdDtTrdTm();
            String mchtId=getClientInter.getVmcht_id();
            String mchtTrdNo=getClientInter.getVmcht_trd_no();
            String pktHash=requestcancleString(mchtTrdNo,getClientInter.getVtrd_amt(), mchtId,map.get("trdDt"),map.get("trdTm"),"0");
            JSONObject body=new JSONObject();
            JSONObject params=new JSONObject();
            JSONObject data=new JSONObject();
            params.put("mchtId", mchtId);
            params.put("ver", "0A17");
            params.put("method", "VA");
            params.put("bizType", "A2");
            params.put("encCd", "23");
            params.put("mchtTrdNo", mchtTrdNo);
            params.put("trdDt", map.get("trdDt"));
            params.put("trdTm", map.get("trdTm"));
            data.put("pktHash", sha256.encrypt(pktHash));
            data.put("orgTrdNo", getClientInter.getVtrd_no());
            data.put("vAcntNo", aes256.encrypt(getClientInter.getVtl_acnt_no()));
            body.put("params", params);
            body.put("data", data);
            deleteRequests.add(body);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    private JSONObject makeGetReAccountBody(getClientInter getClientInter,int newPrice) {
        System.out.println("makeGetReAccountBody");
        JSONObject body=new JSONObject();
        JSONObject params=new JSONObject();
        JSONObject data=new JSONObject();
        Map<String,String>map=utillService.getTrdDtTrdTm();
        String mchtTrdNo=null;
        String itmeName=null;
        if(getClientInter.getSeat()!=null){
            System.out.println("?????? ?????? ?????? ??????????????? ?????????????????????");
            itmeName=getClientInter.getSeat();
            mchtTrdNo=aboutPayEnums.reservation.getString()+utillService.GetRandomNum(10);
        }else{
            System.out.println("?????? ?????? ?????? ??????????????? ?????????????????????");
        }
        System.out.println(itmeName); 
        userDto userDto=userService.sendUserDto();
        String price=Integer.toString(newPrice);
        String mchtId=getClientInter.getVmcht_id();
        String trdDt=map.get("trdDt");
        String trdTm=map.get("trdTm");
        String username=userDto.getName();
        String expireDate=utillService.replaceDate(getClientInter.getVexpire_dt());
        System.out.println(expireDate+" ???????????????");
        System.out.println(price+" ???????????????");
        try {
        String pktHash=sha256.encrypt(requestPayString(trdDt, trdTm,mchtId,mchtTrdNo,price));
           params.put("mchtId", mchtId);
           params.put("ver", "0A17");
           params.put("method", "VA");
           params.put("bizType", "A0");
           params.put("encCd", "23");
           params.put("mchtTrdNo", mchtTrdNo);
           params.put("trdDt", trdDt);
           params.put("trdTm", trdTm);
           data.put("pktHash", pktHash);
           data.put("bankCd", getClientInter.getVfn_cd());
           data.put("acntType", "1");//?????? ????????? ??????
           data.put("prdtNm", itmeName);
           data.put("sellerNm", "kimsshop");
           data.put("ordNm", username);
           data.put("trdAmt", aes256.encrypt(price));
           data.put("dpstrNm", username);
           data.put("taxTypeCd", "N");
           data.put("escrAgrYn", "N");
           data.put("csrcIssReqYn", "Y");
           data.put("cashRcptPrposDivCd", "0");
           data.put("csrcRegNoDivCd", "4");
           data.put("csrcRegNo", userDto.getPhoneNum());
           data.put("expireDate", expireDate.replaceAll("[:,-,' ']",""));
           data.put("notiUrl", "http://kim80800.iptime.org:8080/auth/settlebank");
           body.put("params", params);
           body.put("data", data);
           
           return body;
            


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("makeGetReAccountBody error"+e.getMessage());
           throw new RuntimeException("????????????  body ????????? ??????");
        }

    }
    private int minusPrice(int originPrice,int minusPrice) {
        System.out.println("minusPrice");
        return originPrice-minusPrice;
    }
    private String requestPayString(String trdDt,String trdTm,String mchtId,String mchtTrdNo,String newPrice) {
        return  String.format("%s%s%s%s%s%s",trdDt,trdTm,mchtId,mchtTrdNo,newPrice,"ST1009281328226982205");
    }

}
