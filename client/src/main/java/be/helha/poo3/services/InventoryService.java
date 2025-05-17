package be.helha.poo3.services;

import be.helha.poo3.utils.UserSession;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class InventoryService {
    private static final String BASE_URL = "http://localhost:8080/inventory";
    private final Gson gson = new Gson();

    public List<Map<String, Object>> getInventoryItems() {
        String token = UserSession.getAccessToken();
        if (token == null || token.isEmpty()) return Collections.emptyList();

        HttpGet request = new HttpGet(BASE_URL + "/items");
        request.setHeader("Authorization", "Bearer " + token);
        request.setHeader("Accept", "application/json");

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(request)) {

            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                    return gson.fromJson(EntityUtils.toString(entity), listType);
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        return Collections.emptyList();
    }

    public Map<String, Object> getFullInventory() {
        String token = UserSession.getAccessToken();
        if (token == null || token.isEmpty()) return Map.of();

        HttpGet request = new HttpGet(BASE_URL);
        request.setHeader("Authorization", "Bearer " + token);

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(request)) {

            if (response.getStatusLine().getStatusCode() == 200 && response.getEntity() != null) {
                String json = EntityUtils.toString(response.getEntity());
                return gson.fromJson(json, Map.class);
            }
        } catch (IOException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        return Map.of();
    }

    public boolean equipItem(String slot, String itemId) {
        return sendJsonPost(BASE_URL + "/equip/" + slot, Map.of("id", itemId));
    }

    public boolean consumeItem(String itemId) {
        return sendEmptyPatch(BASE_URL + "/items/" + itemId + "/consume");
    }

    public Map<String, Object> refreshItemById(String itemId) {
        return getInventoryItems().stream()
                .filter(i -> itemId.equals(i.get("_id")) || itemId.equals(i.get("id")))
                .findFirst()
                .orElse(null);
    }

    public Map<String, Object> getEquippedItems() {
        Map<String, Object> fullInventory = getFullInventory();
        return Map.of(
                "main", fullInventory.get("mainSlot"),
                "armor", fullInventory.get("armorSlot"),
                "second", fullInventory.get("secondSlot")
        );
    }

    public boolean unequipSlot(String slot) {
        return sendEmptyPost(BASE_URL + "/unequip/" + slot + "?force=false");
    }

    public boolean deleteItem(String itemId) {
        String token = UserSession.getAccessToken();
        if (token == null || token.isEmpty()) return false;

        HttpDelete request = new HttpDelete(BASE_URL + "/items/" + itemId);
        request.setHeader("Authorization", "Bearer " + token);

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(request)) {

            return response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300;
        } catch (IOException e) {
            System.err.println("Erreur : " + e.getMessage());
            return false;
        }
    }

    // Utilitaires privés pour factoriser
    private boolean sendJsonPost(String url, Map<String, String> payload) {
        String token = UserSession.getAccessToken();
        if (token == null || token.isEmpty()) return false;

        HttpPost request = new HttpPost(url);
        request.setHeader("Authorization", "Bearer " + token);
        request.setHeader("Content-Type", "application/json");

        try {
            StringEntity entity = new StringEntity(gson.toJson(payload));
            request.setEntity(entity);

            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = client.execute(request)) {

                return response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300;
            }
        } catch (IOException e) {
            System.err.println("Erreur : " + e.getMessage());
            return false;
        }
    }

    private boolean sendEmptyPost(String url) {
        String token = UserSession.getAccessToken();
        if (token == null || token.isEmpty()) return false;

        HttpPost request = new HttpPost(url);
        request.setHeader("Authorization", "Bearer " + token);

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(request)) {

            return response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300;
        } catch (IOException e) {
            System.err.println("Erreur : " + e.getMessage());
            return false;
        }
    }

    private boolean sendEmptyPatch(String url) {
        String token = UserSession.getAccessToken();
        if (token == null || token.isEmpty()) return false;

        HttpPatch request = new HttpPatch(url);
        request.setHeader("Authorization", "Bearer " + token);

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(request)) {

            return response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300;
        } catch (IOException e) {
            System.err.println("Erreur : " + e.getMessage());
            return false;
        }
    }
}
