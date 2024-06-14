package com.searchengine.Search;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface searchHistoryRepository extends MongoRepository<searchHistory, String> {

    boolean existsByTextIgnoreCase(String txt) ;

    List<searchHistory> findDistinctByTextStartingWithIgnoreCaseOrderByCountDesc(String txt, Pageable pageable);
    searchHistory findFirstByTextIgnoreCase (String txt) ;


}
