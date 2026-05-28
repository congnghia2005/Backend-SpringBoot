package com.example.UCTalen.model;

import lombok.Data;

import java.util.Map;

@Data 
public class Review {
    private String id;               
    private String placeId;          
    private String authorName;       
    private int rating;              
    private String text;             
    private String status;           
    private Map<String, String> aiResponses; 
    private String selectedResponse;
}