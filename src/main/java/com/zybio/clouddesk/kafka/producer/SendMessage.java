package com.zybio.clouddesk.kafka.producer;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zybio.clouddesk.pojo.domain.BdFileRecord;
import com.zybio.clouddesk.pojo.dto.FileRecordDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.object.RdbmsOperation;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaSendCallback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;


@Service
@Slf4j
public class SendMessage{

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;



    public void sendMessage(String topic, BdFileRecord record) {
        log.debug("开始发送请求");
        MyProducerListener listener = new MyProducerListener();
        kafkaTemplate.setProducerListener(listener);
        FileRecordDTO dto = new FileRecordDTO();
        BeanUtil.copyProperties(record,dto);
        String message = JSON.toJSONString(dto);
        log.debug("发送的消息为：" + message);
        kafkaTemplate.send(topic,message);
    }


}
