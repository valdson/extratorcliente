package com.br.norris.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "produto")
@Getter
@Setter
public class Produto {

    @Id
    private Long idBling;

    private String nome;

    private String codigo;

    private BigDecimal preco;

    private Integer estoque;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private LocalDateTime atualizadoEm;

    private String imagemUrl;

    @Column(length = 1000)
    private String urlProduto;
}
