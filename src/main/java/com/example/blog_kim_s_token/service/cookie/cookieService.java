package com.example.blog_kim_s_token.service.cookie;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class cookieService {
    
    public static void cookieFactory(HttpServletResponse response,String[][] namesAndValues) {
        System.out.println("cookieFactory");
       /* ResponseCookie cookie2 = ResponseCookie.from("sameSiteCookie", "sameSiteCookieValue") 
                .sameSite("None") 
                .secure(true) .path("/") 
                .build(); 
                response.addHeader("Set-Cookie", cookie2.toString());*/


        for(int i =0;i<namesAndValues.length;i++){
            ResponseCookie cookie2 = ResponseCookie.from(namesAndValues[i][0], namesAndValues[i][1]) 
            .sameSite("None") 
            .secure(true) 
            .path("/") 
            .build(); 
            response.addHeader("Set-Cookie", cookie2.toString()+";HttpOnly");
                /*Cookie cookie=new Cookie(namesAndValues[i][0],namesAndValues[i][1]);
                if(namesAndValues[i][2]!=null&&namesAndValues[i][2].equals("httponly")){
                    cookie.setHttpOnly(true);  
                }
                cookie.setPath("/");
                response.addCookie(cookie);  */    
        }
    }
    public static List<Object> openCookie(Cookie[] cookies,List<String>cookiesName) {
        System.out.println("openCookie");
        List<Object>cList=new ArrayList<>();
        for(String s:cookiesName){
            for(Cookie c:cookies){
                if(c.getName().equals(s)){
                    cList.add(c.getValue());
                    break;
                }
                
            }
        }
        return cList;
    }
}
