package net.ruis.databasemeta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<String> getAllTables() {
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = :schema";
        Map<String, Object> params = new HashMap<>();
        params.put("schema", "public");

        return jdbcTemplate.queryForList(sql, params, String.class);
    }


    public List<Map<String, Object>> getColumnsForTable(String tableName) {
        String sql = "SELECT " +
                "    c.column_name, " +
                "    CASE WHEN character_maximum_length IS NOT NULL\n" +
                "        THEN udt_name || '(' || character_maximum_length || ')'\n" +
                "        ELSE udt_name\n" +
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
                "    c.table_schema = :schema " +
                "    AND c.table_name = :tableName";
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableName);
        params.put("schema", "public");

        return jdbcTemplate.queryForList(sql, params);
    }


    public List<Map<String, Object>> getIndexesForTable(String tableName) {
        String sql = "SELECT indexname, indexdef " +
                "FROM pg_indexes " +
                "WHERE tablename = :tableName";
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableName);

        return jdbcTemplate.queryForList(sql, params);
    }
}