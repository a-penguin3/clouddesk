package com.zybio.clouddesk.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@EnableKafka
public class KafkaConfig {



    @Bean
    public KafkaAdmin.NewTopics topics456(){
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name("encryptedFiles")
                        .partitions(3)
                        .replicas(2)
                        .build(),
                TopicBuilder.name("decodeFiles")
                        .partitions(3)
                        .replicas(2)
                        .build());
    }

}
