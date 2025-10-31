package com.example.entries;

import com.example.SearchEntry;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ClusterEntry<T extends SearchEntry> implements SearchEntry {


    private final ExecutorService searchExecutor = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final int entriesSize;

    protected Map<Integer, T> active;

    // TODO: we should spill data on disk when size of frozen entries >= entriesSize
    protected Map<Integer, Queue<T>> frozen;

    public ClusterEntry(short entriesSize) {
        this.entriesSize = entriesSize;
        this.active = new ConcurrentHashMap<>(entriesSize);
        this.frozen = new ConcurrentHashMap<>(entriesSize);
    }

    @Override
    public boolean addDocument(String name, String text) {
        if (name == null) {
            throw new IllegalArgumentException();
        }

        int size = Math.max(active.size(), 1);
        int shardNumber = Math.abs(name.hashCode() % size);

        T entry = active.computeIfAbsent(shardNumber, k -> createEntry());

        if (entry.size() >= entriesSize) {
            frozen.computeIfAbsent(shardNumber, k -> new ConcurrentLinkedQueue<>()).add(entry);
            entry = createEntry();
            active.put(shardNumber, entry);
        }

        return entry.addDocument(name, text);
    }

    @Override
    public Map<String, AtomicLong> searchDocuments(String word) {
        Map<String, AtomicLong> searchActiveIndexes = searchInShard(active.values(), word);
        Map<String, AtomicLong> searchFrozenIndexes = searchInShard(frozen.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toList()), word);

        return Stream.concat(
                        searchActiveIndexes.entrySet().stream(),
                        searchFrozenIndexes.entrySet().stream()
                )
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new AtomicLong(e.getValue().get()),
                        (v1, v2) -> {
                            v1.addAndGet(v2.get());
                            return v1;
                        }
                ));
    }

    @Override
    public List<String> suggest(String prefix, int limit) {
        // TODO: should be optimized via paging + LRU cache
        List<T> allEntries = Stream.concat(
                active.values().stream(),
                frozen.values().stream().flatMap(Collection::stream)
        ).toList();

        List<Future<List<String>>> futures = allEntries.stream()
                .map(entry -> searchExecutor.submit(() -> entry.suggest(prefix, limit)))
                .toList();

        Set<String> resultSet = new LinkedHashSet<>();
        for (Future<List<String>> future : futures) {
            try {
                // blocking call
                resultSet.addAll(future.get());
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            if (resultSet.size() >= limit) break;
        }

        return resultSet.stream()
                .limit(limit)
                .toList();
    }

    @Override
    public int size() {
        return active.size();
    }

    abstract T createEntry();

    private Map<String, AtomicLong> searchInShard(Collection<T> values, String word) {
        // TODO: should be optimized via paging + LRU cache
        List<Future<Map<String, AtomicLong>>> futures = values.stream()
                .map(v -> searchExecutor.submit(() -> v.searchDocuments(word)))
                .toList();

        return futures.stream()
                .map(f -> {
                    try { return f.get(); }
                    catch (Exception e) { return Collections.<String, AtomicLong>emptyMap(); }
                })
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> { a.addAndGet(b.get()); return a; }
                ));
    }
}
