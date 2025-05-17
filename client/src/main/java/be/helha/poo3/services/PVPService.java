package be.helha.poo3.services;

import be.helha.poo3.models.ChallengeRequest;
import be.helha.poo3.models.Config;
import be.helha.poo3.models.PVMFightDTO;
import be.helha.poo3.models.PVPFight;
import be.helha.poo3.utils.UserSession;
import com.google.gson.Gson;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class PVPService {
    private static final String API_URL = Config.getBaseUrl()+"/fight/pvp";

    private static final Gson gson = new Gson();

    private final HttpClient client;

    public PVPService() {
        this.client = HttpClientBuilder.create().build();
    }

    public ChallengeRequest getChallengeToMe() throws IOException {
        String accessToken = UserSession.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("No access token found");
        }
        HttpGet request = new HttpGet(API_URL + "/challenge/toMe");
        request.addHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Bearer " + accessToken);
        try(CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String json = EntityUtils.toString(response.getEntity());
                System.out.println(json);
                return gson.fromJson(json, ChallengeRequest.class);
            } else {
                throw new RuntimeException(EntityUtils.toString(response.getEntity()));
            }
        } catch (ClientProtocolException e) {
            throw new IOException(e.getMessage());
        }
    }

    public ChallengeRequest getChallengeFromMe() throws IOException {
        String accessToken = UserSession.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("No access token found");
        }
        HttpGet request = new HttpGet(API_URL + "/challenge/fromMe");
        request.addHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Bearer " + accessToken);
        try(CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String json = EntityUtils.toString(response.getEntity());
                System.out.println(json);
                return gson.fromJson(json, ChallengeRequest.class);
            } else {
                throw new RuntimeException(EntityUtils.toString(response.getEntity()));
            }
        } catch (ClientProtocolException e) {
            throw new IOException(e.getMessage());
        }
    }

    public ChallengeRequest challenge(int targetId) throws IOException {
        String accessToken = UserSession.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("No access token found");
        }
        HttpPost request = new HttpPost(API_URL + "/challenge/"+targetId);
        request.addHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Bearer " + accessToken);
        try(CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String json = EntityUtils.toString(response.getEntity());
                System.out.println(json);
                return gson.fromJson(json, ChallengeRequest.class);
            } else {
                throw new RuntimeException(EntityUtils.toString(response.getEntity()));
            }
        } catch (ClientProtocolException e) {
            throw new IOException(e.getMessage());
        }
    }

    public ChallengeRequest acceptChallenge(String challengeId) throws IOException {
        String accessToken = UserSession.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("No access token found");
        }
        HttpPost request = new HttpPost(API_URL + "/challenge/"+challengeId+"/accept");
        request.addHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Bearer " + accessToken);
        try(CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String json = EntityUtils.toString(response.getEntity());
                System.out.println(json);
                return gson.fromJson(json, ChallengeRequest.class);
            } else {
                throw new RuntimeException(EntityUtils.toString(response.getEntity()));
            }
        } catch (ClientProtocolException e) {
            throw new IOException(e.getMessage());
        }
    }

    public boolean declineChallenge(String challengeId) throws IOException {
        String accessToken = UserSession.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("No access token found");
        }
        HttpPost request = new HttpPost(API_URL + "/challenge/"+challengeId+"/decline");
        request.addHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Bearer " + accessToken);
        try(CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            return statusCode == 200;
        } catch (ClientProtocolException e) {
            throw new IOException(e.getMessage());
        }
    }

    public PVPFight action(String action) throws IOException {
        String accessToken = UserSession.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("No access token found");
        }
        HttpPost request = new HttpPost(API_URL + "/"+action);
        request.addHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Bearer " + accessToken);
        try(CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String json = EntityUtils.toString(response.getEntity());
                System.out.println(json);
                return gson.fromJson(json, PVPFight.class);
            } else if (statusCode == 204) {
                return null;
            }
            throw new RuntimeException(EntityUtils.toString(response.getEntity()));
        } catch (ClientProtocolException e) {
            throw new IOException(e.getMessage());
        }
    }

    public PVPFight getCurrentFight() throws IOException {
        String accessToken = UserSession.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("No access token found");
        }
        HttpGet request = new HttpGet(API_URL);
        request.addHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Bearer " + accessToken);
        try(CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String json = EntityUtils.toString(response.getEntity());
                System.out.println(json);
                return gson.fromJson(json, PVPFight.class);
            }
            throw new RuntimeException(EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PVPFight getFightById(String id) throws IOException {
        String accessToken = UserSession.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("No access token found");
        }
        HttpGet request = new HttpGet(API_URL+"/" + id);
        request.addHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Bearer " + accessToken);
        try(CloseableHttpResponse response = (CloseableHttpResponse) client.execute(request)){
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String json = EntityUtils.toString(response.getEntity());
                System.out.println(json);
                return gson.fromJson(json, PVPFight.class);
            }
            throw new RuntimeException(EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
