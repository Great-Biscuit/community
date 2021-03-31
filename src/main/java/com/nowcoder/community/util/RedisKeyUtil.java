package com.nowcoder.community.util;

/**
 * 生成 Redis key 的辅助类
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";

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

    //某个用户关注了谁
    public static String getFolloweeKey(int userId, int entityType) {
        // followee:userId:entityType -> zset(entityId,now)
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //某个实体的粉丝 帖子也可以被关注
    public static String getFollowerKey(int entityType, int entityId) {
        // follower:entityType:entityId -> zset(userId,now)
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

}
