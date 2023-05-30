package com.nowcoder.demo1.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class WkConfig {
    private static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

    @Value("${wk.image.storage}")
    private String wkImageStore;

    @PostConstruct
    public void init(){
//        创建wk图片目录
        File file = new File(wkImageStore);
//        System.out.println("初始化存放wk生成长图的目录");
        if (!file.exists()){
            boolean mkdir = file.mkdir();
            logger.info("创建用于存在wk生成长图状态：" + mkdir + "，地址为：" + wkImageStore);
        }
    }

}
