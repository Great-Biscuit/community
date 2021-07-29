package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * 消费者
 */
@Component
public class EventConsumer implements CommunityConstant {
    //记录日志
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    //消息
    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    //监听(公共) 被动触发
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
    private void handleCommentMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }

        //发送通知(往message表里插入内容)
        Message message = new Message();
        //消息发送者
        message.setFromId(SYSTEM_USER_ID);
        //消息接收者
        message.setToId(event.getEntityUserId());
        //处理标题(此处存主题)
        message.setConversationId(event.getTopic());
        //时间
        message.setCreateTime(new Date());
        //设置内容
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());//事件触发者
        content.put("entityType", event.getEntityTpye());//操作类型
        content.put("entityId", event.getEntityId());//实体类型

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        //内容
        message.setContent(JSONObject.toJSONString(content));

        //存入
        messageService.addMessage(message);
    }

    //消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }

        DiscussPost post = discussPostService.findDiscussPortById(event.getEntityId());
        elasticsearchService.saveDiscussPort(post);
    }

    //消费删帖事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }

        DiscussPost post = discussPostService.findDiscussPortById(event.getEntityId());
        elasticsearchService.deleteDiscussPort(event.getEntityId());
    }

    //消费分享长图事件
    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShareMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd = wkImageCommand + " --quality 75 "
                + htmlUrl + " " +
                wkImageStorage + "/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功: " + cmd);
        } catch (Exception e) {
            logger.error("生成长图失败: " + e.getMessage());
        }

        // 因为生成图片可能耗时较长，所以用定时器，定期检查是否生成完成图片
        // 启用服务器，监视图片生成，一旦生成完成，就上传至七牛云
        UploadTask uploadTask = new UploadTask(fileName, suffix);
        Future future = taskScheduler.scheduleAtFixedRate(uploadTask, 500);//500毫秒执行一次
        uploadTask.setFuture(future);
    }

    class UploadTask implements Runnable {
        //文件名称
        private String fileName;

        //文件后缀
        private String suffix;

        //启动任务的返回值，可以用来停止定时器
        private Future future;

        //开始时间,用于当出现问题时,将自动任务停止（30S看任务是否完成）
        private long startTime;
        //上传次数,作用同上
        private int uploadTimes;


        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {
            //判断终止条件
            //生成图片失败
            if (System.currentTimeMillis() - startTime > 30000) {
                //30s都没生成好
                logger.error("执行时间过长,终止任务: " + fileName);
                future.cancel(true);
                return;
            }
            //上传失败
            if (uploadTimes >= 3) {
                //3次都没传完
                logger.error("上传次数过多,终止任务: ", fileName);
                future.cancel(true);
                return;
            }

            //正常执行
            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
            if (file.exists()) {
                //看文件在本地存不存在
                logger.info(String.format("开始第 %d 次上传[%s].", ++uploadTimes, fileName));
                //设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                //生成上传凭证
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                //指定上传的机房(华东)
                UploadManager manager = new UploadManager(new Configuration(Zone.zone0()));
                try {
                    //开始上传图片
                    Response response = manager.put(
                            path, fileName, uploadToken, null, "image/png", false
                    );
                    //处理响应结果
                    JSONObject jsonObject = JSONObject.parseObject(response.bodyString());
                    if (jsonObject == null
                            || jsonObject.get("code") == null
                            || !jsonObject.get("code").toString().equals("0")) {
                        logger.info(String.format("第 %d 次上传失败[%s].", uploadTimes, fileName));
                    } else {
                        logger.info(String.format("第 %d 次上传成功[%s].", uploadTimes, fileName));
                        //结束任务
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    logger.info(String.format("第 %d 次上传失败[%s].", uploadTimes, fileName));
                }

            } else {
                logger.info("等待图片生成[" + fileName + "].");
            }
        }

    }

}
