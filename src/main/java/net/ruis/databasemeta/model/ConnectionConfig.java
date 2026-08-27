package net.ruis.databasemeta.model;

import java.util.Objects;

public class ConnectionConfig {
    private String name;
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String schema;

    public ConnectionConfig() {
    }

    public ConnectionConfig(String name, String url, String username, String password, String driverClassName, String schema) {
        this.name = name;
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
        this.schema = schema;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDriverClassName() { return driverClassName; }
    public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
    public String getSchema() { return schema; }
    public void setSchema(String schema) { this.schema = schema; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConnectionConfig that)) {
            return false;
        }
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return url == null ? "未命名连接" : url;
    }
}
