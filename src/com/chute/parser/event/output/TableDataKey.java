package com.chute.parser.event.output;

public class TableDataKey {
    private long   tableId;
    private long   timeField;
    private String objectField;

    public TableDataKey() {

    }

    public TableDataKey(long tableId, long timeField, String objectField) {
        this.tableId = tableId;
        this.timeField = timeField;
        this.objectField = objectField;
    }

    public long getTableId() {
        return tableId;
    }

    public void setTableId(long tableId) {
        this.tableId = tableId;
    }

    public long getTimeField() {
        return timeField;
    }

    public void setTimeField(long timeField) {
        this.timeField = timeField;
    }

    public String getObjectField() {
        return objectField;
    }

    public void setObjectField(String objectField) {
        this.objectField = objectField;
    }

    @Override
    public String toString() {
        StringBuilder json = new StringBuilder("{");
        json.append("\"tableId\": ").append(Long.toString(tableId)).append(", ");
        json.append("\"timeField\": ").append(Long.toString(timeField)).append(", ");
        json.append("\"objectField\": \"").append(objectField).append("\"");
        json.append("}");
        return json.toString();
    }
}
