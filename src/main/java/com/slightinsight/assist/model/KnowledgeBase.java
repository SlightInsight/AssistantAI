package com.slightinsight.assist.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "KNOWLEDGE_BASE")
public class KnowledgeBase {

    @Id
    private String id;
    @Field("text_data")
    private String textData;
    @Field("vector_data")
    private List<Double> vectorData;

    public KnowledgeBase() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTextData() {
        return textData;
    }

    public void setTextData(String textData) {
        this.textData = textData;
    }

    public List<Double> getVectorData() {
        return vectorData;
    }

    public void setVectorData(List<Double> vectorData) {
        this.vectorData = vectorData;
    }

}