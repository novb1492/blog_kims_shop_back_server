package com.example.blog_kim_s_token.service.ApiServies.kakao;



import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class tryKakaoPayDto {

    @NotBlank(message = "kind가 비어 있습니다")
    private String kind;
    
    @Size(min = 0,message="아이템이 비어 있습니다")
    private String[][] itemArray;
    private String[] other;
}
