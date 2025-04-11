package com.mockachu.web.webapi;

import com.mockachu.model.ApiTestDto;
import com.mockachu.model.ErrorInfo;
import com.mockachu.service.TestRunStatus;
import com.mockachu.service.TestService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("__webapi__/tests")
@CrossOrigin(origins = "*")
public class TestsController {
    private static final Logger log = LoggerFactory.getLogger(TestsController.class);
    private static final String TEST_NOT_FOUND = "Test not found";

    private final TestService testService;

    public TestsController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ApiTestDto> getAll() {
        return testService.getTests();
    }

    @ApiOperation(value = "Create new test or update an existing one", tags = "tests")
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ApiTestDto> createUpdateOne(@RequestBody List<ApiTestDto> list) throws IOException {
        testService.putTest(list.get(0), list.get(1));
        return testService.getTests();
    }

    @ApiOperation(value = "Create tests skipping existing ones", tags = "tests")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ApiTestDto> createAll(@RequestBody List<ApiTestDto> list) throws IOException {
        testService.putTests(list, false);
        return testService.getTests();
    }

    @ApiOperation(value = "Create test and update existing ones", tags = "tests")
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ApiTestDto> createAllUpdating(@RequestBody List<ApiTestDto> list) throws IOException {
        testService.putTests(list, true);
        return testService.getTests();
    }

    @ApiOperation(value = "Delete tests", tags = "tests")
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ApiTestDto> delete(@RequestBody List<ApiTestDto> list) throws IOException {
        testService.deleteTests(list);
        return testService.getTests();
    }

    @ApiOperation(value = "Execute test by alias", tags = "tests")
    @PostMapping(value = "/execute", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> execute(@RequestBody String alias) {
        var status = testService.execute(alias, false, true);

        if (status == TestRunStatus.NOT_FOUND) return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(TEST_NOT_FOUND);
        if (status == TestRunStatus.ALREADY_IN_PROGRESS) return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body("Test is already in progress");

        return ResponseEntity.ok("");
    }

    @ApiOperation(value = "Stop test by alias", tags = "tests")
    @PostMapping(value = "/stop", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> stop(@RequestBody String alias) {
        var status = testService.stop(alias);

        if (status == TestRunStatus.NOT_FOUND) return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(TEST_NOT_FOUND);

        return ResponseEntity.ok("Stopped");
    }

    @ApiOperation(value = "Get test result by alias", tags = "tests")
    @GetMapping(value = "/{alias}/result", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> result(@PathVariable String alias) {
        return ResponseEntity.ok(testService.getTestLog(alias));
    }

    @ApiOperation(value = "Clear test result by alias", tags = "tests")
    @PostMapping(value = "/{alias}/clear", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> clear(@PathVariable String alias) {
        var status = testService.clearTestLog(alias);

        if (status == TestRunStatus.NOT_FOUND) return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(TEST_NOT_FOUND);
        if (status == TestRunStatus.ALREADY_IN_PROGRESS) return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body("Test is in progress");

        return ResponseEntity.ok("");
    }

    @ExceptionHandler(produces = MediaType.APPLICATION_JSON_VALUE)
    protected ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("", e);
        return ResponseEntity.badRequest().body(ErrorInfo.of(e));
    }
}
