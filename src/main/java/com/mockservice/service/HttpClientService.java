package com.mockservice.service;

import com.mockservice.model.HttpRequestResult;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HttpClientService {
    Optional<HttpRequestResult> request(
            RequestMethod method, String uri, String requestBody, Map<String, List<String>> headers);
    void setCertificatePassword(String password) throws Exception;
}
