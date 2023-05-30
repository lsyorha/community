package com.nowcoder.demo1.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.demo1.dao.DiscussPostMapper;
import com.nowcoder.demo1.entity.DiscussPost;
import com.nowcoder.demo1.util.SensitiveFilter;
import jakarta.annotation.PostConstruct;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {
    private Logger logger = LoggerFactory.getLogger(DiscussPostService.class);
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;
    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //    使用Caffeine缓存变化不频繁的数据，优化帖子查询，下载jmeter来进行压力测试
    //    Caffeine核心接口：Cache(普通缓存)、LoadingCache（自动加载，当俩个线程同时调用时后者会被堵塞）、AsyncLoadCache（支持异步）
//    帖子列表的缓存
//    缓存也是按照kv的方式储存
    private LoadingCache<String, List<DiscussPost>> postListCache;
//    帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;

//    初始化
    @PostConstruct
    public void init(){
//        初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(String key) throws Exception {
//                        缓存的数据来源
                        if (key == null || key.length() == 0){
                            throw new IllegalArgumentException("传入缓存的参数错误!");
                        }
//                        创建数据，拆分数据
                        String[] params = key.split(":");
                        if (params.length != 2){
                            throw new IllegalArgumentException("传入缓存的参数错误!");
                        }
                        int offset = Integer.parseInt(params[0]);
                        int limit = Integer.parseInt(params[1]);
//                        只用于查询热帖，在此基础上可以加缓存，存到redis，访问数据库前访问二级缓存
                        logger.info("Caffeine Post load post list from DB");
                        return discussPostMapper.selectDiscussPosts(0,offset,limit,1);
                    }
                });
//        初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(Integer key) throws Exception {
                        logger.info("Caffeine Rows load post list from DB");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode){
        //        优化热门帖子查询
        if (userId == 0 && orderMode == 1){
            return postListCache.get(offset + ":" + limit);
        }

//        查询数据库时添加日志
        logger.info("base load post list from DB");
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }
    public int findDiscussPostRows(int userId){

//        优化总页数
        if (userId == 0){
            return postRowsCache.get(userId);
        }

        logger.info("base load post rows from DB");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post){
        //        敏感词处理
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

//        防html注入进行转义处理
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        return discussPostMapper.addDiscussPost(post);
    }

    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPost(id);
    }

    public int updateCommentCount(int id,int count){
        return discussPostMapper.updateCommentCount(id, count);
    }

    public int updateType(int id, int type){
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id, int status){
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int id, double score){
        return discussPostMapper.updateScore(id, score);
    }

}
