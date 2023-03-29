package com.zybio.clouddesk;

import com.zybio.clouddesk.kafka.producer.SendMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

@EmbeddedKafka(topics = {"encryptedFiles"}, bootstrapServersProperty = "spring.kafka.bootstrap-servers")
public class KafkaTests {

    @Autowired
    private SendMessage sendMessage;

    @Autowired
    private KafkaTemplate<String,Object> kafkaTemplate;

    @Test
    void testKafka(){
        String message = "123456";
        kafkaTemplate.send("encryptedFiles",message);
    }

    @Test
    @KafkaListener(topics = {"encryptedFiles"})
    void getKafkaMessage(ConsumerRecord<?, ?> record){
        System.out.println("简单消费："+record.topic()+"-"+record.partition()+"-"+record.value());
    }
}
