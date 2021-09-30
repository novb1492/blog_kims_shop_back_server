package com.example.blog_kim_s_token.service.aritcle.model;

import javax.validation.constraints.Min;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class tryDeleteArticleDto {
    @Min(value = 1)
    private int bid;
}
