package com.example.blog_kim_s_token.service.aritcle.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class tryInsertComentDto {
    @NotBlank(message = "댓글이 빈칸입니다")
    private String coment;

    @Min(value = 1,message = "부적절한 게시글번호입니다")
    private int bid;

    @Length(min = 1,max = 100,message = "댓글 글자수가 초과합니다")
    private String innertext;
}
