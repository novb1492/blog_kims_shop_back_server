package com.example.blog_kim_s_token.model.article;

import java.sql.Timestamp;

public interface getArticleInter {
    //article
    int getBid();
    int getBclicked();
    Timestamp getBcreated();
    String getBemail();
    String getBkind();
    String getTextarea();
    String getTitle();
    //coment
    int getCid();
    Timestamp getC_created();
    int getCbid();
    String getCemail();
    String getComent();
    //totalcount
    int getTotalcount();
}
