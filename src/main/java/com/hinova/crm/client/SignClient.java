package com.hinova.crm.client;

import com.hinova.crm.dto.ContratoRequestDto;
import com.hinova.crm.dto.ContratoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class SignClient {
    private final RestClient restClient;

    @Value("${sign.service.url}")
    private String signServiceUrl;

    public ContratoResponseDto enviarContrato(ContratoRequestDto request) {
        return restClient.post()
                .uri(signServiceUrl + "/api/v1/contratos")
                .body(request)
                .retrieve()
                .body(ContratoResponseDto.class);
    }
}
