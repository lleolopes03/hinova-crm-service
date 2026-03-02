package com.hinova.crm.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemPropostaResponseDto {
    private Long id;
    private String nome;
    private Integer quantidade;
    private BigDecimal precoUnitario;
}
