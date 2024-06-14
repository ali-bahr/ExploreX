package com.searchengine.Search;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndexerDocumentRepository extends MongoRepository<docelement, ObjectId> {

    List<docelement> findByUrl(String u);
 // TODO:   List<docelement> findBy();
    docelement findFirstBy_id(ObjectId objID) ;

}
