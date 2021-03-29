package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * @param factory 连接工厂
     * @return redisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        /*  序列化方式  */

        //key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        //设置普通的value的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        //设置value为hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        //设置value为hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        template.afterPropertiesSet();//触发  使配置生效

        return template;
    }

}
