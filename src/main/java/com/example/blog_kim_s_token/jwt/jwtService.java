package com.example.blog_kim_s_token.jwt;

import java.sql.Timestamp;
import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.blog_kim_s_token.config.principaldetail;
import com.example.blog_kim_s_token.enums.userEnums;
import com.example.blog_kim_s_token.model.jwt.jwtDao;
import com.example.blog_kim_s_token.model.jwt.jwtDto;
import com.example.blog_kim_s_token.model.user.userDto;
import com.example.blog_kim_s_token.service.utillService;
import com.example.blog_kim_s_token.service.cookie.cookieService;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class jwtService {

    @Value("${jwt.sing}")
    private String jwtSing;
    @Value("${jwt.token.name}")
    private  String jwtTokenName;
    @Value("${jwt.refreshToken.validity}")
    private int refreshTokenValidity;
    @Value("${oauth.pwd}")
    private String oauthPwd;
    @Value("${jwt.accessToken.name}")
    private String AuthorizationTokenName;
    @Value("${jwt.refreshToken.name}")
    private String refreshTokenName;
    @Value("${jwt.type}")
    private String bearer;
    

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private jwtDao jwtDao;


    public  String getJwtToken(int id) {
        System.out.println("getJwtToken 토큰 제작시작");
        return JWT.create().withSubject(jwtTokenName).withExpiresAt(new Date(System.currentTimeMillis()+(1000*30))).withClaim("id",id).sign(Algorithm.HMAC512(jwtSing));
    }
    public  String getJwtToken() {
        System.out.println("getJwtToken 리프레시 토큰 제작시작");
        return JWT.create().withSubject(jwtTokenName).withExpiresAt(new Date(System.currentTimeMillis()+(86400*refreshTokenValidity))).sign(Algorithm.HMAC512(jwtSing));
    }
    private String getNewJwtToken(jwtDto jwtDto) {
        System.out.println(jwtDto.getUserid());
        if(jwtDto.getTokenName()!=null){
            return getJwtToken(jwtDto.getUserid());
        }else{
            System.out.println("존재하지 않는 토큰");
        }
        return null;
    }
    public int onpenJwtToken(String jwtToken) {
        System.out.println("onpenJwtToken");
        return JWT.require(Algorithm.HMAC512(jwtSing)).build().verify(jwtToken).getClaim("id").asInt();
    }
    public Authentication confrimAuthenticate(userDto dto) {
        principaldetail principaldetail=new principaldetail(dto);
        String pwd="";
        if(dto.getProvider()!=null){
            pwd=oauthPwd;
        }else{
            pwd=dto.getPwd();
        }
        //authenticationManager.authenticate() config/userDetailService/loadUserByUsername실행시켜 아이디비번검사
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getEmail(),pwd,principaldetail.getAuthorities()));
    }
    public Authentication makeAuthentication(userDto userDto) {
        System.out.println(userDto.getEmail()+" makeAuthentication 강제로그인");
        principaldetail principaldetail=new principaldetail(userDto);
        return new UsernamePasswordAuthenticationToken(principaldetail,null,principaldetail.getAuthorities());
    }
    public void setSecuritySession(Authentication authentication) {
        System.out.println("setSecuritySession");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    private jwtDto getRefreshToken(String refreshToken) {
        System.out.println(refreshToken+" getRefreshToken 찾기");
        return jwtDao.findByTokenName(refreshToken);
    }
    public jwtDto getRefreshToken(int userid) {
        System.out.println(userid+"기존 getRefreshToken 찾기");
        return jwtDao.findByUserid(userid);
    }
    private boolean checkRefreshTokenValidity(Timestamp refreshTokenDate) {
        return utillService.checkDate(refreshTokenDate,refreshTokenValidity);
    }
    private void insertRefreshToken(String refreshToken,int userid) {
        try {
            jwtDto jwtDto=new jwtDto(0, refreshToken, userid, null);
            jwtDao.save(jwtDto);
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    private void deleteRefreshToken(jwtDto jwtDto) {
        System.out.println("deleteRefreshToken 기한 만료 리프레시 토큰 제거");
        try {
            jwtDao.delete(jwtDto);
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
    public JSONObject deleteRefreshToken(String tokenName) {
        System.out.println("deleteRefreshToken 로그아웃 토큰제거");
        jwtDto jwtDto= jwtDao.findByTokenName(tokenName.replace(bearer+" ", ""));
        try {
            jwtDao.delete(jwtDto);
            return utillService.makeJson(userEnums.sucLogout.getBool(), userEnums.sucLogout.getMessege()); 
        } catch (InvalidDataAccessApiUsageException e) {
           e.printStackTrace();
           System.out.println("존재 하지 않는 토큰");
        }
        return utillService.makeJson(userEnums.failFindRefreshToken.getBool(), userEnums.failFindRefreshToken.getMessege()); 
    }
    public String getRefreshToken(jwtDto jwtDto,int userid) {
        System.out.println("getRefreshToken입장 기존 리프레시토큰 기간확인");
        String refreshToken="";
        if(jwtDto==null){
            refreshToken=getJwtToken();
            insertRefreshToken(refreshToken,userid);
        }else{
            if(checkRefreshTokenValidity(jwtDto.getCreated())){
                refreshToken=getJwtToken();
                deleteRefreshToken(jwtDto);
                insertRefreshToken(refreshToken, userid);
            }else{
                refreshToken=jwtDto.getTokenName();
            }
        }
        return refreshToken;
    }
    public void makeNewAccessToken(HttpServletRequest request,HttpServletResponse response) {
        System.out.println("makeNewAccessToken");
        Cookie[] cookies=request.getCookies();
        String refreshToken=null;
        for(Cookie c:cookies){
            if(c.getName().equals("refreshToken")){
                refreshToken=c.getValue();
            }
        }
        System.out.println(refreshToken+" 리프레시 토큰");
        jwtDto jwtDto=getRefreshToken(refreshToken);
        String newJwtToken=getNewJwtToken(jwtDto);
        System.out.println(newJwtToken+" 새 토큰");
        String[][] cookiesNamesAndValues=new String[1][3];
        cookiesNamesAndValues[0][0]="Authorization";
        cookiesNamesAndValues[0][1]=newJwtToken;
        cookiesNamesAndValues[0][2]="httponly";
        cookieService.cookieFactory(response, cookiesNamesAndValues); 
    }
}
