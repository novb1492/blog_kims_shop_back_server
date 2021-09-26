package com.example.blog_kim_s_token.service.payment.model.tempPaid;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class scanTempOrder implements Tasklet {
    private tempPaidDao tempPaidDao;

    public scanTempOrder(tempPaidDao tempPaidDao){
        this.tempPaidDao=tempPaidDao;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println("scanTempOrder execute");
        tempPaidDao.deleteAll();
        return RepeatStatus.FINISHED;
    }
    
}
