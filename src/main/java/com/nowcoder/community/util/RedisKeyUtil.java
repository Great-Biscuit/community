package com.nowcoder.community.util;

/**
 * 生成 Redis key 的辅助类
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";

    //生成某个实体的赞
    public static String getEntityLikeKey(int entityType, int entityId) {
        //like:entity:entityType:entityId -> set(userId)
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //某个用户的赞
    public static String getUserLikeKey(int userId) {
        //like:user:userId -> int
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

}
