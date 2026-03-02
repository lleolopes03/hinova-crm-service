package com.hinova.crm.repository;

import com.hinova.crm.models.Proposta;
import com.hinova.crm.models.enums.StatusProposta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropostaRepository extends JpaRepository<Proposta,Long> {
    List<Proposta> findByStatus(StatusProposta status);
}
