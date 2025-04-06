package com.mockachu.mapper;

import com.mockachu.domain.ApiTest;
import com.mockachu.model.ApiTestDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiTestMapperImpl implements ApiTestMapper {

    @Override
    public ApiTestDto toDto(ApiTest entity) {
        return new ApiTestDto()
                .setGroup(entity.getGroup())
                .setAlias(entity.getAlias())
                .setPlan(entity.getPlan());
    }

    @Override
    public ApiTest fromDto(ApiTestDto dto) {
        return new ApiTest()
                .setGroup(dto.getGroup())
                .setAlias(dto.getAlias())
                .setPlan(dto.getPlan());
    }

    @Override
    public List<ApiTestDto> toDto(List<ApiTest> entities) {
        return entities.stream().map(this::toDto).toList();
    }

    @Override
    public List<ApiTest> fromDto(List<ApiTestDto> dtos) {
        return dtos.stream().map(this::fromDto).toList();
    }
}
