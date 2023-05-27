package com.nowcoder.demo1;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Date;
import java.util.concurrent.*;

@SpringBootTest
public class ThreadPoolTests {

    //    日志输出
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTests.class);

    //    实例化一个JDK普通线程池，方便调用
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

//    实例化一个JDK可执行定时任务的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

//    Spring线程池
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
//    Spring定时线程池，需配置EnableScheduling，不然会无法正确注入
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    //    自定义一个线程等待方法
    private void sleep(long m){
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    1、JDK普通线程池
    @Test
    public void ExecutorServiceTest(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.error("Hello！ExecutorServiceTest");
            }
        };

        for (int i = 0; i < 10; i++) {
            executorService.submit(task);
        }
//        等待十秒关闭
        sleep(10000);
    }

//    2、JDK定时任务线程池
    @Test
    public void scheduleExecutorServiceTest(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.error("Hello！scheduleExecutorServiceTest");
            }
        };

        scheduledExecutorService.scheduleWithFixedDelay(task,0,2000, TimeUnit.MILLISECONDS);

    //        等待十秒关闭
        sleep(10000);
    }

//  Spring普通线程池
    @Test
    public void springExecutorServiceTest(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.error("Hello！springExecutorServiceTest");
            }
        };
        for (int i = 0; i < 10; i++) {
            taskExecutor.submit(task);
        }
        sleep(10000);
    }

//    Spring定时线程池
    @Test
    public void springScheduleExecutorServiceTest(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.error("Hello！springScheduleExecutorServiceTest");
            }
        };

        Date start = new Date(System.currentTimeMillis() + 1000);
        taskScheduler.scheduleAtFixedRate(task,start,1000);

        //        等待十秒关闭
        sleep(10000);
    }

    @Autowired
    private ThreadCallService threadCallService;
//Spring普通线程池（简化）
    @Test
    public void springSimpleTest(){
        for (int i = 0; i < 10; i++) {
            threadCallService.execute1();
        }
        sleep(10000);
    }

//Spring定时线程池（简化）
    @Test
    public void springSimpleScheduleTest(){
        threadCallService.scheduleExecute();
        sleep(10000);
    }

}
