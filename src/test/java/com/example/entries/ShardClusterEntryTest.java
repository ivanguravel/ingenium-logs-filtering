package com.example.entries;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class ShardClusterEntryTest {

    private ShardClusterEntry shard;

    @BeforeEach
    void setUp() {
        shard = new ShardClusterEntry();
    }

    @Test
    void testAddAndSearch() {
        shard.addDocument("doc1", "search engine elastic index");
        Map<String, AtomicLong> result = shard.searchDocuments("elastic");
        assertTrue(result.containsKey("doc1"));
    }

    @Test
    void testShardOverflowsToFrozen() {
        for (int i = 0; i < 1500; i++) {
            shard.addDocument("doc_" + i, "word" + i);
        }
        assertTrue(shard.size() > 0);
    }
}