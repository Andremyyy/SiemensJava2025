package com.siemens.internship;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepo;

    @Test
    void saveAndFindItem() {
        Item saved = itemRepo.save(new Item(1L, "Test", "test@mail.com"));
        assertNotNull(itemRepo.findById(saved.getId()));
    }
}
