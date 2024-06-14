package com.searchengine.Search;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "SearchHistory")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class searchHistory {
    @Id
    private String ID ;
    private String text ;
    private int count ;
}
