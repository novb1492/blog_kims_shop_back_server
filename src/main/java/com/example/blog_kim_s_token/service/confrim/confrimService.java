package com.example.blog_kim_s_token.service.confrim;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;


import com.example.blog_kim_s_token.enums.confirmEnums;
import com.example.blog_kim_s_token.model.confrim.confrimDao;
import com.example.blog_kim_s_token.model.confrim.confrimDto;
import com.example.blog_kim_s_token.model.confrim.emailCofrimDto;
import com.example.blog_kim_s_token.model.confrim.phoneCofrimDto;
import com.example.blog_kim_s_token.model.user.userDto;
import com.example.blog_kim_s_token.service.coolSmsService;
import com.example.blog_kim_s_token.service.sendEmailService;
import com.example.blog_kim_s_token.service.userService;
import com.example.blog_kim_s_token.service.utillService;
import com.nimbusds.jose.shaded.json.JSONObject;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;




@Service
public class confrimService {

    @Value("${confrim.coolTime}")
    private int coolTime;
    
    @Value("${confrim.overTime}")
    private int overTime;

    private final int t=1;
    private final int tempNumLength=6;
    private final int tempPwdLength=8;
    private final int maxOfday=3;

    @Autowired
    private confrimDao confrimDao;
    @Autowired
    private userService userService;
    @Autowired
    private sendEmailService sendEmailService;
  



    public confrimDto findConfrim(String phoneNum) {
        return confrimDao.findByPhoneNum(phoneNum);
    }
    public confrimDto findConfrimEmai(String email){
        return confrimDao.findByEmail(email);
    }
    public void insertConfrim(confrimInterface confrimInterface,String tempNum){
        System.out.println("insertConfrim");
        confrimDto dto=confrimInterface.getDto();
        dto.setRequestTime(1); 
        if(confrimInterface.unit().equals("phone")){
            dto.setPhoneTempNum(tempNum);
        }else{
            dto.setEmailTempNum(tempNum);
        }
        if(confrimInterface.getPassOneDay()){
            dto.setCreated(Timestamp.valueOf(LocalDateTime.now()));
        }
        confrimDao.save(dto);
    }

