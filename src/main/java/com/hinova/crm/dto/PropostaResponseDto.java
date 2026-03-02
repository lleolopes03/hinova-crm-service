package com.hinova.crm.dto;

import com.hinova.crm.models.enums.StatusProposta;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropostaResponseDto {
    private Long id;
    private String clienteNome;
    private String clienteEmail;
    private String clienteEmpresa;
    private StatusProposta status;
    private Long contratoId;
    private LocalDateTime criadaEm;
    private LocalDateTime atualizadaEm;
    private List<ItemPropostaResponseDto> itens;
}
