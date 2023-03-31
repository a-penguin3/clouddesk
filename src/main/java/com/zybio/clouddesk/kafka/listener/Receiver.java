package com.zybio.clouddesk.kafka.listener;

import com.alibaba.fastjson2.JSONObject;
import com.zybio.clouddesk.pojo.dto.FileRecordDTO;
import com.zybio.clouddesk.service.UserDocService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Receiver {

    @Autowired
    private UserDocService service;

    @KafkaListener(id = "encode-id",topics = "encryptedFiles",idIsGroup = false)
    public void listen(@Payload String message){
        log.info("接受加密文件成功，开始加密：" + message);
        if (message.isBlank()){
            log.error("接受到空白消息");
            return;
        }
        FileRecordDTO dto = JSONObject.parseObject(message,FileRecordDTO.class);
        if (dto == null){
            log.error("消息转义失败，不是标准的消息,请检查发送消息");
            return;
        }
        if (dto.getStatus() != 0){
            return;
        }
        service.encodeFiles(dto);
    }

    @KafkaListener(id = "decode-id",topics = "decodeFiles",idIsGroup = false)
    public void listenEncode(@Payload String message){
        log.info("接受解密文件成功，开始解密：" + message);
        if (message.isBlank()){
            log.error("接受到空白消息");
            return;
        }
        FileRecordDTO dto = JSONObject.parseObject(message,FileRecordDTO.class);
        if (dto == null){
            log.error("消息转义失败，不是标准的消息,请检查发送消息");
            return;
        }
        if (dto.getStatus() != 0){
            return;
        }
        service.decodeFiles(dto);
    }

}
