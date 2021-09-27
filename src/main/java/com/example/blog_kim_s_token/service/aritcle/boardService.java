package com.example.blog_kim_s_token.service.aritcle;

import java.util.ArrayList;
import java.util.List;



import com.example.blog_kim_s_token.model.article.articleDao;
import com.example.blog_kim_s_token.model.article.articleDto;

import com.example.blog_kim_s_token.model.article.insertArticleDto;
import com.example.blog_kim_s_token.service.utillService;
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
    public List<articleDto> getArticle() {
        return articleDao.findAll();
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
