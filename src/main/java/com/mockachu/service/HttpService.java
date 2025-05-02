package com.mockachu.service;

import com.mockachu.exception.HttpServiceException;
import com.mockachu.model.HttpRequestResult;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

public interface HttpService {
    void setCertificatePassword(String password) throws HttpServiceException;
    HttpRequestResult request(RequestMethod method, String uri, String requestBody, Map<String, List<String>> headers);
}
