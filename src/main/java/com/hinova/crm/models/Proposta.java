package com.hinova.crm.models;

import com.hinova.crm.models.enums.StatusProposta;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "propostas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Proposta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String clienteNome;

    @Column(nullable = false)
    private String clienteEmail;

    @Column(nullable = false)
    private String clienteEmpresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProposta status;

    @Column
    private Long contratoId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @Column
    private LocalDateTime atualizadaEm;

    @OneToMany(mappedBy = "proposta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ItemProposta> itens = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.criadaEm = LocalDateTime.now();
        this.status = StatusProposta.RASCUNHO;
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadaEm = LocalDateTime.now();
    }
}
