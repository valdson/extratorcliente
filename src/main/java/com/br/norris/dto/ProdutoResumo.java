package com.br.norris.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProdutoResumo {

    private String nome;

    private BigDecimal preco;

    private Integer estoque;

    private String urlProduto;
}
