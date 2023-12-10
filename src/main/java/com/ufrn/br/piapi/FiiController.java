package com.ufrn.br.piapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
public class FiiController {

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

    private final String apiUrl = "http://localhost:3000/fii";

    @Autowired
    private RestTemplate restTemplate;

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
}