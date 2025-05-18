package be.helha.poo3.services;


import be.helha.poo3.models.Item;
import be.helha.poo3.models.RoomDTOClient;
import be.helha.poo3.utils.UserSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import java.io.IOException;


/**
 * Service permettant la gestion de l'exploration communiquant avec le backend.
 * Contient des méthodes pour se déplacer, ouvrir un coffre,looter un objet.
 */
public class ExplorationService {
    private static final String API_URL = "http://localhost:8080/exploration";

    private final HttpClient client;

    public ExplorationService(){ client = HttpClientBuilder.create().build(); }

    /**
     * Permet de récupérer la salle où se trouve le personnage
     * @return une salle
     * @throws IOException
     */
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

    /**
     * Permet d'ouvrir un coffre pour voir lobjet qu'il contient
     * @return un objet
     * @throws IOException
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

    /**
     * Permet de transférer un objet d'un coffre à l'inventaire d'un personnage
     * @return true  ou false si l'inventaire est pleins
     * @throws IOException
     */
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
                HttpEntity entity = response.getEntity();
                String responseContent = EntityUtils.toString(entity);
                return Boolean.parseBoolean(responseContent);
            } else {
                throw new IOException("Erreur HTTP: " + statusCode);
            }
        }
    }

    /**
     * Permet de se déplacer entre les salles
     * @param direction direction que l'on veut prendre (ex: "north","east",...)
     * @return la salle vers laquelle on se déplace
     * @throws IOException
     */
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
    }

}

