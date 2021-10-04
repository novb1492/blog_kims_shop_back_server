package com.example.blog_kim_s_token.service.aritcle;

import java.util.ArrayList;
import java.util.List;



import com.amazonaws.services.servicequotas.model.IllegalArgumentException;
import com.example.blog_kim_s_token.model.article.articleDao;
import com.example.blog_kim_s_token.model.article.articleDto;
import com.example.blog_kim_s_token.model.article.getArticleDto;
import com.example.blog_kim_s_token.model.article.getArticleInter;
import com.example.blog_kim_s_token.model.article.insertArticleDto;
import com.example.blog_kim_s_token.model.user.userDto;
import com.example.blog_kim_s_token.service.userService;
import com.example.blog_kim_s_token.service.utillService;
import com.example.blog_kim_s_token.service.aritcle.model.getAllArticleDto;
import com.example.blog_kim_s_token.service.aritcle.model.tryDeleteArticleDto;
import com.example.blog_kim_s_token.service.aritcle.model.tryUpdateArticleDto;
import com.example.blog_kim_s_token.service.fileUpload.fileUploadService;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class boardService {

    private final int pagesize=10;
    @Autowired
    private articleDao articleDao;
    @Autowired
    private userService userService;
    @Autowired
    private fileUploadService fileUploadService;

    public JSONObject insertArticle(insertArticleDto insertArticleDto) {
        System.out.println("insertArticle");
        try {
            articleDao.save(makeArticleDto(insertArticleDto, "후기 게시판"));
            return utillService.makeJson(true, "글등록 완료");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("insertArticle error"+e.getMessage());
            throw new RuntimeException("글등록에 실패했습니다");
        }
    }
    private articleDto makeArticleDto(insertArticleDto insertArticleDto,String kind) {
        System.out.println("makeArticleDto");
        articleDto dto=articleDto.builder()
                                    .bemail(SecurityContextHolder.getContext().getAuthentication().getName())
                                    .bkind(kind)
                                    .textarea(insertArticleDto.getTextarea())
                                    .title(insertArticleDto.getTitle())
                                    .bclicked(0)
                                    .build();
                                    return dto;
    }
    public JSONObject getArticle(getArticleDto getArticleDto) {
        System.out.println("getArticle");
        try {
            int bid=getArticleDto.getBid();
            int nowPage=getArticleDto.getPage();
            int first=utillService.getFirst(nowPage, pagesize);
            List<getArticleInter>getArticleinters=articleDao.findByBidJoinComment(bid, bid,first-1,pagesize).orElseThrow(()->new IllegalArgumentException("존재하지 않는 게시물입니다"));
            int totalPage=utillService.getTotalpages(getArticleinters.get(0).getTotalcount(),pagesize);
            boolean f=true;
            JSONObject article=new JSONObject();
            List<JSONObject>coments=new ArrayList<>();
            int clickUp=getArticleinters.get(0).getBclicked()+1;
            articleDao.plusClicked(clickUp, bid);
            for(getArticleInter g:getArticleinters){
                if(f){
                    System.out.println("글담기 시작");
                    article.put("bid", g.getBid());
                    article.put("title", g.getTitle());
                    article.put("email", g.getBemail());
                    article.put("text", g.getTextarea());
                    article.put("created", g.getBcreated());
                    article.put("clicked", clickUp);
                    f=false;
                    System.out.println("글담기 종료");
                }
               if(g.getC_created()!=null){
                JSONObject coment=new JSONObject();
                coment.put("email", g.getCemail());
                coment.put("text", g.getComent());
                coment.put("cid", g.getCid());
                coments.add(coment);
               }
            }
            JSONObject response=new JSONObject();
            response.put("bool", true);
            response.put("article", article);
            response.put("coments", coments);
            response.put("totalPage", totalPage);
            response.put("nowPage", 1);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("getArticle error"+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    public JSONObject getAllArticles(getAllArticleDto getAllArticleDto) {
        System.out.println("getAllArticles");
        int nowPage=getAllArticleDto.getNowPage();
        String title=getAllArticleDto.getTitle();
        try {
            List<getAllArticleinter>array=makeList(title, nowPage);
            if(array.size()==0){
                throw new RuntimeException("검색결과가 없습니다");
            }
            int totalPage=utillService.getTotalpages(array.get(0).getTotalcount(),pagesize);
            utillService.comparePage(nowPage, totalPage);
            List<JSONObject>articles=new ArrayList<>();
            for(getAllArticleinter a:array){
                JSONObject article=new JSONObject();
                article.put("id", a.getBid());
                article.put("writer", a.getBemail());
                article.put("click", a.getBclicked());
                article.put("created", a.getBcreated().toString().replace("T", " ").substring(0, 19));
                article.put("title", a.getTitle());
                article.put("text", a.getTextarea());
                article.put("kind", a.getBkind());
                articles.add(article);
            }
            JSONObject response=new JSONObject();
            response.put("bool", true);
            response.put("articles", articles);
            response.put("totalPage",totalPage);
            response.put("nowPage", nowPage);
            return response;
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("getAllArticles error"+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    private  List<getAllArticleinter> makeList(String title,int nowPage) {
        System.out.println("makeList");
        System.out.println(title+"타이틀");
        int first=utillService.getFirst(nowPage, pagesize);
        if(title==null){
            return articleDao.findALLOrderByDescBidLimiteNative(first-1, pagesize).orElseThrow(()->new RuntimeException("글이 존재 하지않습니다"));
        }
        return articleDao.findByTitleOrderByDescBidLimiteNative(title,title,first-1, pagesize).orElseThrow(()->new RuntimeException("글이 존재 하지않습니다"));
    }
    public JSONObject getArticle(int bid) {
        System.out.println("getArticle");
        try {
            articleDto articleDto=articleDao.findById(bid).orElseThrow(()->new IllegalArgumentException("존재하지 않는 게시글입니다"));
            userDto userDto=userService.sendUserDto();
            confrimUpdateArticle(articleDto.getBemail(),userDto.getEmail());
            JSONObject response=new JSONObject();
            response.put("bool", true);
            response.put("title", articleDto.getTitle());
            response.put("text", articleDto.getTextarea());
            return response;
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("getArticle error"+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    private void confrimUpdateArticle(String email,String loginEmail) {
        System.out.println("confrimUpdateArticle");
        if(!email.equals(loginEmail)){
            throw new RuntimeException("작성자가 일치 하지 않습니다");
        }
        System.out.println("글 수정 검사통과");
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject updateArticle(tryUpdateArticleDto tryUpdateArticleDto) {
        System.out.println("updateArticle");
        try {
            articleDto articleDto=articleDao.findById(tryUpdateArticleDto.getBid()).orElseThrow(()->new IllegalArgumentException("존재하지 않는 게시글입니다"));
            userDto userDto=userService.sendUserDto();
            confrimUpdateArticle(articleDto.getBemail(),userDto.getEmail());
            List<String>originImage=utillService.getImgSrc(articleDto.getTextarea());
            List<String>newImages=utillService.getImgSrc(tryUpdateArticleDto.getTextarea());
            fileUploadService.CompareImgs(originImage, newImages);
            articleDto.setTitle(tryUpdateArticleDto.getTitle());
            articleDto.setTextarea(tryUpdateArticleDto.getTextarea());
            return utillService.makeJson(true, "글수정 성공");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("getArticle error"+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    public JSONObject deleteArticle(tryDeleteArticleDto tryDeleteArticleDto) {
        System.out.println("deleteArticle");
        try {
            int bid=tryDeleteArticleDto.getBid();
            articleDto articleDto=articleDao.findById(bid).orElseThrow(()->new IllegalArgumentException("존재하지 않는 게시글입니다"));
            userDto userDto=userService.sendUserDto();
            confrimUpdateArticle(articleDto.getBemail(),userDto.getEmail());
            List<String>articleImages=utillService.getImgSrc(articleDto.getTextarea());
            fileUploadService.deleteImages(articleImages);
            articleDao.deleteArticleJoinComent(bid);
            return utillService.makeJson(true, "글삭제 성공");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("getArticle error"+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    public void cancleArticle(JSONObject jsonObject) {
        System.out.println("cancleArticle");
        List<String>imgs=utillService.getImgSrc((String)jsonObject.get("text"));
        fileUploadService.deleteImages(imgs);
    }
    
    



}
