package com.example.entries;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class NodeClusterEntryTest {

    private NodeClusterEntry node;

    @BeforeEach
    void setUp() {
        node = new NodeClusterEntry();
    }

    @Test
    void testSearchAcrossShards() throws InterruptedException {
        for (int i =0; i <=11; i++) {
            node.addDocument("docA", "distributed system search");
            node.addDocument("docB", "search cluster distributed");
        }

        Map<String, AtomicLong> results = node.searchDocuments("distributed");

        assertEquals(2, results.size());
    }

    @Test
    void testMultipleAddsAndQueries() {
        for (int i = 0; i < 100; i++) {
            node.addDocument("file_" + i, "cluster shard node index");
        }

        Map<String, AtomicLong> found = node.searchDocuments("cluster");
        assertEquals(100, found.size());
    }
}
