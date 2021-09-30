package com.example.blog_kim_s_token.service.aritcle.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class tryUpdateArticleDto {
    
    @NotBlank(message = "제목이 빈칸입니다")
    @Size(max = 50,message = "제목이 50글자를 초과합니다")
    private String title;
    
    @NotBlank(message = "본문이 빈칸입니다")
    @Size(max = 2000,message = "본문이 2000글자를 초과합니다")
    private String textarea;

    @Min(value = 1)
    private int bid;
}
