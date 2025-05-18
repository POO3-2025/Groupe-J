package be.helha.poo3.services;

import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service gérant l'enregistrement (inscription) d'un nouvel utilisateur.
 */
public class RegistrationService {
    private static final String API_URL = "http://localhost:8080/users";

    public static String register(String username, String password) {
        Gson gson = new Gson();

        Map<String, String> userJson = new HashMap<>();
        userJson.put("username", username);
        userJson.put("password", password);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_URL);
            httpPost.setHeader("Content-Type", "application/json");

            StringEntity entity = new StringEntity(gson.toJson(userJson), "UTF-8");
            httpPost.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    return "success";
                } else if (statusCode == 409) {
                    return "username_taken";
                } else {
                    return "error";
                }
            }

        } catch (IOException e) {
            return "error";
        }
    }
}
