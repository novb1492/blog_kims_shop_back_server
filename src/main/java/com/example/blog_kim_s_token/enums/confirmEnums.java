package com.example.blog_kim_s_token.enums;


public enum confirmEnums {

    notEqualsPwd("비밀번호가 일치하지 않습니다",false),
    alreadyPhone("이미 존재하는 전화번호 입니다",false),
    alreadyEmail("이미 존재하는 이메일 입니다",false),
    sendSmsNum("인증번호가 발송되었습니다",true),
    timeOut("인증시간이 만료되었습니다",false),
    tooManyTime("하루 10회 제한입니다",false),
    nullPhoneNum("핸드폰 번호가 입력되지 않았습니다",false),
    nullPhoneNumInDb("인증번호 요청기록이 존재하지 않습니다",false),
    notEqulsPhoneNum("핸드폰번호가 변조 되었습니다",false),
    notEqulsTempNum("인증번호가 일치 하지 않습니다",false),
    overTime("인증시간이 초과되었습니다",false),
    notTruePhoneCheck("인증되지 않은 핸드폰 번호입니다",false),
    EqulsTempNum("인증 되었습니다",true),
    notFindEmail("없는 이메일 입니다",false),
    notReuestConfrim("인증번호 요청부터 해주세요",false),
    sendEmail("이메일을 전송했습니다",true),
    notFindCsrf("이미 로그아웃 했거나 csrf 미발급 사용자입니다",false),
    notEqualsCsrf("일치 하지 않는 로그아웃 시도",false);

    private  String messege;
    private  boolean bool;
   

    confirmEnums(String messege,boolean bool){
        this.messege=messege;
        this.bool=bool;
    }
    public void setMessege(String messege) {
        this.messege=messege;
    }
    public String getMessege() {
        return messege;
    }
    public Boolean getBool() {
        return bool;
    }
}
