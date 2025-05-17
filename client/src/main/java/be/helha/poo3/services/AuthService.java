package be.helha.poo3.services;

import be.helha.poo3.utils.UserSession;
import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service d'authentification responsable de la connexion et de la déconnexion de l'utilisateur.
 */
public class AuthService {
    private static final String API_URL = "http://localhost:8080/auth/login";

    public static boolean authenticate(String username, String password) {
        Gson gson = new Gson();

        Map<String, String> jsonBody = new HashMap<>();
        jsonBody.put("username", username);
        jsonBody.put("password", password);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_URL);
            httpPost.setHeader("Content-Type", "application/json");

            StringEntity entity = new StringEntity(gson.toJson(jsonBody), "UTF-8");
            httpPost.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 200 && statusCode < 300 && response.getEntity() != null) {
                    String json = EntityUtils.toString(response.getEntity(), "UTF-8");
                    Map<?, ?> responseMap = gson.fromJson(json, Map.class);

                    String token = (String) responseMap.get("accessToken");
                    String refreshToken = (String) responseMap.get("refreshToken");
                    Double idUserDouble = (Double) responseMap.get("id_user");
                    int id_user = idUserDouble != null ? idUserDouble.intValue() : -1;

                    if (token != null && refreshToken != null && id_user > 0) {
                        UserSession.setUsername(username);
                        UserSession.setRefreshToken(refreshToken);
                        UserSession.setAccessToken(token);
                        UserSession.setId_user(id_user);
                        return true;
                    }
                }
            }

        } catch (IOException e) {
            return false;
        }

        return false;
    }

    public static void logout() {
        UserSession.clear();
    }
}
