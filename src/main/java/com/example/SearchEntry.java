package com.example;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public interface SearchEntry {
    boolean addDocument(String name, String text);
    Map<String, AtomicLong> searchDocuments(String word);
    List<String> suggest(String prefix, int limit);

    int size();

    default List<String> suggest(String prefix) {
        return suggest(prefix, 10);
    }
}
