package com.slightinsight.assist.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.slightinsight.assist.model.KnowledgeBase;

@Repository
public interface KnowledgeBaseRepository extends MongoRepository<KnowledgeBase, String> {

}