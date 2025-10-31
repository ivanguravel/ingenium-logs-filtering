package com.example.entries;


import com.example.SearchEntry;

public class NodeClusterEntry extends ClusterEntry<ShardClusterEntry> implements SearchEntry {

    private static final short NODE_SIZE = 10;


    public NodeClusterEntry() {
        super(NODE_SIZE);
        for (int i =0; i < NODE_SIZE; i++) {
            active.put(i, new ShardClusterEntry());
        }
    }

    @Override
    ShardClusterEntry createEntry() {
        return new ShardClusterEntry();
    }
}
