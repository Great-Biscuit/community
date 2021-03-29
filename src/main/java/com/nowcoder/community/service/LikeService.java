package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞
     *
     * @param userId     点赞者
     * @param entityType 给什么类型的东西点赞
     * @param entityId   此物的编号
     */
    public void like(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if (isMember) {
            //已点赞，所以现在是取消
            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        } else {
            //点赞
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }
    }

    /**
     * 查询某个实体的点赞数量
     *
     * @param entityType
     * @param entityId
     * @return 数量
     */
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * 查询某人对某实物点过赞没
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return 不用Boolean是因为后期可能不止点赞，还有别的功能，便于拓展 1:已点赞
     */
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

}
