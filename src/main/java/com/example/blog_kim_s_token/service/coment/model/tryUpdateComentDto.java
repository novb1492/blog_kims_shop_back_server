package com.example.blog_kim_s_token.service.coment.model;

import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class tryUpdateComentDto {

    @NotBlank(message = "댓글이 빈칸입니다")
    @Length(min = 1,max = 300,message = "글자수가 너무 많거나 적습니다")
    private String coment;

    private int cid;

}
