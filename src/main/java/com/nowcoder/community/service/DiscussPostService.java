package com.nowcoder.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine核心接口 Cache
    // 有很多子接口，但两个常用的 LoadingCache(同步缓存，线程排队等)、AsynLoadingCache(异步，并发)

    //帖子列表的缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    //帖子总数的缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    @PostConstruct
    public void init() {
        //初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    //这里是往缓存里加数据
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误!");
                        }
                        String[] params = key.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误!");
                        }
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        //可以增加先访问二级缓存Redis再访问数据库mysql
                        logger.debug("Load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });
        //初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception {
                        //可以增加先访问二级缓存Redis再访问数据库mysql
                        logger.debug("Load post rows from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
//        if (userId == 0 && orderMode == 1) {//访问首页的时候userId不传，为0
//            return postListCache.get(offset + ":" + limit);
//        }
        //访问数据库
        logger.debug("Load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    public int findDiscussPostRows(int userId) {
//        if (userId == 0) {//访问首页的时候userId不传，为0
//            return postRowsCache.get(userId);
//        }
        //访问数据库
        logger.debug("Load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    /**
     * 增加帖子
     */
    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        //转义HTML标记 过滤敏感词
        post.setTitle(sensitiveFilter.filter(HtmlUtils.htmlEscape(post.getTitle())));
        post.setContent(sensitiveFilter.filter(HtmlUtils.htmlEscape(post.getContent())));

        return discussPostMapper.insertDiscussPost(post);

    }

    /**
     * 根据ID查询帖子
     */
    public DiscussPost findDiscussPortById(int id) {
        return discussPostMapper.selectDiscussPortById(id);
    }

    /**
     * 更新评论数
     */
    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    /**
     * 修改帖子类型
     *
     * @param id
     * @param type
     * @return
     */
    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    /**
     * 修改帖子状态
     *
     * @param id
     * @param status
     * @return
     */
    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    /**
     * 修改帖子分数
     *
     * @param id
     * @param score
     * @return
     */
    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }
}
