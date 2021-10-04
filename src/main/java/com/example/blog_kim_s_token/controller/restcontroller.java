package com.example.blog_kim_s_token.controller;





import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.blog_kim_s_token.jwt.jwtService;
import com.example.blog_kim_s_token.model.article.getArticleDto;
import com.example.blog_kim_s_token.model.article.insertArticleDto;
import com.example.blog_kim_s_token.model.confrim.emailCofrimDto;
import com.example.blog_kim_s_token.model.confrim.phoneCofrimDto;
import com.example.blog_kim_s_token.model.payment.getHashInfor;
import com.example.blog_kim_s_token.model.payment.reseponseSettleDto;
import com.example.blog_kim_s_token.model.reservation.getDateDto;
import com.example.blog_kim_s_token.model.reservation.getTimeDto;
import com.example.blog_kim_s_token.model.user.addressDto;
import com.example.blog_kim_s_token.model.user.phoneDto;
import com.example.blog_kim_s_token.model.user.pwdDto;
import com.example.blog_kim_s_token.model.user.singupDto;
import com.example.blog_kim_s_token.model.user.userDto;
import com.example.blog_kim_s_token.service.productService;
import com.example.blog_kim_s_token.service.userService;
import com.example.blog_kim_s_token.service.utillService;
import com.example.blog_kim_s_token.service.ApiServies.kakao.kakaoService;
import com.example.blog_kim_s_token.service.ApiServies.kakao.tryKakaoPayDto;
import com.example.blog_kim_s_token.service.ApiServies.naver.naverLoginService;
import com.example.blog_kim_s_token.service.aritcle.boardService;
import com.example.blog_kim_s_token.service.aritcle.model.getAllArticleDto;
import com.example.blog_kim_s_token.service.aritcle.model.tryDeleteArticleDto;
import com.example.blog_kim_s_token.service.aritcle.model.tryUpdateArticleDto;
import com.example.blog_kim_s_token.service.coment.model.tryInsertComentDto;
import com.example.blog_kim_s_token.service.coment.model.tryUpdateComentDto;
import com.example.blog_kim_s_token.service.coment.service.comentService;
import com.example.blog_kim_s_token.service.confrim.confrimService;
import com.example.blog_kim_s_token.service.fileUpload.fileUploadService;
import com.example.blog_kim_s_token.service.payment.paymentService;
import com.example.blog_kim_s_token.service.payment.model.cancle.tryCancleDto;
import com.example.blog_kim_s_token.service.reservation.reservationService;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;



@RestController
public class restcontroller {
    @Autowired
    private userService userService;
    @Autowired
    private confrimService confrimService;
    @Autowired
    private naverLoginService naverLoingService;
    @Autowired
    private kakaoService kakaoService;
    @Autowired
    private reservationService resevationService;
    @Autowired
    private fileUploadService fileUploadService;
    @Autowired
    private paymentService paymentService;
    @Autowired
    private boardService boardService;
    @Autowired
    private jwtService jwtService;
    @Autowired
    private productService productService;
    @Autowired
    private comentService comentService;


