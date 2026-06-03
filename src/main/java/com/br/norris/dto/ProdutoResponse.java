package com.br.norris.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoResponse {

    private Boolean encontrado;

    private String nome;

    private String codigo;

    private BigDecimal preco;

    private Integer estoque;

    private String imagemUrl;

    private String resposta;
}
