package com.nowcoder.demo1.service;

import com.nowcoder.demo1.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class FollowerService {
    @Autowired
    private RedisTemplate redisTemplate;
// 实现关注
    public void follow(int userId, int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String attentionKey = RedisKeyUtil.getAttentionKey(userId, entityType);
                String fanKey = RedisKeyUtil.getFanKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().add(attentionKey,entityId,System.currentTimeMillis());
                operations.opsForZSet().add(fanKey,userId,System.currentTimeMillis());

                return operations.exec();
            }
        });
    }
//    取消关注
public void unfollow(int userId, int entityType,int entityId){
    redisTemplate.execute(new SessionCallback() {
        @Override
        public Object execute(RedisOperations operations) throws DataAccessException {
            String attentionKey = RedisKeyUtil.getAttentionKey(userId, entityType);
            String fanKey = RedisKeyUtil.getFanKey(entityType, entityId);

            operations.multi();

            operations.opsForZSet().remove(attentionKey,entityId);
            operations.opsForZSet().remove(fanKey,userId);

            return operations.exec();
        }
    });
    }
//    查询关注实体(1帖子,2评论,3用户)的数量
    public long findAttentionCount(int userId, int entityType){
        String attentionKey = RedisKeyUtil.getAttentionKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(attentionKey);
    }
//    查询实体的粉丝数
    public long findFanCount(int entityType, int entityId){
        String fanKey = RedisKeyUtil.getFanKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(fanKey);
    }
//    查询当前用户是否已经关注该实体
    public boolean hasAttention(int userId, int entityType, int entityId){
        String attentionKey = RedisKeyUtil.getAttentionKey(userId, entityType);
//      redis的zscore attention:111:3 159操作，通过时间戳是否存在判断用户是否已经关注
        return redisTemplate.opsForZSet().score(attentionKey,entityId) != null;
    }
}
