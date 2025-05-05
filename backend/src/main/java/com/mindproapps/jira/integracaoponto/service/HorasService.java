package com.mindproapps.jira.integracaoponto.service;

import com.mindproapps.jira.integracaoponto.rest.client.PontoRestClient;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class HorasService {

    private final TempoService tempoService;
    private final PontoRestClient pontoRestClient;

    @Inject
    public HorasService(TempoService tempoService, PontoRestClient pontoRestClient) {
        this.tempoService = tempoService;
        this.pontoRestClient = pontoRestClient;
    }

    public ResultadoHoras buscarComparativoHoras(String userKey, String data) {
        double tempo = tempoService.buscarHorasLogadas(userKey, data);
        double ponto = pontoRestClient.buscarHorasPonto(userKey, data);

        return new ResultadoHoras(tempo, ponto);
    }

    public static class ResultadoHoras {
        public double tempo;
        public double ponto;
        public String status;

        public ResultadoHoras(double tempo, double ponto) {
            this.tempo = tempo;
            this.ponto = ponto;
            this.status = Math.abs(tempo - ponto) <= 0.5 ? "ok" : "inconsistente";
        }
    }
}
