package com.mockservice.service;

import com.mockservice.domain.ApiTest;
import com.mockservice.model.ApiTestDto;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface TestService {
    TestRunStatus executeTest(String alias, boolean allowTrigger);
    TestRunStatus stopTest(String alias);
    String getTestResult(String alias);
    TestRunStatus clearTestResult(String alias);
    Optional<ApiTest> getEnabledTest(String alias);
    List<ApiTestDto> getTests();
    void putTest(ApiTestDto existing, ApiTestDto test) throws IOException;
    void putTests(List<ApiTestDto> dto, boolean overwrite) throws IOException;
    void deleteTests(List<ApiTestDto> dto) throws IOException;
}
