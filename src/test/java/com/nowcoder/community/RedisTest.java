package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testString() {
        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey, 1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));

    }

    @Test
    public void testHashes() {
        String redisKey = "test:user";
        redisTemplate.opsForHash().put(redisKey, "name", "zhangsan");
        redisTemplate.opsForHash().put(redisKey, "id", 0);

        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().size(redisKey));
    }

    @Test
    public void testLists() {
        String redisKey = "test:ids";

        redisTemplate.opsForList().leftPushAll(redisKey, "101", "102", "103");
        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().rightPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));

    }

    @Test
    public void testSets() {
        String redisKey = "test:teachers";

        redisTemplate.opsForSet().add(redisKey, "张三", "李四", "王五", "张三", "李四");
        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));

    }

    @Test
    public void testSortedSets() {
        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey, "aaa", 10);
        redisTemplate.opsForZSet().add(redisKey, "bbb", 20);
        redisTemplate.opsForZSet().add(redisKey, "ccc", 30);
        redisTemplate.opsForZSet().add(redisKey, "ddd", 40);
        redisTemplate.opsForZSet().add(redisKey, "eee", 50);

        System.out.println(redisTemplate.opsForZSet().range(redisKey, 0, 2));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "bbb"));
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "ccc"));
        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));

    }

    @Test
    public void testKeys() {
        redisTemplate.delete("test:user");

        System.out.println(redisTemplate.hasKey("test:user"));

        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);


    }


}
