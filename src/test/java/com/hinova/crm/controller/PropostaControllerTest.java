package com.hinova.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hinova.crm.dto.ItemPropostaRequestDto;
import com.hinova.crm.dto.PropostaRequestDto;
import com.hinova.crm.dto.PropostaResponseDto;
import com.hinova.crm.exception.GlobalExceptionHandler;
import com.hinova.crm.exception.PropostaNaoEncontradaException;
import com.hinova.crm.exception.StatusInvalidoException;
import com.hinova.crm.models.enums.StatusProposta;
import com.hinova.crm.service.PropostaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PropostaControllerTest {

    @Mock
    private PropostaService service;

    @InjectMocks
    private PropostaController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private PropostaRequestDto requestValido;
    private PropostaResponseDto responseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        ItemPropostaRequestDto item = ItemPropostaRequestDto.builder()
                .nome("Licença Software")
                .quantidade(2)
                .precoUnitario(new BigDecimal("500.00"))
                .build();

        requestValido = PropostaRequestDto.builder()
                .clienteNome("João Silva")
                .clienteEmail("joao@empresa.com")
                .clienteEmpresa("Empresa LTDA")
                .itens(List.of(item))
                .build();

        responseDto = PropostaResponseDto.builder()
                .id(1L)
                .clienteNome("João Silva")
                .clienteEmail("joao@empresa.com")
                .clienteEmpresa("Empresa LTDA")
                .status(StatusProposta.RASCUNHO)
                .build();
    }

    @Test
    void criar_deveRetornar201ComPropostaCriada() throws Exception {
        when(service.criar(any())).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/propostas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.clienteNome").value("João Silva"))
                .andExpect(jsonPath("$.status").value("RASCUNHO"));
    }

    @Test
    void criar_deveRetornar400QuandoNomeFaltando() throws Exception {
        requestValido.setClienteNome(null);

        mockMvc.perform(post("/api/v1/propostas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_deveRetornar400QuandoEmailInvalido() throws Exception {
        requestValido.setClienteEmail("email-invalido");

        mockMvc.perform(post("/api/v1/propostas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_deveRetornar400QuandoListaDeItensVazia() throws Exception {
        requestValido.setItens(List.of());

        mockMvc.perform(post("/api/v1/propostas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarPorId_deveRetornar200QuandoEncontrada() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/propostas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.clienteNome").value("João Silva"));
    }

    @Test
    void buscarPorId_deveRetornar404QuandoNaoEncontrada() throws Exception {
        when(service.buscarPorId(99L))
                .thenThrow(new PropostaNaoEncontradaException("Proposta não encontrada: 99"));

        mockMvc.perform(get("/api/v1/propostas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Proposta não encontrada: 99"));
    }

    @Test
    void listarTodas_deveRetornar200ComLista() throws Exception {
        when(service.listarTodas()).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/v1/propostas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void listarTodas_deveRetornar200ComListaVazia() throws Exception {
        when(service.listarTodas()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/propostas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void enviarParaAssinatura_deveRetornar200ComStatusAtualizado() throws Exception {
        PropostaResponseDto enviada = PropostaResponseDto.builder()
                .id(1L)
                .status(StatusProposta.ENVIADA_PARA_ASSINATURA)
                .contratoId(10L)
                .build();

        when(service.enviarParaAssinatura(1L)).thenReturn(enviada);

        mockMvc.perform(post("/api/v1/propostas/1/enviar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENVIADA_PARA_ASSINATURA"))
                .andExpect(jsonPath("$.contratoId").value(10L));
    }

    @Test
    void enviarParaAssinatura_deveRetornar422QuandoStatusInvalido() throws Exception {
        when(service.enviarParaAssinatura(1L))
                .thenThrow(new StatusInvalidoException("Apenas propostas em RASCUNHO podem ser enviadas para assinatura"));

        mockMvc.perform(post("/api/v1/propostas/1/enviar"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Apenas propostas em RASCUNHO podem ser enviadas para assinatura"));
    }

    @Test
    void callbackAssinatura_deveRetornar204() throws Exception {
        doNothing().when(service).receberCallbackAssinatura(1L);

        mockMvc.perform(post("/api/v1/propostas/callback/assinatura/1"))
                .andExpect(status().isNoContent());

        verify(service).receberCallbackAssinatura(1L);
    }

    @Test
    void callbackAssinatura_deveRetornar404QuandoPropostaNaoEncontrada() throws Exception {
        doThrow(new PropostaNaoEncontradaException("Proposta não encontrada: 99"))
                .when(service).receberCallbackAssinatura(99L);

        mockMvc.perform(post("/api/v1/propostas/callback/assinatura/99"))
                .andExpect(status().isNotFound());
    }
}
