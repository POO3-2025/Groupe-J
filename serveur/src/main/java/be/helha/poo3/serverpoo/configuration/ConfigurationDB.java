package be.helha.poo3.serverpoo.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "config")
public class ConfigurationDB {
    @JsonProperty("databases")
    private Databases databases;

    public Databases getDatabases() {
        return this.databases;
    }

    public void setDatabases(Databases databases) {
        this.databases = databases;
    }

    public static class Databases {
        private Map<String, Details> mysql;
        private Map<String, Details> mongoDB;

        public Map<String, Details> getMysql() {
            return this.mysql;
        }

        public void setMysql(Map<String, Details> mysql) {
            this.mysql = mysql;
        }

        public Map<String, Details> getMongoDB() {
            return this.mongoDB;
        }

        public void setMongoDB(Map<String, Details> mongoDB) {
            this.mongoDB = mongoDB;
        }

        public static class Details {
            private String host;
            private int port;
            private String database;
            private String user;
            private String password;

            public String getHost() {
                return this.host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getPort() {
                return this.port;
            }

            public void setPort(int port) {
                this.port = port;
            }

            public String getDatabase() {
                return this.database;
            }

            public void setDatabase(String database) {
                this.database = database;
            }

            public String getUser() {
                return this.user;
            }

            public void setUser(String user) {
                this.user = user;
            }

            public String getPassword() {
                return this.password;
            }

            public void setPassword(String password) {
                this.password = password;
            }

            public String toString() {
                return "Details{host='" + this.host + "', port=" + this.port + ", database='" + this.database + "', user='" + this.user + "', password='" + this.password + "'}";
            }
        }
    }
}
