package com.example.blog_kim_s_token.enums;

public enum trueFalseEnum {
    sucCheck(null,true),
    failCheck(null,false);

    private  String messege;
    private  Boolean bool;
   

    trueFalseEnum(String messege,Boolean bool){
        this.messege=messege;
        this.bool=bool;
    }
    public void setperiod(String messege) {
        this.messege=messege;
    }
    public String getperiod() {
        return messege;
    }
    public Boolean getBool() {
        return bool;
    }
}
