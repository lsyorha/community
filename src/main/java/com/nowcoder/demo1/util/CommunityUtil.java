package com.nowcoder.demo1.util;

import com.alibaba.fastjson2.JSONObject;
import io.micrometer.common.util.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

public class CommunityUtil {
//    生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    /**
     * MD5加密，对前端传来的数值进行加密
     * @param key
     * @return
     */
    public static String md5(String key){
        if (StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
     * 就收用户传入数据，返回JSON字符串
     * @param code
     * @param msg
     * @param map
     * @return
     */
    public static String getJsonString(int code, String msg, Map<String,Object> map){
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if (map != null){
            for(String key : map.keySet()){
//                这里是key，不是"key"，n久前的坑，现在才发现
                json.put(key,map.get(key));
            }
        }
        return json.toJSONString();
    }

    /**
     * 传入不同参数时回调
     * @param code
     * @param msg
     * @return
     */
    public static String getJsonString(int code, String msg){
        return getJsonString(code,msg,null);
    }
    public static String getJsonString(int code){
        return getJsonString(code,null,null);
    }
}