    public void updateconfrim(confrimInterface confrimInterface,String tempNum) {
        try {
            System.out.println("updateconfrim");
            int requestTime=confrimInterface.getRequestTime();
            requestTime+=1;
            confrimDto dto=confrimInterface.getDto();
            System.out.println(dto.getRequestTime()+"??????");
            dto.setRequestTime(requestTime);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("updateconfrim error");
           throw new RuntimeException();
        }
       
        
    }
    public void updateconfrimEmail(confrimDto confrimDto,String tempNum) {
        System.out.println("updateconfrimEmail"+tempNum+confrimDto.getEmail());
        int requestTime=confrimDto.getRequestTime();
        confrimDao.updateEmailTempNum(tempNum,requestTime+=1,utillService.getNowTimestamp(),confrimDto.getEmail());
    }
    public void deleteCofrim(confrimDto confrimDto){
        confrimDao.delete(confrimDto);
    }
    public void deleteCofrim(String phoneNum){
        confrimDao.deleteByPhoneNum(phoneNum);
    }
    public void sendSms(String phoneNum,String tempNum){
        coolSmsService.sendMessege(phoneNum,"??????????????? "+tempNum+"?????????");
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject sendPhone(HttpServletRequest request) {
        try {
        System.out.println("sendMessege ??????"+request.getParameter("phoneNum"));
        String phoneNum=request.getParameter("phoneNum");
        if(phoneNum.isEmpty()||phoneNum==null){
            System.out.println("???????????????");
            return utillService.makeJson(confirmEnums.nullPhoneNum.getBool(), confirmEnums.nullPhoneNum.getMessege());
        }
        confrimDto dto=findConfrim(phoneNum);
        if(dto==null){
            System.out.println("dto is null");
            dto=confrimDto.builder().phoneNum(phoneNum).build();
        }
        confrimInterface confrimInterface=new phoneConfrim(dto);
        String tempNum=utillService.GetRandomNum(tempNumLength);
        confirmEnums confirmEnums=sendSms(confrimInterface,tempNum);
        if(confirmEnums.getBool()){
           //sendSms(phoneNum, tempNum);
        };  
        return utillService.makeJson(confirmEnums.getBool(), confirmEnums.getMessege());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("sendPhone error");
            throw new RuntimeException("sendPhone ???????????? ????????????");
        }
    }
    public confirmEnums sendSms(confrimInterface confrimInterface,String tempNum) {
            if(confrimInterface.getRequestTime()==0){
                System.out.println("?????? ????????????"); 
                insertConfrim(confrimInterface,tempNum);
            }
            else{
                System.out.println("?????? ????????????");
                if(utillService.checkDate(confrimInterface.getCreated(),coolTime)){
                    System.out.println(maxOfday+"??? ????????? ??????????????? or ?????? ????????????");
                    confrimInterface.setPassOneDay(true);
                    insertConfrim(confrimInterface,tempNum);
                }
                else{
                    if(confrimInterface.getRequestTime()<maxOfday){
                        System.out.println(maxOfday+"??? ??????");
                        updateconfrim(confrimInterface, tempNum);
                    }else{
                        System.out.println(maxOfday+"????????? ?????? ?????????");
                        confirmEnums.tooManyTime.setMessege("?????? "+maxOfday+"??? ???????????????");
                        return confirmEnums.tooManyTime;
                    }
                }
            }
        return confirmEnums.sendSmsNum;
    }
    public JSONObject cofrimTempNum(phoneCofrimDto phoneCofrimDto) {
        System.out.println("cofrimTempNum");
        confrimDto confrimDto=confrimDao.findByPhoneNum(phoneCofrimDto.getPhoneNum());
        new confrimDtos<confrimDto>(confrimDto, "??????????????? ??? ?????????????????????");

        confrimInterface confrimInterface=new phoneConfrim(confrimDto);
        confirmEnums result=compareTempNum(confrimInterface,phoneCofrimDto.getTempNum());
        if( result.getBool()==false){
            return utillService.makeJson(result.getBool(), result.getMessege());
        }
        confrimDao.updatePhoneCheckTrue(t, phoneCofrimDto.getPhoneNum());
        return utillService.makeJson(confirmEnums.EqulsTempNum.getBool(), confirmEnums.EqulsTempNum.getMessege());
                   
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject sendEmail(String email) {
        System.out.println("sendEmail");
        userDto userDto=userService.findEmail(email);
        if(userDto==null){
            System.out.println("???????????? ?????? ?????????");
            return utillService.makeJson(confirmEnums.notFindEmail.getBool(), confirmEnums.notFindEmail.getMessege());
        }
        String tempNum=utillService.GetRandomNum(tempNumLength);
        confrimDto dto=findConfrimEmai(email);
        if(dto==null){
            dto=confrimDto.builder().email(email).build();
        }
        confrimInterface confrimInterface=new emailConfrim(dto);
        confirmEnums result=sendSms(confrimInterface, tempNum);
        if(result.getBool()){
            sendEmailService.sendEmail(email,"??????????????? kim's Shop?????????","??????????????? "+tempNum+" ?????????.");
        }
        return utillService.makeJson(result.getBool(), result.getMessege());

    }
    public void updateconfrimEmail(String email){
        System.out.println("updateconfrimEmail ?????? ??????????????? ??????");
        confrimDao.updateEmailCheckTrue(t, email);
    }
    public JSONObject sendTempPwd(emailCofrimDto emailCofrimDto) {
        System.out.println("sendTempPwd");
        confrimDto confrimDto=confrimDao.findByEmail(emailCofrimDto.getEmail());
        confrimInterface confrimInterface=new emailConfrim(confrimDto);
        confirmEnums result=compareTempNum(confrimInterface,emailCofrimDto.getTempNum());
        if(result.getBool()==false){
            return utillService.makeJson(result.getBool(), result.getMessege());
        }
        String tempPwd=utillService.GetRandomNum(tempPwdLength);
                        System.out.println(tempPwd+"??????????????????");
                        userService.updatePwd(confrimDto.getEmail(),tempPwd);
                        deleteCofrim(confrimDto);
                        sendEmailService.sendEmail(confrimDto.getEmail(),"??????????????? kim's Shop?????????","????????????????????? "+tempPwd+" ?????????.");
        return utillService.makeJson(true, "?????? ??????????????? ????????? ?????????????????????");
    }
    public confirmEnums compareTempNum(confrimInterface confrimInterface,String requestTempNum) {
        System.out.println("confrimTempNum ??????");
        if(confrimInterface.isNULL()){
            System.out.println("???????????? ???????????????");
            return confirmEnums.notReuestConfrim;
        }
        if(utillService.checkTime(confrimInterface.getCreated(),overTime)){
            System.out.println("?????? ??????????????? ??????");
            return confirmEnums.overTime; 
        }
        if(!requestTempNum.trim().equals(confrimInterface.TempNumAtDb())){
            System.out.println("???????????? ?????????");
            return confirmEnums.notEqulsTempNum;
        }
        return confirmEnums.EqulsTempNum;  
    } 
}
