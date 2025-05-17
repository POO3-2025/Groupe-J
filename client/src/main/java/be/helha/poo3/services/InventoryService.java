package be.helha.poo3.services;

import be.helha.poo3.utils.UserSession;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service permettant la gestion de l'inventaire du joueur en communiquant avec le backend.
 * Contient des méthodes pour récupérer, équiper, consommer, déséquiper et supprimer des objets.
 */
public class InventoryService {
    private static final String BASE_URL = "http://localhost:8080/inventory";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    /**
     * Récupère la liste des objets présents dans l'inventaire.
     *
     * @return Une liste de maps représentant les objets.
     */
    public List<Map<String, Object>> getInventoryItems() {
        String token = UserSession.getAccessToken();
        if (token == null || token.isEmpty()) {
            System.err.println("Aucun token présent pour l'utilisateur.");
            return Collections.emptyList();
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "/items")
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                return gson.fromJson(response.body().charStream(), listType);
            } else {
                System.err.println("Erreur lors de la récupération des items : " + response.code());
            }
        } catch (IOException e) {
            System.err.println("Exception réseau : " + e.getMessage());
        }

        return Collections.emptyList();
    }

    /**
     * Récupère toutes les informations de l'inventaire, y compris les slots d'équipement.
     *
     * @return Une map contenant les données de l'inventaire complet.
     */
    public Map<String, Object> getFullInventory() {
        String token = UserSession.getAccessToken();
        if (token == null || token.isEmpty()) {
            System.err.println("Token manquant");
            return Map.of();
        }

        Request request = new Request.Builder()
                .url(BASE_URL)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                return gson.fromJson(json, Map.class);
            }
        } catch (IOException e) {
            System.err.println("Erreur réseau : " + e.getMessage());
        }

        return Map.of();
    }

    /**
     * Équipe un objet dans un slot donné.
     *
     * @param slot   Le slot d'équipement (main, armor, second).
     * @param itemId L'identifiant de l'objet à équiper.
     * @return true si l’opération a réussi, false sinon.
     */
    public boolean equipItem(String slot, String itemId) {
        String token = UserSession.getAccessToken();
        if (token == null || token.isEmpty()) {
            System.err.println("Token manquant");
            return false;
        }

        Map<String, String> payload = Map.of("id", itemId);

        RequestBody body = RequestBody.create(
                gson.toJson(payload),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(BASE_URL + "/equip/" + slot)
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String msg = response.body() != null ? response.body().string() : "Erreur inconnue";
                System.err.println("Erreur d'équipement : " + msg);
            }
            return response.isSuccessful();
        } catch (IOException e) {
            System.err.println("Erreur réseau : " + e.getMessage());
            return false;
        }
    }

    /**
     * Consomme un objet (généralement de type consommable).
     *
     * @param itemId L'identifiant de l'objet à consommer.
     * @return true si la consommation a réussi, false sinon.
     */
    public boolean consumeItem(String itemId) {
        String token = UserSession.getAccessToken();
        if (token == null || token.isEmpty()) {
            System.err.println("Token manquant");
            return false;
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "/items/" + itemId + "/consume")
                .patch(RequestBody.create(new byte[0], null)) // corps vide
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            System.err.println("Erreur réseau lors de consume : " + e.getMessage());
            return false;
        }
    }

    /**
     * Rafraîchit un item depuis la liste actuelle de l’inventaire.
     *
     * @param itemId L'identifiant de l'objet à rafraîchir.
     * @return L'objet correspondant, ou null s'il n’est pas trouvé.
     */
    public Map<String, Object> refreshItemById(String itemId) {
        List<Map<String, Object>> items = getInventoryItems();
        if (items == null) return null;

        return items.stream()
                .filter(i -> itemId.equals(i.get("_id")) || itemId.equals(i.get("id")))
                .findFirst()
                .orElse(null);
    }

    /**
     * Récupère uniquement les objets équipés (slots main, armor, second).
     *
     * @return Une map avec les objets équipés par slot.
     */
    public Map<String, Object> getEquippedItems() {
        String token = UserSession.getAccessToken();
        if (token == null || token.isEmpty()) {
            System.err.println("Token manquant");
            return Map.of();
        }

        Request request = new Request.Builder()
                .url(BASE_URL)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                Map<String, Object> rawMap = gson.fromJson(json, Map.class);

                return Map.of(
                        "main", rawMap.get("mainSlot"),
                        "armor", rawMap.get("armorSlot"),
                        "second", rawMap.get("secondSlot")
                );
            }
        } catch (IOException e) {
            System.err.println("Erreur réseau : " + e.getMessage());
        }

        return Map.of();
    }

    /**
     * Déséquipe un objet d’un slot donné.
     *
     * @param slot Le slot à déséquiper (main, armor, second).
     * @return true si l’opération a réussi, false sinon.
     */
    public boolean unequipSlot(String slot) {
        String token = UserSession.getAccessToken();
        if (token == null || token.isEmpty()) return false;

        Request request = new Request.Builder()
                .url(BASE_URL + "/unequip/" + slot + "?force=false")
                .post(RequestBody.create(new byte[0], null))
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            System.err.println("Erreur lors du déséquipement : " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un objet de l’inventaire.
     *
     * @param itemId L'identifiant de l'objet à supprimer.
     * @return true si la suppression a réussi, false sinon.
     */
    public boolean deleteItem(String itemId) {
        String token = UserSession.getAccessToken();
        if (token == null || token.isEmpty()) return false;

        Request request = new Request.Builder()
                .url(BASE_URL + "/items/" + itemId)
                .delete()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            return false;
        }
    }
}
