package com.mockachu.mapper;

import com.mockachu.domain.OutboundRequest;
import com.mockachu.domain.RouteType;
import com.mockachu.model.OutboundRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OutboundRequestMapperImplTest {

    private static final RequestMethod METHOD = RequestMethod.POST;
    private static final String STR = "AAA";

    private OutboundRequest entity() {
        return new OutboundRequest()
                .setId(STR)
                .setGroup(STR)
                .setType(RouteType.REST)
                .setMethod(METHOD)
                .setPath(STR)
                .setHeaders(STR)
                .setBody(STR)
                .setTriggerRequestIds(STR)
                .setTriggerRequest(true)
                .setResponseToVars(true)
                .setDisabled(true);
    }

    private OutboundRequestDto dto() {
        return new OutboundRequestDto()
                .setId(STR)
                .setGroup(STR)
                .setType(RouteType.REST)
                .setMethod(METHOD)
                .setPath(STR)
                .setHeaders(STR)
                .setBody(STR)
                .setTriggerRequestIds(STR)
                .setTriggerRequest(true)
                .setResponseToVars(true)
                .setDisabled(true);
    }

    @Test
    void toDto_OneInput() {
        var mapper = new OutboundRequestMapperImpl();
        var entity = entity();
        var dto = mapper.toDto(entity);

        assertAll(() -> {
            assertEquals(entity.getId(), dto.getId());
            assertEquals(entity.getGroup(), dto.getGroup());
            assertEquals(entity.getType(), dto.getType());
            assertEquals(entity.getMethod(), dto.getMethod());
            assertEquals(entity.getPath(), dto.getPath());
            assertEquals(entity.getHeaders(), dto.getHeaders());
            assertEquals(entity.getBody(), dto.getBody());
            assertEquals(entity.getTriggerRequestIds(), dto.getTriggerRequestIds());
            assertEquals(entity.isTriggerRequest(), dto.isTriggerRequest());
            assertEquals(entity.isResponseToVars(), dto.isResponseToVars());
            assertEquals(entity.isDisabled(), dto.isDisabled());
        });
    }

    @Test
    void toDto_ListInput() {
        var mapper = new OutboundRequestMapperImpl();
        var entity = entity();
        var list = mapper.toDto(List.of(entity));

        assertEquals(1, list.size());
        var dto = list.get(0);
        assertAll(() -> {
            assertEquals(entity.getId(), dto.getId());
            assertEquals(entity.getGroup(), dto.getGroup());
            assertEquals(entity.getType(), dto.getType());
            assertEquals(entity.getMethod(), dto.getMethod());
            assertEquals(entity.getPath(), dto.getPath());
            assertEquals(entity.getHeaders(), dto.getHeaders());
            assertEquals(entity.getBody(), dto.getBody());
            assertEquals(entity.getTriggerRequestIds(), dto.getTriggerRequestIds());
            assertEquals(entity.isTriggerRequest(), dto.isTriggerRequest());
            assertEquals(entity.isResponseToVars(), dto.isResponseToVars());
            assertEquals(entity.isDisabled(), dto.isDisabled());
        });
    }

    @Test
     void fromDto_OneInput() {
        var mapper = new OutboundRequestMapperImpl();
        var dto = dto();
        var entity = mapper.fromDto(dto);

        assertAll(() -> {
            assertEquals(dto.getId(),      entity.getId());
            assertEquals(dto.getGroup(),   entity.getGroup());
            assertEquals(dto.getType(),    entity.getType());
            assertEquals(dto.getMethod(),  entity.getMethod());
            assertEquals(dto.getPath(),    entity.getPath());
            assertEquals(dto.getHeaders(), entity.getHeaders());
            assertEquals(dto.getBody(),    entity.getBody());
            assertEquals(dto.getTriggerRequestIds(), entity.getTriggerRequestIds());
            assertEquals(dto.isTriggerRequest(),     entity.isTriggerRequest());
            assertEquals(dto.isResponseToVars(),     entity.isResponseToVars());
            assertEquals(dto.isDisabled(),           entity.isDisabled());
        });
    }

    @Test
    void fromDto_ListInput() {
        var mapper = new OutboundRequestMapperImpl();
        var dto = dto();
        var list = mapper.fromDto(List.of(dto));

        assertEquals(1, list.size());
        var entity = list.get(0);
        assertAll(() -> {
            assertEquals(dto.getId(),      entity.getId());
            assertEquals(dto.getGroup(),   entity.getGroup());
            assertEquals(dto.getType(),    entity.getType());
            assertEquals(dto.getMethod(),  entity.getMethod());
            assertEquals(dto.getPath(),    entity.getPath());
            assertEquals(dto.getHeaders(), entity.getHeaders());
            assertEquals(dto.getBody(),    entity.getBody());
            assertEquals(dto.getTriggerRequestIds(), entity.getTriggerRequestIds());
            assertEquals(dto.isTriggerRequest(),     entity.isTriggerRequest());
            assertEquals(dto.isResponseToVars(),     entity.isResponseToVars());
            assertEquals(dto.isDisabled(),           entity.isDisabled());
        });
    }
}
