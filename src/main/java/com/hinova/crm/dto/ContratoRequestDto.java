package com.hinova.crm.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContratoRequestDto {
    private Long propostaId;
    private String clienteNome;
    private String clienteEmail;
    private String clienteEmpresa;
    private List<ItemContratoDto> itens;
}
