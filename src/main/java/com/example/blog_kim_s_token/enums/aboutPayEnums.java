package com.example.blog_kim_s_token.enums;

public enum aboutPayEnums {
    
    statusPaid("paid"),
    statusReady("ready"),
    reservation("reservation"),
    food("food"),
    product("product"),
    kakaoPay("kakaoPay"),
    vbank("vbank"),
    cardmehtod("nxca_jt_il"),
    vbankmehthod("nx_mid_il");


    private String messege;

    aboutPayEnums(String messege){
        this.messege=messege;
    
    }
    public String getString() {
        return messege;
    }
}
