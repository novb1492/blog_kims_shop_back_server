package com.example.blog_kim_s_token.service.aritcle.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class tryInsertComentDto {
    @NotBlank(message = "댓글이 빈칸입니다")
    @Length(min = 1,max = 100,message = "글자수가 너무 많거나 적습니다")
    private String coment;

    @Min(value = 1,message = "부적절한 게시글번호입니다")
    private int bid;

}
