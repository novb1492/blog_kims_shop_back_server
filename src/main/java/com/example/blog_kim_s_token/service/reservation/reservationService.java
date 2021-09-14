package com.example.blog_kim_s_token.service.reservation;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


import com.amazonaws.services.managedblockchain.model.IllegalActionException;
import com.example.blog_kim_s_token.enums.aboutPayEnums;
import com.example.blog_kim_s_token.enums.reservationEnums;
import com.example.blog_kim_s_token.model.payment.reseponseSettleDto;
import com.example.blog_kim_s_token.model.reservation.*;
import com.example.blog_kim_s_token.model.user.userDto;
import com.example.blog_kim_s_token.service.userService;
import com.example.blog_kim_s_token.service.utillService;
import com.example.blog_kim_s_token.service.payment.paymentService;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
public class reservationService {

    private final int openTime=9;
    private final int closeTime=22;
    private final int maxPeopleOfDay=60;
    private final int maxPeopleOfTime=6;
    private final int cantFlag=100;
    private final int pagingNum=3;

    @Value("${payment.minusHour}")
    private  int minusHour;
   
    @Value("${payment.limitedCancleHour}")
    private  int limitedCancleHour;
    @Autowired
    private reservationDao reservationDao;
    @Autowired
    private paymentService paymentService;
    @Autowired
    private tempReservationDao tempReservationDao;
    @Autowired
    private userService userService;



