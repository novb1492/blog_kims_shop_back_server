package com.example.blog_kim_s_token.service.aritcle;

import com.example.blog_kim_s_token.model.article.articleDao;
import com.example.blog_kim_s_token.model.article.articleDto;
import com.example.blog_kim_s_token.model.user.userDto;
import com.example.blog_kim_s_token.service.userService;
import com.example.blog_kim_s_token.service.utillService;
import com.example.blog_kim_s_token.service.aritcle.model.comentDao;
import com.example.blog_kim_s_token.service.aritcle.model.tryInsertComentDto;
import com.example.blog_kim_s_token.service.coment.model.comentDto;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class comentService {
    private final int min=0;
    private final int max=100;
    @Autowired
    private comentDao comentDao;
    @Autowired
    private userService userService;
    @Autowired
    private articleDao articleDao;

    public JSONObject insertComent(tryInsertComentDto tryInsertComentDto) {
        System.out.println("insertComent");
        try {
            int bid=tryInsertComentDto.getBid();
            if(articleDao.countByBid(bid)==0){
                throw new RuntimeException("존재하지 않는 게시물입니다");
            }
            userDto userDto=userService.sendUserDto();
            comentDto dto=comentDto.builder()
                                    .cbid(bid)
                                    .cemail(userDto.getEmail())
                                    .coment(tryInsertComentDto.getComent())
                                    .build();
                                    comentDao.save(dto);
            return utillService.makeJson(true, "댓글 등록 성공");
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("insertComent error"+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject updateComent(tryInsertComentDto tryInsertComentDto) {
        System.out.println("updateComent");
        try {
            userDto userDto=userService.sendUserDto();
            comentDto dto=comentDto.builder()
                                    .cbid(tryInsertComentDto.getBid())
                                    .cemail(userDto.getEmail())
                                    .coment(tryInsertComentDto.getComent())
                                    .build();
                                    comentDao.save(dto);
            return utillService.makeJson(true, "댓글 수정 성공");
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("updateComent error"+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
