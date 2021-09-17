package com.example.blog_kim_s_token.customException;






import org.springframework.dao.DataAccessException;

public class failCancleException extends DataAccessException {


    public failCancleException(String msg) {
        super(msg);
   
    }

}
