package com.mockservice.web.webapp;

import com.mockservice.model.OutboundRequestDto;
import com.mockservice.model.OutboundRequestResult;
import com.mockservice.service.RequestService;
import com.mockservice.template.MockVariables;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("web-api/requests")
@CrossOrigin(origins = "*")
public class WebApiRequestsController {

    private static final Logger log = LoggerFactory.getLogger(WebApiRequestsController.class);

    private final RequestService requestService;

    public WebApiRequestsController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    public List<OutboundRequestDto> getRequests() {
        return requestService.getRequests();
    }

    @ApiOperation(value = "Create new request or update existing one", tags = "requests")
    @PatchMapping
    public List<OutboundRequestDto> createUpdateOne(@RequestBody List<OutboundRequestDto> list) throws IOException {
        requestService.putRequest(list.get(0), list.get(1));
        return requestService.getRequests();
    }

    @ApiOperation(value = "Create requests skipping existing ones", tags = "requests")
    @PostMapping
    public List<OutboundRequestDto> createAll(@RequestBody List<OutboundRequestDto> list) throws IOException {
        requestService.putRequests(list, false);
        return requestService.getRequests();
    }

    @ApiOperation(value = "Create requests and update existing ones", tags = "requests")
    @PutMapping
    public List<OutboundRequestDto> createAllUpdating(@RequestBody List<OutboundRequestDto> list) throws IOException {
        requestService.putRequests(list, true);
        return requestService.getRequests();
    }

    @ApiOperation(value = "Delete requests", tags = "requests")
    @DeleteMapping
    public List<OutboundRequestDto> deleteRequests(@RequestBody List<OutboundRequestDto> list) throws IOException {
        requestService.deleteRequests(list);
        return requestService.getRequests();
    }

    @ApiOperation(value = "Execute request by ID", tags = "requests")
    @PostMapping("/execute")
    public String execute(@RequestBody OutboundRequestExecuteRequest request) throws IOException {
        Optional<OutboundRequestResult> result = requestService.executeRequest(
                request.getRequestId(), MockVariables.empty(), true);
        return result.map(Objects::toString).orElse("Request not found or disabled");
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
