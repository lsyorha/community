package com.nowcoder.demo1.controller;

import com.nowcoder.demo1.entity.Message;
import com.nowcoder.demo1.entity.Page;
import com.nowcoder.demo1.entity.User;
import com.nowcoder.demo1.service.MessageService;
import com.nowcoder.demo1.service.UserService;
import com.nowcoder.demo1.util.CommunityUtil;
import com.nowcoder.demo1.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
//私信页列表
    @RequestMapping("/letter/list")
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();
//        分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
//        会话列表
        List<Message> conversationList = messageService.findConversations(
                user.getId(), page.getOffset(), page.getLimit()
        );
        List<Map<String,Object>> conversations = new ArrayList<>();
        if (conversationList != null){
            for (Message message : conversationList) {
//                这里用Map而不用HashMap是因为Map是接口，而HashMap是实现类，这里用接口是为了扩展性
                Map<String, Object> map = new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(),message.getConversationId()));
                int targetId = user.getId() == message.getFromId()?message.getToId():message.getFromId();
                map.put("target",userService.findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);

//        查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);

        return "/site/letter";
    }
//    具体用户私信列表
    @RequestMapping(value = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getConversationList(@PathVariable("conversationId") String conversationId, Model model, Page page){

        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

//        私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if (letterList != null){
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
//        私信目标
        model.addAttribute("target",getLetterTarget(conversationId));

//        设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }

//    获取未读消息列表
    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids = new ArrayList<>();

        if (letterList != null){
            for (Message message : letterList){
//                用户为message的接受方以及消息状态0是未读态
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus()==0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

//    查询目标对象
    private User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");

        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0){
            return userService.findUserById(id1);
        }else return userService.findUserById(id0);
    }

//    发送私信
    @RequestMapping(value = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendMessage(String toName,String content){
        User target = userService.findUserByName(toName);

        if (target == null){
            return CommunityUtil.getJsonString(1,"目标用户不存在");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setCreateTime(new Date());
        message.setToId(target.getId());
        message.setContent(content);
        int toId = message.getToId();
        int fromId = message.getFromId();

        if (toId > fromId) {
            message.setConversationId(fromId + "_" + toId);
        } else {
            message.setConversationId(toId + "_" + fromId);
        }

        messageService.addMessage(message);
        return CommunityUtil.getJsonString(0);
    }

}
