package com.zybio.clouddesk.kafka.producer;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;
import com.zybio.clouddesk.mapper.BdFileRecordMapper;
import com.zybio.clouddesk.pojo.domain.BdFileRecord;
import com.zybio.clouddesk.pojo.dto.FileRecordDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.lang.Nullable;

import javax.annotation.Resource;

@Slf4j
public class MyProducerListener implements ProducerListener {

    @Resource
    private BdFileRecordMapper mapper;

    @Override
    public void onSuccess(ProducerRecord producerRecord, RecordMetadata recordMetadata) {
        log.info(producerRecord.topic() + "   发送成功!!");
    }

    @Override
    public void onError(ProducerRecord producerRecord, @Nullable RecordMetadata recordMetadata, Exception exception) {
        log.error(producerRecord.topic() + "    推送失败，推送数据：" + producerRecord.value() + "，失败原因：" + exception.getMessage());
        String value = String.valueOf(producerRecord.value());
        if (!value.equals("null")){
            FileRecordDTO dto = JSONObject.parseObject(value,FileRecordDTO.class);
            dto.setStatus(-1);
            BdFileRecord record = new BdFileRecord();
            BeanUtil.copyProperties(dto,record);
            mapper.updateById(record);
            log.info("更新状态完成");
        }
    }
}
