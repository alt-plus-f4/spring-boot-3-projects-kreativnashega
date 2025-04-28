package com.mnb;

import com.mnb.controller.PublisherController;
import com.mnb.entity.Publisher;
import com.mnb.exception.NotFoundException;
import com.mnb.service.PublisherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Arrays;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(MockitoExtension.class)
class PublisherControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PublisherService publisherService;

    @InjectMocks
    private PublisherController publisherController;

    private Publisher publisher;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(publisherController).build();

        publisher = new Publisher();
        publisher.setId(1);
        publisher.setPublisherName("Penguin Books");
        publisher.setDescription("Major international publisher");
    }

    @Test
    void listPublishers_ShouldReturnViewWithPublishers() throws Exception {
        Publisher publisher2 = new Publisher();
        publisher2.setId(2);
        List<Publisher> publishers = Arrays.asList(publisher, publisher2);

        when(publisherService.findAll()).thenReturn(publishers);

        mockMvc.perform(get("/publisher/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("list-publishers"))
                .andExpect(model().attribute("publishers", hasSize(2)))
                .andExpect(model().attribute("publishers", hasItem(
                        allOf(
                                hasProperty("id", is(1)),
                                hasProperty("publisherName", is("Penguin Books"))
                        )
                )));

        verify(publisherService, times(1)).findAll();
    }

    @Test
    void showFormForAdd_ShouldReturnEmptyPublisherForm() throws Exception {
        mockMvc.perform(get("/publisher/showFormForAdd"))
                .andExpect(status().isOk())
                .andExpect(view().name("publisher-form"))
                .andExpect(model().attributeExists("publishers"))
                .andExpect(model().attribute("publishers", hasProperty("id", nullValue())));
    }

    @Test
    void showFormForUpdate_ShouldReturnPopulatedPublisherForm() throws Exception {
        when(publisherService.findById(1)).thenReturn(publisher);

        mockMvc.perform(get("/publisher/showFormForUpdate").param("publisherId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("publisher-form"))
                .andExpect(model().attributeExists("publishers"))
                .andExpect(model().attribute("publishers",
                        allOf(
                                hasProperty("id", is(1)),
                                hasProperty("publisherName", is("Penguin Books"))
                        )
                ));

        verify(publisherService, times(1)).findById(1);
    }

    @Test
    void showFormForUpdate_ShouldHandleNotFound() throws Exception {
        when(publisherService.findById(99))
                .thenThrow(new NotFoundException("Publisher not found with ID 99"));

        mockMvc.perform(get("/publisher/showFormForUpdate").param("publisherId", "99"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", is("Publisher not found with ID 99")));

        verify(publisherService, times(1)).findById(99);
    }

    @Test
    void savePublisher_ShouldRedirectToList() throws Exception {
        mockMvc.perform(post("/publisher/save")
                        .flashAttr("publishers", publisher))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/publisher/list"));

        verify(publisherService, times(1)).save(publisher);
    }

    @Test
    void delete_ShouldRedirectToList() throws Exception {
        mockMvc.perform(get("/publisher/delete").param("publisherId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/publisher/list"));

        verify(publisherService, times(1)).deleteById(1);
    }

    @Test
    void listPublishers_ShouldHandleEmptyList() throws Exception {
        when(publisherService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/publisher/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("list-publishers"))
                .andExpect(model().attribute("publishers", empty()));

        verify(publisherService, times(1)).findAll();
    }

    @Test
    void savePublisher_ShouldHandleNullAttributes() throws Exception {
        Publisher emptyPublisher = new Publisher();
        emptyPublisher.setPublisherName(null);
        emptyPublisher.setDescription(null);

        mockMvc.perform(post("/publisher/save")
                        .flashAttr("publishers", emptyPublisher))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/publisher/list"));

        verify(publisherService, times(1)).save(emptyPublisher);
    }
}