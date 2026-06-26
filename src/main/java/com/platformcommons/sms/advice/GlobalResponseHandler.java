package com.platformcommons.sms.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    private static final String CONTROLLER_PACKAGE = "com.platformcommons.sms.controller";

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        String packageName = returnType.getDeclaringClass().getPackageName();
        return packageName.startsWith(CONTROLLER_PACKAGE);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        if (body instanceof ApiResponse<?>) {
            return body;
        }

        if (body instanceof String) {
            try {
                return objectMapper.writeValueAsString(new ApiResponse<>(body));
            } catch (Exception e) {
                throw new RuntimeException("Error converting string body to JSON payload architecture", e);
            }
        }

        return new ApiResponse<>(body);
    }
}
