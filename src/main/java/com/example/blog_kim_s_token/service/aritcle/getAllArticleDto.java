package com.example.blog_kim_s_token.service.aritcle;

import javax.validation.constraints.Min;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class getAllArticleDto {

    @Min(value = 1)
    private int nowPage;

    private String title;
}
