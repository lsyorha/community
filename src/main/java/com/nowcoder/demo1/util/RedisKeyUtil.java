package com.nowcoder.demo1.util;
// 用于生成Redis的key
public class RedisKeyUtil {
//    分割符
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
//    关注
    private static final String PREFIX_ATTENTION = "attention";
//    粉丝
    private static final String PREFIX_FAN = "fan";
//    验证码
    private static final String PREFIX_KAPTCHA = "kaptcha";
//    登录凭证
    private static final String PREFIX_TICKET = "ticket";

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
//    某个用户关注的实体
//    attention:userId:entityType -> zset(entityId,now)
    public static String getAttentionKey(int userId, int entityType){
        return PREFIX_ATTENTION + SPLIT + userId + SPLIT + entityType;
    }
//    某用户拥有的粉丝
//    fan:entityType:entityId -> zset(userId,now)
    public static String getFanKey(int entityType,int entityId){
        return PREFIX_FAN + SPLIT + entityType + SPLIT + entityId;
    }
//    登录验证码
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }
//    登录凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }
    public static String getTicketKeyId(){
        return PREFIX_TICKET + SPLIT + "id";
    }
}
