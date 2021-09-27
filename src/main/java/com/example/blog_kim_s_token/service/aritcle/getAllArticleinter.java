package com.example.blog_kim_s_token.service.aritcle;

import java.sql.Timestamp;

public interface getAllArticleinter {
    int getBid();
    int getBclicked();
    Timestamp getBcreated();
    String getBemail();
    String getBkind();
    String getTextarea();
    String getTitle();
    ///
    int getTotalcount();
}
