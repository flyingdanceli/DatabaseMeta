package net.ruis.databasemeta.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Builder;
import lombok.Data;

@Data

public class Column {

    private  StringProperty columnName = new SimpleStringProperty();
    private  StringProperty columnType = new SimpleStringProperty();
    private  StringProperty columnComment = new SimpleStringProperty();
    private  StringProperty columnDefault = new SimpleStringProperty();
    private  StringProperty isNullable = new SimpleStringProperty();
    private  StringProperty extra = new SimpleStringProperty();
    private  StringProperty columnKey = new SimpleStringProperty();
    private  StringProperty columnLength = new SimpleStringProperty();
    private  StringProperty columnScale = new SimpleStringProperty();
    private  StringProperty columnPrecision = new SimpleStringProperty();

    // Builder 方法会基于 String 字段生成，但构建时依然使用 StringProperty
    public Column(StringProperty columnName, StringProperty columnType, StringProperty columnComment,StringProperty isNullable) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.columnComment = columnComment;
        this.isNullable = isNullable;
    }

    public String getColumnName() {
        return columnName.get();
    }

    public StringProperty columnNameProperty() {
        return columnName;
    }

    public String getColumnType() {
        return columnType.get();
    }

    public StringProperty columnTypeProperty() {
        return columnType;
    }

    public String getColumnComment() {
        return columnComment.get();
    }

    public StringProperty columnCommentProperty() {
        return columnComment;
    }

    public String getColumnDefault() {
        return columnDefault.get();
    }

    public StringProperty columnDefaultProperty() {
        return columnDefault;
    }

    public String getIsNullable() {
        return isNullable.get();
    }

    public StringProperty isNullableProperty() {
        return isNullable;
    }

    public String getExtra() {
        return extra.get();
    }

    public StringProperty extraProperty() {
        return extra;
    }

    public String getColumnKey() {
        return columnKey.get();
    }

    public StringProperty columnKeyProperty() {
        return columnKey;
    }

    public String getColumnLength() {
        return columnLength.get();
    }

    public StringProperty columnLengthProperty() {
        return columnLength;
    }

    public String getColumnScale() {
        return columnScale.get();
    }

    public StringProperty columnScaleProperty() {
        return columnScale;
    }

    public String getColumnPrecision() {
        return columnPrecision.get();
    }

    public StringProperty columnPrecisionProperty() {
        return columnPrecision;
    }
}
