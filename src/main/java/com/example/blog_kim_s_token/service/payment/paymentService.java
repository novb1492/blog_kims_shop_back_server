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
import com.example.blog_kim_s_token.service.priceService;
import com.example.blog_kim_s_token.service.userService;
import com.example.blog_kim_s_token.service.utillService;
import com.example.blog_kim_s_token.service.ApiServies.kakao.kakaoService;
import com.example.blog_kim_s_token.service.hash.aes256;
import com.example.blog_kim_s_token.service.hash.sha256;
import com.example.blog_kim_s_token.service.payment.model.cancle.tryCancleDto;
import com.example.blog_kim_s_token.service.payment.model.card.cardService;
import com.example.blog_kim_s_token.service.payment.model.tempPaid.tempPaidDto;
import com.example.blog_kim_s_token.service.payment.model.vbank.insertvbankDto;
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

    @Autowired
    private priceService priceService;
    @Value("${payment.period}")
    private  int period;
    @Value("${payment.minusHour}")
    private  int minusHour;
    @Autowired
    private kakaoService kakaoService;
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



    private String  getVbankDate(String kind,int year,int month,int date,List<Integer>times) {
        System.out.println("getVbankDate");
        String expiredDate=null;
        try {
            if(kind.equals(aboutPayEnums.reservation.getString())){
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
        System.out.println("가상계좌 시간 검증" +time);
        LocalDateTime shortestTime=Timestamp.valueOf(year+"-"+month+"-"+date+" "+time+":00:00").toLocalDateTime();
        if(LocalDateTime.now().plusHours(minusHour).isAfter(shortestTime)){
            System.out.println("가상 계좌 제한시간은 최대 "+minusHour+"시간입니다");
            throw new RuntimeException("가상 계좌 제한시간은 최대 "+minusHour+"시간입니다");
        }
    }
    private String getVbankDate(long diffDays,int shortestTime,String requestDate ) {
        System.out.println("getVbankDate");   
        String expiredDate=null;
        String time=null;
        if(diffDays<period){
            System.out.println(shortestTime+" 가장작은시간");
            expiredDate=requestDate+" "+(shortestTime-minusHour)+":00:00";
            System.out.println(expiredDate+" 새로만든 기한");
            String[]temp=expiredDate.split(" ");
            time=temp[1];
            temp=temp[0].split("-");
            if(temp[1].length()<2){
                System.out.println("10월보다작음");
                temp[1]="0"+temp[1];
            }
            if(temp[2].length()<2){
                System.out.println("10일보다작음");
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
            System.out.println("예약 일자가 "+period+"이상임");
            expiredDate=getVbankDate();
        }
        System.out.println(expiredDate+" 최종"+ time);
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
        throw new RuntimeException("환불 잔액이 총액보다 큽니다");
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
            if(kind.equals(aboutPayEnums.reservation.getString())){
                System.out.println("예약 상품 입니다 시간 분리 시작");
                timesOrSize.add(Integer.parseInt(itemArray[i][2]));
                if(i==itemArraySize-1){
                    System.out.println("시간 분리 완료");
                    result.put("timesOrSize", timesOrSize);
                }
            }else if(kind.equals(aboutPayEnums.product.getString())){
                System.out.println("일반 상품입니다 사이즈 분리시작");
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
        String messege="검증실패";
        for(int i=0;i<splitNameSize;i++){
           productDto productDto=priceService.selectProduct(splitName[i]);
           int remainCount=productDto.getCount();
            if(remainCount<=0||remainCount-count<=0){
               System.out.println("재고 부족");
               messege="재고가 없거나 요청수량 보다 적습니다"+splitName[i];
               break;
           }else{
               System.out.println("confrimProduct 통과");
               if(i==splitNameSize-1){
                System.out.println("confrimProduct 완전 통과");
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
                System.out.println("가상계좌 입금기한 생성시도");
                response.put("expiredate",  getVbankDate(kind, getHashInfor.getYear(), getHashInfor.getMonth() , getHashInfor.getDate(), getHashInfor.getTimes()));
            }
            if(kind.equals(aboutPayEnums.reservation.getString())){
                System.out.println("예약 임시 테이블 저장");
                reservationService.insertTemp(getHashInfor,email,mchtTrdNo,userDto.getName(),mchtCustId);
            }else {
                System.out.println("일반상품");
            }
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
    @Transactional(rollbackFor = Exception.class)
    public JSONObject confrimSettle(reseponseSettleDto reseponseSettleDto) {
        System.out.println("confrimSettle");
        try {
            String trdAmt =aesToNomal(reseponseSettleDto.getTrdAmt());
            reseponseSettleDto.setTrdAmt(trdAmt);
            userDto userDto=userService.sendUserDto();
            checkDetails(reseponseSettleDto,userDto.getEmail());
            if(reseponseSettleDto.getMchtId().equals(aboutPayEnums.cardmehtod.getString())){
                System.out.println("카드 결제 상품입니다");
                cardService.insertCard(reseponseSettleDto);
               
            }else if(reseponseSettleDto.getMchtId().equals(aboutPayEnums.vbankmehthod.getString())){
                System.out.println("가상계좌 채번완료");
                reseponseSettleDto.setVbankStatus(aboutPayEnums.statusReady.getString());
                reseponseSettleDto.setVbankFlag("false");
                reseponseSettleDto.setVtlAcntNo(aesToNomal(reseponseSettleDto.getVtlAcntNo()));
                vbankService.insertVbank(reseponseSettleDto);
            }
            if(reseponseSettleDto.getMchtTrdNo().startsWith(aboutPayEnums.reservation.getString())){
                System.out.println("예약 상품 검증완료");
                reservationService.tempToMain(reseponseSettleDto);
            }else {
                System.out.println("일반 상품 검증완료");
            }
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("bool", true);
            jsonObject.put("messege", "완료되었습니다");
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("confrimSettle error"+e.getMessage());
            throw new failBuyException(e.getMessage(),reseponseSettleDto);
        }
    }
    private String aesToNomal(String hash) {
        try {
            byte[] aesCipherRaw2=aes256.decodeBase64(hash);
            return new String(aes256.aes256DecryptEcb(aesCipherRaw2),"UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("복호화 실패");
        }
    }
    private void checkDetails(reseponseSettleDto reseponseSettleDto,String email) {
        System.out.println("checkDetails");
        String message=null;
        String mchtTrdNo=reseponseSettleDto.getMchtTrdNo();
        tempPaidDto tempPaidDto=tempService.selectByMchtTrdNo(mchtTrdNo);
        if(!tempPaidDto.getTpemail().equals(email)){
            System.out.println("이메일이 다름");
            message="이메일이 다름";
        }else if(!tempPaidDto.getTpprice().equals(reseponseSettleDto.getTrdAmt())){
            System.out.println("결제금액이 다릅니다");
            message="결제금액이 다릅니다";
        }else{
            System.out.println("세틀뱅크 결제검증 통과");
            tempService.deleteByMchtTrdNo(mchtTrdNo);
            System.out.println("임시 테이블 삭제완료");
            return;
        }
        throw new RuntimeException(message);
    
    }
    public String requestcancleString(String mchtTrdNo,String price,String mchtId,String trdDt,String trdTm) {
        String pain=null;
        if(mchtId.equals(aboutPayEnums.vbankmehthod.getString())){
            System.out.println("가상계좌  plain생성");
            pain=String.format("%s%s%s%s%s%s",trdDt,trdTm,mchtId,mchtTrdNo,"0","ST1009281328226982205"); 
        }else{
            System.out.println("일반 plain생성");
            pain=String.format("%s%s%s%s%s%s",trdDt,trdTm,mchtId,mchtTrdNo,price,"ST1009281328226982205"); 
        }
        return  pain;
    }
    private void requestToSettle(String url) {
        System.out.println("reuqestToSettle");
        try {
            headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.set("charset", "UTF-8");
      
            HttpEntity<JSONObject>entity=new HttpEntity<>(body,headers);
            System.out.println(entity.getBody()+" 요청정보"+entity.getHeaders());
            JSONObject response= restTemplate.postForObject(url,entity,JSONObject.class);
            System.out.println(response+" 세틀뱅크 통신결과");
            showResponse(response);
           
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("requestToSettle error "+ e.getMessage());
            throw new RuntimeException("세틀뱅크 통신 실패");
        }finally{
            body.clear();
            headers.clear();
        }
    }
    private void showResponse(JSONObject response) {
        LinkedHashMap<String,Object> data=(LinkedHashMap<String,Object>) response.get("data");
        System.out.println(data+" 세틀뱅크 통신결과");
        if(data.get("cnclAmt")!=null){
            System.out.println("환불 금액"+aesToNomal((String)data.get("cnclAmt")));
            System.out.println("환불 가능 금액"+aesToNomal((String)data.get("blcAmt")));
        }
        LinkedHashMap<String,Object> params=(LinkedHashMap<String,Object>) response.get("params");
        if(params.get("outStatCd").equals("0031")){
            System.out.println("세틀 뱅크 0031 ");
            throw new RuntimeException((String) params.get("outRsltMsg"));
        }
    }
    public void tryUpdateVbank(reseponseSettleDto reseponseSettleDto) {
        System.out.println("tryUpdateVbank");
        if(reseponseSettleDto.getMchtId().equals(aboutPayEnums.vbankmehthod.getString())&&reseponseSettleDto.getOutStatCd().equals("0021")){
            System.out.println("vbank 입금이 되었습니다");
            vbankService.okVank(reseponseSettleDto);
        }else{
            System.out.println("vbank가 아니거나 채번요청");
        }
       
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject cancel(tryCancleDto tryCancleDto ) {
        System.out.println("cancle");
        try {
            List<Integer>cancleIds=tryCancleDto.getIds();
            List<getClientInter>clientInters=new ArrayList<>();
            String kind=aboutPayEnums.valueOf(tryCancleDto.getKind()).getString();
            if(kind.equals(aboutPayEnums.reservation.getString())){
                System.out.println("예약 상품 테이블 삭제");
                clientInters=reservationService.deleteReservationDb(cancleIds);
            }else if(kind.equals(aboutPayEnums.product.getString())){
                System.out.println("일반 상품 취소검증");
            }
            reseponseSettleDto reseponseSettleDto=new reseponseSettleDto();
            deletePaymentDb(clientInters,reseponseSettleDto);
            return utillService.makeJson(true, "환불 되었습니다");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("cancel error"+e.getMessage());
            throw new failCancleException(e.getMessage());
        }
    }
    private void deletePaymentDb(List<getClientInter>clientInters,reseponseSettleDto reseponseSettleDto) {
        System.out.println("deleteDb");
        int newPrice=0;
        List<getClientInter>cards=new ArrayList<>();
        try {
            for(getClientInter g:clientInters){
                if(g.getCid()!=null){
                    System.out.println("카드 취소 요청 카드 취소 리스트에 저장시도");
                    cards.add(g);
                }
            }
            if(!cards.isEmpty()){
                System.out.println("카드 취소 요청 묶음 분류 시작");
                int cardsSize=clientInters.size();
                int minusPrice=0;
                int nextMinusPrice=0;
                for(int i=0;i<cardsSize;i++){
                    if(i==0){
                        System.out.println("카드결제 제일 처음분류 ");
                        minusPrice+=Integer.parseInt(cards.get(i).getPrice());
                        if(i==cardsSize-1){
                            cardService.updateCardPay(minusPrice, cards.get(i).getCid(),reseponseSettleDto);
                            cardService.getClientInterToDto(cards.get(i),reseponseSettleDto,minusPrice);
                            requestCancle(reseponseSettleDto);
                        }
                    }else if(cards.get(i).getCmcht_trd_no().equals(cards.get(i-1).getCmcht_trd_no())){
                        System.out.println("이전번호와 일치함");
                        minusPrice+=Integer.parseInt(cards.get(i).getPrice());
                        if(i==cardsSize-1){
                            cardService.updateCardPay(minusPrice, cards.get(i).getCid(),reseponseSettleDto);
                            cardService.getClientInterToDto(cards.get(i),reseponseSettleDto,minusPrice);
                            requestCancle(reseponseSettleDto);
                        }
                    }else if(!cards.get(i).getCmcht_trd_no().equals(cards.get(i-1).getCmcht_trd_no())){
                        System.out.println("이전번호와 일치하지 않음");
                        cardService.updateCardPay(minusPrice, cards.get(i-1).getCid(),reseponseSettleDto);
                        cardService.getClientInterToDto(cards.get(i-1),reseponseSettleDto,minusPrice);
                        requestCancle(reseponseSettleDto);
                        if(i==cardsSize-1){
                            minusPrice=nextMinusPrice;
                            cardService.updateCardPay(minusPrice, cards.get(i).getCid(),reseponseSettleDto);
                            cardService.getClientInterToDto(cards.get(i),reseponseSettleDto,minusPrice);
                            requestCancle(reseponseSettleDto);
                        }
                        minusPrice=Integer.parseInt(cards.get(i).getPrice());
                    }
                }
            }
            /*for(getClientInter g:clientInters){
                if(g.getCid()!=null){
                    System.out.println("카드로 결제된 상품 취소");
                    newPrice=minusPrice(g.getCtrd_amt(), Integer.parseInt(g.getPrice()));
                    cardService.getClientInterToDto(g,reseponseSettleDto);
                    cardService.updateCardPay(newPrice, g.getCid(),reseponseSettleDto);
                    requestCancle(reseponseSettleDto);
                }else if(g.getVid()!=null){
                    System.out.println("가상계좌로 결제된 상품 취소");
                    newPrice=minusPrice(Integer.parseInt(g.getVtrd_amt()), Integer.parseInt(g.getPrice()));
                    vbankService.getClientInterToDto(g,reseponseSettleDto);
                    vbankService.updateVBankPay(newPrice, g.getVid(),reseponseSettleDto);
                    if(reseponseSettleDto.getVbankStatus().equals(aboutPayEnums.statusReady.getString())&&reseponseSettleDto.getVbankFlag().equals("true")){
                        System.out.println("가상계좌 입금전 부분취소 요청");
                        vbankService.getReAccount(reseponseSettleDto);
                    }
                    requestCancle(reseponseSettleDto);
                }else if(g.getKtid()!=null){
                    System.out.println("카카오페이로 결제한 상품 취소");
                    kakaoService.requestCancleToKakaoPay(g.getKtid(), Integer.parseInt(g.getPrice()));
                }
            }*/
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("deleteDb error"+e.getMessage());
            throw new RuntimeException("테이블 삭제 실패");
        }
     
    }
    public void requestCancle(reseponseSettleDto reseponseSettleDto) {
        System.out.println("requestCancle");
        String url=null;
        if(reseponseSettleDto.getMchtId().equals(aboutPayEnums.cardmehtod.getString())){
            System.out.println("카드결제 환불");
            this.body=cardService.makecancelBody(reseponseSettleDto);
            url="https://tbgw.settlebank.co.kr/spay/APICancel.do";
        }else if(reseponseSettleDto.getMchtId().equals(aboutPayEnums.vbankmehthod.getString())){
            if(reseponseSettleDto.getVbankStatus().equals(aboutPayEnums.statusReady.getString())){///채번취소는 미입금 변경시에도 일어나야함
                System.out.println("가상계좌 채번취소");
                this.body=vbankService.makeCancleAccountBody(reseponseSettleDto);
                url="https://tbgw.settlebank.co.kr/spay/APIVBank.do";
                 
            }else{
                System.out.println("가상계좌 환불");
                this.body=vbankService.makeCancleBody(reseponseSettleDto);
                url="https://tbgw.settlebank.co.kr/spay/APIRefund.do";  
            }
     
        }
        requestToSettle(url);
    }


  


}
