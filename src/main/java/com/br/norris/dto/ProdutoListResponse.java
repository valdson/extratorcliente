package com.br.norris.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProdutoListResponse {

    private Boolean encontrado;

    private String mensagem;

    private List<ProdutoResumo> produtos;
}
