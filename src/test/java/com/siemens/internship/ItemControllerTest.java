package com.siemens.internship;

import com.siemens.internship.controller.ItemController;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean ItemService itemService;

    @Test
    void createValidItem() throws Exception {
        mockMvc.perform(post("/items")
                        .content("{\"name\":\"Test\", \"email\":\"good@test.com\"}")
                        .contentType("application/json"))
                .andExpect(status().isCreated());
    }

    @Test
    void rejectInvalidEmail() throws Exception {
        mockMvc.perform(post("/items")
                        .content("{\"name\":\"Test\", \"email\":\"bad-email\"}")
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }
}