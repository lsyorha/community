package com.nowcoder.demo1.controller;

import com.nowcoder.demo1.entity.Event;
import com.nowcoder.demo1.event.EventProducer;
import com.nowcoder.demo1.util.CommunityConstant;
import com.nowcoder.demo1.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletResponse;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ShareController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

//    生成图片过程时间长，需用异步处理
    @Autowired
    private EventProducer eventProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @RequestMapping(path = "/share", method = RequestMethod.GET)
    @ResponseBody
    private String share(String htmlUrl){

        if (htmlUrl == null){
            return CommunityUtil.getJsonString(404,"图片生成的页面路径不能为空");
        }

//        文件名随机生成，预防撞名
        String fileName = CommunityUtil.generateUUID();

//        异步生成长图
        Event event = new Event()
                .setTopic(TOPIC_SHARE)
                .setData("htmlUrl", htmlUrl)
                .setData("fileName", fileName)
                .setData("suffix", ".png");
        eventProducer.fireEvent(event);

//        返回访问路径
        Map<String, Object> map = new HashMap<>();
        String shareUrl = domain + contextPath + "/share/image/" + fileName;
        map.put("shareUrl", shareUrl);
        System.out.println("shareUrl" + shareUrl);

        return CommunityUtil.getJsonString(0,null,map);
    }

//    获取长图
    @RequestMapping(path = "/share/image/{fileName}", method = RequestMethod.GET)
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {

        if (fileName.isEmpty()){
            throw new IllegalArgumentException("图片名不能为空");
        }

        //        服务器存放路径
        fileName = wkImageStorage + "/" + fileName + ".png";

        //        文件后缀
        String suffix = ".png";
//        响应图片
        response.setContentType("image/png" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("生成图片获取失败：" + e.getMessage());
        }
    }
}
