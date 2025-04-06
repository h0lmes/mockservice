package com.mockachu.mapper;

import com.mockachu.domain.Route;
import com.mockachu.domain.RouteType;
import com.mockachu.model.RouteDto;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RouteMapperImplTest {

    private static final RequestMethod METHOD = RequestMethod.POST;
    private static final String STR = "AAA";
    private static final int INT = 404;

    private Route entity() {
        return new Route()
            .setGroup(STR)
            .setType(RouteType.SOAP)
            .setMethod(METHOD)
            .setPath(STR)
            .setAlt(STR)
            .setResponseCode(INT)
            .setResponse(STR)
            .setRequestBodySchema(STR)
            .setDisabled(true);
    }

    private RouteDto dto() {
        return new RouteDto()
            .setGroup(STR)
            .setType(RouteType.SOAP)
            .setMethod(METHOD)
            .setPath(STR)
            .setAlt(STR)
            .setResponseCode(INT)
            .setResponse(STR)
            .setRequestBodySchema(STR)
            .setDisabled(true);
    }

    @Test
    void toDto_OneInput_MappedCorrect() {
        var mapper = new RouteMapperImpl();
        var entity = entity();
        var dto = mapper.toDto(entity);

        assertAll(() -> {
            assertEquals(entity.getGroup(), dto.getGroup());
            assertEquals(entity.getType(), dto.getType());
            assertEquals(entity.getMethod(), dto.getMethod());
            assertEquals(entity.getPath(), dto.getPath());
            assertEquals(entity.getAlt(), dto.getAlt());
            assertEquals(entity.getResponseCode(), dto.getResponseCode());
            assertEquals(entity.getResponse(), dto.getResponse());
            assertEquals(entity.getRequestBodySchema(), dto.getRequestBodySchema());
            assertEquals(entity.getDisabled(), dto.getDisabled());
        });
    }

    @Test
    void toDto_ListInput_MappedCorrect() {
        var mapper = new RouteMapperImpl();
        var entity = entity();
        var list = mapper.toDto(List.of(entity));

        assertEquals(1, list.size());
        var dto = list.get(0);
        assertAll(() -> {
            assertEquals(entity.getGroup(), dto.getGroup());
            assertEquals(entity.getType(), dto.getType());
            assertEquals(entity.getMethod(), dto.getMethod());
            assertEquals(entity.getPath(), dto.getPath());
            assertEquals(entity.getAlt(), dto.getAlt());
            assertEquals(entity.getResponseCode(), dto.getResponseCode());
            assertEquals(entity.getResponse(), dto.getResponse());
            assertEquals(entity.getRequestBodySchema(), dto.getRequestBodySchema());
            assertEquals(entity.getDisabled(), dto.getDisabled());
        });
    }

    @Test
    void fromDto_OneInput_MappedCorrect() {
        var mapper = new RouteMapperImpl();
        var dto = dto();
        var entity = mapper.fromDto(dto);

        assertAll(() -> {
            assertEquals(dto.getGroup(), entity.getGroup());
            assertEquals(dto.getType(), entity.getType());
            assertEquals(dto.getMethod(), entity.getMethod());
            assertEquals(dto.getPath(), entity.getPath());
            assertEquals(dto.getAlt(), entity.getAlt());
            assertEquals(dto.getResponseCode(), entity.getResponseCode());
            assertEquals(dto.getResponse(), entity.getResponse());
            assertEquals(dto.getRequestBodySchema(), entity.getRequestBodySchema());
            assertEquals(dto.getDisabled(), entity.getDisabled());
        });
    }

    @Test
    void fromDto_ListInput_MappedCorrect() {
        var mapper = new RouteMapperImpl();
        var dto = dto();
        var list = mapper.fromDto(List.of(dto));

        assertEquals(1, list.size());
        var entity = list.get(0);
        assertAll(() -> {
            assertEquals(dto.getGroup(), entity.getGroup());
            assertEquals(dto.getType(), entity.getType());
            assertEquals(dto.getMethod(), entity.getMethod());
            assertEquals(dto.getPath(), entity.getPath());
            assertEquals(dto.getAlt(), entity.getAlt());
            assertEquals(dto.getResponseCode(), entity.getResponseCode());
            assertEquals(dto.getResponse(), entity.getResponse());
            assertEquals(dto.getRequestBodySchema(), entity.getRequestBodySchema());
            assertEquals(dto.getDisabled(), entity.getDisabled());
        });
    }
}
