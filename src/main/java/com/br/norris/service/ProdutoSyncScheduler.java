package com.br.norris.service;

import com.br.norris.dto.ProdutoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoSyncScheduler {
    @Autowired
    private NorrisContratoService service;

    //Atualiza os dados na base de dades
    @Scheduled(fixedRate = 120000) // 15 min
    public void atualizarProdutos() {
        String token = service.buscarAccessToken();
        List<ProdutoDTO> produtos =
                service.buscarProdutos(token);
        service.sincronizar(produtos);
    }
}
