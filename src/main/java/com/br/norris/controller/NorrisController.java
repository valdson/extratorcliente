package com.br.norris.controller;

import com.br.norris.dto.ConsultaProdutoRequest;
import com.br.norris.dto.ProdutoDTO;
import com.br.norris.dto.ProdutoListResponse;
import com.br.norris.dto.ProdutoResponse;
import com.br.norris.dto.request.ProdutoBuscaRequest;
import com.br.norris.entity.Produto;
import com.br.norris.repository.ProdutoRepository;
import com.br.norris.service.NorrisContratoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bling")

public class NorrisController {
    @Autowired
    private NorrisContratoService service;

    @Value("${bling.client-id}")
    private String clientId;

    @Value("${bling.client-secret}")
    private String clientSecret;

    @Value("${bling.redirect-uri}")
    private String redirectUri;

    @GetMapping("/sincronizar")
    public String sincronizar() {
        service.sincronizarContatos();
        return "Sincronização iniciada";
    }

    @GetMapping("/oauth/url")
    public void gerarUrlOAuth(HttpServletResponse response) throws IOException {

        String state = UUID.randomUUID().toString();

        String url = "https://www.bling.com.br/Api/v3/oauth/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&state=" + state;

        response.sendRedirect(url);

    }

    @GetMapping("/callback")
    public String callback(@RequestParam String code) {

        String retorno = service.gerarPrimeiroToken(code);

        return "Token salvo com sucesso" + retorno;
    }

    @PostMapping("/produto")
    public ProdutoResponse buscarProduto(@RequestBody ConsultaProdutoRequest request) {
        return service.buscarProduto(request.getNome());
    }

    //Busca Esilo Google para o ElevenLabs
    @PostMapping("/produtos/busca")
    public ProdutoListResponse buscar(@RequestBody ProdutoBuscaRequest request) {
        return service.buscarInteligente(request.getQuery());
    }

 }
