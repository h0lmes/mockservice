package com.mockachu.web.webapi;

import com.mockachu.model.ErrorInfo;
import com.mockachu.model.HttpRequestResult;
import com.mockachu.model.OutboundRequestDto;
import com.mockachu.model.OutboundRequestExecuteRequest;
import com.mockachu.service.RequestService;
import com.mockachu.template.MockVariables;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("__webapi__/requests")
@CrossOrigin(origins = "*")
public class RequestsController {
    private static final Logger log = LoggerFactory.getLogger(RequestsController.class);

    private final RequestService requestService;

    public RequestsController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OutboundRequestDto> getRequests() {
        return requestService.getRequests();
    }

    @ApiOperation(value = "Create new request or update existing one", tags = "requests")
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OutboundRequestDto> createUpdateOne(@RequestBody List<OutboundRequestDto> list) throws IOException {
        requestService.putRequest(list.get(0), list.get(1));
        return requestService.getRequests();
    }

    @ApiOperation(value = "Create requests skipping existing ones", tags = "requests")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OutboundRequestDto> createAll(@RequestBody List<OutboundRequestDto> list) throws IOException {
        requestService.putRequests(list, false);
        return requestService.getRequests();
    }

    @ApiOperation(value = "Create requests and update existing ones", tags = "requests")
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OutboundRequestDto> createAllUpdating(@RequestBody List<OutboundRequestDto> list) throws IOException {
        requestService.putRequests(list, true);
        return requestService.getRequests();
    }

    @ApiOperation(value = "Delete requests", tags = "requests")
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OutboundRequestDto> deleteRequests(@RequestBody List<OutboundRequestDto> list) throws IOException {
        requestService.deleteRequests(list);
        return requestService.getRequests();
    }

    @ApiOperation(value = "Execute request by ID", tags = "requests")
    @PostMapping(value = "/execute", produces = MediaType.TEXT_PLAIN_VALUE)
    public String execute(@RequestBody OutboundRequestExecuteRequest request) throws IOException {
        Optional<HttpRequestResult> result = requestService.executeRequest(
                request.getRequestId(), MockVariables.empty(), true);
        return result.map(Objects::toString).orElse("Request not found or disabled");
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
