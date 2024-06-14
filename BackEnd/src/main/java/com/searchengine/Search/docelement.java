package com.searchengine.Search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "docs")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class docelement {
    @Id
    private ObjectId  _id;
    private String url;
    private List<String> elements;
    private int numWords;

}