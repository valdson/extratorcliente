package com.br.norris.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProdutoDTO {

    private Long id;

    private String nome;

    private String codigo;

    private BigDecimal preco;

    private BigDecimal precoCusto;

    private EstoqueDTO estoque;

    private String tipo;

    private String situacao;

    private String formato;

    private String descricaoCurta;

    private String imagemURL;
}
