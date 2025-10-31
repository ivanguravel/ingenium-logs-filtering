package com.example.entries;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class InvertedIndexEntryTest {

    private InvertedIndexEntry index;

    @BeforeEach
    void setUp() {
        index = new InvertedIndexEntry();
    }

    @Test
    void testAddAndSearchSingleDocument() {
        index.addDocument("doc1", "hello world hello");
        Map<String, AtomicLong> results = index.searchDocuments("hello");


        assertEquals(1, results.size(), "Must contain one document");
        assertEquals(2L, results.get("doc1").get(), "Word count must match");
    }

    @Test
    void testAddMultipleDocuments() {
        index.addDocument("doc1", "java spring boot");
        index.addDocument("doc2", "spring framework");
        Map<String, AtomicLong> result = index.searchDocuments("spring");

        assertEquals(2, result.size());
        assertTrue(result.containsKey("doc1"));
        assertTrue(result.containsKey("doc2"));
    }

    @Test
    void testAutocompleteSuggestions(){
        index.addDocument("doc1", "alpha beta gamma");
        index.addDocument("doc2", "alphabet alphanumeric");


        List<String> suggestions = index.suggest("alp", 10);

        assertTrue(suggestions.contains("alpha"));
        assertTrue(suggestions.contains("alphabet"));
        assertTrue(suggestions.contains("alphanumeric"));
    }

    @Test
    void testSearchUnknownWord() {
        index.addDocument("doc1", "java code index");
        Map<String, AtomicLong> results = index.searchDocuments("python");
        assertTrue(results.isEmpty());
    }

//    @Test
//    void testConcurrentAccess() throws InterruptedException {
//        int threads = 8;
//        int docsPerThread = 100;
//        ExecutorService executor = Executors.newFixedThreadPool(threads);
//
//        CountDownLatch latch = new CountDownLatch(threads);
//
//        for (int t = 0; t < threads; t++) {
//            int threadId = t;
//            executor.submit(() -> {
//                for (int i = 0; i < docsPerThread; i++) {
//                    index.addDocument("doc_" + threadId + "_" + i, "concurrent test data java");
//                }
//                latch.countDown();
//            });
//        }
//
//        latch.await();
//        executor.shutdown();
//
//        Map<String, AtomicLong> result = index.searchDocuments("java");
//
//        assertEquals(threads * docsPerThread, result.size(),
//                "Each document must be indexed and searchable");
//    }


    @Test
    void testSize() {
        index.addDocument("d1", "foo bar baz");
        assertTrue(index.size() > 0);
    }
}
