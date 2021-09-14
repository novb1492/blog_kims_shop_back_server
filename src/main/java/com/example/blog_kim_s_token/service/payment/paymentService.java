package com.example.blog_kim_s_token.service.payment;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.managedblockchain.model.IllegalActionException;
import com.example.blog_kim_s_token.enums.aboutPayEnums;
import com.example.blog_kim_s_token.model.payment.getHashInfor;
import com.example.blog_kim_s_token.model.payment.getVankDateDto;
import com.example.blog_kim_s_token.model.payment.reseponseSettleDto;
import com.example.blog_kim_s_token.model.payment.tryCanclePayDto;
import com.example.blog_kim_s_token.model.product.productDto;
import com.example.blog_kim_s_token.model.user.userDto;
import com.example.blog_kim_s_token.service.priceService;
import com.example.blog_kim_s_token.service.userService;
import com.example.blog_kim_s_token.service.utillService;
import com.example.blog_kim_s_token.service.ApiServies.kakao.kakaoService;
import com.example.blog_kim_s_token.service.hash.aes256;
import com.example.blog_kim_s_token.service.hash.sha256;
import com.example.blog_kim_s_token.service.payment.model.card.cardDao;
import com.example.blog_kim_s_token.service.payment.model.card.cardDto;
import com.example.blog_kim_s_token.service.payment.model.tempPaid.tempPaidDao;
import com.example.blog_kim_s_token.service.payment.model.tempPaid.tempPaidDto;
import com.example.blog_kim_s_token.service.payment.model.vbank.insertvbankDto;
import com.example.blog_kim_s_token.service.payment.model.vbank.vbankDao;
import com.example.blog_kim_s_token.service.reservation.reservationService;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class paymentService {

    @Autowired
    private reservationService resevationService;
    @Autowired
    private priceService priceService;
    @Value("${payment.period}")
    private  int period;
    @Value("${payment.minusHour}")
    private  int minusHour;
    @Value("${kakao.kakaoPay.cid}")
    private String kakaoPayCid;
    @Autowired
    private kakaoService kakaoService;
    @Autowired
    private sha256 sha256;
    @Autowired
    private userService userService;
    @Autowired
    private tempPaidDao tempPaidDao;
    @Autowired
    private cardDao cardDao;
    @Autowired
    private vbankDao vbankDao;

    public JSONObject  getVbankDate(getVankDateDto getVankDateDto) {
        System.out.println("getVbankDate");
        try {
            if(getVankDateDto.getKind().equals(aboutPayEnums.reservation.getString())){
                Calendar getToday = Calendar.getInstance();
                getToday.setTime(new Date()); 
                String requestDate=getVankDateDto.getYear()+"-"+getVankDateDto.getMonth()+"-"+getVankDateDto.getDate();
                long diffDays = utillService.getDateGap(getToday, requestDate);
                Collections.sort(getVankDateDto.getTimes());
                int shortestTime=getVankDateDto.getTimes().get(0);
                checkTime(getVankDateDto.getYear(),getVankDateDto.getMonth(),getVankDateDto.getDate(),shortestTime);
                return utillService.makeJson(true,getVbankDate(diffDays, shortestTime, requestDate));
            }else{
                return utillService.makeJson(true,getVbankDate());
            }
            
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
    private String getVbankDate(long diffDays,int shortestTime,String requestDate) {
        System.out.println("getVbankDate");   
        String expiredDate=null;
        if(diffDays<period){
            System.out.println(shortestTime+" 가장작은시간");
            expiredDate=requestDate+" "+(shortestTime-minusHour)+":00:00";
            System.out.println(expiredDate+" 새로만든 기한");
            String[]temp=expiredDate.split(" ");
            String time=temp[1];
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
                time=splitTime[0]+":"+splitTime[1]+":"+splitTime[2];
            }
            expiredDate=temp[0]+"-"+temp[1]+"-"+temp[2]+" "+time;
 
        }else{
            System.out.println("예약 일자가 "+period+"이상임");
            expiredDate=getVbankDate();
        }
        System.out.println(expiredDate+" 최종");
        return expiredDate;
    }
    private String getVbankDate() {
        System.out.println("getVbankDate");
        String expiredDate=LocalDateTime.now().plusDays(period).toString();
        expiredDate=expiredDate.replace("T", " ");
        return expiredDate;
    }


   
    public int minusPrice(int totalPrice,int minusPrice) {
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
    public void confrimProduct(int requestTotalPrice,int totalPrice,int count,String productName) {
        System.out.println("confrimProduct");
        String[] splitName=productName.split(",");
        int splitNameSize=splitName.length;
        String messege="검증실패";
        for(int i=0;i<splitNameSize;i++){
           productDto productDto=priceService.selectProduct(splitName[i]);
           int remainCount=productDto.getCount();
           if(requestTotalPrice!=totalPrice){
               System.out.println("가격이 변조되었습니다");
               messege="가격이 변조되었습니다";
               break;
           }else if(remainCount<=0||remainCount-count<=0){
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
    public JSONObject canclePay(tryCanclePayDto tryCanclePayDto ) {
        System.out.println("canclePay");
        try {
            String kind=aboutPayEnums.valueOf(tryCanclePayDto.getKind()).getString();
            List<Integer> idArray=tryCanclePayDto.getId();
            if(kind.equals(aboutPayEnums.reservation.getString())){
                System.out.println("예약 상품 취소 시도");
              
            }else if(kind.equals(aboutPayEnums.product.getString())){
                System.out.println("일반 상품 취소 시도");
            }
            return utillService.makeJson(true, "완료되었습니다");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("canclePay error"+ e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    public void requestCancleToKakaoPay(String tid,int price) {
        System.out.println("requestCancleToKakaoPay");
        MultiValueMap<String,Object> body=new LinkedMultiValueMap<>();
        body.add("cid", kakaoPayCid);
        body.add("tid", tid);
        body.add("cancel_amount", price);
        body.add("cancel_tax_free_amount",0);
        kakaoService.cancleKakaopay(body);
    }
    public JSONObject makeTohash(getHashInfor getHashInfor) {
        System.out.println("makeTohash");
        JSONObject response=new JSONObject();
        try {
            String kind=aboutPayEnums.valueOf(getHashInfor.getKind()).getString();
            System.out.println(kind); 
            String email=SecurityContextHolder.getContext().getAuthentication().getName();
            String price=getHashInfor.getTotalPrice()+"";
            String mchtTrdNo=kind+utillService.GetRandomNum(10);
            getHashInfor.setMchtTrdNo(mchtTrdNo);
            getHashInfor.setRequestDate("20210913");
            getHashInfor.setRequestTime("132000");
            String pktHash=sha256.encrypt(getHashInfor);
            String hashPrice=aes256.encrypt(price);
            String mchtCustId=aes256.encrypt(email);
            response.put("mchtCustId", mchtCustId);
            response.put("mchtTrdNo", mchtTrdNo);
            response.put("trdAmt", hashPrice);
            response.put("trdDt", getHashInfor.getRequestDate());
            response.put("trdTm", getHashInfor.getRequestTime());
            response.put("pktHash", pktHash);
            tempPaidDto dto=tempPaidDto.builder()
                                        .tpemail(email)
                                        .tpaymentid(mchtTrdNo)
                                        .tpprice(price)
                                        .build();
                                        tempPaidDao.save(dto);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("makeTohash error"+e.getMessage());
            throw new RuntimeException("구매정보 해시화 실패");
        }
    }
    public void okSettle(reseponseSettleDto reseponseSettleDto ) {
        System.out.println("okSettle");
        try {
            String mchtTrdNo=reseponseSettleDto.getMchtTrdNo();
            if(mchtTrdNo.startsWith(aboutPayEnums.reservation.getString())){
                System.out.println("예약 상품웹훅");
                resevationService.tempToMain(reseponseSettleDto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("okSettle error"+e.getMessage());
        }
    }
    public void confrimSettle(reseponseSettleDto reseponseSettleDto) {
        System.out.println("confrimSettle");
        try {
            String outStatCd=reseponseSettleDto.getOutStatCd();
            byte[] aesCipherRaw=aes256.decodeBase64(reseponseSettleDto.getTrdAmt());
            String trdAmt =new String(aes256.aes256DecryptEcb(aesCipherRaw),"UTF-8");
            reseponseSettleDto.setTrdAmt(trdAmt);
            userDto userDto=userService.sendUserDto();
            checkDetails(reseponseSettleDto,userDto.getEmail());
            if(outStatCd.equals("0021")){
                System.out.println("일반 결제 상품입니다");
                insertPaid(reseponseSettleDto);
               
            }else if(outStatCd.equals("0051")){
                System.out.println("가상계좌 채번완료");
                byte[] aesCipherRaw2=aes256.decodeBase64(reseponseSettleDto.getVtlAcntNo());
                reseponseSettleDto.setVtlAcntNo(new String(aes256.aes256DecryptEcb(aesCipherRaw2),"UTF-8"));
                insertVbank(reseponseSettleDto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("confrimSettle error"+e.getMessage());
            throw new RuntimeException("결제 검증에 실패했습니다");
        }
    }
    private void checkDetails(reseponseSettleDto reseponseSettleDto,String email) {
        System.out.println("checkDetails");
        tempPaidDto tempPaidDto=tempPaidDao.findByTpaymentid(reseponseSettleDto.getMchtTrdNo()).orElseThrow(()->new IllegalActionException("결제 요청 정보가 없습니다"));
        if(!tempPaidDto.getTpemail().equals(email)){
            System.out.println("이메일이 다름");
        }else if(!tempPaidDto.getTpprice().equals(reseponseSettleDto.getTrdAmt())){
            System.out.println("결제금액이 다릅니다");
        }
    }
    private void insertPaid(reseponseSettleDto reseponseSettleDto) {
        System.out.println("insertPaid");
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
    private void insertVbank(reseponseSettleDto reseponseSettleDto) {
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
                                .build();
            vbankDao.save(dto);
                                
                                
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("insertVbank error"+e.getMessage());
            throw new RuntimeException("가상 계좌 정보 저장 실패");
        }
    }
  


}
