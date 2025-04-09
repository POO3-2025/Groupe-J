package be.helha.poo3;

import java.util.Map;

class ConfigurationDB {
    private Databases databases;

    static class Databases {
        private Map<String, Details> mysql;
        private Map<String, Details> mongoDB;

        static class Details {
            private String host;
            private int port;
            private String database;
            private String user;
            private String password;

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }

            public String getDatabase() {
                return database;
            }

            public void setDatabase(String database) {
                this.database = database;
            }

            public String getUser() {
                return user;
            }

            public void setUser(String user) {
                this.user = user;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }


            @Override
            public String toString() {
                return "Details{" +
                        "host='" + host + '\'' +
                        ", port=" + port +
                        ", database='" + database + '\'' +
                        ", user='" + user + '\'' +
                        ", password='" + password + '\'' +
                        '}';
            }
        }

        public Map<String, Details> getMysql() {
            return mysql;
        }

        public void setMysql(Map<String, Details> mysql) {
            this.mysql = mysql;
        }

        public Map<String, Details> getMongoDB() {
            return mongoDB;
        }

        public void setMongoDB(Map<String, Details> mongoDB) {
            this.mongoDB = mongoDB;
        }
    }

    public Databases getDatabases() {
        return databases;
    }

    public void setDatabases(Databases databases) {
        this.databases = databases;
    }
}
