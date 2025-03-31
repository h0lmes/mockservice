package com.mockservice.mapper;

import com.mockservice.domain.ApiTest;
import com.mockservice.model.ApiTestDto;

import java.util.List;

public interface ApiTestMapper {
    ApiTestDto toDto(ApiTest entity);
    ApiTest fromDto(ApiTestDto dto);
    List<ApiTestDto> toDto(List<ApiTest> entities);
    List<ApiTest> fromDto(List<ApiTestDto> dtos);
}
