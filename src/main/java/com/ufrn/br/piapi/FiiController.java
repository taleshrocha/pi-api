package com.ufrn.br.piapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Controller
public class FiiController {

    private static final String ARQUIVO_CACHE = "cachedFiiList.ser";
    private final String apiUrl = "http://localhost:3000/fii";
    private List<Completo> cachedFiiList;
    @Autowired
    private RestTemplate restTemplate;

    private static <T> boolean ehValido(T value, T referencia, Predicate<T> condicao) {
        return referencia == null || (value != null && condicao.test(value));
    }

    @GetMapping("/fii/todos")
    public ResponseEntity<List<Geral>> getFiiData() {
        Geral[] responseDataArray = restTemplate.getForObject(apiUrl, Geral[].class);
        List<Geral> fiiList = Arrays.asList(responseDataArray);
        return ResponseEntity.ok(fiiList);
    }

    @GetMapping("/fii/geral/{ticket}")
    public ResponseEntity<Geral> getFiiGeneral(@PathVariable String ticket) {
        Geral[] responseDataArray = restTemplate.getForObject(apiUrl, Geral[].class);
        List<Geral> fiiList = Arrays.asList(responseDataArray);

        Optional<Geral> matchingFii = fiiList.stream()
                .filter(fii -> Objects.equals(ticket, fii.getPostTitle()))
                .findFirst();

        return matchingFii.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/fii/desempenho/{ticket}")
    public ResponseEntity<Desempenho> getFiiDesempenho(@PathVariable String ticket) {
        Desempenho[] responseDataArray = restTemplate.getForObject(apiUrl, Desempenho[].class);
        List<Desempenho> fiiList = Arrays.asList(responseDataArray);

        Optional<Desempenho> matchingFii = fiiList.stream()
                .filter(fii -> Objects.equals(ticket, fii.getPostTitle()))
                .findFirst();

        return matchingFii.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/fii/dividendo-yield/{ticket}")
    public ResponseEntity<DividendoYield> getFiiDividendoYield(@PathVariable String ticket) {
        DividendoYield[] responseDataArray = restTemplate.getForObject(apiUrl, DividendoYield[].class);
        List<DividendoYield> fiiList = Arrays.asList(responseDataArray);

        Optional<DividendoYield> matchingFii = fiiList.stream()
                .filter(fii -> Objects.equals(ticket, fii.getPostTitle()))
                .findFirst();

        return matchingFii.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/fii/historico/{ticket}")
    public ResponseEntity getFiiHistorico(@PathVariable String ticket) {
        HistoricoDividendo[] responseDataArray = restTemplate.getForObject("http://localhost:3000/fii/" + ticket
                + "/dividend-history", HistoricoDividendo[].class);
        List<HistoricoDividendo> fiiList = Arrays.asList(responseDataArray);

        return ResponseEntity.ok(fiiList);
    }

    private List<Completo> carregarListaDeArquivo() {
        try {
            Resource resource = new ClassPathResource(ARQUIVO_CACHE);

            if (!resource.exists()) {
                return null;
            }

            InputStream inputStream = resource.getInputStream();

            try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
                return (List<Completo>) ois.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void salvarListaEmArquivo(List<Completo> lista) {
        try {
            Resource resource = new ClassPathResource(ARQUIVO_CACHE);
            File file = resource.getFile();

            if (!file.exists() || foiModificadoHaMaisDeUmDia(file)) {
                if (!file.exists()) {
                    file.createNewFile();
                }

                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                    oos.writeObject(lista);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean foiModificadoHaMaisDeUmDia(File file) throws IOException {
        Instant oneDayAgo = Instant.now().minus(Duration.ofDays(1));
        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        Instant lastModified = attr.lastModifiedTime().toInstant();
        return lastModified.isBefore(oneDayAgo);
    }

    @GetMapping("/fii/filtrar")
    public ResponseEntity<List<Completo>> filtrar(
            @RequestParam(value = "valorMin", required = false) Double valorMin,
            @RequestParam(value = "valorMax", required = false) Double valorMax,
            @RequestParam(value = "pvpMin", required = false) Double pvpMin,
            @RequestParam(value = "pvpMax", required = false) Double pvpMax,
            @RequestParam(value = "dividendoMin", required = false) Double dividendoMin,
            @RequestParam(value = "dividendoMax", required = false) Double dividendoMax,
            @RequestParam(value = "setor", required = false) String setor,
            @RequestParam(value = "liquidezMin", required = false) Double liquidezMin,
            @RequestParam(value = "liquidezMax", required = false) Double liquidezMax,
            @RequestParam(value = "vpaMin", required = false) Double vpaMin,
            @RequestParam(value = "vpaMax", required = false) Double vpaMax,
            @RequestParam(value = "volatilityMin", required = false) Double volatilityMin,
            @RequestParam(value = "volatilityMax", required = false) Double volatilityMax,
            @RequestParam(value = "txGestaoMin", required = false) Double txGestaoMin,
            @RequestParam(value = "txGestaoMax", required = false) Double txGestaoMax,
            @RequestParam(value = "txAdminMin", required = false) Double txAdminMin,
            @RequestParam(value = "txAdminMax", required = false) Double txAdminMax,
            @RequestParam(value = "rentabilidadeMin", required = false) Double rentabilidadeMin,
            @RequestParam(value = "rentabilidadeMax", required = false) Double rentabilidadeMax,
            @RequestParam(value = "vpa_yieldMin", required = false) Double vpa_yieldMin,
            @RequestParam(value = "vpa_yieldMax", required = false) Double vpa_yieldMax,
            @RequestParam(value = "vpa_changeMin", required = false) Double vpa_changeMin,
            @RequestParam(value = "vpa_changeMax", required = false) Double vpa_changeMax,
            @RequestParam(value = "vpa_rent_mMin", required = false) Double vpa_rent_mMin,
            @RequestParam(value = "vpa_rent_mMax", required = false) Double vpa_rent_mMax,
            @RequestParam(value = "vpa_rentMin", required = false) Double vpa_rentMin,
            @RequestParam(value = "vpa_rentMax", required = false) Double vpa_rentMax,
            @RequestParam(value = "p_vpaMin", required = false) Double p_vpaMin,
            @RequestParam(value = "p_vpaMax", required = false) Double p_vpaMax,
            @RequestParam(value = "tx_performanceMin", required = false) Double tx_performanceMin,
            @RequestParam(value = "tx_performanceMax", required = false) Double tx_performanceMax,
            @RequestParam(value = "media_yield_3mMin", required = false) Double media_yield_3mMin,
            @RequestParam(value = "media_yield_3mMax", required = false) Double media_yield_3mMax,
            @RequestParam(value = "media_yield_6mMin", required = false) Double media_yield_6mMin,
            @RequestParam(value = "media_yield_6mMax", required = false) Double media_yield_6mMax,
            @RequestParam(value = "media_yield_12mMin", required = false) Double media_yield_12mMin,
            @RequestParam(value = "media_yield_12mMax", required = false) Double media_yield_12mMax,
            @RequestParam(value = "soma_yield_ano_correnteMin", required = false) Double soma_yield_ano_correnteMin,
            @RequestParam(value = "soma_yield_ano_correnteMax", required = false) Double soma_yield_ano_correnteMax,
            @RequestParam(value = "variacao_cotacao_mesMin", required = false) Double variacao_cotacao_mesMin,
            @RequestParam(value = "variacao_cotacao_mesMax", required = false) Double variacao_cotacao_mesMax,
            @RequestParam(value = "rentabilidade_mesMin", required = false) Double rentabilidade_mesMin,
            @RequestParam(value = "rentabilidade_mesMax", required = false) Double rentabilidade_mesMax,
            @RequestParam(value = "numero_cotistaMin", required = false) Double numero_cotistaMin,
            @RequestParam(value = "numero_cotistaMax", required = false) Double numero_cotistaMax
    ) {

        cachedFiiList = carregarListaDeArquivo();

        if (cachedFiiList == null) {
            Completo[] responseDataArray = restTemplate.getForObject(apiUrl, Completo[].class);
            cachedFiiList = Arrays.asList(responseDataArray);
            salvarListaEmArquivo(cachedFiiList);
        }

        List<Completo> filteredFiiList = cachedFiiList.stream()
                .filter(fii ->
                        ehValido(fii.getLiquidezMediaDiaria(), liquidezMin, valor -> valor >= liquidezMin) &&
                                ehValido(fii.getLiquidezMediaDiaria(), liquidezMax, valor -> valor <= liquidezMax) &&
                                ehValido(fii.getValor(), 0.0, valor -> valor > 0.0) &&
                                ehValido(fii.getSetor(), setor, valor -> valor.contains(setor)) &&
                                ehValido(fii.getValor(), valorMax, valor -> valor <= valorMax) &&
                                ehValido(fii.getValor(), valorMin, valor -> valor >= valorMin) &&
                                ehValido(fii.getDividendo(), dividendoMax, valor -> valor <= dividendoMax) &&
                                ehValido(fii.getDividendo(), dividendoMin, valor -> valor >= dividendoMin) &&
                                ehValido(fii.getPvp(), pvpMax, valor -> valor <= pvpMax) &&
                                ehValido(fii.getPvp(), pvpMin, valor -> valor >= pvpMin) &&
                                ehValido(fii.getVpa(), vpaMin, valor -> valor >= vpaMin) &&
                                ehValido(fii.getVpa(), vpaMax, valor -> valor <= vpaMax) &&
                                ehValido(fii.getVolatility(), volatilityMin, valor -> valor >= volatilityMin) &&
                                ehValido(fii.getVolatility(), volatilityMax, valor -> valor <= volatilityMax) &&
                                ehValido(fii.getTxGestao(), txGestaoMin, valor -> valor >= txGestaoMin) &&
                                ehValido(fii.getTxGestao(), txGestaoMax, valor -> valor <= txGestaoMax) &&
                                ehValido(fii.getTxAdmin(), txAdminMin, valor -> valor >= txAdminMin) &&
                                ehValido(fii.getTxAdmin(), txAdminMax, valor -> valor <= txAdminMax) &&
                                ehValido(fii.getRentabilidade(), rentabilidadeMin, valor -> valor >= rentabilidadeMin) &&
                                ehValido(fii.getRentabilidade(), rentabilidadeMax, valor -> valor <= rentabilidadeMax) &&
                                ehValido(fii.getVpaYield(), vpa_yieldMin, valor -> valor >= vpa_yieldMin) &&
                                ehValido(fii.getVpaYield(), vpa_yieldMax, valor -> valor <= vpa_yieldMax) &&
                                ehValido(fii.getVpaChange(), vpa_changeMin, valor -> valor >= vpa_changeMin) &&
                                ehValido(fii.getVpaChange(), vpa_changeMax, valor -> valor <= vpa_changeMax) &&
                                ehValido(fii.getVpaRentM(), vpa_rent_mMin, valor -> valor >= vpa_rent_mMin) &&
                                ehValido(fii.getVpaRentM(), vpa_rent_mMax, valor -> valor <= vpa_rent_mMax) &&
                                ehValido(fii.getVpaRent(), vpa_rentMin, valor -> valor >= vpa_rentMin) &&
                                ehValido(fii.getVpaRent(), vpa_rentMax, valor -> valor <= vpa_rentMax) &&
                                ehValido(fii.getPVpa(), p_vpaMin, valor -> valor >= p_vpaMin) &&
                                ehValido(fii.getPVpa(), p_vpaMax, valor -> valor <= p_vpaMax) &&
                                ehValido(fii.getTxPerformance(), tx_performanceMin, valor -> valor >= tx_performanceMin) &&
                                ehValido(fii.getTxPerformance(), tx_performanceMax, valor -> valor <= tx_performanceMax) &&
                                ehValido(fii.getMediaYield3m(), media_yield_3mMin, valor -> valor >= media_yield_3mMin) &&
                                ehValido(fii.getMediaYield3m(), media_yield_3mMax, valor -> valor <= media_yield_3mMax) &&
                                ehValido(fii.getMediaYield6m(), media_yield_6mMin, valor -> valor >= media_yield_6mMin) &&
                                ehValido(fii.getMediaYield6m(), media_yield_6mMax, valor -> valor <= media_yield_6mMax) &&
                                ehValido(fii.getMediaYield12m(), media_yield_12mMin, valor -> valor >= media_yield_12mMin) &&
                                ehValido(fii.getMediaYield12m(), media_yield_12mMax, valor -> valor <= media_yield_12mMax) &&
                                ehValido(fii.getSomaYieldAnoCorrente(), soma_yield_ano_correnteMin, valor -> valor >= soma_yield_ano_correnteMin) &&
                                ehValido(fii.getSomaYieldAnoCorrente(), soma_yield_ano_correnteMax, valor -> valor <= soma_yield_ano_correnteMax) &&
                                ehValido(fii.getVariacaoCotacaoMes(), variacao_cotacao_mesMin, valor -> valor >= variacao_cotacao_mesMin) &&
                                ehValido(fii.getVariacaoCotacaoMes(), variacao_cotacao_mesMax, valor -> valor <= variacao_cotacao_mesMax) &&
                                ehValido(fii.getRentabilidadeMes(), rentabilidade_mesMin, valor -> valor >= rentabilidade_mesMin) &&
                                ehValido(fii.getRentabilidadeMes(), rentabilidade_mesMax, valor -> valor <= rentabilidade_mesMax) &&
                                ehValido(fii.getNumeroCotista(), numero_cotistaMin, valor -> valor >= numero_cotistaMin) &&
                                ehValido(fii.getNumeroCotista(), numero_cotistaMax, valor -> valor <= numero_cotistaMax)
                ).collect(Collectors.toList());

        return ResponseEntity.ok(filteredFiiList);
    }

    @Data
    public static class Completo implements Serializable {
        @JsonProperty("post_title")
        private String postTitle;

        private String setor;

        private Double valor;

        @JsonProperty("liquidezmediadiaria")
        private Double liquidezMediaDiaria;

        private Double pvp;

        private Double dividendo;

        private Double yeld;

        @JsonProperty("soma_yield_3m")
        private Double somaYield3m;

        @JsonProperty("soma_yield_6m")
        private Double somaYield6m;

        @JsonProperty("soma_yield_12m")
        private Double somaYield12m;

        @JsonProperty("media_yield_3m")
        private Double mediaYield3m;

        @JsonProperty("media_yield_6m")
        private Double mediaYield6m;

        @JsonProperty("media_yield_12m")
        private Double mediaYield12m;

        @JsonProperty("soma_yield_ano_corrente")
        private Double somaYieldAnoCorrente;

        @JsonProperty("variacao_cotacao_mes")
        private Double variacaoCotacaoMes;

        @JsonProperty("rentabilidade_mes")
        private Double rentabilidadeMes;

        private Double rentabilidade;

        private Double patrimonio;

        private Double vpa;

        @JsonProperty("p_vpa")
        private Double pVpa;

        @JsonProperty("vpa_yield")
        private Double vpaYield;

        @JsonProperty("vpa_change")
        private Double vpaChange;

        @JsonProperty("vpa_rent_m")
        private Double vpaRentM;

        @JsonProperty("vpa_rent")
        private Double vpaRent;

        private String ativos;

        private Double volatility;

        @JsonProperty("numero_cotista")
        private Double numeroCotista;

        private Double txGestao;

        private Double txAdmin;

        @JsonProperty("tx_performance")
        private Double txPerformance;
    }

    @Data
    public static class Geral {
        @JsonProperty("post_title")
        private String postTitle;

        private String setor;

        private Double valor;

        @JsonProperty("liquidezmediadiaria")
        private Double liquidezMediaDiaria;

        private Double patrimonio;

        @JsonProperty("numero_cotista")
        private Double numeroCotista;
    }

    @Data
    public static class Desempenho {
        @JsonProperty("post_title")
        private String postTitle;
        @JsonProperty("rentabilidade_mes")
        private Double rentabilidadeMes;
        private Double rentabilidade;
        @JsonProperty("soma_yield_3m")
        private Double somaYield3m;
        @JsonProperty("soma_yield_6m")
        private Double somaYield6m;
        @JsonProperty("soma_yield_12m")
        private Double somaYield12m;
        @JsonProperty("media_yield_3m")
        private Double mediaYield3m;
        @JsonProperty("media_yield_6m")
        private Double mediaYield6m;
        @JsonProperty("media_yield_12m")
        private Double mediaYield12m;
        @JsonProperty("soma_yield_ano_corrente")
        private Double somaYieldAnoCorrente;
    }

    @Data
    public static class DividendoYield {
        @JsonProperty("post_title")
        private String postTitle;
        private String dividendo;
        @JsonProperty("yeld")
        private String yield;
        @JsonProperty("vpa_yield")
        private String vpaYield;
    }

    @Data
    public static class HistoricoDividendo {
        @JsonProperty("DATA COM")
        private String dataCom;
        @JsonProperty("PAGAMENTO")
        private String pagamento;
        @JsonProperty("VALOR")
        private String valor;
    }
}
