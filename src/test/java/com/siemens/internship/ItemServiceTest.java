package com.siemens.internship;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    ItemRepository itemRepo;
    @InjectMocks
    ItemService itemService;

    @Test
    void processItemsCorrectly() {
        when(itemRepo.findById(1L)).thenReturn(Optional.of(new Item()));
        when(itemRepo.findById(2L)).thenReturn(Optional.empty());

        List<Long> result = itemService.processItemsAsync(List.of(1L, 2L));

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0));
    }
}
