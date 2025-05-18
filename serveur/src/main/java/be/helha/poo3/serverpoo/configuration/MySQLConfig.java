package be.helha.poo3.serverpoo.configuration;

import be.helha.poo3.serverpoo.configuration.ConfigurationDB.Databases.Details;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class MySQLConfig {

    private final ConfigurationDB configurationDB;
    private Details sqlConfig;

    // "production" ou "test"
    private static final String ENV = "production";

    public MySQLConfig(ConfigurationDB configurationDB) {
        this.configurationDB = configurationDB;
    }

    @PostConstruct
    public void loadConfig() {
        this.sqlConfig = configurationDB.getDatabases().getMysql().get(ENV);

        if (sqlConfig == null) {
            throw new IllegalStateException("Configuration MySQL pour l'environnement '" + ENV + "' introuvable dans config.json");
        }
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(String.format(
                "jdbc:mysql://%s:%d/%s",
                sqlConfig.getHost(),
                sqlConfig.getPort(),
                sqlConfig.getDatabase()
        ));
        config.setUsername(sqlConfig.getUser());
        config.setPassword(sqlConfig.getPassword());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        return new HikariDataSource(config);
    }
}
