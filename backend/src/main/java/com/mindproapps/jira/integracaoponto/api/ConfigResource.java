package com.mindproapps.jira.integracaoponto.api;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfigResource {

    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public ConfigResource(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @GET
    public Response getConfig() {
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

        Map<String, String> config = new HashMap<>();
        config.put("apiLoginUrl", (String) settings.get("integracaoponto.apiLoginUrl"));
        config.put("apiJsonKey", (String) settings.get("integracaoponto.apiJsonKey"));
        config.put("apiHorasUrl", (String) settings.get("integracaoponto.apiHorasUrl"));

        return Response.ok(config).build();
    }

    @POST
    public Response saveConfig(Map<String, String> input) {
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

        settings.put("integracaoponto.apiLoginUrl", input.get("apiLoginUrl"));
        settings.put("integracaoponto.apiJsonKey", input.get("apiJsonKey"));
        settings.put("integracaoponto.apiHorasUrl", input.get("apiHorasUrl"));

        return Response.ok().build();
    }
}
