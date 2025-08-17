package com.antonioteca.cc42.model;

import java.util.Map;

public class Question {
    public String category;
    public String difficulty;
    public String question;
    public String code;                  // pode ser null
    public Map<String, String> options;  // chaves "a","b","c","d"
    public String answer;                // ex: "b"
    public int time_limit;               // segundos

    public Question() {
    }
}
