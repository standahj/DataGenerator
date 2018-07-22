package com.chute.parser.event.output;

public class TableData {
    private String indexName;
    private int partitionNumber;
    private TableDataKey dataKey;
    private String mainData;

    public TableData() {

    }

    public TableData(String indexName, int partitionNumber, TableDataKey tableDataKey, String mainData) {
        this.indexName = indexName;
        this.partitionNumber = partitionNumber;
        this.dataKey = tableDataKey;
        this.mainData = mainData;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public int getPartitionNumber() {
        return partitionNumber;
    }

    public void setPartitionNumber(int partitionNumber) {
        this.partitionNumber = partitionNumber;
    }

    public TableDataKey getDataKey() {
        return dataKey;
    }

    public void setDataKey(TableDataKey dataKey) {
        this.dataKey = dataKey;
    }

    public String getMainData() {
        return mainData;
    }

    public void setMainData(String mainData) {
        this.mainData = mainData;
    }

    @Override
    public String toString() {
        StringBuilder json = new StringBuilder("{");
        json.append("\"indexName\": \"").append(indexName).append("\", ");
        json.append("\"partitionNumber\": ").append(Integer.toString(partitionNumber)).append(", ");
        json.append("\"dataKey\": ").append(dataKey.toString()).append(", ");
        json.append("\"mainData\": \"").append(mainData).append("\"");
        json.append("}");
        return json.toString();
    }
}
