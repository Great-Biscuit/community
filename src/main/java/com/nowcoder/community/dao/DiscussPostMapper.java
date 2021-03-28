package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    //userId不是0才用  用动态sql
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    //@Param  别名   动态sql的条件要用这个参数且只有一个参数就必须要别名
    int selectDiscussPostRows(@Param("userId") int userId);

    //增加帖子
    int insertDiscussPost(DiscussPost discussPost);

    //查询帖子
    DiscussPost selectDiscussPortById(int id);

    //更新评论数
    int updateCommentCount(int id, int commentCount);

}
