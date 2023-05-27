package com.nowcoder.demo1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ThreadCallService {
    private static final Logger logger = LoggerFactory.getLogger(ThreadCallService.class);

//    让该方法在多线程环境下能使用
    @Async
    public void execute1(){
        logger.error("Async---->ThreadPoolTaskExecutor");
    }

    @Scheduled(initialDelay = 0, fixedDelay = 2000)
    public void scheduleExecute(){
        logger.error("Schedule--------->ThreadPoolTaskScheduler");
    }
}
