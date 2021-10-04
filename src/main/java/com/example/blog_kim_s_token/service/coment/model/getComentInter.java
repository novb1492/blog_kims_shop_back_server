package com.example.blog_kim_s_token.service.coment.model;

import java.sql.Timestamp;

public interface getComentInter {
    int getCid();
    Timestamp getC_created();
    int getCbid();
    String getCemail();
    String getComent();
    int getTotalcount();
}
