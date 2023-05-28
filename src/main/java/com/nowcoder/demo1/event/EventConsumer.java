package com.nowcoder.demo1.event;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.demo1.entity.DiscussPost;
import com.nowcoder.demo1.entity.Event;
import com.nowcoder.demo1.entity.Message;
import com.nowcoder.demo1.service.DiscussPostService;
import com.nowcoder.demo1.service.ElasticsearchService;
import com.nowcoder.demo1.service.MessageService;
import com.nowcoder.demo1.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


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
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功：" + cmd);
        } catch (IOException e) {
            logger.info("生成长图失败：" + e.getMessage());
        }
    }
}
