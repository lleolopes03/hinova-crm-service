package com.hinova.crm.dto.mapper;

import com.hinova.crm.dto.ItemPropostaRequestDto;
import com.hinova.crm.dto.ItemPropostaResponseDto;
import com.hinova.crm.dto.PropostaRequestDto;
import com.hinova.crm.dto.PropostaResponseDto;
import com.hinova.crm.models.ItemProposta;
import com.hinova.crm.models.Proposta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PropostaMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "contratoId", ignore = true)
    @Mapping(target = "criadaEm", ignore = true)
    @Mapping(target = "atualizadaEm", ignore = true)
    @Mapping(target = "itens", ignore = true)
    Proposta toEntity(PropostaRequestDto dto);

    PropostaResponseDto toResponseDto(Proposta proposta);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "proposta", ignore = true)
    ItemProposta toItemEntity(ItemPropostaRequestDto dto);

    ItemPropostaResponseDto toItemResponseDto(ItemProposta item);
}
