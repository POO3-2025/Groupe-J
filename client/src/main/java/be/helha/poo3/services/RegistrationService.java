package be.helha.poo3.services;

import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service gérant l'enregistrement (inscription) d'un nouvel utilisateur.
 */
public class RegistrationService {
    private static final String API_URL = "http://localhost:8080/users";

    public static String register(String username, String password) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        Map<String, String> userJson = new HashMap<>();
        userJson.put("username", username);
        userJson.put("password", password);

        RequestBody body = RequestBody.create(
                gson.toJson(userJson),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return "success";
            } else if (response.code() == 409) {
                return "username_taken"; // Nom d'utilisateur existe déjà
            } else {
                return "error"; // Autre erreur
            }
        } catch (IOException e) {
            return "error"; // Erreur réseau
        }
    }
}