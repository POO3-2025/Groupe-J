package be.helha.poo3.models;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Config {
    private static String baseUrl;

    static {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> config = mapper.readValue(new File("config.json"), Map.class);
            baseUrl = config.get("baseUrl");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.json", e);
        }
    }

    public static String getBaseUrl() {
        return baseUrl;
    }
}