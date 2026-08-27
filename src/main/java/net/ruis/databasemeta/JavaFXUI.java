package net.ruis.databasemeta;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.ruis.databasemeta.model.Column;
import net.ruis.databasemeta.model.ConnectionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JavaFXUI {

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private ConnectionHistoryService connectionHistoryService;

    @Value("${spring.datasource.url:}")
    private String defaultUrl;

    @Value("${spring.datasource.username:}")
    private String defaultUsername;

    @Value("${spring.datasource.password:}")
    private String defaultPassword;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String defaultDriverClassName;

    public void start(Stage stage) {
        buildMainWindow(stage);
    }

    private void buildMainWindow(Stage stage) {
        ComboBox<ConnectionConfig> connectionBox = new ComboBox<>();
        connectionBox.setPrefWidth(220);
        refreshConnections(connectionBox);

        TextField nameField = new TextField();
        nameField.setPromptText("连接名称");
        TextField urlField = new TextField(defaultUrl);
        urlField.setPromptText("JDBC URL");
        TextField usernameField = new TextField(defaultUsername);
        usernameField.setPromptText("用户名");
        PasswordField passwordField = new PasswordField();
        passwordField.setText(defaultPassword);
        passwordField.setPromptText("密码");
        TextField driverField = new TextField(defaultDriverClassName);
        driverField.setPromptText("驱动类");
        TextField schemaField = new TextField("public");
        schemaField.setPromptText("Schema");

        connectionBox.setOnAction(event -> fillConnectionFields(connectionBox.getValue(), nameField, urlField,
                usernameField, passwordField, driverField, schemaField));
        fillConnectionFields(connectionBox.getValue(), nameField, urlField, usernameField, passwordField,
                driverField, schemaField);
        if (connectionBox.getValue() == null && defaultUrl != null && !defaultUrl.isBlank()) {
            nameField.setText("默认连接");
        }

        Button saveConnectionButton = new Button("保存连接");
        saveConnectionButton.setOnAction(event -> {
            try {
                ConnectionConfig connectionConfig = currentConnection(nameField, urlField, usernameField,
                        passwordField, driverField, schemaField);
                connectionHistoryService.saveConnection(connectionConfig);
                refreshConnections(connectionBox);
                connectionBox.getSelectionModel().select(connectionConfig);
                showInfo("保存成功", "已保存连接: " + connectionConfig.getName());
            } catch (Exception e) {
                showError("保存连接失败", e);
            }
        });

        Button deleteConnectionButton = new Button("删除连接");
        deleteConnectionButton.setOnAction(event -> {
            try {
                ConnectionConfig selected = connectionBox.getValue();
                if (selected != null) {
                    connectionHistoryService.deleteConnection(selected.getName());
                    refreshConnections(connectionBox);
                    clearConnectionFields(nameField, urlField, usernameField, passwordField, driverField, schemaField);
                }
            } catch (Exception e) {
                showError("删除连接失败", e);
            }
        });

        Button newWindowButton = new Button("打开表信息窗口");
        newWindowButton.setOnAction(event -> {
            try {
                ConnectionConfig connectionConfig = currentConnection(nameField, urlField, usernameField,
                        passwordField, driverField, schemaField);
                validateConnectionForWindow(connectionConfig);
                buildTableWindow(new Stage(), connectionConfig);
            } catch (Exception e) {
                showError("打开窗口失败", e);
            }
        });

        GridPane connectionPane = new GridPane();
        connectionPane.setHgap(5);
        connectionPane.setVgap(8);
        connectionPane.addRow(0, new Label("历史连接"), connectionBox, saveConnectionButton, deleteConnectionButton);
        connectionPane.addRow(1, new Label("名称"), nameField, new Label("Schema"), schemaField);
        connectionPane.addRow(2, new Label("URL"), urlField);
        connectionPane.addRow(3, new Label("用户"), usernameField, new Label("密码"), passwordField);
        connectionPane.addRow(4, new Label("驱动"), driverField);
        GridPane.setHgrow(connectionBox, Priority.ALWAYS);
        GridPane.setHgrow(urlField, Priority.ALWAYS);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(usernameField, Priority.ALWAYS);
        GridPane.setHgrow(driverField, Priority.ALWAYS);

        Label helpLabel = new Label("主窗口只维护数据库连接。选择或填写连接后，点击“打开表信息窗口”创建独立表信息窗口。");
        VBox vbox = new VBox(10, connectionPane, newWindowButton, helpLabel);
        Scene scene = new Scene(vbox, 760, 260);
        stage.setScene(scene);
        stage.setTitle("Database Meta - 连接配置");
        stage.setAlwaysOnTop(true);
        stage.show();
    }

    private void buildTableWindow(Stage stage, ConnectionConfig connectionConfig) {
        TextField textField = new TextField();
        textField.setPromptText("输入表名");

        TableView<Column> table = createTable();

        Button searchButton = new Button("查  询");
        searchButton.setOnAction(event -> search(stage, textField, table, connectionConfig));

        HBox topControls = new HBox(5, new Label(connectionConfig.toString()), textField, searchButton);
        HBox.setHgrow(textField, Priority.ALWAYS);

        HBox bottomControls = new HBox(3);
        Button pastButton = new Button("粘贴并查询(Ctrl+D)");
        pastButton.setOnMouseClicked(event -> pasteAndSearch(textField, searchButton));
        Button select = new Button("生成select");
        select.setOnMouseClicked(event -> copySelect(table, textField, false));
        Button selectAll = new Button("生成select(所有)");
        selectAll.setOnMouseClicked(event -> copySelect(table, textField, true));
        bottomControls.getChildren().addAll(pastButton, select, selectAll, new Label("-使用Ctrl+D直接粘贴并查询"));

        VBox vbox = new VBox(5, topControls, table, bottomControls);
        VBox.setVgrow(table, Priority.ALWAYS);
        Scene scene = new Scene(vbox, 620, 800);
        stage.setScene(scene);
        stage.setTitle(connectionConfig + " - 表信息");
        stage.setAlwaysOnTop(true);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.D && event.isControlDown()) {
                pasteAndSearch(textField, searchButton);
            }
        });
        stage.show();
    }

    private TableView<Column> createTable() {
        TableView<Column> table = new TableView<>();
        TableColumn<Column, String> columnNameColumn = new TableColumn<>("名称");
        columnNameColumn.setCellValueFactory(cellData -> cellData.getValue().columnNameProperty());
        columnNameColumn.setPrefWidth(170);
        TableColumn<Column, String> columnTypeColumn = new TableColumn<>("类型");
        columnTypeColumn.setCellValueFactory(cellData -> cellData.getValue().columnTypeProperty());
        columnTypeColumn.setPrefWidth(110);
        TableColumn<Column, String> isNullableColumn = new TableColumn<>("必填");
        isNullableColumn.setCellValueFactory(cellData -> cellData.getValue().isNullableProperty());
        isNullableColumn.setPrefWidth(45);
        TableColumn<Column, String> columnCommentColumn = new TableColumn<>("备注");
        columnCommentColumn.setCellValueFactory(cellData -> cellData.getValue().columnCommentProperty());
        columnCommentColumn.prefWidthProperty().bind(table.widthProperty().subtract(340));
        table.getColumns().addAll(columnNameColumn, columnTypeColumn, isNullableColumn, columnCommentColumn);
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.C && event.isControlDown()) {
                copySelected(table);
            }
        });
        return table;
    }

    private void search(Stage stage, TextField textField, TableView<Column> table, ConnectionConfig connectionConfig) {
        try {
            String tableName = textField.getText();
            table.getItems().clear();
            List<Map<String, Object>> columns = databaseService.getColumnsForTable(connectionConfig, tableName);
            columns.forEach(column -> table.getItems().add(new Column(
                    new SimpleStringProperty((String) column.get("column_name")),
                    new SimpleStringProperty((String) column.get("data_type")),
                    new SimpleStringProperty((String) column.get("column_comment")),
                    new SimpleStringProperty((String) column.get("is_nullable_text"))
            )));
            stage.setTitle(connectionConfig.getName() + " - " + tableName);
        } catch (Exception e) {
            showError("查询失败", e);
        }
    }

    private void refreshConnections(ComboBox<ConnectionConfig> connectionBox) {
        List<ConnectionConfig> connections = connectionHistoryService.loadConnections();
        connectionBox.setItems(FXCollections.observableArrayList(connections));
        if (!connections.isEmpty()) {
            connectionBox.getSelectionModel().selectFirst();
        }
    }

    private ConnectionConfig currentConnection(TextField nameField, TextField urlField, TextField usernameField,
                                               PasswordField passwordField, TextField driverField, TextField schemaField) {
        return new ConnectionConfig(nameField.getText(), urlField.getText(), usernameField.getText(),
                passwordField.getText(), driverField.getText(), schemaField.getText());
    }

    private void fillConnectionFields(ConnectionConfig connectionConfig, TextField nameField, TextField urlField,
                                      TextField usernameField, PasswordField passwordField, TextField driverField,
                                      TextField schemaField) {
        if (connectionConfig == null) {
            return;
        }
        nameField.setText(connectionConfig.getName());
        urlField.setText(connectionConfig.getUrl());
        usernameField.setText(connectionConfig.getUsername());
        passwordField.setText(connectionConfig.getPassword());
        driverField.setText(connectionConfig.getDriverClassName());
        schemaField.setText(connectionConfig.getSchema());
    }

    private void clearConnectionFields(TextField nameField, TextField urlField, TextField usernameField,
                                       PasswordField passwordField, TextField driverField, TextField schemaField) {
        nameField.clear();
        urlField.setText(defaultUrl);
        usernameField.setText(defaultUsername);
        passwordField.setText(defaultPassword);
        driverField.setText(defaultDriverClassName);
        schemaField.setText("public");
    }

    private void validateConnectionForWindow(ConnectionConfig connectionConfig) {
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

    private void pasteAndSearch(TextField textField, Button searchButton) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            textField.setText(clipboard.getString());
            searchButton.fire();
        }
    }

    private void copySelect(TableView<Column> table, TextField textField, boolean allColumns) {
        List<Column> columns = allColumns ? table.getItems() : table.getSelectionModel().getSelectedItems();
        if (columns.isEmpty()) {
            return;
        }
        StringBuilder selectSql = new StringBuilder("select ");
        columns.forEach(column -> selectSql.append(column.getColumnName()).append(","));
        selectSql.deleteCharAt(selectSql.length() - 1);
        selectSql.append(" from ").append(textField.getText()).append(";");
        ClipboardContent content = new ClipboardContent();
        content.putString(selectSql.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void copySelected(TableView<Column> table) {
        StringBuilder copiedText = new StringBuilder();
        Map<Integer, List<String>> rows = getSelected(table);
        rows.forEach((key, value) -> {
            value.forEach(cellValue -> copiedText.append(cellValue).append("\t"));
            copiedText.append(",\n");
        });
        if (copiedText.length() > 0) {
            String textToCopy = copiedText.toString().trim();
            if (textToCopy.lastIndexOf(",") != -1) {
                textToCopy = textToCopy.substring(0, textToCopy.lastIndexOf(","));
            }
            ClipboardContent content = new ClipboardContent();
            content.putString(textToCopy);
            Clipboard.getSystemClipboard().setContent(content);
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(e.getMessage());
        alert.setContentText(e.getCause() == null ? e.toString() : e.getCause().toString());
        alert.showAndWait();
    }

    /**
     * 获取表格选中的值
     * @return selected row values
     */
    public Map<Integer, List<String>> getSelected(TableView<Column> table) {
        Map<Integer, List<String>> rows = new HashMap<>();
        for (TablePosition<Column, ?> pos : table.getSelectionModel().getSelectedCells()) {
            int row = pos.getRow();
            TableColumn<Column, ?> column = pos.getTableColumn();
            Object cellValue = column.getCellData(row);
            rows.computeIfAbsent(row, key -> new ArrayList<>()).add((String) cellValue);
        }
        return rows;
    }
}
