package com.example.blog_kim_s_token.service.ApiServies.naver;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.blog_kim_s_token.config.security;
import com.example.blog_kim_s_token.enums.confrimTrue;
import com.example.blog_kim_s_token.enums.role;
import com.example.blog_kim_s_token.jwt.jwtService;
import com.example.blog_kim_s_token.model.jwt.jwtDto;
import com.example.blog_kim_s_token.model.oauth.naver.naverDto;
import com.example.blog_kim_s_token.model.user.userDao;
import com.example.blog_kim_s_token.model.user.userDto;
import com.example.blog_kim_s_token.service.csrfTokenService;
import com.example.blog_kim_s_token.service.cookie.cookieService;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;



@Service
public class naverLoginService   {
    
    private final String id="DrqDuzgTpM_sfreaZMly";
    private final String pwd="wCLQZ1kaQT";
    private final String callBackUrl="http://localhost:8080/auth/navercallback";
    private final String naver="naver";
    @Value("${oauth.pwd}")
    private String oauthPwd;
    @Value("${jwt.accessToken.name}")
    private String AuthorizationTokenName;
    @Value("${jwt.refreshToken.name}")
    private String refreshTokenName;

    private RestTemplate restTemplate=new RestTemplate();
    private HttpHeaders headers=new HttpHeaders();

    @Autowired
    private userDao dao;
    @Autowired
    private security security;
    @Autowired
    private jwtService jwtService;
    @Autowired
    private csrfTokenService csrfService;



    public String naverLogin() {
        String state="";
        try {
            state = URLEncoder.encode(callBackUrl, "UTF-8");
            return "https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id="+id+"&redirect_uri="+""+callBackUrl+""+"&state="+state+"";
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            throw new RuntimeException("naverLogin 오류 발생");
        } 
    }
    public JSONObject getNaverToken(String code,String state) {
         JSONObject jsonObject= restTemplate.getForObject("https://nid.naver.com/oauth2.0/token?grant_type=authorization_code&client_id="+id+"&client_secret="+pwd+"&code="+code+"&state="+state+"",JSONObject.class);
         System.out.println(jsonObject+" token"); 
         return jsonObject;
     }
     public void LoginNaver(JSONObject jsonObject,HttpServletRequest request,HttpServletResponse response) {
        headers.add("Authorization", "Bearer "+jsonObject.get("access_token"));
        HttpEntity<JSONObject>entity=new HttpEntity<JSONObject>(headers);
        try {
           naverDto naverDto =restTemplate.postForObject("https://openapi.naver.com/v1/nid/me",entity,naverDto.class);
           System.out.println(naverDto+ "정보");
           
           String email=(String)naverDto.getResponse().get("email");

               userDto dto=dao.findByEmail(email);
               if(dto==null){
                dto=userDto.builder().email(email)
                                    .name((String)naverDto.getResponse().get("name"))
                                    .pwd(security.pwdEncoder().encode(oauthPwd))
                                    .postCode("111111")
                                    .address("address")
                                    .detailAddress("detailAddress")
                                    .phoneNum((String)naverDto.getResponse().get("mobile"))
                                    .phoneCheck(confrimTrue.yes.getValue())
                                    .emailCheck(confrimTrue.yes.getValue())
                                    .role(role.USER.getValue())
                                    .provider(naver).build();
                                    dao.save(dto);
               }
               Authentication authentication=jwtService.confrimAuthenticate(dto);
               jwtService.setSecuritySession(authentication);
   
               String jwtToken=jwtService.getJwtToken(dto.getId());
               jwtDto jwtDto=jwtService.getRefreshToken(dto.getId());
               String refreshToken=jwtService.getRefreshToken(jwtDto,dto.getId());
               String csrfToken=csrfTokenService.getCsrfToken();
               csrfService.insertCsrfToken(dto.getId(),csrfToken,dto.getEmail());

               String[][] cookiesNamesAndValues=new String[3][3];
               cookiesNamesAndValues[0][0]=AuthorizationTokenName;
               cookiesNamesAndValues[0][1]=jwtToken;
               cookiesNamesAndValues[0][2]="httponly";
               cookiesNamesAndValues[1][0]=refreshTokenName;
               cookiesNamesAndValues[1][1]=refreshToken;
               cookiesNamesAndValues[1][2]="httponly";
               cookiesNamesAndValues[2][0]="csrfToken";
               cookiesNamesAndValues[2][1]=csrfToken;
               cookiesNamesAndValues[2][2]="httponly";
               cookieService.cookieFactory(response, cookiesNamesAndValues);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("LoginNaver 오류가 발생 했습니다");
        }finally{
            headers.clear();
        }
     }
     
}
