package be.helha.poo3.services;

import be.helha.poo3.models.GameCharacter;
import be.helha.poo3.utils.UserSession;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CharacterService {
    private static final String API_URL = "http://localhost:8080/character";

    private static final Gson gson = new Gson();

    private final HttpClient client;

    public CharacterService() {
        client = HttpClientBuilder.create().build();
    }

    public List<GameCharacter> getUserCharacter() throws IOException {
        String accessToken = UserSession.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("No access token found");
        }
        final Type listeType = new TypeToken<List<GameCharacter>>() {}.getType();

        HttpGet request = new HttpGet(API_URL + "/myCharacters");
        request.addHeader("Authorization", "Bearer " + accessToken);

        try (CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                return gson.fromJson(responseBody, listeType);
            } else if (statusCode == 401) {
                throw new ClientProtocolException("Unauthorized");
            } else if (statusCode == 403) {
                throw new ClientProtocolException("Forbidden");
            } else {
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                throw new RuntimeException("Erreur API (" + statusCode + ") : " + body);
            }
        }
    }
}
