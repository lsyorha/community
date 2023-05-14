package com.nowcoder.demo1.controller;

import com.nowcoder.demo1.entity.Event;
import com.nowcoder.demo1.entity.User;
import com.nowcoder.demo1.event.EventProducer;
import com.nowcoder.demo1.service.LikeService;
import com.nowcoder.demo1.util.CommunityConstant;
import com.nowcoder.demo1.util.CommunityUtil;
import com.nowcoder.demo1.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于点赞的控制器
 */
@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;
    @Autowired
    private EventProducer eventProducer;


    /**
     * 接收用户传入的点赞对象类型、点赞对象ID、点赞对象作者ID，调用业务层处理点赞，完成后返回Jason字符串
     * @param entityType
     * @param entityId
     * @param entityUserId
     * @return
     */
    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
//    新传入帖子id，方便后续跳转
    public String like(int entityType,int entityId,int entityUserId, int postId){
        User user = hostHolder.getUser();

//        点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);

//        数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
//        状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String, Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

//        触发点赞事件，点赞时通知
        if (likeStatus == 1){
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityId(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId",postId);
            eventProducer.fireEvent(event);
        }

        return CommunityUtil.getJsonString(0,null,map);
    }

}
