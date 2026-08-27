package net.ruis.databasemeta;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.ruis.databasemeta.model.ConnectionConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ConnectionHistoryService {
    private static final Type CONNECTION_LIST_TYPE = new TypeToken<List<ConnectionConfig>>() { }.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath = Path.of(System.getProperty("user.home"), ".databasemeta", "connections.json");

    public List<ConnectionConfig> loadConnections() {
        if (!Files.exists(configPath)) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(configPath)) {
            List<ConnectionConfig> connections = gson.fromJson(reader, CONNECTION_LIST_TYPE);
            if (connections == null) {
                return new ArrayList<>();
            }
            connections.sort(Comparator.comparing(ConnectionConfig::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
            return new ArrayList<>(connections);
        } catch (IOException e) {
            throw new IllegalStateException("读取连接历史失败: " + configPath, e);
        }
    }

    public void saveConnection(ConnectionConfig connectionConfig) {
        validate(connectionConfig);
        List<ConnectionConfig> connections = loadConnections();
        connections.removeIf(connection -> connection.getName() != null
                && connection.getName().equalsIgnoreCase(connectionConfig.getName()));
        connections.add(connectionConfig);
        connections.sort(Comparator.comparing(ConnectionConfig::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
        saveConnections(connections);
    }

    public void deleteConnection(String name) {
        List<ConnectionConfig> connections = loadConnections();
        connections.removeIf(connection -> connection.getName() != null && connection.getName().equalsIgnoreCase(name));
        saveConnections(connections);
    }

    private void saveConnections(List<ConnectionConfig> connections) {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                gson.toJson(connections, CONNECTION_LIST_TYPE, writer);
            }
        } catch (IOException e) {
            throw new IllegalStateException("保存连接历史失败: " + configPath, e);
        }
    }

    private void validate(ConnectionConfig connectionConfig) {
        if (connectionConfig.getName() == null || connectionConfig.getName().isBlank()) {
            throw new IllegalArgumentException("连接名称不能为空");
        }
        if (connectionConfig.getUrl() == null || connectionConfig.getUrl().isBlank()) {
            throw new IllegalArgumentException("JDBC URL 不能为空");
        }
        if (connectionConfig.getDriverClassName() == null || connectionConfig.getDriverClassName().isBlank()) {
            throw new IllegalArgumentException("驱动类不能为空");
        }
        if (connectionConfig.getSchema() == null || connectionConfig.getSchema().isBlank()) {
            connectionConfig.setSchema("public");
        }
    }
}
