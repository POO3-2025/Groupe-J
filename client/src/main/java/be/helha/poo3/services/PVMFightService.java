package be.helha.poo3.services;

import be.helha.poo3.models.Config;
import be.helha.poo3.models.PVMFightDTO;
import be.helha.poo3.models.PvmTurnResult;
import be.helha.poo3.utils.UserSession;
import com.google.gson.Gson;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

public class PVMFightService {
    private static final String API_URL = Config.getBaseUrl()+"/fight";

    private static final Gson gson = new Gson();

    private final HttpClient client;

    public PVMFightService() {
        this.client = HttpClientBuilder.create().build();
    }

    public PVMFightDTO startFight() throws IOException {
        String accessToken = UserSession.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("No access token found");
        }
        HttpPost request = new HttpPost(API_URL + "/pvm");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer " + accessToken);
        try(CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String json = EntityUtils.toString(response.getEntity());
                System.out.println(json);
                return gson.fromJson(json, PVMFightDTO.class);
            } else {
                throw new RuntimeException(EntityUtils.toString(response.getEntity()));
            }
        } catch (ClientProtocolException e) {
            throw new IOException(e.getMessage());
        }
    }

    public PvmTurnResult playTurn(String action) throws IOException {
        String accessToken = UserSession.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("No access token found");
        }
        HttpPost request = new HttpPost(API_URL+"/pvm/"+action);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer " + accessToken);
        try(CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String json = EntityUtils.toString(response.getEntity());
                System.out.println(json);
                return gson.fromJson(json, PvmTurnResult.class);
            } else {
                throw new RuntimeException(EntityUtils.toString(response.getEntity()));
            }
        } catch (ClientProtocolException e) {
            throw new IOException(e.getMessage());
        }
    }

    public Map<String,Object> endFight() throws IOException {
        String accessToken = UserSession.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("No access token found");
        }
        HttpPost request = new HttpPost(API_URL+"/pvm/end/get");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer " + accessToken);
        try(CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String json = EntityUtils.toString(response.getEntity());
                System.out.println(json);
                return gson.fromJson(json, Map.class);
            } else {
                throw new RuntimeException(EntityUtils.toString(response.getEntity()));
            }
        } catch (ClientProtocolException e) {
            throw new IOException(e.getMessage());
        }
    }
}
