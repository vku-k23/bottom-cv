package com.cnpm.bottomcv.service;

public interface SearchService {
    void searchJobs(String query);

    void getSuggestions(String query);
}