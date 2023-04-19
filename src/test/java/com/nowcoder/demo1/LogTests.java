package com.nowcoder.demo1;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LogTests {
    private static final Logger logger = LoggerFactory.getLogger(LogTests.class);
    @Test
    public void t1(){
        System.out.println(logger.getName());
        logger.debug("debug");
        logger.info("info");
        logger.warn("warn");
        logger.error("error");
    }
}
