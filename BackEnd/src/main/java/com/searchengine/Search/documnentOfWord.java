package com.searchengine.Search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class documnentOfWord {

    private String url ;
    private List<String> metadata;
    private int tf;
    private double  final_score ;
    private ObjectId objectid;

}