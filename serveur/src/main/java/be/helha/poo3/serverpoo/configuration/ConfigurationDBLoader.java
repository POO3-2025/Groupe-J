package be.helha.poo3.serverpoo.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;
import java.io.InputStream;

@Configuration
public class ConfigurationDBLoader {

    @Bean
    public ConfigurationDB configurationDB() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getClassLoader().getResourceAsStream("config.json");

        if (is == null) {
            throw new FileNotFoundException("config.json non trouvé dans resources/");
        }

        return mapper.readValue(is, ConfigurationDB.class);
    }

}
