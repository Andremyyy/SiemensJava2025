package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import com.siemens.internship.exceptions.*;
import com.siemens.internship.model.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private List<Item> processedItems = new ArrayList<>();
    private int processedCount = 0;
    private static final Logger log = LoggerFactory.getLogger(ItemService.class);


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */

    /**
     * Processes all items asynchronously with proper tracking and error handling
     * Uses CompletableFuture for parallel processing with controlled concurrency
     *
     * @param itemIds List of item IDs to process
     * @return List of successfully processed items' IDs
     * @throws ProcessingException if major failure occurs during processing
     */
    public List<Long> processItemsAsync(List<Long> itemIds) {
        // Validate input first
        if (itemIds == null || itemIds.isEmpty()) {
            log.warn("Empty item list provided for processing");
            return Collections.emptyList();
        }

        // Create executor with reasonable thread pool size
        int threadPoolSize = Math.min(itemIds.size(), 10); // Don't exceed 10 threads
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        // List to store all futures for tracking
        List<CompletableFuture<Optional<Long>>> processingFutures = new ArrayList<>();

        // Process each item in parallel
        for (Long id : itemIds) {
            CompletableFuture<Optional<Long>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    Item item = itemRepository.findById(id)
                            .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + id));

                    log.debug("Processing item {}", id);
                    updateItemStatus(item, Status.PROCESSING);

                    // Simulate processing (in real app this would be business logic)
                    simulateProcessing();

                    updateItemStatus(item, Status.COMPLETED);
                    log.info("Successfully processed item {}", id);
                    return Optional.of(id);
                } catch (Exception e) {
                    log.error("Failed to process item {}: {}", id, e.getMessage());
                    return Optional.empty();
                }
            }, executor);

            processingFutures.add(future);
        }

        // Combine all futures and wait for completion
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                processingFutures.toArray(new CompletableFuture[0])
        );

        try {
            // Wait with timeout to prevent hanging
            allFutures.get(2, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            log.error("Timeout while processing items", e);
            throw new ProcessingException("Processing timeout exceeded");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Processing interrupted", e);
            throw new ProcessingException("Processing interrupted", e);
        } catch (ExecutionException e) {
            log.error("Error during processing", e);
            throw new ProcessingException("Error during processing", e);
        } finally {
            executor.shutdown(); // Always shutdown executor
        }

        // Collect successfully processed IDs
        return processingFutures.stream()
                .map(CompletableFuture::join)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    // Helper method for status update
    private void updateItemStatus(Item item, Status status) {
        item.setStatus(status);
        item.setLastUpdated(LocalDateTime.now());
        itemRepository.save(item);
    }

    // Helper method to simulate processing
    private void simulateProcessing() throws InterruptedException {
        // Random delay between 0.5-2 seconds
        long delay = 500 + (long) (Math.random() * 1500);
        Thread.sleep(delay);
    }

    public boolean emailExists(String email) {
        return itemRepository.existsByEmail(email);
    }

}

