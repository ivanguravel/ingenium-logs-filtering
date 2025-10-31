package com.example.entries;

import com.example.SearchEntry;
import com.example.threads.TrieUpdater;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class InvertedIndexEntry implements SearchEntry {

    private Map<String, Map<String, AtomicLong>> indexedMap = new ConcurrentHashMap<>();
    private Queue<String> trieUpdateQueue = new ConcurrentLinkedQueue<>();
    private Trie.TrieBuilder trieBuilder = Trie.builder().ignoreCase();

    private AtomicReference<Trie> lastBuiltTrie = new AtomicReference<>();

    private final PatriciaTrie<Boolean> suggestions = new PatriciaTrie<>();

    private AtomicBoolean trieUpdateInProgress = new AtomicBoolean(false);


    public InvertedIndexEntry() {
        this.lastBuiltTrie.set(trieBuilder.build());
    }

    public boolean addDocument(String name, String text) {
        if (name == null || text == null) {
            return false;
        }
        text = text.toLowerCase();
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                final String word = sb.toString();
                addText(name, word);
                trieUpdateQueue.add(word);
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        if (sb.length() > 0) {
            addText(name, sb.toString());
            trieUpdateQueue.add(sb.toString());
        }

        addWordsToTrie();

        return true;
    }

    public Map<String, AtomicLong> searchDocuments(String query) {
        if (query == null || query.isEmpty()) {
            throw new IllegalArgumentException();
        }

        Trie trie = lastBuiltTrie.get();
        if (trie == null) {
            return Collections.emptyMap();
        }

        Collection<Emit> emits = trie.parseText(query);

        Map<String, AtomicLong> merged = new HashMap<>();
        for (Emit emit : emits) {
            Map<String, AtomicLong> docs = indexedMap.get(emit.getKeyword());
            if (docs != null) {
                for (Map.Entry<String, AtomicLong> e : docs.entrySet()) {
                    merged.merge(e.getKey(), new AtomicLong(e.getValue().get()),
                            (a, b) -> { a.addAndGet(b.get()); return a; });
                }
            }
        }

        return merged;
    }

    public List<String> suggest(String prefix, int limit) {
        if (prefix == null || prefix.isEmpty()) return Collections.emptyList();

        return suggestions.prefixMap(prefix.toLowerCase())
                .keySet().stream()
                .limit(limit)
                .toList();
    }


    public int size() {
        return indexedMap.size();
    }

    public void addWordsToTrie() {
        try {
            while (!trieUpdateQueue.isEmpty()) {
                String polled = trieUpdateQueue.poll().toLowerCase();
                trieBuilder.addKeyword(polled);
                suggestions.put(polled, true);
            }
            this.lastBuiltTrie.set(trieBuilder.build());
        } finally {
            trieUpdateInProgress.set(false);
        }
    }

    private void addText(String name, String word) {
        indexedMap
                .computeIfAbsent(word, t -> new ConcurrentHashMap<>())
                .computeIfAbsent(name, n -> new AtomicLong(0L))
                .incrementAndGet();
    }
}
