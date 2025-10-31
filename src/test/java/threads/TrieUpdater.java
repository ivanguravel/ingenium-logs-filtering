package threads;


import com.example.entries.InvertedIndexEntry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrieUpdater {

    public static final TrieUpdater INSTANCE = new TrieUpdater();

    private final ExecutorService executorService;

    private TrieUpdater() {
        this.executorService = Executors
                .newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdownNow));
    }

    public void updateEventually(InvertedIndexEntry invertedIndexEntry) {
        executorService.submit(
                invertedIndexEntry::addWordsToTrie);
    }
}