    public JSONObject getDateBySeat(getDateDto getDateDto) {
        System.out.println("getDateBySeat");
        try {
            int month=getDateDto.getMonth();
            LocalDate selectDate=LocalDate.of(getDateDto.getYear(),month,1);
            YearMonth yearMonth=YearMonth.from(selectDate);
            int lastDay=yearMonth.lengthOfMonth();
            System.out.println(lastDay+" lastDay");
            int start=0;
            DayOfWeek dayOfWeek = selectDate.getDayOfWeek();
            int temp=1;
            start=dayOfWeek.getValue();
            System.out.println(start+" start");
            int endDayIdOfMonth=lastDay+start;
            System.out.println(endDayIdOfMonth+" endDayIdOfMonth");
            JSONObject dates=new JSONObject();
            int [][]dateAndValue=new int[endDayIdOfMonth][3];
            for(int i=1;i<start;i++) {
                dateAndValue[i][0]=0;
                dateAndValue[i][1]=0;
                dateAndValue[i][2]=cantFlag;
            }
            for(int i=start;i<endDayIdOfMonth;i++) {
                Timestamp  timestamp=Timestamp.valueOf(getDateDto.getYear()+"-"+month+"-"+temp+" 00:00:00");
                int countAlready=getCountAlreadyInDate(timestamp,getDateDto.getSeat());
                dateAndValue[i][0]=temp;
                dateAndValue[i][1]=countAlready;
                if(countAlready>=maxPeopleOfDay||utillService.compareDate(timestamp, LocalDateTime.now())){
                    dateAndValue[i][2]=cantFlag; 
                }
                temp+=1;
            }
            dates.put("dates", dateAndValue);
            
            System.out.println(dates);
            return dates;
        } catch (Exception e) {
           e.printStackTrace();
           throw new RuntimeException("getDateBySeat error");
        }
    }
    private int getCountAlreadyInDate(Timestamp timestamp,String seat) {
        System.out.println("getCountAlreadyIn");
        System.out.println(timestamp);
        return reservationDao.findByRdate(timestamp,seat);
    }
    public JSONObject getTimeByDate(getTimeDto getTimeDto) {
        System.out.println("getTimeByDate");
        try {
            JSONObject timesJson=new JSONObject();
            int totalHour=closeTime-openTime;
            System.out.println(totalHour+" totalHour");
            int[][] timesArray=new int[totalHour+1][3];
            for(int i=0;i<=totalHour;i++){
                Timestamp timestamp=Timestamp.valueOf(getTimeDto.getYear()+"-"+getTimeDto.getMonth()+"-"+getTimeDto.getDate()+" "+(i+openTime)+":00:00");
                int count=getCountAlreadyInTime(timestamp,getTimeDto.getSeat());
                timesArray[i][0]=i+openTime;
                timesArray[i][1]=count;
                System.out.println(count);
                if(LocalDateTime.now().getDayOfMonth()==getTimeDto.getDate()&&LocalDate.now().getYear()==getTimeDto.getYear()&&LocalDate.now().getMonthValue()==getTimeDto.getMonth()){
                    System.out.println(getTimeDto.getDate()+" "+getTimeDto.getYear()+"월년");
                    if((i+openTime)<=LocalDateTime.now().getHour()){
                        System.out.println("지난시간");
                        timesArray[i][2]=cantFlag;
                    }
                }
                else if(count==maxPeopleOfTime){
                    System.out.println("자리가 다찬시간");
                    timesArray[i][2]=cantFlag;
                }
            }
            timesJson.put("times", timesArray);
            return timesJson;
        } catch (Exception e) {
           e.printStackTrace();
           throw new RuntimeException("getTimeByDate error");
        }
    }
    public int getCountAlreadyInTime(Timestamp timestamp,String seat) {
        System.out.println("getCountAlreadyInTime");
        System.out.println(timestamp);
        return reservationDao.findByTime(timestamp,seat);
    }
    public void doReservation(String email,String name,String paymentid,String[][]itemArray,String[] other,List<Integer>times,String status,String usedKind) {
        System.out.println("doReservation");
        reservationInsertDto reservationInsertDto=new reservationInsertDto();
                reservationInsertDto.setEmail(email);
                reservationInsertDto.setName(name);
                reservationInsertDto.setPaymentId(paymentid);
                reservationInsertDto.setSeat(itemArray[0][0]);
                reservationInsertDto.setStatus(status);
                reservationInsertDto.setUsedKind(usedKind);
                reservationInsertDto.setYear(Integer.parseInt(other[0]));
                reservationInsertDto.setMonth(Integer.parseInt(other[1]));
                reservationInsertDto.setDate(Integer.parseInt(other[2]));
                reservationInsertDto.setTimes(times);
       confrimContents(reservationInsertDto);
    }
    public JSONObject insertTemp(reservationInsertDto reservationInsertDto) {
        confrimInsert(reservationInsertDto);
        insertTempTable(reservationInsertDto);
        return utillService.makeJson(true, "");
    }
    private void insertTempTable(reservationInsertDto reservationInsertDto) {
        System.out.println("insertTempTable");
        List<Integer>times=reservationInsertDto.getTimes();
        userDto userDto=userService.sendUserDto();
        try {  
            System.out.println(Timestamp.valueOf(reservationInsertDto.getYear()+"-"+reservationInsertDto.getMonth()+"-"+reservationInsertDto.getDate()+" 00:00:00")+" 사용예정일");
            for(int i=0;i<times.size();i++){
                tempReservationDto dto=tempReservationDto.builder()
                                        .trEmail(userDto.getEmail())
                                        .trName(userDto.getName())
                                        .trTime(times.get(i))
                                        .trSeat(reservationInsertDto.getSeat())
                                        .trPaymentid(reservationInsertDto.getPaymentId())
                                        .trRdate(Timestamp.valueOf(reservationInsertDto.getYear()+"-"+reservationInsertDto.getMonth()+"-"+reservationInsertDto.getDate()+" 00:00:00"))
                                        .trDateAndTime(Timestamp.valueOf(reservationInsertDto.getYear()+"-"+reservationInsertDto.getMonth()+"-"+reservationInsertDto.getDate()+" "+times.get(i)+":00:00"))
                                        .trstatus("temp")
                                        .trUsedPayMethod(reservationInsertDto.getUsedKind())
                                        .build();
                                        tempReservationDao.save(dto);
            }
        } catch (Exception e) {
           e.printStackTrace();
           System.out.println("insertTempTable error");
           throw new RuntimeException("예약 저장 실패");
        }
    }
    public void tempToMain(reseponseSettleDto reseponseSettleDto) {
        System.out.println("tempToMain");
        try {
           /* List<tempReservationDto>tempReservationDtos=tempReservationDao.findByTrpaymentId(reseponseSettleDto.getMchtTrdNo()).orElseThrow(()->new IllegalActionException("주문요청을 찾을 수없습니다"));
           for(tempReservationDto t: tempReservationDtos){
                mainReservationDto dto=mainReservationDto.builder().dateAndTime(t.getTrDateAndTime())
                .email(t.getTrEmail())
                .mainpgpaymentId(reseponseSettleDto.getTrdNo())
                .name(t.getTrName())
                .paymentId(t.getTrPaymentid())
                .rDate(t.getTrRdate())
                .seat(t.getTrSeat())
                .status("paid")
                .time(t.getTrTime())
                .usedPayKind(reseponseSettleDto.getCardNm())
                .build();
                reservationDao.save(dto);
                tempReservationDao.delete(t);
           }
         */

            
        }catch (IllegalActionException e){
            throw new IllegalActionException(e.getMessage());
        } 
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
    private void confrimContents(reservationInsertDto reservationInsertDto) {
        confrimInsert(reservationInsertDto);
        insertReservation(reservationInsertDto);
    }
    private void insertReservation(reservationInsertDto reservationInsertDto) {
        System.out.println("insertReservation");
        List<Integer>times=reservationInsertDto.getTimes();
        try {  
            System.out.println(Timestamp.valueOf(reservationInsertDto.getYear()+"-"+reservationInsertDto.getMonth()+"-"+reservationInsertDto.getDate()+" 00:00:00")+" 사용예정일");
            for(int i=0;i<times.size();i++){
                mainReservationDto dto=mainReservationDto.builder()
                                        .email(reservationInsertDto.getEmail())
                                        .name(reservationInsertDto.getName())
                                        .time(times.get(i))
                                        .seat(reservationInsertDto.getSeat())
                                        .paymentId(reservationInsertDto.getPaymentId())
                                        .rDate(Timestamp.valueOf(reservationInsertDto.getYear()+"-"+reservationInsertDto.getMonth()+"-"+reservationInsertDto.getDate()+" 00:00:00"))
                                        .dateAndTime(Timestamp.valueOf(reservationInsertDto.getYear()+"-"+reservationInsertDto.getMonth()+"-"+reservationInsertDto.getDate()+" "+times.get(i)+":00:00"))
                                        .status(reservationInsertDto.getStatus())
                                        .usedPayKind(reservationInsertDto.getUsedKind())
                                        .build();
                                        reservationDao.save(dto);
            }
        } catch (Exception e) {
           e.printStackTrace();
           System.out.println("insertReservation error");
           throw new RuntimeException("예약 저장 실패");
        }
    }
    private void confrimInsert(reservationInsertDto reservationInsertDto){
        System.out.println("confrimInsert");
            List<mainReservationDto>array=reservationDao.findByEmailNative(reservationInsertDto.getEmail(),reservationInsertDto.getSeat());
            List<Integer>times=reservationInsertDto.getTimes();
            System.out.println(array.toString()+" 내역들");
            if(reservationInsertDto.getTimes().size()<=0){
                System.out.println("몇시간 쓸지 선택 되지 않음");
                throw new RuntimeException("시간을 선택하지 않았습니다");
            }else if(array!=null){
                System.out.println("show");
                for(mainReservationDto m:array){
                    for(int i:times){
                        String date=reservationInsertDto.getYear()+"-"+reservationInsertDto.getMonth()+"-"+reservationInsertDto.getDate()+" "+i+":00:00";
                        Timestamp DateAndTime=Timestamp.valueOf(date);
                        System.out.println(DateAndTime+" show");
                        if(m.getDateAndTime().equals(DateAndTime)||utillService.compareDate(DateAndTime, LocalDateTime.now())){
                            System.out.println("이미 예약한 시간 발견or지난 날짜 예약시도");
                            throw new RuntimeException("이미 예약한 시간 발견 이거나 지난 날짜 예약시도입니다 "+date);
                        }else if(getCountAlreadyInTime(DateAndTime,reservationInsertDto.getSeat())==maxPeopleOfTime){
                            System.out.println("예약이 다찬 시간입니다");
                            throw new RuntimeException("예약이 가득찬 시간입니다 "+date);
                        }else if(i<openTime||i>closeTime){
                            System.out.println("영업 시간외 예약시도");
                            throw new RuntimeException("영업 시간외 예약시도 입니다");
                        }
                    }
                }
            }
            if(reservationInsertDto.getStatus().equals(aboutPayEnums.statusReady.getString())){
                paymentService.checkTime(reservationInsertDto.getYear(),reservationInsertDto.getMonth(),reservationInsertDto.getDate(),times.get(0));
            }
    }
    public JSONObject getClientReservation(JSONObject JSONObject) {
        System.out.println("getClientReservation");
        String startDate=(String) JSONObject.get("startDate");
        String endDate=(String) JSONObject.get("endDate");
        System.out.println("시작일"+startDate);
        System.out.println("종료일"+endDate);
        try {
            JSONObject respone=new JSONObject();
            int nowPage=(int) JSONObject.get("nowPage");
            String email=SecurityContextHolder.getContext().getAuthentication().getName();
            int totalPage=getTotalPage(email, startDate, endDate);
            reservationEnums enums=confrimDateAndPage(nowPage,totalPage,startDate,endDate);
            if(enums.getBool()==false){
                System.out.println("조건 안맞음");
                return utillService.makeJson(enums.getBool(), enums.getMessege());
            }
            List<getClientInter>dtoArray=getClientReservationDTO(email,startDate,endDate,nowPage,totalPage,respone);
            String[][] array=makeResponse(respone, dtoArray);

            respone.put("bool", true);
            respone.put("nowPage", nowPage);
            respone.put("reservations", array);
            return respone;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("getClientReservation error");
            throw new RuntimeException("예약조회에 실패했습니다");
        }
    }
    private reservationEnums confrimDateAndPage(int nowPage,int totalPage,String startDate,String endDate){
        System.out.println("confrimDate");
        String enumName="fail";
        String messege=null;
        if(nowPage<=0){
            System.out.println("페이지가 0보다 작거나 같습니다 ");
            messege="페이지가 0보다 작거나 같습니다";
        }else if(nowPage>totalPage){
            System.out.println("nowpage>totalpage");
            messege="전체 페이지 보다 현재 페이지가 큽니다";
        }else if(startDate.isEmpty()&&!endDate.isEmpty()){
            System.out.println("시작날이 없습니다 ");
            messege="시작날이 없습니다";
        }else if(!startDate.isEmpty()&&endDate.isEmpty()){
            System.out.println("끝나는 날이 없습니다 ");
            messege="끝나는 날이 없습니다";
        }else if(!startDate.isEmpty()&&!endDate.isEmpty()){
            if(LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE).isAfter(LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE))){
                System.out.println("날짜 선택이 잘못되었습니다 ");
                messege="날짜 선택이 잘못되었습니다";
            }else{
                enumName="yes";
            }
        }else{
            enumName="yes";
        }
        reservationEnums.valueOf(enumName).setMessete(messege);
        return reservationEnums.valueOf(enumName);
    }
    private List<getClientInter>getClientReservationDTO(String email,String startDate,String endDate,int nowPage,int totalPage,JSONObject respone){
        System.out.println("getClientReservationDTO");
        List<getClientInter>dtoArray=new ArrayList<>();
        int fisrt=0;
            if(startDate.isEmpty()&&endDate.isEmpty()){
                System.out.println("날짜 미지정 검색");
                fisrt=utillService.getFirst(nowPage, pagingNum);
                dtoArray=reservationDao.findByEmailJoinOrderByIdDescNative(email, fisrt-1,utillService.getEnd(fisrt, pagingNum)-fisrt+1);
            }else{
                System.out.println("날짜 지정 검색");
                fisrt=utillService.getFirst(nowPage, pagingNum);
                dtoArray=reservationDao.findByEmailJoinOrderByIdBetweenDescNative(email,Timestamp.valueOf(startDate+" "+"00:00:00"),Timestamp.valueOf(endDate+" 00:00:00"),fisrt-1,utillService.getEnd(fisrt, pagingNum)-fisrt+1);
            }
        respone.put("totalPage", totalPage);
        return dtoArray;
    }
    private int  getTotalPage(String email,String startDate,String endDate) {
        if(startDate.isEmpty()&&endDate.isEmpty()){
            return utillService.getTotalpages(reservationDao.countByEmail(email), pagingNum);
        }else{
            return utillService.getTotalpages(reservationDao.countByEmailNative(email,Timestamp.valueOf(startDate+" "+"00:00:00"),Timestamp.valueOf(endDate+" 00:00:00")), pagingNum);
        
        }
    }
    private String[][] makeResponse(JSONObject jsonObject,List<getClientInter>dtoArray) {
        System.out.println("makeResponse");
        String[][] array=new String[dtoArray.size()][9];
            int temp=0;
            for(getClientInter m:dtoArray){
                array[temp][0]=Integer.toString(m.getId());
                array[temp][1]=m.getSeat();
                array[temp][2]=m.getCreated().toString();
                array[temp][3]=m.getDate_and_time().toString();
                if(m.getStatus().equals("ready")){
                    /*vBankDto vBankDto=null;
                    array[temp][4]="미입금";
                    array[temp][5]=vBankDto.getBank()+" "+vBankDto.getBankNum();
                    array[temp][6]=vBankDto.getEndDate().toString();
                    array[temp][7]=vBankDto.getVbankTotalPrice()+"";
                    array[temp][8]=null;*/
                }else{
                    array[temp][4]="결제완료";
                    array[temp][5]=m.getUsed_pay_kind();
                    array[temp][6]=m.getCreated().toString();
                    array[temp][7]=m.getPrice()+"";
                    if(LocalDateTime.now().plusHours(limitedCancleHour).isAfter(m.getDate_and_time().toLocalDateTime())){
                        System.out.println("현재시간이 사용시간 이후입니다");
                        array[temp][8]=Integer.toString(cantFlag);
                    }
                }
                temp++;
            }
        return array;
    }
   
        
  
    
    private void confrimCancle(Timestamp dateAndTime,String remail) {
        System.out.println("confrimCancle");
        String messege=null;
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        if(LocalDateTime.now().plusHours(limitedCancleHour).isAfter(dateAndTime.toLocalDateTime())){
            messege="예약시간 한시간 전까지 취소가능합니다";
        }else if(!email.equals(remail)){
            messege="예약과 예약자 이메일이 다릅니다";
        }else{
            System.out.println("예약 취소 가능");
            return;
        }
       throw new RuntimeException(messege);
    }
    public void readyTopaid(String paymentId) {
        System.out.println("readyTopaid");
        List<mainReservationDto>array=reservationDao.findByPaymentId(paymentId);
        for(mainReservationDto m:array){
            m.setStatus("paid");
        }
        System.out.println("예약테이블 paid로 변경완료");
    }
}
