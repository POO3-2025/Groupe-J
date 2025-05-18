package be.helha.poo3.services;

import be.helha.poo3.utils.UserSession;
import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service pour changer le mot de passe utilisateur.
 */
public class PasswordService {
    private static final String API_URL = "http://localhost:8080/users/";

    public static boolean changePassword(String newPassword) {
        Gson gson = new Gson();

        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("password", newPassword);

        String url = API_URL + UserSession.getId_user();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut httpPut = new HttpPut(url);
            httpPut.setHeader("Authorization", "Bearer " + UserSession.getAccessToken());
            httpPut.setHeader("Content-Type", "application/json");

            String json = gson.toJson(bodyMap);
            StringEntity entity = new StringEntity(json, "UTF-8");
            httpPut.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(httpPut)) {
                return response.getStatusLine().getStatusCode() >= 200 &&
                        response.getStatusLine().getStatusCode() < 300;
            }

        } catch (IOException e) {
            return false;
        }
    }
}
