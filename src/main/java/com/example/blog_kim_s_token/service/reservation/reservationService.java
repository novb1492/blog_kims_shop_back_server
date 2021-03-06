package com.example.blog_kim_s_token.service.reservation;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;


import com.amazonaws.services.managedblockchain.model.IllegalActionException;
import com.example.blog_kim_s_token.enums.aboutPayEnums;
import com.example.blog_kim_s_token.enums.reservationEnums;
import com.example.blog_kim_s_token.model.payment.getHashInfor;
import com.example.blog_kim_s_token.model.payment.reseponseSettleDto;
import com.example.blog_kim_s_token.model.reservation.*;
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
    private final int pagesize=10;

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
                reservationInsertDto.setYear(Integer.parseInt(other[0]));
                reservationInsertDto.setMonth(Integer.parseInt(other[1]));
                reservationInsertDto.setDate(Integer.parseInt(other[2]));
                reservationInsertDto.setTimes(times);
       confrimContents(reservationInsertDto);
    }
    public JSONObject insertTemp(getHashInfor getHashInfor,String email,String mchtTrdNo,String name,String mchtCustId) {
        System.out.println("insertTemp");
        reservationInsertDto dto=reservationInsertDto.builder()
                                                    .date(getHashInfor.getDate())
                                                    .email(email)
                                                    .month(getHashInfor.getMonth())
                                                    .name(name)
                                                    .paymentId(mchtTrdNo)
                                                    .seat(getHashInfor.getSeat())
                                                    .status("temp")
                                                    .times(getHashInfor.getTimes())
                                                    .year(getHashInfor.getYear())
                                                    .mchtCustId(mchtCustId)
                                                    .build();
        confrimInsert(dto);
        insertTempTable(dto);
        return utillService.makeJson(true, "");
    }
    private void insertTempTable(reservationInsertDto reservationInsertDto) {
        System.out.println("insertTempTable");
        List<Integer>times=reservationInsertDto.getTimes();
        try {  
            System.out.println(Timestamp.valueOf(reservationInsertDto.getYear()+"-"+reservationInsertDto.getMonth()+"-"+reservationInsertDto.getDate()+" 00:00:00")+" 사용예정일");
            for(int i=0;i<times.size();i++){
                tempReservationDto dto=tempReservationDto.builder()
                                        .trEmail(reservationInsertDto.getEmail())
                                        .trName(reservationInsertDto.getName())
                                        .trTime(times.get(i))
                                        .trSeat(reservationInsertDto.getSeat())
                                        .trPaymentid(reservationInsertDto.getPaymentId())
                                        .trDateAndTime(Timestamp.valueOf(reservationInsertDto.getYear()+"-"+reservationInsertDto.getMonth()+"-"+reservationInsertDto.getDate()+" "+times.get(i)+":00:00"))
                                        .trstatus("temp")
                                        .trRdate(Timestamp.valueOf(reservationInsertDto.getYear()+"-"+reservationInsertDto.getMonth()+"-"+reservationInsertDto.getDate()+" 00:00:00"))
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
           List<tempReservationDto>tempReservationDtos=tempReservationDao.findByTrPaymentid(reseponseSettleDto.getMchtTrdNo()).orElseThrow(()->new IllegalActionException("주문요청을 찾을 수없습니다"));
           for(tempReservationDto t: tempReservationDtos){
                mainReservationDto dto=mainReservationDto.builder()
                                        .dateAndTime(t.getTrDateAndTime())
                                        .email(t.getTrEmail())
                                        .name(t.getTrName())
                                        .paymentId(t.getTrPaymentid())
                                        .seat(t.getTrSeat())
                                        .time(t.getTrTime())
                                        .rDate(t.getTrRdate())
                                        .build();
                                        reservationDao.save(dto);
                                        tempReservationDao.delete(t);
           }
        

            
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
            System.out.println(array.isEmpty());
            List<Integer>times=reservationInsertDto.getTimes();
            if(reservationInsertDto.getTimes().size()<=0){
                System.out.println("몇시간 쓸지 선택 되지 않음");
                throw new RuntimeException("시간을 선택하지 않았습니다");
            }else if(array.isEmpty()==false){
                System.out.println(array.toString()+" 내역들");
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
            if(reservationInsertDto.getMchtCustId()!=null){
                System.out.println("세틀뱅크일때만 검사");
                if(reservationInsertDto.getMchtCustId().equals(aboutPayEnums.vbankmehthod.getString())){
                    paymentService.checkTime(reservationInsertDto.getYear(),reservationInsertDto.getMonth(),reservationInsertDto.getDate(),times.get(0));
                }
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
            int nowPage=(int) JSONObject.get("nowPage")+1;
            String email=SecurityContextHolder.getContext().getAuthentication().getName();
            List<getClientInter>dtoArray=getClientReservationDTO(email,startDate,endDate,nowPage);
           
            int totalPage=utillService.getTotalpages(dtoArray.get(0).getTotalpage(), pagesize);
            reservationEnums enums=confrimDateAndPage(nowPage,totalPage,startDate,endDate);
            if(enums.getBool()==false){
                System.out.println("조건 안맞음");
                return utillService.makeJson(enums.getBool(), enums.getMessege());
            }
            respone.put("totalPage", totalPage);
            respone.put("bool", true);
            respone.put("nowPage", nowPage);
            respone.put("reservations", makeResponse(respone, dtoArray));
            return respone;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("getClientReservation error");
            throw new RuntimeException(e.getMessage());
        }
    }
    private reservationEnums confrimDateAndPage(int nowPage,int totalPage,String startDate,String endDate){
        System.out.println("confrimDate");
        String enumName="fail";
        String messege=null;
        System.out.println(nowPage);
        if(nowPage<=0){
            System.out.println("페이지가 0보다 작거나 같습니다 ");
            messege="페이지가 0보다 작거나 같습니다";
        }else if(nowPage>totalPage){
            System.out.println("nowpage>totalpage");
            messege="전체 페이지 보다 현재 페이지가 큽니다";
        }else{
            enumName="yes";
        }
        reservationEnums.valueOf(enumName).setMessete(messege);
        return reservationEnums.valueOf(enumName);
    }
    private List<getClientInter>getClientReservationDTO(String email,String startDate,String endDate,int nowPage){
        System.out.println("getClientReservationDTO");
        List<getClientInter>dtoArray=new ArrayList<>();
        int fisrt=utillService.getFirst(nowPage, pagesize);
            if(startDate.isEmpty()&&endDate.isEmpty()){
                System.out.println("날짜 미지정 검색");
                //utillService.getEnd(fisrt, pagingNum)-fisrt+1=결국 몇개씩가져올지 였다면 그냥 pagesize?
                dtoArray=reservationDao.findByEmailJoinOrderByIdDescNative(email,email, fisrt-1,pagesize).orElseThrow(()-> new IllegalActionException("예약내역이 없습니다"));
            }else{
                System.out.println("날짜 지정 검색");
                Timestamp start=Timestamp.valueOf(startDate+" "+"00:00:00");
                Timestamp end=Timestamp.valueOf(endDate+" 00:00:00");
                System.out.println(start+"날짜"+end);
                dtoArray=reservationDao.findByEmailJoinOrderByIdBetweenDescNative(email,start,end,email,start,end,fisrt-1,pagesize).orElseThrow(()-> new IllegalActionException("예약내역이 없습니다"));
            }
        return dtoArray;
    }
    private List<JSONObject> makeResponse(JSONObject jsonObject,List<getClientInter>dtoArray) {
        System.out.println("makeResponse");
        System.out.println(dtoArray.toString()+" 배열속");
        String status=null;
        String usedKind=null;
        String paidDate=null;
        String paidPrice=null;
        Boolean flag=true;
        List<JSONObject>jsons=new ArrayList<>();
            for(getClientInter m:dtoArray){
                JSONObject itemInfor=new JSONObject();
                itemInfor.put("id", m.getId());
                itemInfor.put("seat", m.getSeat());
                itemInfor.put("created", m.getCreated().toString().substring(0, 19));
                itemInfor.put("dateAndTime", m.getDate_and_time().toString().substring(0, 19));
                if(m.getCid()!=null){
                    System.out.println("카드결제 예약");
                    status="결제완료";
                    usedKind=m.getCfn_nm();
                    paidDate=m.getC_created().toString();
                    paidPrice=m.getPrice()+"";
                }else if(m.getVid()!=null){
                    System.out.println("가상계좌결제 예약");
                    if(m.getVbankstatus().equals(aboutPayEnums.statusReady.getString())){
                        status="입금대기";
                        usedKind=m.getVfn_nm()+" "+m.getVtl_acnt_no();
                        paidDate=m.getVexpire_dt().toString();
                        paidPrice=m.getVtrd_amt();
                    }else{
                        status="결제완료";
                        usedKind=m.getVfn_nm()+" "+m.getVtl_acnt_no();
                        paidDate=m.getVtrd_dtm().toString();
                        paidPrice=m.getPrice();
                    }
                   
                }else if(m.getKtid()!=null){
                    System.out.println("카카오페이 결제");
                    status="결제완료";
                    usedKind="카카오페이";
                    paidDate=m.getK_created().toString();
                    paidPrice=m.getPrice()+"";
                }
                itemInfor.put("status", status);
                itemInfor.put("usedKind", usedKind);
                itemInfor.put("paidDate", paidDate);
                itemInfor.put("paidPrice", paidPrice);
                if(LocalDateTime.now().plusHours(limitedCancleHour).isAfter(m.getDate_and_time().toLocalDateTime())){
                    System.out.println("현재시간이 사용시간 이후입니다");
                    flag=true;
                }else{
                    flag=false;
                }
                itemInfor.put("cantflag", flag);
                jsons.add(itemInfor);
            }
        return jsons;
    }
    public List<getClientInter> deleteReservationDb(List<Integer>ids) {
        System.out.println("cancleReservation");
        try {
            List<getClientInter>clientInters=new ArrayList<>();
            String email=SecurityContextHolder.getContext().getAuthentication().getName();
            for(int id:ids){
                getClientInter clientInter=reservationDao.findByIdJoinNative(id).orElseThrow(()->new IllegalAccessException("존재하지 않는 예약내역입니다"));
                confrimCancle(clientInter.getDate_and_time(), email);
                clientInters.add(clientInter);
                reservationDao.deleteById(id);
            }
           return clientInters;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("cancleReservation error"+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
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
    public void updatenewpayment_id(String newpayment_id,String originpayment_id) {
        System.out.println("updatenewpayment_id"+newpayment_id+" "+originpayment_id);
        reservationDao.updatepayment_idNative(newpayment_id, originpayment_id);
    }
    

}
