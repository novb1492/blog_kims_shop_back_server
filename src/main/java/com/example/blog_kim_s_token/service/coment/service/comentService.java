package com.example.blog_kim_s_token.service.coment.service;

import java.util.List;

import com.example.blog_kim_s_token.model.article.articleDao;
import com.example.blog_kim_s_token.model.user.userDto;
import com.example.blog_kim_s_token.service.userService;
import com.example.blog_kim_s_token.service.utillService;
import com.example.blog_kim_s_token.service.aritcle.model.comentDao;
import com.example.blog_kim_s_token.service.aritcle.model.tryInsertComentDto;
import com.example.blog_kim_s_token.service.aritcle.model.tryUpdateComentDto;
import com.example.blog_kim_s_token.service.coment.model.comentDto;
import com.example.blog_kim_s_token.service.fileUpload.fileUploadService;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class comentService {
    @Autowired
    private comentDao comentDao;
    @Autowired
    private userService userService;
    @Autowired
    private articleDao articleDao;
    @Autowired
    private fileUploadService fileUploadService;

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
    public JSONObject updateComent(tryUpdateComentDto tryUpdateComentDto) {
        System.out.println("updateComent");
        try {
            comentDto comentDto=comentDao.findById(tryUpdateComentDto.getCid()).orElseThrow(()->new IllegalArgumentException("존재하지 않는 댓글입니다"));
            userDto userDto=userService.sendUserDto();
            confirmWriter(comentDto.getCemail(), userDto.getEmail());
            List<String>originImgs=utillService.getImgSrc(comentDto.getComent());
            List<String>newImgs=utillService.getImgSrc(tryUpdateComentDto.getComent());
            fileUploadService.CompareImgs(originImgs, newImgs);
            comentDto.setComent(tryUpdateComentDto.getComent());
            return utillService.makeJson(true, "댓글 수정 성공");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("updateComent error"+e.getMessage());
            throw new RuntimeException("댓글 수정 실패");
        }
    }
    public JSONObject deleteComent(int cid) {
        System.out.println("deleteComent");
        try {
            comentDto comentDto=comentDao.findById(cid).orElseThrow(()->new IllegalArgumentException("존재하지 않는 댓글입니다"));
            userDto userDto=userService.sendUserDto();
            confirmWriter(comentDto.getCemail(),userDto.getEmail());
            List<String>imgs=utillService.getImgSrc(comentDto.getComent());
            fileUploadService.deleteImages(imgs);
            comentDao.delete(comentDto);
            return utillService.makeJson(true, "댓글 삭제 성공");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("updateComent error"+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    private void confirmWriter(String originEmail,String loginEmail) {
        System.out.println("confirmWriter");
        if(!originEmail.equals(loginEmail)){
            throw new RuntimeException("작성자가 일치 하지 않습니다");
        }
    }
}
