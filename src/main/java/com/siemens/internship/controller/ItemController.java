package com.siemens.internship.controller;

import com.siemens.internship.service.ItemService;
import com.siemens.internship.exceptions.ProcessingException;
import com.siemens.internship.model.Item;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    // Constructor injection
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody Item item, BindingResult result) {
        // 1. Validare manuală
        if (result.hasErrors()) {
            // Colectează toate mesajele de eroare
            List<String> errors = result.getFieldErrors()
                    .stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());

            return ResponseEntity.badRequest().body(errors);
        }

        // 2. Validare suplimentară (opțională)
        if (itemService.emailExists(item.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        // 3. Salvare și răspuns
        try {
            Item savedItem = itemService.save(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to save item");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

//    @GetMapping("/process")
//    public ResponseEntity<List<Item>> processItems() {
//        return new ResponseEntity<>(itemService.processItemsAsync(), HttpStatus.OK);
//    }

    /**
     * Endpoint for asynchronous item processing
     * @param itemIds List of item IDs to process
     * @return ResponseEntity with list of processed IDs
     */
    @PostMapping("/process-async")
    public ResponseEntity<?> processItemsAsync(@RequestBody List<Long> itemIds) {
        try {
            if (itemIds == null || itemIds.isEmpty()) {
                return ResponseEntity.badRequest().body("Item list cannot be empty");
            }

            List<Long> processedIds = itemService.processItemsAsync(itemIds);

            if (processedIds.isEmpty()) {
                return ResponseEntity.unprocessableEntity()
                        .body("No items were processed successfully");
            }

            return ResponseEntity.ok(processedIds);

        } catch (ProcessingException e) {
            return ResponseEntity.internalServerError()
                    .body("Processing failed: " + e.getMessage());
        }
    }
}
