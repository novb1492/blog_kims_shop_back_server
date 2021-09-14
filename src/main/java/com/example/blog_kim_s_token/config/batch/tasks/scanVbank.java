package com.example.blog_kim_s_token.config.batch.tasks;


import java.sql.Timestamp;
import java.time.LocalDateTime;



import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;


import lombok.NoArgsConstructor;

@NoArgsConstructor
public class scanVbank implements Tasklet  {
   
    /*private vbankDao vbankDao;
    

    public scanVbank(vbankDao vbankDao){
        this.vbankDao=vbankDao;
    }*/

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
       System.out.println("scanVbank execute");

        return RepeatStatus.FINISHED;
    }
    
}
