package com.mockachu.service;

import com.mockachu.domain.ApiTest;
import com.mockachu.model.ApiTestDto;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface TestService {
    TestRunStatus execute(String alias, boolean allowTrigger, boolean async);
    TestRunStatus stop(String alias);
    String getTestLog(String alias);
    TestRunStatus clearTestLog(String alias);
    Optional<ApiTest> getEnabledTest(String alias);
    List<ApiTestDto> getTests();
    void putTest(ApiTestDto existing, ApiTestDto test) throws IOException;
    void putTests(List<ApiTestDto> dto, boolean overwrite) throws IOException;
    void deleteTests(List<ApiTestDto> dto) throws IOException;
}
