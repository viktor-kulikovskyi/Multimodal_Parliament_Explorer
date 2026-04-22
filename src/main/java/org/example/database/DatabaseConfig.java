package org.example.database;

/**
 * This class is a container for MongoDB connection details
 */
public class DatabaseConfig {
    private final String host;
    private final int port;
    private final String databaseName;
    private final String username;
    private final String password;

    public DatabaseConfig(String host, int port, String databaseName, String username, String password) {
        this.host = host; // server address
        this.port = port; // server port
        this.databaseName = databaseName; // name of the database
        this.username = username; // username for login
        this.password = password; // password for login
    }

    // Getter methods

    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getDatabaseName() { return databaseName; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public String getConnectionString() {
        // Checks if username and password is provided.
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            // If yes, build a connection string
        }
        return String.format("mongodb://%s:%s@%s:%d/%s?authSource=%s",username,password,host,port,databaseName,password);
    }
}
