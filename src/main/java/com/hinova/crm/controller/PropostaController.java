package com.hinova.crm.controller;

import com.hinova.crm.dto.PropostaRequestDto;
import com.hinova.crm.dto.PropostaResponseDto;
import com.hinova.crm.service.PropostaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/propostas")
@RequiredArgsConstructor
@Tag(name = "Propostas", description = "Gerenciamento de propostas comerciais")
public class PropostaController {

    private final PropostaService service;

    @PostMapping
    @Operation(summary = "Criar nova proposta")
    public ResponseEntity<PropostaResponseDto> criar(@RequestBody @Valid PropostaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar proposta por ID")
    public ResponseEntity<PropostaResponseDto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar todas as propostas")
    public ResponseEntity<List<PropostaResponseDto>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @PostMapping("/{id}/enviar")
    @Operation(summary = "Enviar proposta para assinatura no SIGN")
    public ResponseEntity<PropostaResponseDto> enviarParaAssinatura(@PathVariable Long id) {
        return ResponseEntity.ok(service.enviarParaAssinatura(id));
    }

    @PostMapping("/callback/assinatura/{propostaId}")
    @Operation(summary = "Callback recebido do SIGN quando contrato é assinado")
    public ResponseEntity<Void> callbackAssinatura(@PathVariable Long propostaId) {
        service.receberCallbackAssinatura(propostaId);
        return ResponseEntity.noContent().build();
    }
}
