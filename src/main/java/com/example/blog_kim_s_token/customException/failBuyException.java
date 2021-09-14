package com.example.blog_kim_s_token.customException;





import com.example.blog_kim_s_token.model.payment.reseponseSettleDto;

import org.springframework.dao.DataAccessException;

public class failBuyException extends DataAccessException {


    private reseponseSettleDto reseponseSettleDto;


    public failBuyException(String msg,reseponseSettleDto reseponseSettleDto ) {
        super(msg);
        this.reseponseSettleDto=reseponseSettleDto;
    }
    public reseponseSettleDto getReseponseSettleDtod() {
        return this.reseponseSettleDto;
    }
}
