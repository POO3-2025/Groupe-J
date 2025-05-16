package be.helha.poo3.services;

import be.helha.poo3.models.CharacterDTO;
import be.helha.poo3.models.CharacterWithPos;
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

    public boolean addCharacter(CharacterDTO character) throws IOException {
        if (character == null ||
                (character.getConstitution() + character.getConstitution() + character.getStrength()) > 5) {
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
