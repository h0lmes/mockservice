package com.mockachu.mapper;

import com.mockachu.domain.KafkaTopic;
import com.mockachu.model.KafkaTopicDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KafkaTopicMapperImpl implements KafkaTopicMapper {

    @Override
    public KafkaTopicDto toDto(KafkaTopic entity) {
        return new KafkaTopicDto()
                .setGroup(entity.getGroup())
                .setTopic(entity.getTopic())
                .setPartition(entity.getPartition())
                .setInitialData(entity.getInitialData())
                .setPersistent(true);
    }

    @Override
    public KafkaTopic fromDto(KafkaTopicDto dto) {
        return new KafkaTopic()
                .setGroup(dto.getGroup())
                .setTopic(dto.getTopic())
                .setPartition(dto.getPartition())
                .setInitialData(dto.getInitialData());
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
