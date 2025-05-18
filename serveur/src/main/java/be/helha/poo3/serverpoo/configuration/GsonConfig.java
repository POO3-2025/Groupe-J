package be.helha.poo3.serverpoo.configuration;

import be.helha.poo3.serverpoo.utils.ObjectIdAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

/**
 * Configuration Spring pour utiliser Gson avec support des ObjectId.
 */
@Configuration
public class GsonConfig {

    /**
     * Crée un convertisseur HTTP utilisant Gson avec un adaptateur ObjectId.
     *
     * @return un GsonHttpMessageConverter prêt à (dé)sérialiser les ObjectId
     */
    @Bean
    public GsonHttpMessageConverter gsonHttpMessageConverter() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ObjectId.class, new ObjectIdAdapter())
                .create();

        GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
        converter.setGson(gson);
        return converter;
    }
}
