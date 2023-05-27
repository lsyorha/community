package com.nowcoder.demo1.quartz;

import com.nowcoder.demo1.entity.DiscussPost;
import com.nowcoder.demo1.service.DiscussPostService;
import com.nowcoder.demo1.service.ElasticsearchService;
import com.nowcoder.demo1.service.LikeService;
import com.nowcoder.demo1.util.CommunityConstant;
import com.nowcoder.demo1.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class PostScoreRefreshJob implements Job, CommunityConstant {
//    添加日志，方便调试
    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

//    论坛纪元
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2017-09-21 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("论坛纪元初始化失败~~", e);
        }
    }


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations.size() == 0){
            logger.info("[任务取消] 没有需要刷新的帖子!");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数,涉及帖子数为：" + operations.size());
        while (operations.size() > 0){
//            随机移除集合中的任意元素
            this.refresh((Integer) operations.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完毕！");
    }
//    更新分数
    private void refresh(int postId){
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if (post == null){
            logger.info("该帖子不存在：id = " + postId);
            return;
        }
//        该帖子是否是精华帖
        boolean wonderful = post.getStatus() == 1;
//        评论数量
        int commentCount = post.getCommentCount();
//        点赞数
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

//        计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
//        分数 = 帖子权重 / 距离天数  log特性初始波动大，后续趋于稳定
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
//        更新帖子分数
        discussPostService.updateScore(postId,score);
//        同步更新elasticsearch内的数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
