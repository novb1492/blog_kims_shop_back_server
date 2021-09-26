package com.example.blog_kim_s_token.service.reservation.tempReservation;

import com.example.blog_kim_s_token.model.reservation.tempReservationDao;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class scanTempReservation implements Tasklet {
    private tempReservationDao tempReservationDao;
    
    public scanTempReservation(tempReservationDao tempReservationDao){
        this.tempReservationDao=tempReservationDao;
    }
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println("scanTempReservation execute");
        tempReservationDao.deleteAll();
        return RepeatStatus.FINISHED;
    }
    
}
