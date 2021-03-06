package com.example.blog_kim_s_token.service.payment;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.example.blog_kim_s_token.customException.failBuyException;
import com.example.blog_kim_s_token.customException.failCancleException;
import com.example.blog_kim_s_token.enums.aboutPayEnums;
import com.example.blog_kim_s_token.model.payment.getHashInfor;
import com.example.blog_kim_s_token.model.payment.reseponseSettleDto;
import com.example.blog_kim_s_token.model.product.productDto;
import com.example.blog_kim_s_token.model.reservation.getClientInter;
import com.example.blog_kim_s_token.model.user.userDto;
import com.example.blog_kim_s_token.service.productService;
import com.example.blog_kim_s_token.service.userService;
import com.example.blog_kim_s_token.service.utillService;
import com.example.blog_kim_s_token.service.ApiServies.kakao.kakaopayService;
import com.example.blog_kim_s_token.service.food.service.foodService;
import com.example.blog_kim_s_token.service.hash.aes256;
import com.example.blog_kim_s_token.service.hash.sha256;
import com.example.blog_kim_s_token.service.payment.model.cancle.tryCancleDto;
import com.example.blog_kim_s_token.service.payment.model.card.cardService;
import com.example.blog_kim_s_token.service.payment.model.tempPaid.tempPaidDto;
import com.example.blog_kim_s_token.service.payment.model.vbank.vbankService;
import com.example.blog_kim_s_token.service.reservation.reservationService;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class paymentService {
    private RestTemplate restTemplate=new RestTemplate();
    private HttpHeaders headers=new HttpHeaders();
    private JSONObject body=new JSONObject();
    private final String reservationString=aboutPayEnums.reservation.getString();
    private final String foodString=aboutPayEnums.food.getString();

    @Autowired
    private productService priceService;
    @Value("${payment.period}")
    private  int period;
    @Value("${payment.minusHour}")
    private  int minusHour;
    @Autowired
    private kakaopayService kakaopayService;
    @Autowired
    private userService userService;
    @Autowired
    private cardService cardService;
    @Autowired
    private vbankService vbankService;
    @Autowired
    private reservationService reservationService;
    @Autowired
    private tempService tempService;
    @Autowired
    private foodService foodService;



    private String  getVbankDate(String kind,int year,int month,int date,List<Integer>times) {
        System.out.println("getVbankDate");
        String expiredDate=null;
        try {
            if(kind.equals(reservationString)){
                Calendar getToday = Calendar.getInstance();
                getToday.setTime(new Date()); 
                String requestDate=year+"-"+month+"-"+date;
                long diffDays = utillService.getDateGap(getToday, requestDate);
                Collections.sort(times);
                int shortestTime=times.get(0);
                checkTime(year,month,date,shortestTime);
                expiredDate=getVbankDate(diffDays, shortestTime, requestDate);
                
            }else{
                expiredDate=getVbankDate();
            }
            return expiredDate;
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("getVbankDate error "+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    public void checkTime(int year,int month,int date,int time) {
        System.out.println("???????????? ?????? ??????" +time);
        LocalDateTime shortestTime=Timestamp.valueOf(year+"-"+month+"-"+date+" "+time+":00:00").toLocalDateTime();
        if(LocalDateTime.now().plusHours(minusHour).isAfter(shortestTime)){
            System.out.println("?????? ?????? ??????????????? ?????? "+minusHour+"???????????????");
            throw new RuntimeException("?????? ?????? ??????????????? ?????? "+minusHour+"???????????????");
        }
    }
    private String getVbankDate(long diffDays,int shortestTime,String requestDate ) {
        System.out.println("getVbankDate");   
        String expiredDate=null;
        String time=null;
        if(diffDays<period){
            System.out.println(shortestTime+" ??????????????????");
            expiredDate=requestDate+" "+(shortestTime-minusHour)+":00:00";
            System.out.println(expiredDate+" ???????????? ??????");
            String[]temp=expiredDate.split(" ");
            time=temp[1];
            temp=temp[0].split("-");
            if(temp[1].length()<2){
                System.out.println("10???????????????");
                temp[1]="0"+temp[1];
            }
            if(temp[2].length()<2){
                System.out.println("10???????????????");
                temp[2]="0"+temp[2];
            }
            String[] splitTime=time.split(":");
            if(splitTime[0].length()<2){
                splitTime[0]="0"+splitTime[0];
            }
            time=splitTime[0]+splitTime[1]+splitTime[2];
            expiredDate=temp[0]+temp[1]+temp[2];
            expiredDate+=time;
 
        }else{
            System.out.println("?????? ????????? "+period+"?????????");
            expiredDate=getVbankDate();
        }
        System.out.println(expiredDate+" ??????"+ time);
        return expiredDate;
    }
    private String getVbankDate() {
        System.out.println("getVbankDate");
        String expiredDate=LocalDateTime.now().plusDays(period).toString();
        expiredDate= expiredDate.replaceAll("[:T-]", "");
        System.out.println(expiredDate+" afterreplace");
        int idx = expiredDate.indexOf("."); 
        System.out.println(idx+" aft");
        expiredDate = expiredDate.substring(0, idx);
        System.out.println(expiredDate+" aft");
        return expiredDate;
    }


   
    public int minusPrice(int totalPrice,int minusPrice) {
        System.out.println("minusPrice");
        int newPrice=totalPrice-minusPrice;
        if(newPrice==0||newPrice>0){
            return newPrice;
        }
        throw new RuntimeException("?????? ????????? ???????????? ?????????");
    }
    public Map<String,Object> getTotalPriceAndOther(String[][] itemArray,String kind) {
        System.out.println("getTotalPriceAndOther");
        int itemArraySize=itemArray.length;
        int totalPrice=0;
        String itemName="";
        int count=0;
        List<Integer>timesOrSize=new ArrayList<>();
        Map<String,Object>result=new HashMap<>();
        for(int i=0;i<itemArraySize;i++){
            totalPrice+=priceService.getTotalPrice(itemArray[i][0],Integer.parseInt(itemArray[i][1]));
            itemName+=itemArray[i][0];
            if(i!=itemArraySize-1){
                itemName+=",";
            }
            count+=Integer.parseInt(itemArray[i][1]);
            if(kind.equals(reservationString)){
                System.out.println("?????? ?????? ????????? ?????? ?????? ??????");
                timesOrSize.add(Integer.parseInt(itemArray[i][2]));
                if(i==itemArraySize-1){
                    System.out.println("?????? ?????? ??????");
                    result.put("timesOrSize", timesOrSize);
                }
            }else if(kind.equals(aboutPayEnums.product.getString())){
                System.out.println("?????? ??????????????? ????????? ????????????");
            }
        }
        result.put("totalPrice", totalPrice);
        result.put("itemName", itemName);
        result.put("count", count);
        return result;
    }
    public void confrimProduct(int count,String productName) {
        System.out.println("confrimProduct");
        String[] splitName=productName.split(",");
        int splitNameSize=splitName.length;
        String messege="????????????";
        for(int i=0;i<splitNameSize;i++){
           productDto productDto=priceService.selectProduct(splitName[i]);
           int remainCount=productDto.getCount();
            if(remainCount<=0||remainCount-count<=0){
               System.out.println("?????? ??????");
               messege="????????? ????????? ???????????? ?????? ????????????"+splitName[i];
               break;
           }else{
               System.out.println("confrimProduct ??????");
               if(i==splitNameSize-1){
                System.out.println("confrimProduct ?????? ??????");
                return;
               }
           }
        }
        throw new RuntimeException(messege);
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject makeTohash(getHashInfor getHashInfor) {
        System.out.println("makeTohash");
        JSONObject response=new JSONObject();
        try {
            String kind=aboutPayEnums.valueOf(getHashInfor.getKind()).getString();
            System.out.println(kind); 
            userDto userDto=userService.sendUserDto();
            String email=userDto.getEmail();
            String price=Integer.toString(priceService.responeTotalprice(getHashInfor.getProductNameAndCount()));
            getHashInfor.setTotalPrice(price);
            Map<String,String>map=utillService.getTrdDtTrdTm();
            String mchtTrdNo=kind+utillService.GetRandomNum(10);
            getHashInfor.setMchtTrdNo(mchtTrdNo);
            getHashInfor.setRequestDate(map.get("trdDt"));
            getHashInfor.setRequestTime(map.get("trdTm"));
            String pktHash=sha256.encrypt(requestPayString(getHashInfor));
            String hashPrice=aes256.encrypt(price);
            String mchtCustId=aes256.encrypt(email);
            tempService.insert(email, mchtTrdNo, price);
            if(getHashInfor.getMchtId().equals(aboutPayEnums.vbankmehthod.getString())){
                System.out.println("???????????? ???????????? ????????????");
                response.put("expiredate",  getVbankDate(kind, getHashInfor.getYear(), getHashInfor.getMonth() , getHashInfor.getDate(), getHashInfor.getTimes()));
            }
            inertTemp(getHashInfor, email, mchtTrdNo, userDto.getName(), mchtCustId, kind);
            response.put("mchtCustId", mchtCustId);
            response.put("mchtTrdNo", mchtTrdNo);
            response.put("trdAmt", hashPrice);
            response.put("trdDt", getHashInfor.getRequestDate());
            response.put("trdTm", getHashInfor.getRequestTime());
            response.put("pktHash", pktHash);
            response.put("bool", true);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("makeTohash error"+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    private String requestPayString(getHashInfor getHashInfor) {
        return  String.format("%s%s%s%s%s%s%s",getHashInfor.getMchtId(),getHashInfor.getMethod(),getHashInfor.getMchtTrdNo(),getHashInfor.getRequestDate(),getHashInfor.getRequestTime(),getHashInfor.getTotalPrice(),"ST1009281328226982205");
    }
    private void inertTemp(getHashInfor getHashInfor,String email,String mchtTrdNo,String name,String mchtCustId,String kind) {
        System.out.println("inertTemp");
        if(kind.equals(reservationString)){
            System.out.println("?????? ?????? ????????? ??????");
            reservationService.insertTemp(getHashInfor,email,mchtTrdNo,name,mchtCustId);
        }else if(kind.equals(aboutPayEnums.food.getString())) {
            System.out.println("?????? ??????");
            foodService.insertTemp(getHashInfor, email, name, mchtTrdNo);
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject confrimSettle(reseponseSettleDto reseponseSettleDto) {
        System.out.println("confrimSettle");
        try {
            String kind="";
            String trdAmt =aesToNomal(reseponseSettleDto.getTrdAmt());
            reseponseSettleDto.setTrdAmt(trdAmt);
            userDto userDto=userService.sendUserDto();
            checkDetails(reseponseSettleDto,userDto.getEmail());
            String mchtTrdNo=reseponseSettleDto.getMchtTrdNo();
            if(reseponseSettleDto.getMchtId().equals(aboutPayEnums.cardmehtod.getString())){
                System.out.println("?????? ?????? ???????????????");
                cardService.insertCard(reseponseSettleDto);
               
            }else if(reseponseSettleDto.getMchtId().equals(aboutPayEnums.vbankmehthod.getString())){
                System.out.println("???????????? ????????????");
                reseponseSettleDto.setVbankStatus(aboutPayEnums.statusReady.getString());
                reseponseSettleDto.setVtlAcntNo(aesToNomal(reseponseSettleDto.getVtlAcntNo()));
                vbankService.insertVbank(reseponseSettleDto);
            }
            if(mchtTrdNo.startsWith(reservationString)){
                System.out.println("?????? ?????? ????????????");
                reservationService.tempToMain(reseponseSettleDto);
                kind=reservationString;
                //throw new Exception("test");
            }else if(mchtTrdNo.startsWith(aboutPayEnums.food.getString())) {
                System.out.println("?????? ?????? ????????????");
                foodService.tempToMain(reseponseSettleDto);
                kind=foodString;
            }
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("bool", true);
            jsonObject.put("message", "?????????????????????");
            jsonObject.put("kind", kind);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("confrimSettle error"+e.getMessage());
            throw new failBuyException(e.getMessage(),reseponseSettleDto);
        }
    }
    public String aesToNomal(String hash) {
        try {
            byte[] aesCipherRaw2=aes256.decodeBase64(hash);
            return new String(aes256.aes256DecryptEcb(aesCipherRaw2),"UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("????????? ??????");
        }
    }
    private void checkDetails(reseponseSettleDto reseponseSettleDto,String email) {
        System.out.println("checkDetails");
        String message=null;
        String mchtTrdNo=reseponseSettleDto.getMchtTrdNo();
        tempPaidDto tempPaidDto=tempService.selectByMchtTrdNo(mchtTrdNo);
        if(!tempPaidDto.getTpemail().equals(email)){
            System.out.println("???????????? ??????");
            message="???????????? ??????";
        }else if(!tempPaidDto.getTpprice().equals(reseponseSettleDto.getTrdAmt())){
            System.out.println("??????????????? ????????????");
            message="??????????????? ????????????";
        }else{
            System.out.println("???????????? ???????????? ??????");
            tempService.deleteByMchtTrdNo(mchtTrdNo);
            System.out.println("?????? ????????? ????????????");
            return;
        }
        throw new RuntimeException(message);
    
    }
    private JSONObject requestToSettle(String url) {
        System.out.println("reuqestToSettle");
        try {
            headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.set("charset", "UTF-8");
      
            HttpEntity<JSONObject>entity=new HttpEntity<>(body,headers);
            System.out.println(entity.getBody()+" ????????????"+entity.getHeaders());
            JSONObject response= restTemplate.postForObject(url,entity,JSONObject.class);
            System.out.println(response+" ???????????? ????????????");
            showResponse(response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("requestToSettle error "+ e.getMessage());
            throw new RuntimeException("???????????? ?????? ??????");
        }finally{
            body.clear();
            headers.clear();
        }
    }
    private void showResponse(JSONObject response) {
        LinkedHashMap<String,Object> data=(LinkedHashMap<String,Object>) response.get("data");
        System.out.println(data+" ???????????? ????????????");
        if(data.get("cnclAmt")!=null){
            System.out.println("?????? ??????"+aesToNomal((String)data.get("cnclAmt")));
            System.out.println("?????? ?????? ??????"+aesToNomal((String)data.get("blcAmt")));
        }
        LinkedHashMap<String,Object> params=(LinkedHashMap<String,Object>) response.get("params");
        if(params.get("outStatCd").equals("0031")){
            System.out.println("?????? ?????? 0031 ");
            throw new RuntimeException((String) params.get("outRsltMsg"));
        }
    }
    public void tryUpdateVbank(reseponseSettleDto reseponseSettleDto) {
        System.out.println("tryUpdateVbank");
        if(reseponseSettleDto.getMchtId().equals(aboutPayEnums.vbankmehthod.getString())&&reseponseSettleDto.getOutStatCd().equals("0021")){
            System.out.println("vbank ????????? ???????????????");
            vbankService.okVank(reseponseSettleDto);
        }else{
            System.out.println("vbank??? ???????????? ????????????");
        }
       
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject cancel(tryCancleDto tryCancleDto ) {
        System.out.println("cancle");
        try {
            List<Integer>cancleIds=tryCancleDto.getIds();
            List<getClientInter>clientInters=new ArrayList<>();
            String kind=aboutPayEnums.valueOf(tryCancleDto.getKind()).getString();
            if(kind.equals(reservationString)){
                System.out.println("?????? ?????? ????????? ??????");
                clientInters=reservationService.deleteReservationDb(cancleIds);
            }else if(kind.equals(aboutPayEnums.product.getString())){
                System.out.println("?????? ?????? ????????????");
            }
            deletePaymentDb(clientInters);
            return utillService.makeJson(true, "?????? ???????????????");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("cancel error"+e.getMessage());
            throw new failCancleException(e.getMessage());
        }
    }
    private void deletePaymentDb(List<getClientInter>clientInters ) {
        System.out.println("deleteDb");
        List<getClientInter>cards=new ArrayList<>();
        List<getClientInter>vbankPaids=new ArrayList<>();
        List<getClientInter>vbankReadys=new ArrayList<>();
        List<getClientInter>kakaopays=new ArrayList<>();
        try {
            for(getClientInter g:clientInters){
                if(g.getCid()!=null){
                    System.out.println("?????? ?????? ?????? ?????? ?????? ???????????? ????????????");
                    cards.add(g);
                }else if(g.getVid()!=null&&g.getVbankstatus().equals(aboutPayEnums.statusPaid.getString())){
                    System.out.println("??????????????? vbank ?????? ???????????? ????????????");
                    vbankPaids.add(g);
                }else if(g.getVid()!=null&&g.getVbankstatus().equals(aboutPayEnums.statusReady.getString())){
                    System.out.println("???????????? vbank ?????? ???????????? ????????????");
                    vbankReadys.add(g);
                }else if(g.getKtid()!=null){
                    System.out.println("??????????????? ?????? ???????????? ????????????");
                    kakaopays.add(g);
                }
            }
            if(!cards.isEmpty()){
                System.out.println("?????? ?????? ?????? ?????? ?????? ??????");
                cardService.requestCancleCard(cards);
            }
            if(!vbankPaids.isEmpty()){
                System.out.println("??????????????? vbank ?????? ?????? ?????? ?????? ??????");
                vbankService.requqestCanclePaidVbank(vbankPaids);
            }
            if(!vbankReadys.isEmpty()){
                System.out.println("????????? vbank ?????? ?????? ?????? ?????? ??????");
                vbankService.reGetAccount(vbankReadys);
            }
            if(!kakaopays.isEmpty()){
                System.out.println("??????????????? ?????? ?????? ?????? ?????? ??????");
                kakaopayService.requestCancleToKakaoPay(kakaopays);
            }      
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("deleteDb error"+e.getMessage());
            throw new RuntimeException("?????? ????????? ?????? ??????");
        }
     
    }
    public void requestCancleCard(JSONObject body) {
        System.out.println("requestCancleCard");
        this.body=body;
        String url="https://tbgw.settlebank.co.kr/spay/APICancel.do";
        requestToSettle(url);
    }
    public void requestCanclePaidVbank(reseponseSettleDto reseponseSettleDto) {
        System.out.println("requestCanclePaidVbank");
        this.body=vbankService.makeCancleBody(reseponseSettleDto);
        String url="https://tbgw.settlebank.co.kr/spay/APIRefund.do";  
        requestToSettle(url);
    }
    public JSONObject requestGetNewAccount(JSONObject body) {
        System.out.println("requestGetNewAccount");
        this.body=body;
        String url="https://tbgw.settlebank.co.kr/spay/APIVBank.do";  
        return requestToSettle(url);
    }
    public JSONObject requestCancleAccount(JSONObject body) {
        System.out.println("requestCancleAccount");
        this.body=body;
        String url="https://tbgw.settlebank.co.kr/spay/APIVBank.do";  
        return requestToSettle(url);
    }
    public void requestCancle(reseponseSettleDto reseponseSettleDto) {
        System.out.println("requestCancle");
        String url=null;
        reseponseSettleDto.setCnclOrd(1);
        if(reseponseSettleDto.getMchtId().equals(aboutPayEnums.cardmehtod.getString())){
            System.out.println("???????????? ??????");
            this.body=cardService.makecancelBody(reseponseSettleDto);
            url="https://tbgw.settlebank.co.kr/spay/APICancel.do";
        }else if(reseponseSettleDto.getMchtId().equals(aboutPayEnums.vbankmehthod.getString())){
                System.out.println("???????????? ????????????");
                this.body=vbankService.makeCancleAccountBody(reseponseSettleDto);
                url="https://tbgw.settlebank.co.kr/spay/APIVBank.do";
        }
        requestToSettle(url);
    }


  


}
