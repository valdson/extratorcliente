package com.br.norris.service;

import com.br.norris.dto.*;
import com.br.norris.entity.Cliente;
import com.br.norris.entity.ControleSync;
import com.br.norris.entity.Produto;
import com.br.norris.repository.ClienteRepository;
import com.br.norris.repository.ControleSyncRepository;
import com.br.norris.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service

public class NorrisContratoService {
    private static final String BASE_URL = "https://www.bling.com.br/Api/v3";
    private static final int LIMITE = 100;
    @Autowired
    private ProdutoRepository repositoryProduto;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ControleSyncRepository controleRepository;
    @Value("${bling.token}")
    private String token;
    private final BlingTokenService tokenService;

    @Value("${bling.client-id}")
    private String clientId;

    @Value("${bling.client-secret}")
    private String clientSecret;

    public NorrisContratoService(BlingTokenService tokenService) {
        this.tokenService = tokenService;
    }

    public ProdutoResponse buscarProduto(String nome) {

        List<Produto> produtos =
                repositoryProduto.findByNomeContainingIgnoreCase(nome);

        if (produtos.isEmpty()) {
            return new ProdutoResponse(
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "Produto não encontrado"
            );
        }

        Produto produto = produtos.get(0);

        return new ProdutoResponse(
                true,
                produto.getNome(),
                produto.getCodigo(),
                produto.getPreco(),
                produto.getEstoque(),
                produto.getImagemUrl(),
                String.format(
                        "%s custa R$ %.2f e possui %d unidades em estoque.",
                        produto.getNome(),
                        produto.getPreco(),
                        produto.getEstoque()
                )
        );
    }

 /*   public List<ProdutoDTO> buscarProdutos(String accessToken) {

        String url = "https://www.bling.com.br/Api/v3/produtos";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ProdutosResponseDTO> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        ProdutosResponseDTO.class
                );

        return response.getBody().getData();
    }*/

    public List<ProdutoDTO> buscarProdutos(String accessToken) {

        String urlBase = "https://www.bling.com.br/Api/v3/produtos";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        List<ProdutoDTO> todosProdutos = new ArrayList<>();

        int pagina = 1;
        int tamanhoPagina = 100;

        while (true) {

            String url = urlBase + "?pagina=" + pagina + "&limite=" + tamanhoPagina;

            ResponseEntity<ProdutosResponseDTO> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            ProdutosResponseDTO.class
                    );

            ProdutosResponseDTO body = response.getBody();

            if (body == null || body.getData() == null || body.getData().isEmpty()) {
                break;
            }

            todosProdutos.addAll(body.getData());

            // se veio menos que 100, acabou
            if (body.getData().size() < tamanhoPagina) {
                break;
            }

            pagina++;
        }

        return todosProdutos;
    }

    public void sincronizarContatos() {

        int pagina = 1;

        while (true) {
            try {
                List<ContratoResumoDTO> contatos = buscarPagina(pagina);
                for (ContratoResumoDTO contato : contatos) {
                    try {
                        Thread.sleep(300); ContratoDetalheDTO detalhe = buscarDetalheContato(contato.getId());
                        if (deveSalvar(detalhe)) {
                            salvarCliente(detalhe);
                        }
                    } catch (Exception ex) {
                        System.out.println( "Erro contato " + contato.getId() );
                        ex.printStackTrace();
                        salvarCheckpoint(pagina, contato.getId());
                    }
                }
                salvarCheckpoint(pagina-1, null);
                pagina++;
            } catch (Exception ex) {
                System.out.println("Erro página " + pagina);
                ex.printStackTrace();
                salvarCheckpoint(pagina, null);
            }
        }
    }

