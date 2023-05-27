package com.nowcoder.demo1;

import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class QuartsTests {

    @Autowired
    private Scheduler scheduler;

    @Test
    public void deleteJobTest(){
        try {
            boolean b = scheduler.deleteJob(new JobKey("yorhaJobDetail", "yorhaGroup"));
            System.out.println(b);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

}
