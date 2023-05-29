package com.nowcoder.demo1.event;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.demo1.entity.DiscussPost;
import com.nowcoder.demo1.entity.Event;
import com.nowcoder.demo1.entity.Message;
import com.nowcoder.demo1.service.DiscussPostService;
import com.nowcoder.demo1.service.ElasticsearchService;
import com.nowcoder.demo1.service.MessageService;
import com.nowcoder.demo1.util.CommunityConstant;
import com.nowcoder.demo1.util.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;


@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger logger =  LoggerFactory.getLogger(EventConsumer.class);
    @Autowired
    private MessageService messageService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

//    执行定时任务的线程池
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

//    根据传入的业务类型做出对应的处理方式，去常量定义不同类型的主题对应名称
    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_LIKE,TOPIC_FOLLOW})
    private void handleComment(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息内容为空");
            return;
        }
//        解析收到的JSON字符串
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if (event == null){
//            有值但解析JSON无法还原的情况
            logger.error("消息格式错误");
            return;
        }

        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
//        事件发送给谁
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
//        message表中的content内容存到一个map中
//     {"entityType":1,"entityId":271,"postId":271,"userId":138}
        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());

        if (!event.getData().isEmpty()){
//            这里可以使用content.putAll(event.getData());
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(),entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

//    消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息内容为空");
            return;
        }
//        解析收到的JSON字符串
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if (event == null){
//            有值但解析JSON无法还原的情况
            logger.error("消息格式错误");
            return;
        }

        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }

//    消费删帖事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息内容为空");
            return;
        }

    //        解析收到的JSON字符串
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if (event == null){
    //            有值但解析JSON无法还原的情况
            logger.error("消息格式错误");
            return;
        }

        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

//    消费分享事件
    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息内容为空");
            return;
        }

        //        解析收到的JSON字符串
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if (event == null){
            //            有值但解析JSON无法还原的情况
            logger.error("消息格式错误");
            return;
        }

        String htmlUrl =(String) event.getData().get("htmlUrl");
        String fileName =(String) event.getData().get("fileName");
        String suffix =(String) event.getData().get("suffix");

        String cmd = wkImageCommand + " --quality 75 " + htmlUrl + " "
                + wkImageStorage + "/" + fileName + suffix;

        try {
//            生成过程耗时，主线程会继续执行，会出现输出“生成长图成功"但图片还没生成的情况，所以添加定时器等候长图生成的过程
            Runtime.getRuntime().exec(cmd);
            logger.info("执行生成长图指令：" + cmd);
        } catch (IOException e) {
            logger.info("生成长图失败：" + e.getMessage());
        }

//        启用定时器，监听长图是否生成，生成后就传到云服务器
        UploadTask task = new UploadTask(fileName, suffix);
//        每5秒执行一次，太短会导致服务器没收到完整图片进程就关闭的情况 ，添加Future用于处理定时器卡死的情况以及关闭进程
        Future future = taskScheduler.scheduleAtFixedRate(task, 5000);
//        执行顺序要早于5000毫秒后才执行的task
        task.setFuture(future);
    }

    class UploadTask implements Runnable{

//        文件名
        private String fileName;
//        文件后缀
        private String suffix;

//        启动任务的返回值，用于停止定时器
        private Future future;
//        任务开始时间
        private long startTime;
//        任务上传次数
        private int uploadTimes;

        public void setFuture(Future future) {
            this.future = future;
        }

        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
//            生成图片时间超过30秒就默认生成图片失败
            if (System.currentTimeMillis() - startTime > 30000){
                logger.error("执行时间过长，终止任务：" + fileName);
                future.cancel(true);
                return;
            }
//            上传到云服务器失败
            if (uploadTimes >= 3){
                logger.error("上传次数过多，终止任务：" + fileName);
                future.cancel(true);
                return;
            }
//            本地存放长图的路径
            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
//            判断文件是否存在
            if (file.exists()){
                logger.info(String.format("开始第%d次上传[%s]。",++uploadTimes, fileName));
//                设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJsonString(0));

                Auth auth = Auth.create(accessKey, secretKey);
//                生成上传凭证
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
//                指定上传的机房，区域名称：z0 华东  z1 华北  z2 华南  na0 北美  as0 东南亚
                UploadManager manager = new UploadManager(new Configuration(Zone.autoZone()));

                try {
//                    开始上传图片
                    Response response = manager.put(
//                            警惕“+”号陷阱
                            path, fileName, uploadToken, null, "image/" + suffix.substring(suffix.lastIndexOf(".")+1), false);
//                    处理响应结果
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")){
                        logger.info(String.format("第%d次上传失败[%s]",uploadTimes,fileName));
                    }else {
                        logger.info(String.format("第%d次上传成功[%s]",uploadTimes,fileName));
                        future.cancel(true);
                    }

                }catch (QiniuException e){
                    logger.info(String.format("第%d次上传失败[%s]",uploadTimes,fileName));
                }

            }else {
                logger.info("等待图片生成[" + fileName + "]。");
            }
        }
    }

}
