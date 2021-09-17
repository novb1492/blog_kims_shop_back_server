package com.example.blog_kim_s_token.service.payment.model.cancle;

import java.util.List;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class tryCancleDto {
    private List<Integer>ids;

    @NotBlank(message = "항목이 빈칸입니다")
    private String kind;
}
