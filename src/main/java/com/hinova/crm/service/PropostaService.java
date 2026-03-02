package com.hinova.crm.service;

import com.hinova.crm.client.SignClient;
import com.hinova.crm.dto.*;
import com.hinova.crm.dto.mapper.PropostaMapper;
import com.hinova.crm.exception.PropostaNaoEncontradaException;
import com.hinova.crm.exception.StatusInvalidoException;
import com.hinova.crm.models.ItemProposta;
import com.hinova.crm.models.Proposta;
import com.hinova.crm.models.enums.StatusProposta;
import com.hinova.crm.repository.PropostaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropostaService {
    private final PropostaRepository repository;
    private final PropostaMapper mapper;
    private final SignClient signClient;

    @Transactional
    public PropostaResponseDto criar(PropostaRequestDto dto) {
        Proposta proposta = mapper.toEntity(dto);

        List<ItemProposta> itens = dto.getItens().stream()
                .map(itemDto -> {
                    ItemProposta item = mapper.toItemEntity(itemDto);
                    item.setProposta(proposta);
                    return item;
                }).toList();

        proposta.getItens().addAll(itens);
        return mapper.toResponseDto(repository.save(proposta));
    }


    public PropostaResponseDto buscarPorId(Long id) {
        return mapper.toResponseDto(buscarOuLancar(id));
    }


    public List<PropostaResponseDto> listarTodas() {
        return repository.findAll().stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Transactional
    public PropostaResponseDto enviarParaAssinatura(Long id) {
        Proposta proposta = buscarOuLancar(id);

        if (proposta.getStatus() != StatusProposta.RASCUNHO) {
            throw new StatusInvalidoException("Apenas propostas em RASCUNHO podem ser enviadas para assinatura");
        }

        ContratoRequestDto contratoRequest = ContratoRequestDto.builder()
                .propostaId(proposta.getId())
                .clienteNome(proposta.getClienteNome())
                .clienteEmail(proposta.getClienteEmail())
                .clienteEmpresa(proposta.getClienteEmpresa())
                .itens(proposta.getItens().stream()
                        .map(item -> ItemContratoDto.builder()
                                .nome(item.getNome())
                                .quantidade(item.getQuantidade())
                                .precoUnitario(item.getPrecoUnitario())
                                .build())
                        .toList())
                .build();

        ContratoResponseDto contratoResponse = signClient.enviarContrato(contratoRequest);

        proposta.setStatus(StatusProposta.ENVIADA_PARA_ASSINATURA);
        proposta.setContratoId(contratoResponse.getId());

        return mapper.toResponseDto(repository.save(proposta));
    }

    @Transactional
    public void receberCallbackAssinatura(Long propostaId) {
        Proposta proposta = buscarOuLancar(propostaId);
        proposta.setStatus(StatusProposta.ASSINADA);
        repository.save(proposta);
    }

    private Proposta buscarOuLancar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new PropostaNaoEncontradaException("Proposta não encontrada: " + id));
    }

}
