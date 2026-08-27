package net.ruis.databasemeta;

import net.ruis.databasemeta.model.ConnectionConfig;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseService {

    public List<String> getAllTables(ConnectionConfig connectionConfig) {
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = ? ORDER BY table_name";
        try (Connection connection = openConnection(connectionConfig);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, getSchema(connectionConfig));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<String> tables = new ArrayList<>();
                while (resultSet.next()) {
                    tables.add(resultSet.getString("table_name"));
                }
                return tables;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("查询数据表失败", e);
        }
    }

    public List<Map<String, Object>> getColumnsForTable(ConnectionConfig connectionConfig, String tableName) {
        String sql = "SELECT " +
                "    c.column_name, " +
                "    CASE WHEN character_maximum_length IS NOT NULL " +
                "        THEN udt_name || '(' || character_maximum_length || ')' " +
                "        ELSE udt_name " +
                "               END as data_type, " +
                "    CASE " +
                "           WHEN c.is_nullable = 'YES' THEN '是' " +
                "           ELSE NULL " +
                "       END AS is_nullable_text, " +
                "    d.description AS column_comment " +
                "FROM " +
                "    information_schema.columns c " +
                "LEFT JOIN " +
                "    pg_catalog.pg_description d " +
                "    ON d.objsubid = c.ordinal_position " +
                "    AND d.objoid = ( " +
                "        SELECT oid " +
                "        FROM pg_catalog.pg_class " +
                "        WHERE relname = c.table_name " +
                "        AND relnamespace = (SELECT oid FROM pg_catalog.pg_namespace WHERE nspname = c.table_schema) " +
                "    ) " +
                "WHERE " +
                "    c.table_schema = ? " +
                "    AND c.table_name = ? " +
                "ORDER BY c.ordinal_position";
        try (Connection connection = openConnection(connectionConfig);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, getSchema(connectionConfig));
            statement.setString(2, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return toList(resultSet);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("查询字段失败", e);
        }
    }

    public List<Map<String, Object>> getIndexesForTable(ConnectionConfig connectionConfig, String tableName) {
        String sql = "SELECT indexname, indexdef FROM pg_indexes WHERE schemaname = ? AND tablename = ?";
        try (Connection connection = openConnection(connectionConfig);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, getSchema(connectionConfig));
            statement.setString(2, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return toList(resultSet);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("查询索引失败", e);
        }
    }

    private Connection openConnection(ConnectionConfig connectionConfig) throws SQLException {
        try {
            Class.forName(connectionConfig.getDriverClassName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("未找到数据库驱动: " + connectionConfig.getDriverClassName(), e);
        }
        return DriverManager.getConnection(connectionConfig.getUrl(), connectionConfig.getUsername(), connectionConfig.getPassword());
    }

    private String getSchema(ConnectionConfig connectionConfig) {
        if (connectionConfig.getSchema() == null || connectionConfig.getSchema().isBlank()) {
            return "public";
        }
        return connectionConfig.getSchema();
    }

    private List<Map<String, Object>> toList(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        List<Map<String, Object>> rows = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }
}
