package com.nowcoder.demo1.event;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.demo1.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
//事件生产者
@Component
public class EventProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;
//    触发事件后处理事件
    public void fireEvent(Event event){
//        将事件发布到指定的主题，内容转为Json字符串方便回显
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
