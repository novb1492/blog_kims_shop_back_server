package com.example.blog_kim_s_token.service.payment.model.tempPaid;

import com.example.blog_kim_s_token.model.reservation.reservationDao;
import com.example.blog_kim_s_token.model.reservation.tempReservationDao;
import com.example.blog_kim_s_token.service.reservation.tempReservation.scanTempReservation;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class tempPaidConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final String batchName="clearTemp";

    
    @Autowired
    private tempReservationDao tempReservationDao;
    @Autowired
    private tempPaidDao tempPaidDao;

    @Bean 
    public Job tempPaidConfigjob(){ 
        System.out.println("tempPaidConfig job");
        return jobBuilderFactory.get(batchName).start(clearTempOrder()).next(clearTempReservation()).build();
    } 
    @Bean
    protected Step clearTempOrder() {
        System.out.println("clearTempOrder");
        return stepBuilderFactory
            .get("clearTempOrder")
            .tasklet(new scanTempOrder(tempPaidDao))
            .build();
    }
    @Bean
    protected Step clearTempReservation() {
        System.out.println("clearTempReservation");
        return stepBuilderFactory
                .get("doClearTempTable")
                .tasklet(new scanTempReservation(tempReservationDao))
                .build();
    }
}
