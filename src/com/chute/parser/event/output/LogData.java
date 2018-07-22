package com.chute.parser.event.output;

public class LogData {
    private String indexName;
    private int    partitionNumber;
    private long   eventTimeStamp;
    private String sensorName;
    private String origin;
    private int    dataType;
    private String data;
    private int    indexTimeStamp;

    public LogData() {

    }

    public LogData(String indexName, int partitionNumber, long eventTimeStamp, String sensorName,
                   String origin, int dataType, String data, int indexTimeStamp) {
        this.indexName = indexName;
        this.partitionNumber = partitionNumber;
        this.eventTimeStamp = eventTimeStamp > 0 ? eventTimeStamp : System.currentTimeMillis();
        this.sensorName = sensorName;
        this.origin = origin;
        this.dataType = dataType;
        this.data = data;
        this.indexTimeStamp = indexTimeStamp > 0 ? indexTimeStamp : new Long(eventTimeStamp/1000L).intValue();
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

    public long getEventTimeStamp() {
        return eventTimeStamp;
    }

    public void setEventTimeStamp(long eventTimeStamp) {
        this.eventTimeStamp = eventTimeStamp;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getIndexTimeStamp() {
        return indexTimeStamp;
    }

    public void setIndexTimeStamp(int indexTimeStamp) {
        this.indexTimeStamp = indexTimeStamp;
    }

    @Override
    public String toString() {
        StringBuilder json = new StringBuilder("{");
        json.append("\"indexName\": \"").append(indexName).append("\", ");
        json.append("\"partitionNumber\": ").append(Integer.toString(partitionNumber)).append(", ");
        json.append("\"eventTimeStamp\": ").append(Long.toString(eventTimeStamp)).append(", ");
        json.append("\"sensorName\": \"").append(sensorName).append("\", ");
        json.append("\"origin\": \"").append(origin).append("\", ");
        json.append("\"dataType\": ").append(Integer.toString(dataType)).append(", ");
        json.append("\"data\": \"").append(data).append("\", ");
        json.append("\"indexTimeStamp\": ").append(Integer.toString(indexTimeStamp));
        json.append("}");
        return json.toString();
    }
}
