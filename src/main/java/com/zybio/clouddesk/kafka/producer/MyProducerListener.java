package com.zybio.clouddesk.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.lang.Nullable;

@Slf4j
public class MyProducerListener implements ProducerListener {

    @Override
    public void onSuccess(ProducerRecord producerRecord, RecordMetadata recordMetadata) {
        log.info(producerRecord.topic() + "   发送成功!!");
        String value = String.valueOf(producerRecord.value());
        if (!value.equals("null")){
//            FileRecordDTO dto = JSONObject.parseObject(value,FileRecordDTO.class);
//            dto.setStatus(1);
//            BdFileRecord record = new BdFileRecord();
//            BeanUtil.copyProperties(dto,record);
//            FileStatusTUtils.updateStatus(record);
            log.info("更新成功状态完成:" + value);
        }
    }

    @Override
    public void onError(ProducerRecord producerRecord, @Nullable RecordMetadata recordMetadata, Exception exception) {
        log.error(producerRecord.topic() + "    推送失败，推送数据：" + producerRecord.value() + "，失败原因：" + exception.getMessage());
        String value = String.valueOf(producerRecord.value());
        if (!value.equals("null")){
//            UserDocServiceImpl mapper = new UserDocServiceImpl();
//            FileRecordDTO dto = JSONObject.parseObject(value,FileRecordDTO.class);
//            dto.setStatus(-1);
//            BdFileRecord record = new BdFileRecord();
//            BeanUtil.copyProperties(dto,record);
//            mapper.updateStatus(record);
            log.info("更新失败状态完成:" + value);
        }
    }
}
