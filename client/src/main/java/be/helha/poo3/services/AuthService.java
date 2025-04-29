package be.helha.poo3.services;

import be.helha.poo3.utils.UserSession;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service d'authentification responsable de la connexion et de la déconnexion de l'utilisateur.
 */
public class AuthService {
    private static final String API_URL = "http://localhost:8080/auth/login";

    public static boolean authenticate(String username, String password) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        Map<String, String> jsonBody = new HashMap<>();
        jsonBody.put("username", username);
        jsonBody.put("password", password);

        RequestBody body = RequestBody.create(
                gson.toJson(jsonBody),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                Map<?, ?> responseMap = gson.fromJson(json, Map.class);

                String token = (String) responseMap.get("accessToken");
                Double idUserDouble = (Double) responseMap.get("id_user"); // Gson mappe les nombres JSON en Double
                int id_user = idUserDouble.intValue(); // Convertit proprement en int

                if (token != null && id_user > 0) {
                    UserSession.setUsername(username);
                    UserSession.setAccessToken(token);
                    UserSession.setId_user(id_user); // Stocke l'ID utilisateur dans la session
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public static void logout() {
        UserSession.clear();
    }
}
