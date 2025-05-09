package be.helha.poo3.services;

import be.helha.poo3.utils.UserSession;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service pour changer le mot de passe utilisateur.
 */
public class PasswordService {
    private static final String API_URL = "http://localhost:8080/users/";

    public static boolean changePassword(String newPassword) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("password", newPassword);

        System.out.println(UserSession.getId_user());

        RequestBody body = RequestBody.create(
                gson.toJson(bodyMap),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(API_URL + UserSession.getId_user()) // Envoie l'ID de l'utilisateur
                .put(body)
                .addHeader("Authorization", "Bearer " + UserSession.getAccessToken()) // Ajout du token
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            return false;
        }
    }
}
