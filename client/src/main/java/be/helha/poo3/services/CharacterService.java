package be.helha.poo3.services;

import be.helha.poo3.models.CharacterDTO;
import be.helha.poo3.models.CharacterWithPos;
import be.helha.poo3.models.Config;
import be.helha.poo3.models.GameCharacter;
import be.helha.poo3.utils.UserSession;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Service permettant d'interagir avec l'API liée aux personnages (Character).
 * Utilise les requêtes HTTP pour effectuer des opérations CRUD sur les personnages.
 */
public class CharacterService {
    private static final String API_URL = Config.getBaseUrl()+ "/character";

    private static final Gson gson = new Gson();

    private final HttpClient client;

    public CharacterService() {
        client = HttpClientBuilder.create().build();
    }

    /**
     * Récupère la liste des personnages appartenant à l'utilisateur connecté.
     *
     * @return une liste de GameCharacter
     * @throws IOException en cas d'erreur de connexion ou d'erreur d'authentification
     */
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
                List<GameCharacter> liste = gson.fromJson(responseBody, listeType);
                return liste;
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

    /**
     * Récupère le personnage actuellement utilisé dans le jeu avec sa position.
     *
     * @return une instance de CharacterWithPos
     * @throws IOException en cas d'erreur réseau ou de réponse invalide
     */
    public CharacterWithPos getInGameCharacter() throws IOException {
        HttpGet request = new HttpGet(API_URL + "/myCharacter");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer " + UserSession.getAccessToken());
        try (CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode  == 200) {
                return gson.fromJson(EntityUtils.toString(response.getEntity()), CharacterWithPos.class);
            } else {
                throw new RuntimeException(EntityUtils.toString(response.getEntity()));
            }
        } catch (ClientProtocolException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Met à jour le nom d'un personnage donné.
     *
     * @param characterId l'identifiant du personnage à modifier
     * @param newName le nouveau nom à attribuer
     * @return true si la mise à jour a réussi, false sinon
     * @throws IOException en cas de problème lors de l'appel HTTP
     */
    public boolean updateCharacterName(int characterId, String newName) throws IOException {
        String json = "{\"newName\":\"" + newName.replace("\"", "\\\"") + "\"}";

        HttpPut request = new HttpPut(API_URL + "/" + characterId);
        StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
        request.setEntity(entity);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer " + UserSession.getAccessToken());

        try (CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode  == 200) {
                return true;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprime un personnage via l'API.
     *
     * @param characterId l'identifiant du personnage à supprimer
     * @return true si la suppression a réussi, false sinon
     * @throws IOException en cas de problème réseau
     */
    public boolean deleteCharacter(int characterId) throws IOException {
        HttpDelete request = new HttpDelete(API_URL + "/" + characterId);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer " + UserSession.getAccessToken());
        try (CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode  == 200) {
                return true;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Ajoute un nouveau personnage à l'utilisateur connecté.
     *
     * @param character un objet CharacterDTO contenant les statistiques du personnage
     * @return true si le personnage a été ajouté avec succès, false si les données sont invalides
     * @throws IOException si une erreur survient pendant la communication avec l'API
     */
    public boolean addCharacter(CharacterDTO character) throws IOException {
        if (character == null ||
                (character.getConstitution() + character.getDexterity() + character.getStrength()) > 5) {
            return false;
        }
        HttpPost request = new HttpPost(API_URL);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer " + UserSession.getAccessToken());
        String json = gson.toJson(character);
        StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
        request.setEntity(entity);
        try (CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode  == 200) {
                return true;
            } else {
                throw new RuntimeException("Erreur API (" + statusCode + ") : " + EntityUtils.toString(response.getEntity()));
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Sélectionne un personnage comme personnage actif dans le jeu.
     *
     * @param characterId l'identifiant du personnage à sélectionner
     * @return le personnage sélectionné avec ses informations de position
     * @throws IOException si une erreur se produit lors de la requête
     */
    public CharacterWithPos choiceCharacter(int characterId) throws IOException {
        HttpPost request = new HttpPost(API_URL + "/choice/" + characterId);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer " + UserSession.getAccessToken());
        final Type type = new TypeToken<CharacterWithPos>() {}.getType();
        try (CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode  == 200) {
                return gson.fromJson(EntityUtils.toString(response.getEntity()), type);
            } else {
                throw new RuntimeException(EntityUtils.toString(response.getEntity()));
            }
        } catch (ClientProtocolException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Permet à l'utilisateur de quitter la partie en cours.
     *
     * @return true si la sortie de jeu est réussie, false sinon
     * @throws IOException en cas d'erreur réseau
     */
    public boolean leaveGame() throws IOException {
        HttpDelete request = new HttpDelete(API_URL + "/leave");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer " + UserSession.getAccessToken());
        try (CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode  == 200) {
                return true;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        }
        return false;
    }


}
