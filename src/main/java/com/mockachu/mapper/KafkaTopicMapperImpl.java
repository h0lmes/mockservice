package com.mockachu.mapper;

import com.mockachu.domain.KafkaTopic;
import com.mockachu.model.KafkaTopicDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KafkaTopicMapperImpl implements KafkaTopicMapper {

    @Override
    public KafkaTopicDto toDto(KafkaTopic route) {
        return new KafkaTopicDto()
                .setGroup(route.getGroup())
                .setTopic(route.getTopic())
                .setPartition(route.getPartition())
                .setPersistent(true);
    }

    @Override
    public KafkaTopic fromDto(KafkaTopicDto dto) {
        return new KafkaTopic()
                .setGroup(dto.getGroup())
                .setTopic(dto.getTopic())
                .setPartition(dto.getPartition());
    }

    @Override
    public List<KafkaTopicDto> toDto(List<KafkaTopic> routes) {
        return routes.stream().map(this::toDto).toList();
    }

    @Override
    public List<KafkaTopic> fromDto(List<KafkaTopicDto> list) {
        return list.stream().map(this::fromDto).toList();
    }
}
