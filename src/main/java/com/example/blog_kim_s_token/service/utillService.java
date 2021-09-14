package com.example.blog_kim_s_token.service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;



import com.nimbusds.jose.shaded.json.JSONObject;




public class utillService {
    
    public static boolean checkDate(Timestamp timestamp,int refreshTokenValidity) {
        System.out.println(timestamp+"토큰 기간");
        System.out.println("날짜 비교 시작");
        LocalDateTime timestamp2=timestamp.toLocalDateTime();
        timestamp2=timestamp2.plusDays(refreshTokenValidity);
        LocalDateTime today= LocalDateTime.now(); 
        if(timestamp2.isBefore(today)){
            System.out.println("날짜가 지났습니다");
           return true;
        }
        return false;
    }
    public static Map<String,String> getTrdDtTrdTm() {
        System.out.println("getTrdDtTrdTm");
        Timestamp timestamp=Timestamp.valueOf(LocalDateTime.now());
        System.out.println(timestamp+" 시간");
        String[] spl=timestamp.toString().split(" ");
        String trdDt=spl[0].replace("-","");
        System.out.println(trdDt+" 요일");
        String min=LocalDateTime.now().getMinute()+"";
        String second=LocalDateTime.now().getSecond()+"";
        String hour=LocalDateTime.now().getHour()+"";
        if(hour.length()<2){
            hour="0"+hour;
        }
        if(min.length()<2){
            min="0"+min;
        }
        if(second.length()<2){
            second="0"+second;
        }
        String trdTm=hour+min+second;
        System.out.println(trdTm+" 요일");
        Map<String,String>map=new HashMap<>();
        map.put("trdDt", trdDt);
        map.put("trdTm", trdTm);
        return map;
    }
    public static boolean compareDate(Timestamp timestamp,LocalDateTime localDateTime) {
        System.out.println("checkDate");
        System.out.println("날짜 비교 시작");
        LocalDateTime timestamp2=timestamp.toLocalDateTime(); 
        if(timestamp2.getDayOfMonth()==localDateTime.getDayOfMonth()){
            System.out.println("당일입니다");
            return false;
        }
        if(timestamp2.isBefore(localDateTime)){
            System.out.println("날짜가 지났습니다");
           return true;
        }
        return false;
    }
    public static boolean checkTime(Timestamp timestamp,int totalTokenTime) {
        System.out.println(timestamp+"인증시간");
        System.out.println("날짜 비교 시작");
        LocalDateTime timestamp2=timestamp.toLocalDateTime();
        timestamp2=timestamp2.plusMinutes(totalTokenTime);
        LocalDateTime today= LocalDateTime.now(); 
        if(timestamp2.isBefore(today)){
            System.out.println("시간이 지났습니다");
           return true;
        }
        return false;
    }
    public static boolean checkDate(Timestamp timestamp) {
        System.out.println(timestamp+"인증시간");
        System.out.println("날짜 비교 시작");
        LocalDateTime timestamp2=timestamp.toLocalDateTime();
        timestamp2=timestamp2.plusSeconds(10);
        LocalDateTime today= LocalDateTime.now(); 
        if(timestamp2.isBefore(today)){
            System.out.println("시간이 지났습니다");
           return true;
        }
        return false;
    }
    public static Timestamp getNowTimestamp() {
        System.out.println("getNowTimestamp 현재 시간 가져오기");
        return new Timestamp(System.currentTimeMillis());
    }
    public static JSONObject makeJson(boolean bool,String messege) {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("bool",bool);
        jsonObject.put("messege", messege);
        return jsonObject;
    }
    public static JSONObject makeJson(boolean bool,int messege) {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("bool",bool);
        jsonObject.put("messege", messege);
        return jsonObject;
    }
    public static JSONObject makeJson(boolean bool,String messege,List<String>list) {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("bool",bool);
        jsonObject.put("messege", messege);
        jsonObject.put("errorPart",list);
        return jsonObject;
    }
    public static String GetRandomNum(int end) {
        String num="";
        Random random=new Random();
        for(int i=0;i<end;i++){
            num+=Integer.toString(random.nextInt(10));
        }
        return num;
    } 
    public static int getTotalpages(int totalCount,int pagesize) {
        int totalpage=0;
        totalpage=totalCount/pagesize;
        if(totalCount%pagesize>0){
            totalpage++;
        }
        System.out.println(totalpage);
        return totalpage;
    }
    public static int getFirst(int page,int pagesize) {
        return (page-1)*pagesize+1;
    }
    public static int getEnd(int fisrt,int pagesize) {
        return fisrt+pagesize-1;
    }
    public static long getDateGap(Calendar requestDate,String requestDate2) {
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(requestDate2);
            Calendar cmpDate = Calendar.getInstance();
            cmpDate.setTime(date);
            long diffSec = (cmpDate.getTimeInMillis()-requestDate.getTimeInMillis()) / 1000;
            long diffDays = diffSec / (24*60*60); 
            System.out.println(diffDays+" 날짜 차이");
            return diffDays;
        }catch (ParseException e) {
            e.printStackTrace();
            System.out.println("getDateGap error");
            throw new RuntimeException();
        }
    }



}
