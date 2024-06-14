package com.searchengine.Search;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface IndexerRepository extends MongoRepository<word, ObjectId> {

    List<word> findByWordID(String w);

    List<word> findAll();
}
