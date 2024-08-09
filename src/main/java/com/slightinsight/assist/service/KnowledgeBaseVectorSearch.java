package com.slightinsight.assist.service;

import java.util.List;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.search.SearchPath.fieldPath;

import static java.util.Arrays.asList;

import org.bson.conversions.Bson;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.search.FieldSearchPath;

@Service
public class KnowledgeBaseVectorSearch {

    @Autowired
    private MongoClient mongoClient;

    @Value("${spring.data.mongodb.database}")
    private String appDatabase;

    public AggregateIterable<Document> findByVectorData(List<Double> queryVector) {

        MongoDatabase database = mongoClient.getDatabase(appDatabase);
        MongoCollection<Document> collection = database.getCollection("KNOWLEDGE_BASE");

        String indexName = "vector_index";
        FieldSearchPath fieldSearchPath = fieldPath("vector_data");
        int numCandidates = 10;
        int limit = 1;

        List<Bson> pipeline = asList(
                vectorSearch(fieldSearchPath, queryVector, indexName, numCandidates, limit),
                project(fields(exclude("_id"), include("text_data"), include("active"),
                        metaVectorSearchScore("score"))));

        return collection.aggregate(pipeline);
    }

}