    @PostMapping("/auth/confrimEmail")
    public boolean confrimEmail(HttpServletRequest request,HttpServletResponse response) {
        return userService.confrimEmail((String)request.getParameter("email"));
    }
    @PostMapping("/auth/confrimPhoneNum")
    public boolean confrimPhoneNum(HttpServletRequest request,HttpServletResponse response) {
        return userService.confrimPhone((String)request.getParameter("phoneNum"));
    }
    @PostMapping("/auth/sendSms")
    public JSONObject sendSms(HttpServletRequest request,HttpServletResponse response) {
        return confrimService.sendPhone(request);
    }
    @PostMapping("/auth/cofrimSmsNum")
    public JSONObject cofrimSmsNum(@Valid @RequestBody phoneCofrimDto phoneCofrimDto,HttpServletResponse response) {
        return confrimService.cofrimTempNum(phoneCofrimDto);
    }
    @PostMapping("/auth/insertUser")
    public JSONObject insertUser(@Valid @RequestBody singupDto singupDto) {
        return userService.insertUser(singupDto);
    }
    @PostMapping("/login")
    public JSONObject login(HttpServletRequest request,HttpServletResponse response) {
        return userService.doLogin();
    }
    @PostMapping("/auth/findEmail")
    public JSONObject findEmail(HttpServletRequest request,HttpServletResponse response) {
        return userService.findLostEmail(request.getParameter("phoneNum"));
    }
    @PostMapping("/auth/sendEmail")
    public JSONObject sendEmail(HttpServletRequest request,HttpServletResponse response) {
        return confrimService.sendEmail(request.getParameter("email"));
    }
    @PostMapping("/auth/sendTempPwd")
    public JSONObject sendTempPwd(@Valid @RequestBody emailCofrimDto emailCofrimDto,HttpServletResponse response) {
        return confrimService.sendTempPwd(emailCofrimDto);
    }
    @PostMapping("/auth/naver")
    public String naverLogin() {
        return  naverLoingService.naverLogin();
    }
    @PostMapping("/auth/kakao")
    public String kakaoLogin(HttpServletRequest request,HttpServletResponse response) {
        return kakaoService.kakaoGetLoginCode();
    }
    @PostMapping("/api/userInfor")
    public userDto getUserInfor(HttpServletRequest request,HttpServletResponse response) {
        return userService.sendUserDto();
    }
    @PostMapping("/api/email")
    public JSONObject getEmail(HttpServletRequest request,HttpServletResponse response) {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("email", userService.sendUserDto().getEmail());
        return jsonObject;
    }
    @RequestMapping("/auth/jwtex")
    public void TokenExpired() {
        System.out.println("auth/jwtex");
        throw new TokenExpiredException(null);
    }
    @PostMapping("/api/logout")
    public JSONObject logout(HttpServletRequest request,HttpServletResponse response) {
        System.out.println("logout");
        userService.logout(request, response);
        return utillService.makeJson(true, "로그아웃완료");
    }
    @PostMapping("/api/updateAddress")
    public JSONObject updateAddress(@Valid @RequestBody addressDto addressDto,HttpServletResponse response) {
        System.out.println("updateAddress");
        return  userService.updateAddress(addressDto);
    }
    @PostMapping("/api/updatePhoneNum")
    public JSONObject changePhoneNum(@Valid @RequestBody phoneDto phoneDto,HttpServletResponse response) {
        System.out.println("updatePhoneNum");
        return userService.updatephoneNum(phoneDto);
    }
    @PostMapping("/api/updatePwd")
    public JSONObject changePhoneNum(@Valid @RequestBody pwdDto pwdDto,HttpServletResponse response) {
        System.out.println("updatePwd");
        return userService.updatePwd(pwdDto);
    }
    @PostMapping("/auth/failOpenToken")
    public void onlyBearer() {
        throw new JWTDecodeException(null);
    }
    @PostMapping("/api/getDateBySeat")
    public JSONObject getDateBySeat(@Valid @RequestBody getDateDto getDateDto,HttpServletResponse response) {
        System.out.println("getDateBySeat");
        return resevationService.getDateBySeat(getDateDto);
    }
    @PostMapping("/api/getTimeByDate")
    public JSONObject getTimeByDate(@Valid @RequestBody getTimeDto getTimeDto,HttpServletResponse response) {
        System.out.println("getTimeByDate");
        return resevationService.getTimeByDate(getTimeDto);
    }
    @PostMapping("/api/getClientReservation")
    public JSONObject getClientReservation(@RequestBody JSONObject JSONObject,HttpServletResponse response) {
        System.out.println("getClientReservation");
       return resevationService.getClientReservation(JSONObject);
    }
    @PostMapping("/auth/reseponseAtImp")
    public void bootPay(@RequestBody JSONObject jsonObject,HttpServletResponse response) {
        System.out.println("payment");
        //paymentService.vbankOk(jsonObject);
    }
    @PostMapping("/api/kakaopay")
    public JSONObject getKakaoPayLink(@Valid @RequestBody tryKakaoPayDto tryKakaoPayDto,HttpServletRequest request,HttpServletResponse response) {
        System.out.println("getKakaoPayLink");
        return kakaoService.showPaidWindow(tryKakaoPayDto, request, response);
    }
    @RequestMapping("/api/okKakaopay")
    public void okKakaopay(HttpServletRequest request,HttpSession session,HttpServletResponse response) {
        System.out.println("okKakaopay");
        kakaoService.requestKakaopay(request.getParameter("pg_token"),session);
        doRedirect(response,"https://localhost:8443/doneKakaoPagevar1.html?nextUrl=showReservationPage.html");
    }
    @PostMapping("/api/canclePay")
    public JSONObject canclePay(@Valid @RequestBody tryCancleDto tryCancleDto,HttpServletRequest request,HttpServletResponse response) {
        System.out.println("canclePay"); 
        return paymentService.cancel(tryCancleDto);
    }
    @PostMapping("/api/imageUpload")
    public JSONObject imageUpload(@RequestParam("file")MultipartFile multipartFile,HttpServletRequest request,HttpServletResponse response) {
        System.out.println("imageUpload"); 
        return fileUploadService.awsS3ImageUpload(multipartFile);
    }
    @PostMapping("/api/insertArticle")
    public JSONObject insertArticle(@Valid @RequestBody insertArticleDto insertArticleDto,HttpServletResponse response) {
        System.out.println("insertArticle"); 
        return boardService.insertArticle(insertArticleDto);
    }
    @PostMapping("/auth/getArticle")
    public JSONObject getArticle(@Valid @RequestBody getArticleDto getArticleDto,HttpServletResponse response) {
        System.out.println("getArticle"); 
        return  boardService.getArticle(getArticleDto);
    }
    @PostMapping("/auth/getAllArticle")
    public JSONObject getAllArticle(@Valid @RequestBody getAllArticleDto getAllArticleDto,HttpServletRequest request,HttpServletResponse response) {
        System.out.println("getAllArticle"); 
        return boardService.getAllArticles(getAllArticleDto);
    }
    @PostMapping("/api/kakaoMore")
    public JSONObject kakaoMore(HttpServletRequest request,HttpServletResponse response) {
        System.out.println("kakaoMore");
        return utillService.makeJson(true, kakaoService.getMoreOk(request)); 
    }
    @RequestMapping("/api/sendKakaoMessage")
    public void sendKakaoMessage(HttpServletRequest request,HttpServletResponse response) {
        System.out.println("sendKakaoMessage");
        kakaoService.sendMessege();
    }
    @RequestMapping("/auth/kakaoLogincallback")
    public void kakaoLogincallback(HttpServletRequest request,HttpServletResponse response) {
        System.out.println("kakaologin요청");   
        kakaoService.kakaoLogin(request.getParameter("code"),response);
        doRedirect(response, "https://localhost:8443/kakaoplusOkPage.html");
    }
    @RequestMapping("/auth/kakaoMoreOkcallback")
    public void kakaoMoreOkcallback(HttpServletRequest request,HttpServletResponse response) {
        System.out.println("kakaoMoreOkcallback"+request.getHeader("REFERER"));
        doRedirect(response, "https://localhost:8443/kakaoPlusOkDetailPage.html");

    }
    @RequestMapping("/auth/navercallback")
    public void naverRollback(HttpServletRequest request,HttpServletResponse response) {
        System.out.println("naverlogin요청");
        naverLoingService.LoginNaver(naverLoingService.getNaverToken(request.getParameter("code"), request.getParameter("state")),request,response);
        doRedirect(response, "https://localhost:8443/doneNaverLogin.html");
    }
    @PostMapping("/api/getSha256Hash")
    public JSONObject getSha256Hash(@RequestBody getHashInfor getHashInfor,HttpServletRequest request,HttpServletResponse response) {
        System.out.println("getSha256Hash");
        jwtService.makeNewAccessToken(request, response);
        return paymentService.makeTohash(getHashInfor);
    }
    @RequestMapping("/auth/settlebank")
    public void settlebank(reseponseSettleDto reseponseSettleDto,HttpServletResponse response) {
        System.out.println("settlebank");
        System.out.println(reseponseSettleDto.toString());
        paymentService.tryUpdateVbank(reseponseSettleDto);
        
    }
    @PostMapping("/api/confrimSettle")
    public JSONObject confrimSettle(@RequestBody reseponseSettleDto reseponseSettleDto,HttpServletResponse response) {
        System.out.println("confrimSettle");
        System.out.println(reseponseSettleDto.toString());
        return paymentService.confrimSettle(reseponseSettleDto);
    }
    @PostMapping("/api/getItems")
    public JSONObject getItem(HttpServletRequest request,HttpServletResponse response) {
        System.out.println("getItems");
        return productService.getItems(request);
    }
    @PostMapping("/api/getOnlyArticle")
    public JSONObject getOnlyArticle(@RequestBody JSONObject jsonObject,HttpServletResponse response) {
        System.out.println("getOnlyArticle");
        return boardService.getArticle(Integer.parseInt(jsonObject.getAsString("bid")));
    }
    @PostMapping("/api/updateArticle")
    public JSONObject updateArticle(@Valid @RequestBody tryUpdateArticleDto tryUpdateArticleDto,HttpServletResponse response) {
        System.out.println("updateArticle");
        return boardService.updateArticle(tryUpdateArticleDto);
    }
    @PostMapping("/api/deleteArticle")
    public JSONObject deleteArticle(@Valid @RequestBody tryDeleteArticleDto tryDeleteArticleDto,HttpServletResponse response) {
        System.out.println("updateArticle");
        return boardService.deleteArticle(tryDeleteArticleDto);
    }
    @PostMapping("/api/insertComent")
    public JSONObject insertComent(@Valid @RequestBody tryInsertComentDto tryInsertComentDto,HttpServletResponse response) {
        System.out.println("insertComent");
        return comentService.insertComent(tryInsertComentDto);
    }
    @PostMapping("/api/deleteComent")
    public JSONObject deleteComent(@RequestBody JSONObject JSONObject,HttpServletResponse response) {
        System.out.println("deleteComent");
        return comentService.deleteComent(Integer.parseInt(JSONObject.getAsString("cid")));
    }
    @PostMapping("/api/updateComent")
    public JSONObject updateComent(@Valid @RequestBody tryUpdateComentDto tryUpdateComentDto,HttpServletResponse response) {
        System.out.println("deleteComent");
        return comentService.updateComent(tryUpdateComentDto);
    }
    @PostMapping("/auth/getComent")
    public JSONObject getComent(@Valid @RequestBody getArticleDto getArticleDto,HttpServletResponse response) {
        System.out.println("getComent");
        return comentService.getComent(getArticleDto);
    }
    @PostMapping("/api/v1/user/test")
    public JSONObject  user(HttpServletRequest request,HttpServletResponse response) {
        System.out.println("user 입장");
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("role", "user");
        jsonObject.put("hello", "world");
        return jsonObject;
    }
    @PostMapping("/api/v1/manage/test")
    public String  manage(HttpServletRequest request,HttpServletResponse response) {
        System.out.println("manage 입장");
        return "manage";
    }
    @PostMapping("/api/v1/admin/test")
    public String  admin(HttpServletRequest request,HttpServletResponse response) {
        System.out.println("admin 입장");
        return "admin";
    }
    private void doRedirect(HttpServletResponse response,String url) {
        System.out.println("doRedirect");
        System.out.println(url+"리다이렉트 요청 url");
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("doRedirect error"+e.getMessage());
        }
    }

    
}
