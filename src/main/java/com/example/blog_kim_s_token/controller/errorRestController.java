package com.example.blog_kim_s_token.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.blog_kim_s_token.customException.failBuyException;
import com.example.blog_kim_s_token.customException.failKakaoPay;
import com.example.blog_kim_s_token.jwt.jwtService;
import com.example.blog_kim_s_token.service.utillService;
import com.example.blog_kim_s_token.service.ApiServies.kakao.kakaoService;
import com.example.blog_kim_s_token.service.ApiServies.kakao.kakaopayService;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class errorRestController {
    @Autowired
    private jwtService jwtService;
    @Autowired
    private kakaoService kakaoService;

    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public JSONObject processValidationError(MethodArgumentNotValidException exception) {
        System.out.println("processValidationError 유효성 검사 실패");
        BindingResult bindingResult = exception.getBindingResult();
        StringBuilder builder = new StringBuilder();
        List<String>list=new ArrayList<>();
        
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            builder.append(fieldError.getDefaultMessage());
            list.add(fieldError.getField());
        }

        return utillService.makeJson(false, builder.toString(),list);
    }
    @ExceptionHandler(TokenExpiredException.class)
    public JSONObject TokenExpiredException(TokenExpiredException exception,HttpServletRequest request,HttpServletResponse response) {
        jwtService.makeNewAccessToken(request, response);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("status", "newJwtToken");
        System.out.println("새토큰 발급완료");
        return jsonObject;
    }
    @ExceptionHandler(JWTDecodeException.class)
    public JSONObject JWTDecodeException(JWTDecodeException exception,HttpServletRequest request,HttpServletResponse response) {
        System.out.println("JWTDecodeException 입장");
        return utillService.makeJson(false, "재 로그인 부탁드립니다");
    }
    @ExceptionHandler(RuntimeException.class)
    public JSONObject runtimeException(RuntimeException exception) {
        System.out.println("runtimeException");
        exception.printStackTrace();
        return utillService.makeJson(false, exception.getMessage());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public JSONObject IllegalArgumentException(IllegalArgumentException exception) {
        System.out.println("IllegalArgumentException");
        return utillService.makeJson(false, exception.getMessage());
    }
    @ExceptionHandler(failKakaoPay.class)
    public JSONObject failKakaoPay(failKakaoPay failKakaoPay) {
        MultiValueMap<String,Object> body=new LinkedMultiValueMap<>();
        body.add("cid", failKakaoPay.getCid());
        body.add("tid", failKakaoPay.getTid());
        body.add("cancel_amount", failKakaoPay.getTotalPrice());
        body.add("cancel_tax_free_amount",0);
        kakaoService.cancleKakaopay(body);
        return utillService.makeJson(false, failKakaoPay.getMessage());
    }
}
