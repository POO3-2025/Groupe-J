package be.helha.poo3.services;

import be.helha.poo3.models.CharacterWithPos;
import be.helha.poo3.models.Item;
import be.helha.poo3.models.RoomDTOClient;
import be.helha.poo3.utils.UserSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

import java.awt.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ExplorationService {
    private static final String API_URL = "http://localhost:8080/exploration";

    private final HttpClient client;

    public ExplorationService(){ client = HttpClientBuilder.create().build(); }

    public RoomDTOClient getCurrentRoom() throws IOException{
        String accessToken = UserSession.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("No access token found");
        }

        HttpGet request = new HttpGet(API_URL + "/room");
        request.addHeader("Authorization", "Bearer " + accessToken);

        try (CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.getEntity().getContent(), RoomDTOClient.class);
            } else {
                throw new IOException("Erreur HTTP: " + statusCode);
            }
        }
    }


    /*public RoomDTOClient move (Object direction) throws IOException {
        URL url = new URL(API_URL + "/move/" + direction);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        if (connection.getResponseCode() == 201) {
            Scanner scanner = new Scanner(connection.getInputStream()).useDelimiter("\\A");
            String response = scanner.hasNext() ? scanner.next() : "";
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response, RoomDTOClient.class);
        } else {
            throw new IOException("Erreur HTTP: " + connection.getResponseCode());
        }
    }

     */

    public Item openChest() throws IOException{
        String accessToken = UserSession.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("No access token found");
        }

        HttpGet request = new HttpGet(API_URL + "/openChest");
        request.addHeader("Authorization", "Bearer " + accessToken);

        try (CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(response.getEntity().getContent(), Item.class);

            } else {
                throw new IOException("Erreur HTTP: " + statusCode);
            }
        }
    }

    public boolean getLootFromChest() throws IOException{
        String accessToken = UserSession.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("No access token found");
        }

        HttpPost request = new HttpPost(API_URL + "/getLootFromChest");
        request.addHeader("Authorization", "Bearer " + accessToken);
        try (CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return true;
            } else {
                throw new IOException("Erreur HTTP: " + statusCode);
            }
        }
    }

    public RoomDTOClient move(String direction) throws IOException {
        String accessToken = UserSession.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("No access token found");
        }
        HttpPost request = new HttpPost(API_URL + "/move/" + direction);
        request.addHeader("Authorization", "Bearer " + accessToken);
        try (CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                ObjectMapper mapper = new ObjectMapper();

                return mapper.readValue(response.getEntity().getContent(), RoomDTOClient.class);

            } else {
                throw new IOException("Erreur HTTP: " + statusCode);
            }
        }


        /*Point position = character.getPosition();
        switch (direction) {
            case "north" -> character.setPosition(new Point(position.x, position.y + 1));
            case "south" -> character.setPosition(new Point(position.x, position.y - 1));
            case "east"  -> character.setPosition(new Point(position.x + 1, position.y));
            case "west"  -> character.setPosition(new Point(position.x - 1, position.y));
        }

         */

    }


}

