package com.example.blog_kim_s_token.service.aritcle;

import com.example.blog_kim_s_token.model.user.userDto;
import com.example.blog_kim_s_token.service.userService;
import com.example.blog_kim_s_token.service.utillService;
import com.example.blog_kim_s_token.service.aritcle.model.comentDao;
import com.example.blog_kim_s_token.service.aritcle.model.tryInsertComentDto;
import com.example.blog_kim_s_token.service.coment.model.comentDto;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class comentService {
    @Autowired
    private comentDao comentDao;
    @Autowired
    private userService userService;

    public JSONObject insertComent(tryInsertComentDto tryInsertComentDto) {
        System.out.println("insertComent");
        try {
            userDto userDto=userService.sendUserDto();
            comentDto dto=comentDto.builder()
                                    .cbid(tryInsertComentDto.getBid())
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
}
