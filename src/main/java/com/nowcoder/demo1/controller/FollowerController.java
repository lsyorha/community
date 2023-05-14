package com.nowcoder.demo1.controller;

import com.nowcoder.demo1.entity.Event;
import com.nowcoder.demo1.entity.User;
import com.nowcoder.demo1.event.EventProducer;
import com.nowcoder.demo1.service.FollowerService;
import com.nowcoder.demo1.util.CommunityConstant;
import com.nowcoder.demo1.util.CommunityUtil;
import com.nowcoder.demo1.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FollowerController implements CommunityConstant {
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private FollowerService followerService;
    @Autowired
    private EventProducer eventProducer;

//关注
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId){
        User user = hostHolder.getUser();

        followerService.follow(user.getId(),entityType,entityId);

//        触发关键事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJsonString(0,"已关注");
    }
//取消关注
    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId){
        User user = hostHolder.getUser();

        followerService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJsonString(0,"取消关注成功");
    }
}