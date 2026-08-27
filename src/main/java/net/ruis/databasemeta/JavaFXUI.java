package net.ruis.databasemeta;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.ruis.databasemeta.model.Column;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JavaFXUI {

    @Autowired
    private DatabaseService databaseService;

    public void start(Stage stage) {
        TextField textField = new TextField();
        textField.setPromptText("输入表名");
        textField.setText("tenant_forms");

        // 创建 TableView
        TableView<Column> table = new TableView<>();

        // 创建并绑定各个列
        TableColumn<Column, String> columnNameColumn = new TableColumn<>("名称");
        columnNameColumn.setCellValueFactory(cellData -> cellData.getValue().columnNameProperty());
        columnNameColumn.setPrefWidth(170);

        TableColumn<Column, String> columnTypeColumn = new TableColumn<>("类型");
        columnTypeColumn.setCellValueFactory(cellData -> cellData.getValue().columnTypeProperty());
        columnTypeColumn.setPrefWidth(110);

        TableColumn<Column, String> isNullableColumn = new TableColumn<>("必填");
        isNullableColumn.setCellValueFactory(cellData -> cellData.getValue().isNullableProperty());
        isNullableColumn.setPrefWidth(30);


        TableColumn<Column, String> columnCommentColumn = new TableColumn<>("备注");
        columnCommentColumn.setCellValueFactory(cellData -> cellData.getValue().columnCommentProperty());
        // 绑定剩余空间
        columnCommentColumn.prefWidthProperty().bind(table.widthProperty().subtract(columnNameColumn.getPrefWidth() + columnTypeColumn.getPrefWidth() + isNullableColumn.getPrefWidth() + 15));  // 剩下的空间


        /*TableColumn<Column, String> columnDefaultColumn = new TableColumn<>("Column Default");
        columnDefaultColumn.setCellValueFactory(cellData -> cellData.getValue().columnDefaultProperty());

        TableColumn<Column, String> extraColumn = new TableColumn<>("Extra");
        extraColumn.setCellValueFactory(cellData -> cellData.getValue().extraProperty());

        TableColumn<Column, String> columnKeyColumn = new TableColumn<>("Column Key");
        columnKeyColumn.setCellValueFactory(cellData -> cellData.getValue().columnKeyProperty());

        TableColumn<Column, String> columnLengthColumn = new TableColumn<>("Column Length");
        columnLengthColumn.setCellValueFactory(cellData -> cellData.getValue().columnLengthProperty());

        TableColumn<Column, String> columnScaleColumn = new TableColumn<>("Column Scale");
        columnScaleColumn.setCellValueFactory(cellData -> cellData.getValue().columnScaleProperty());

        TableColumn<Column, String> columnPrecisionColumn = new TableColumn<>("Column Precision");
        columnPrecisionColumn.setCellValueFactory(cellData -> cellData.getValue().columnPrecisionProperty());*/

        // 将列添加到 TableView
        table.getColumns().addAll(columnNameColumn, columnTypeColumn,isNullableColumn , columnCommentColumn/*, columnDefaultColumn,
                , extraColumn, columnKeyColumn, columnLengthColumn,
                columnScaleColumn, columnPrecisionColumn*/);
        // 设置 TableView 可单元格选择
        table.getSelectionModel().setCellSelectionEnabled(true);
        // 设置 TableView 支持多选单元格
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // 创建查询按钮
        Button searchButton = new Button("查  询");
        searchButton.setOnAction(event -> {
            String tableName = textField.getText();
            table.getItems().clear();  // 清空表格
            List<Map<String, Object>> columns = databaseService.getColumnsForTable(tableName);
            columns.forEach(column -> {
                // 使用 builder 创建 Column 对象并添加到 TableView
                Column column2 = new Column(
                        new SimpleStringProperty((String) column.get("column_name")),
                        new SimpleStringProperty((String) column.get("data_type")),
                        new SimpleStringProperty((String) column.get("column_comment")),
                        new SimpleStringProperty((String) column.get("is_nullable_text"))
                );
                table.getItems().add(column2);
            });
            stage.setTitle(tableName);
        });
        HBox topControls = new HBox();
        topControls.getChildren().addAll(textField,searchButton);
        HBox.setHgrow(textField, Priority.ALWAYS);
        Label label = new Label("-使用Ctrl+D直接粘贴并查询");


        // 监听键盘事件（Ctrl+C复制）
        table.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.C && event.isControlDown()) {
                // 处理 Ctrl+C 事件
                StringBuilder copiedText = new StringBuilder();

                Map<Integer,List<String>> rows =  getSelected(table);
                rows.forEach((key, value) -> {
                    value.forEach(cellValue -> copiedText.append(cellValue).append("\t"));
                    copiedText.append(",\n");
                });

                if (copiedText.length() > 0) {
                    // 将选中的内容复制到剪贴板
                    String textToCopy = copiedText.toString().trim();
                    if(textToCopy.lastIndexOf(",") != -1){
                        textToCopy = textToCopy.substring(0,textToCopy.lastIndexOf(",") );
                    }
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();
                    content.putString(textToCopy);
                    clipboard.setContent(content);
                }
            }
        });

        HBox bottomControls = new HBox(3);
        Button pastButton = new Button("粘贴并查询(Ctrl+D)");
        pastButton.setOnMouseClicked(event -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            if (clipboard.hasString()) {
                String clipboardText = clipboard.getString();
                textField.setText(clipboardText);
                searchButton.fire();
            }
        });

        Button select = new Button("生成select");

        Button selectAll = new Button("生成select(所有)");
        selectAll.setOnMouseClicked(event -> {
            StringBuilder selectSql = new StringBuilder();
            selectSql.append("select ");
            table.getItems().forEach(column -> {
                selectSql.append(column.getColumnName()).append( ",");
            });
            selectSql.deleteCharAt(selectSql.length()-1);
            selectSql.append(" from ").append(textField.getText()).append(";");
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(selectSql.toString());
            clipboard.setContent(content);

        });

        bottomControls.getChildren().addAll(pastButton,select,selectAll);



        // 布局设置
        VBox vbox = new VBox();
        vbox.getChildren().addAll(topControls, table,bottomControls);

        // 设置 TableView 在 VBox 中垂直伸缩
        VBox.setVgrow(table, Priority.ALWAYS);  // 让 TableView 占据 VBox 中的剩余空间
        // 创建场景并设置舞台
        Scene scene = new Scene(vbox, 600, 800);
        stage.setScene(scene);
        stage.setTitle("Database Column Viewer");
        stage.setAlwaysOnTop(true);  // 设置窗口始终在最前端

        // 为整个场景注册快捷键 Alt + D
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // 检测 Ctrl + D 组合键
            if (event.getCode() == KeyCode.D && event.isControlDown()) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                if (clipboard.hasString()) {
                    String clipboardText = clipboard.getString();
                    textField.setText(clipboardText);
                    searchButton.fire();
                }
            }
        });
        stage.show();
    }

    /**
     * 获取表格选中的值
     * @return
     */
    public Map<Integer,List<String>> getSelected(TableView<Column> table){
        Map<Integer,List<String>> rows = new HashMap<>();
        for (TablePosition<Column, ?> pos : table.getSelectionModel().getSelectedCells()) {
            // 获取选中的内容
            int row = pos.getRow();
            int col = pos.getColumn();
            TableColumn<Column, ?> column = pos.getTableColumn();
            Object cellValue = column.getCellData(row);
            if(rows.containsKey(row)){
                List<String> list = rows.get(row);
                list.add((String)cellValue);
            }else{
                List<String> list = new ArrayList<>();
                list.add((String)cellValue);
                rows.put(row,list);
            }
        }
        return rows;
    }

}
