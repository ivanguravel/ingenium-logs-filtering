package com.example;

import com.example.entries.ClusterEntry;
import com.example.entries.NodeClusterEntry;
import com.example.entries.ShardClusterEntry;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import java.util.Collection;

public class Main {
    public static void main(String[] args) {

        NodeClusterEntry node = new NodeClusterEntry();

        node.addDocument("test1", "bla bla bla");

        System.out.println(node.suggest("bl"));
        System.out.println(node.searchDocuments("bla"));
    }
}