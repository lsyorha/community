package com.nowcoder.demo1.util;
// 用于生成Redis的key
public class RedisKeyUtil {
//    分割符
    private static final String SPLIT = ";";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";

//    某实体赞的个数
//    like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }
//    某个用户的赞
//    like:user:userId -> int
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }
}
