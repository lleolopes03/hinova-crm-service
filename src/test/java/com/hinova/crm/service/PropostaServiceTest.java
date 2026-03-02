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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropostaServiceTest {

    @Mock
    private PropostaRepository repository;

    @Mock
    private PropostaMapper mapper;

    @Mock
    private SignClient signClient;

    @InjectMocks
    private PropostaService service;

    private PropostaRequestDto requestDto;
    private Proposta proposta;
    private PropostaResponseDto responseDto;

    @BeforeEach
    void setUp() {
        ItemPropostaRequestDto itemDto = ItemPropostaRequestDto.builder()
                .nome("Licença Software")
                .quantidade(2)
                .precoUnitario(new BigDecimal("500.00"))
                .build();

        requestDto = PropostaRequestDto.builder()
                .clienteNome("João Silva")
                .clienteEmail("joao@empresa.com")
                .clienteEmpresa("Empresa LTDA")
                .itens(List.of(itemDto))
                .build();

        ItemProposta item = ItemProposta.builder()
                .id(1L)
                .nome("Licença Software")
                .quantidade(2)
                .precoUnitario(new BigDecimal("500.00"))
                .build();

        proposta = Proposta.builder()
                .id(1L)
                .clienteNome("João Silva")
                .clienteEmail("joao@empresa.com")
                .clienteEmpresa("Empresa LTDA")
                .status(StatusProposta.RASCUNHO)
                .criadaEm(LocalDateTime.now())
                .itens(new java.util.ArrayList<>(List.of(item)))
                .build();

        ItemPropostaResponseDto itemResponseDto = new ItemPropostaResponseDto();

        responseDto = PropostaResponseDto.builder()
                .id(1L)
                .clienteNome("João Silva")
                .clienteEmail("joao@empresa.com")
                .clienteEmpresa("Empresa LTDA")
                .status(StatusProposta.RASCUNHO)
                .itens(List.of(itemResponseDto))
                .build();
    }

    @Test
    void criar_deveSalvarERetornarPropostaDto() {
        when(mapper.toEntity(requestDto)).thenReturn(proposta);
        when(mapper.toItemEntity(any())).thenReturn(proposta.getItens().get(0));
        when(repository.save(proposta)).thenReturn(proposta);
        when(mapper.toResponseDto(proposta)).thenReturn(responseDto);

        PropostaResponseDto resultado = service.criar(requestDto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getClienteNome()).isEqualTo("João Silva");
        verify(repository).save(proposta);
    }

    @Test
    void buscarPorId_deveRetornarPropostaQuandoEncontrada() {
        when(repository.findById(1L)).thenReturn(Optional.of(proposta));
        when(mapper.toResponseDto(proposta)).thenReturn(responseDto);

        PropostaResponseDto resultado = service.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    void buscarPorId_deveLancarExcecaoQuandoNaoEncontrada() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(PropostaNaoEncontradaException.class)
                .hasMessageContaining("99");
    }

    @Test
    void listarTodas_deveRetornarListaDePropostas() {
        when(repository.findAll()).thenReturn(List.of(proposta));
        when(mapper.toResponseDto(proposta)).thenReturn(responseDto);

        List<PropostaResponseDto> resultado = service.listarTodas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getClienteNome()).isEqualTo("João Silva");
    }

    @Test
    void listarTodas_deveRetornarListaVaziaQuandoNaoHaPropostas() {
        when(repository.findAll()).thenReturn(List.of());

        List<PropostaResponseDto> resultado = service.listarTodas();

        assertThat(resultado).isEmpty();
    }

    @Test
    void enviarParaAssinatura_deveAtualizarStatusEContratoId() {
        ContratoResponseDto contratoResponse = ContratoResponseDto.builder()
                .id(10L)
                .status("AGUARDANDO_ASSINATURA")
                .build();

        PropostaResponseDto responseEnviada = PropostaResponseDto.builder()
                .id(1L)
                .status(StatusProposta.ENVIADA_PARA_ASSINATURA)
                .contratoId(10L)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(proposta));
        when(signClient.enviarContrato(any())).thenReturn(contratoResponse);
        when(repository.save(proposta)).thenReturn(proposta);
        when(mapper.toResponseDto(proposta)).thenReturn(responseEnviada);

        PropostaResponseDto resultado = service.enviarParaAssinatura(1L);

        assertThat(resultado.getStatus()).isEqualTo(StatusProposta.ENVIADA_PARA_ASSINATURA);
        assertThat(resultado.getContratoId()).isEqualTo(10L);
        assertThat(proposta.getStatus()).isEqualTo(StatusProposta.ENVIADA_PARA_ASSINATURA);
        assertThat(proposta.getContratoId()).isEqualTo(10L);
        verify(signClient).enviarContrato(any());
        verify(repository).save(proposta);
    }

    @Test
    void enviarParaAssinatura_deveLancarExcecaoQuandoStatusNaoERascunho() {
        proposta.setStatus(StatusProposta.ENVIADA_PARA_ASSINATURA);
        when(repository.findById(1L)).thenReturn(Optional.of(proposta));

        assertThatThrownBy(() -> service.enviarParaAssinatura(1L))
                .isInstanceOf(StatusInvalidoException.class)
                .hasMessageContaining("RASCUNHO");
    }

    @Test
    void enviarParaAssinatura_deveLancarExcecaoParaPropostaAssinada() {
        proposta.setStatus(StatusProposta.ASSINADA);
        when(repository.findById(1L)).thenReturn(Optional.of(proposta));

        assertThatThrownBy(() -> service.enviarParaAssinatura(1L))
                .isInstanceOf(StatusInvalidoException.class);
    }

    @Test
    void receberCallbackAssinatura_deveAtualizarStatusParaAssinada() {
        when(repository.findById(1L)).thenReturn(Optional.of(proposta));
        when(repository.save(proposta)).thenReturn(proposta);

        service.receberCallbackAssinatura(1L);

        assertThat(proposta.getStatus()).isEqualTo(StatusProposta.ASSINADA);
        verify(repository).save(proposta);
    }

    @Test
    void receberCallbackAssinatura_deveLancarExcecaoQuandoPropostaNaoEncontrada() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.receberCallbackAssinatura(99L))
                .isInstanceOf(PropostaNaoEncontradaException.class);
    }
}
