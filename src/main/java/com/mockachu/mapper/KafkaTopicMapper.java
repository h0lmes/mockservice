package com.mockachu.mapper;

import com.mockachu.domain.KafkaTopic;
import com.mockachu.model.KafkaTopicDto;

import java.util.List;

public interface KafkaTopicMapper {
    KafkaTopicDto toDto(KafkaTopic route);
    KafkaTopic fromDto(KafkaTopicDto dto);
    List<KafkaTopicDto> toDto(List<KafkaTopic> routes);
    List<KafkaTopic> fromDto(List<KafkaTopicDto> list);
}
