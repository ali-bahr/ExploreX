package com.searchengine.Search;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController

@RequestMapping("/api")
public class SearchController {


    @Autowired
    private QueryService queryService;

    @Autowired
    private searchHistoryService sHisService ;


    @GetMapping("/search")
    public ResponseEntity<List<ArrayList<String>>> search(@RequestParam String keyword) {

        List<ArrayList<String>> result = queryService.searchly(keyword);

        sHisService.saveSearch(keyword);

        return new ResponseEntity<List<ArrayList<String>>>(result,HttpStatus.OK);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam String str){
        return new ResponseEntity<List<String>>(sHisService.getSuggestions(str),HttpStatus.OK);
    }
}
