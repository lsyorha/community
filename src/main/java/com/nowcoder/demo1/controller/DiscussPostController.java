package com.nowcoder.demo1.controller;

import com.nowcoder.demo1.annotation.LoginRequired;
import com.nowcoder.demo1.entity.*;
import com.nowcoder.demo1.event.EventProducer;
import com.nowcoder.demo1.service.CommentService;
import com.nowcoder.demo1.service.DiscussPostService;
import com.nowcoder.demo1.service.LikeService;
import com.nowcoder.demo1.service.UserService;
import com.nowcoder.demo1.util.CommunityConstant;
import com.nowcoder.demo1.util.CommunityUtil;
import com.nowcoder.demo1.util.HostHolder;
import com.nowcoder.demo1.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;


@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private CommentService commentService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;
//    调UserService查询用户效率会比多表连接查询慢，但可以降低耦合
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 接收用户传入的帖子标题和内容，调用业务层处理标题和内容，完成后返回Jason字符串
     * @param title
     * @param content
     * @return
     */
    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String addDiscussPost(String title,String content){
        User user = hostHolder.getUser();
        DiscussPost post = new DiscussPost();

        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());

        post.setType(0);
        post.setStatus(0);
        post.setScore(0);
        post.setCommentCount(0);
        discussPostService.addDiscussPost(post);

//        触发发帖事件，用事件是因为发帖后需要更新Elasticsearch中的数据，而Elasticsearch是独立的服务，不需要影响主业务
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

//        计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,post.getId());

//        插入错误的情况后面通过AOP实现
        return CommunityUtil.getJsonString(0,"发布成功！");
    }
    @RequestMapping(value = "/detail/{discussPostId}",method = RequestMethod.GET)
//        未登录用户不能查看帖子详情页
//    @LoginRequired
    public String findDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        User visit = hostHolder.getUser();
//        帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);
//        作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);
//        帖子点赞数
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount",likeCount);
//        点赞状态
        int likeStatus = (visit == null) ? 0 : likeService.findEntityLikeStatus(visit.getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus",likeStatus);
//        评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

//        评论：回复帖子的
//        回复：回复评论的
//        评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
//        总评论页
        List<Map<String,Object>> commentViewList = new ArrayList<>();
        if (commentList != null){
            for (Comment comment : commentList) {
//            评论页列表
                Map<String, Object> commentView = new HashMap<>();
//                评论列表
                commentView.put("comment",comment);
//                评论发出作者
                commentView.put("user",userService.findUserById(comment.getUserId()));
//                评论点赞数
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentView.put("likeCount",likeCount);
                //点赞状态
                likeStatus = (visit == null) ? 0 : likeService.findEntityLikeStatus(visit.getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentView.put("likeStatus",likeStatus);

//                总回复页
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
//                回复页列表
                List<Map<String,Object>> replyViewList = new ArrayList<>();
                if (replyList != null){
                    for (Comment reply : replyList) {
//                  回复页列表
                        Map<String, Object> replyView = new HashMap<>();
    //                  回复列表
                        replyView.put("reply",reply);
    //                  发出人
                        replyView.put("user",userService.findUserById(reply.getUserId()));
//                        回复对象,这里要根据targetId查，debug一天的源头！！！
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyView.put("target",target);
//                        回复的点赞数
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyView.put("likeCount",likeCount);
//                        回复点赞的状态
                        likeStatus = (visit == null) ? 0 : likeService.findEntityLikeStatus(visit.getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyView.put("likeStatus",likeStatus);
                        replyViewList.add(replyView);
                    }
                }
                commentView.put("replys",replyViewList);
//                回复数量
                int replyCount = commentService.findCommentCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentView.put("replyCount",replyCount);

                commentViewList.add(commentView);
            }
        }
        model.addAttribute("comments",commentViewList);
        return "/site/discuss-detail";
    }
//    置顶&&取消置顶
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        DiscussPost post = discussPostService.findDiscussPostById(id);
//        获取贴子置顶状态，1为置顶，0为正常，1^1 = 1; 1^0 = 0;
        int type = post.getType() ^ 1;
        discussPostService.updateType(id, type);
//        返回结果
        Map<String, Object> map = new HashMap<>();
        map.put("type",type);

//       调kafka，触发事件，更改贴子状态
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        return CommunityUtil.getJsonString(0,null,map);
    }

    //    加精&&取消加精
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        DiscussPost post = discussPostService.findDiscussPostById(id);
//        1为加精，0为正常，1^1 = 1; 1^0 = 0;
        int status = post.getStatus() ^ 1;
        discussPostService.updateStatus(id, status);
//        返回结果
        Map<String, Object> map = new HashMap<>();
        map.put("status",status);

//       调kafka，触发事件，更改贴子状态
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

//        计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,id);

        return CommunityUtil.getJsonString(0,null,map);
    }

    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
//        1为加精，0为正常，1^1 = 1; 1^0 = 0;
        discussPostService.updateStatus(id, 2);

//       调kafka，触发事件，更改贴子状态
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJsonString(0);
    }
}