/*    private int buscarTotalPaginas() {
        String url = BASE_URL + "/contatos?pagina=1&limite=" + LIMITE;
        HttpEntity<Void> entity = new HttpEntity<>(criarHeaders());
        ResponseEntity<Map> response = restTemplate.exchange( url, HttpMethod.GET, entity, Map.class );
        Map body = response.getBody();
        Integer total = (Integer) body.get("total");
        if (total == null) {
            total = 0;
        }
        return (int) Math.ceil((double) total / LIMITE);
    }*/

    public int buscarTotalPaginas() {
        String token = tokenService.getValidToken();
        int limite = 100;

        String url = "https://www.bling.com.br/Api/v3/contatos?pagina=1&limite=" + limite;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                JsonNode.class
        );

        System.out.println(" GETBODY ====" +response.getBody().toPrettyString());

        JsonNode meta = response.getBody().get("meta");

        int total = meta.get("total").asInt();

        return (int) Math.ceil((double) total / limite);
    }

    private List<ContratoResumoDTO> buscarPagina(int pagina) {
        String url = BASE_URL + "/contatos?pagina=" + pagina + "&limite=" + LIMITE;
        HttpEntity<Void> entity = new HttpEntity<>(criarHeaders());
        ResponseEntity<Map> response = chamarComRetry(url, HttpMethod.GET, entity);
        Map body = response.getBody();
        List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
        List<ContratoResumoDTO> lista = new ArrayList<>();
        for (Map<String, Object> item : data) {
            ContratoResumoDTO dto = new ContratoResumoDTO();
            dto.setId( Long.valueOf(item.get("id").toString()) );
            dto.setNome( String.valueOf(item.get("nome")) );
            lista.add(dto);
        }
        return lista;
    }

    private ContratoDetalheDTO buscarDetalheContato(Long id) {
        String url = BASE_URL + "/contatos/" + id;
        HttpEntity<Void> entity = new HttpEntity<>(criarHeaders());
        ResponseEntity<Map> response = chamarComRetry(url, HttpMethod.GET, entity);
        Map body = response.getBody();
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        ContratoDetalheDTO dto = new ContratoDetalheDTO();
        dto.setId( Long.valueOf(data.get("id").toString()) );
        dto.setNome( String.valueOf(data.get("nome")) );
        dto.setEmail( String.valueOf(data.get("email")) );
        dto.setTelefone( normalizarTelefone( String.valueOf(data.get("telefone")) ) ); return dto;
    }
    private void salvarCliente(ContratoDetalheDTO dto) { Cliente cliente = clienteRepository .findByBlingId(dto.getId()) .orElse(new Cliente());
        cliente.setBlingId(dto.getId());
        cliente.setNome(dto.getNome());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefone(dto.getTelefone());
        clienteRepository.save(cliente);
    }

    private ResponseEntity<Map> chamarComRetry(String url, HttpMethod method, HttpEntity<?> entity ) {
        int tentativas = 0; while (tentativas < 2) {
            try {
                return restTemplate.exchange( url, method, entity, Map.class );
            } catch (Exception ex) {
                    tentativas++;
                    System.out.println( "Tentativa " + tentativas );
                    System.out.println("Erro Insert : " + ex);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } throw new RuntimeException( "Falha após várias tentativas" );
    }

    private boolean deveSalvar(ContratoDetalheDTO dto ) {
        boolean temEmail = dto.getEmail() != null && !dto.getEmail().isBlank() && !dto.getEmail().equalsIgnoreCase("null");
        boolean temTelefone = dto.getTelefone() != null && !dto.getTelefone().isBlank();
        return temEmail || temTelefone; }
    private String normalizarTelefone(String telefone) {
        if (telefone == null) { return null; }
        return telefone.replaceAll("\\D", "");
    }

    private ControleSync buscarControle() {
        return controleRepository .findTopByProcessoOrderByIdDesc( "CONTATOS_BLING" ) .orElse(null);
    }

    private void salvarCheckpoint(Integer pagina, Long contatoId ){
        ControleSync controle = new ControleSync();
        controle.setProcesso("CONTATOS_BLING"); controle.setUltimaPagina(pagina);
        controle.setUltimoContatoId(contatoId); controle.setDataExecucao( LocalDateTime.now() );
        controleRepository.save(controle);
    }
    private HttpHeaders criarHeaders() {
        String token = tokenService.getValidToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType( MediaType.APPLICATION_JSON );
        return headers;
    }

    public String gerarPrimeiroToken(String code) {

        String url = "https://www.bling.com.br/Api/v3/oauth/token";

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_FORM_URLENCODED
        );

        // client_id + client_secret
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body =
                new LinkedMultiValueMap<>();

        body.add("grant_type", "authorization_code");

        // code recebido do callback OAuth
        body.add("code", code);

        HttpEntity<?> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        url,
                        request,
                        Map.class
                );

        tokenService.saveToken(response.getBody());

        return response.getBody().toString();
    }

    public String buscarAccessToken(){
        return token = tokenService.getValidToken();
    }

    public void sincronizar(List<ProdutoDTO> produtos) {

        for (ProdutoDTO dto : produtos) {
            Produto produto =
                    repositoryProduto.findById(dto.getId())
                            .orElse(new Produto());

            produto.setIdBling(dto.getId());
            produto.setNome(dto.getNome());
            produto.setCodigo(dto.getCodigo());
            produto.setPreco(dto.getPreco());

            if(dto.getEstoque() != null) {
                produto.setEstoque(
                        dto.getEstoque().getSaldoVirtualTotal()
                );
            }
            produto.setDescricao(dto.getDescricaoCurta());

            repositoryProduto.save(produto);
        }
    }

    public ProdutoListResponse buscarInteligente(String query) {

        String q = query.toLowerCase();

        List<Produto> produtos = repositoryProduto.findAll();

        List<Produto> filtrados = produtos.stream()
                .filter(p ->
                        p.getNome().toLowerCase().contains(q) ||
                                (p.getCodigo() != null && p.getCodigo().toLowerCase().contains(q)) ||
                                (p.getDescricao() != null && p.getDescricao().toLowerCase().contains(q))
                )
                .toList();

        if (filtrados.isEmpty()) {
            return new ProdutoListResponse(
                    false,
                    "Nenhum produto encontrado",
                    List.of()
            );
        }

        List<ProdutoResumo> lista = filtrados.stream()
                .map(p -> new ProdutoResumo(
                        p.getNome(),
                        p.getPreco(),
                        p.getEstoque()
                ))
                .toList();

        return new ProdutoListResponse(
                true,
                "Produtos encontrados",
                lista
        );
    }

}
