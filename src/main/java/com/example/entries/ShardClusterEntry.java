package com.example.entries;

import com.example.SearchEntry;

public class ShardClusterEntry extends ClusterEntry<InvertedIndexEntry> implements SearchEntry {

    static final short INDEX_SIZE = 100;
    private static final short SHARD_SIZE = 10;

    public ShardClusterEntry() {
        super(SHARD_SIZE);
        for (int i =0; i < SHARD_SIZE; i++) {
            active.put(i, new InvertedIndexEntry());
        }
    }

    @Override
    InvertedIndexEntry createEntry() {
        return new InvertedIndexEntry();
    }
}
