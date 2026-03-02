package com.hinova.crm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropostaRequestDto {
    @NotBlank(message = "Nome do cliente é obrigatório")
    private String clienteNome;

    @NotBlank(message = "Email do cliente é obrigatório")
    @Email(message = "Email inválido")
    private String clienteEmail;

    @NotBlank(message = "Empresa do cliente é obrigatória")
    private String clienteEmpresa;

    @NotEmpty(message = "A proposta deve ter ao menos um item")
    @Valid
    private List<ItemPropostaRequestDto> itens;
}
