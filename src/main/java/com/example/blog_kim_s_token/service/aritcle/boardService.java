package com.example.blog_kim_s_token.service.aritcle;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import com.amazonaws.services.servicequotas.model.IllegalArgumentException;
import com.example.blog_kim_s_token.model.article.articleDao;
import com.example.blog_kim_s_token.model.article.articleDto;
import com.example.blog_kim_s_token.model.article.getArticleDto;
import com.example.blog_kim_s_token.model.article.getArticleInter;
import com.example.blog_kim_s_token.model.article.insertArticleDto;
import com.example.blog_kim_s_token.service.utillService;
import com.mysql.cj.xdevapi.JsonArray;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class boardService {

    private final int pagesize=10;
    @Autowired
    private articleDao articleDao;

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
            int first=utillService.getFirst(1, pagesize);
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
                    article.put("title", g.getTitle());
                    article.put("email", g.getBemail());
                    article.put("text", g.getTextarea());
                    article.put("created", g.getBcreated());
                    article.put("clicked", clickUp);
                    f=false;
                    System.out.println("글담기 종료");
                }
                JSONObject coment=new JSONObject();
                coment.put("email", g.getCemail());
                coment.put("text", g.getComent());
                coments.add(coment);
            }
            JSONObject response=new JSONObject();
            response.put("bool", true);
            response.put("article", article);
            response.put("coments", coments);
            response.put("totalPage", totalPage);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("getArticle error"+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    public JSONObject getAllArticles(getAllArticleDto getAllArticleDto) {
        System.out.println("getAllArticles");
        List<getAllArticleinter>jsonObjects=new ArrayList<>();
        int nowPage=getAllArticleDto.getNowPage();
        String title=getAllArticleDto.getTitle();
        try {
            List<getAllArticleinter>array=makeList(title, nowPage);
            int totalPage=utillService.getTotalpages(array.get(0).getTotalcount(),pagesize);
            for(getAllArticleinter a:array){
                jsonObjects.add(a);
            }
            JSONObject response=new JSONObject();
            response.put("articles", array);
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
        if(title==null){
            return articleDao.findALLOrderByDescBidLimiteNative(utillService.getFirst(nowPage, pagesize)-1, pagesize).orElseThrow(()->new RuntimeException("글이 존재 하지않습니다"));
        }
        return articleDao.findByTitleOrderByDescBidLimiteNative(title,utillService.getFirst(nowPage, pagesize)-1, pagesize).orElseThrow(()->new RuntimeException("글이 존재 하지않습니다"));
    }



}
