package com.mindproapps.jira.integracaoponto.rest.client;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindproapps.jira.integracaoponto.model.dto.ponto.PontoDTO;
import com.mindproapps.jira.integracaoponto.model.dto.ponto.PontoRequestDTO;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Component
public class PontoRestClient {

    private final PluginSettingsFactory pluginSettingsFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public PontoRestClient(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    public double buscarHorasPonto(String userKey, String data) {
        try {
            PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            String loginUrlStr = (String) settings.get("integracaoponto.apiLoginUrl");
            String loginJson = (String) settings.get("integracaoponto.apiJsonKey");
            String apiHorasUrl = (String) settings.get("integracaoponto.apiHorasUrl");

            URL loginUrl = new URL(loginUrlStr);
            HttpURLConnection loginConn = (HttpURLConnection) loginUrl.openConnection();
            loginConn.setRequestMethod("POST");
            loginConn.setRequestProperty("Content-Type", "application/json");
            loginConn.setDoOutput(true);

            try (OutputStream os = loginConn.getOutputStream()) {
                os.write(loginJson.getBytes(StandardCharsets.UTF_8));
            }

            InputStream loginResponse = loginConn.getInputStream();
            String loginResponseJson = IOUtils.toString(loginResponse, StandardCharsets.UTF_8);
            JsonNode loginRoot = objectMapper.readTree(loginResponseJson);
            String authToken = loginRoot.get("token").asText();

            URL pontoUrl = new URL(apiHorasUrl);
            HttpURLConnection pontoConn = (HttpURLConnection) pontoUrl.openConnection();
            pontoConn.setRequestMethod("GET");
            pontoConn.setRequestProperty("Authorization", "Bearer " + authToken);

            InputStream pontoResponse = pontoConn.getInputStream();
            String pontoJson = IOUtils.toString(pontoResponse, StandardCharsets.UTF_8);
            JsonNode pontoRoot = objectMapper.readTree(pontoJson);

            return pontoRoot.get("totalHoras").asDouble();

        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public List<PontoDTO> getList() {
        return getList(null, null, null);
    }

    public List<PontoDTO> getList(String email, LocalDate start, LocalDate end) {
        try {
            PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            String loginUrlStr = (String) settings.get("integracaoponto.apiLoginUrl");
            String loginJson = (String) settings.get("integracaoponto.apiJsonKey");
            String apiHorasUrl = (String) settings.get("integracaoponto.apiHorasUrl");

            URL loginUrl = new URL(loginUrlStr);
            HttpURLConnection loginConn = (HttpURLConnection) loginUrl.openConnection();
            loginConn.setRequestMethod("POST");
            loginConn.setRequestProperty("Content-Type", "application/json");
            loginConn.setDoOutput(true);

            try (OutputStream os = loginConn.getOutputStream()) {
                os.write(loginJson.getBytes(StandardCharsets.UTF_8));
            }

            InputStream loginResponse = loginConn.getInputStream();
            String loginResponseJson = IOUtils.toString(loginResponse, StandardCharsets.UTF_8);
            String token = objectMapper.readTree(loginResponseJson).get("token").asText();

            URL pontoUrl = new URL(apiHorasUrl);
            HttpURLConnection pontoConn = (HttpURLConnection) pontoUrl.openConnection();
            pontoConn.setRequestMethod("GET");
            pontoConn.setRequestProperty("Authorization", "Bearer " + token);

            InputStream pontoResponse = pontoConn.getInputStream();
            String json = IOUtils.toString(pontoResponse, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, new TypeReference<List<PontoDTO>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<PontoDTO> getList(List<PontoRequestDTO> lista) {
        // Implementação futura: envio de payload com POST para filtro avançado
        return Collections.emptyList();
    }
}
