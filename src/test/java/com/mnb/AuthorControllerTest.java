package com.mnb;

import com.mnb.controller.AuthorController;
import com.mnb.entity.Author;
import com.mnb.service.AuthorService;
import com.mnb.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(MockitoExtension.class)
class AuthorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthorService authorService;

    @Mock
    private BookService bookService;

    @InjectMocks
    private AuthorController authorController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authorController).build();
    }

    @Test
    void listAuthors_ShouldReturnViewWithAuthors() throws Exception {
        Author author1 = new Author();
        author1.setId(1);
        Author author2 = new Author();
        author2.setId(2);
        List<Author> authors = Arrays.asList(author1, author2);

        when(authorService.findAll()).thenReturn(authors);

        mockMvc.perform(get("/author/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("list-authors"))
                .andExpect(model().attribute("authors", hasSize(2)))
                .andExpect(model().attribute("authors", hasItem(
                        allOf(
                                hasProperty("id", is(1))
                        )
                )));

        verify(authorService, times(1)).findAll();
    }

    @Test
    void showFormForAdd_ShouldReturnEmptyAuthorForm() throws Exception {
        mockMvc.perform(get("/author/showFormForAdd"))
                .andExpect(status().isOk())
                .andExpect(view().name("author-form"))
                .andExpect(model().attributeExists("authors"))
                .andExpect(model().attribute("authors", hasProperty("id", nullValue())));
    }

    @Test
    void showFormForUpdate_ShouldReturnPopulatedAuthorForm() throws Exception {
        Author author = new Author();
        author.setId(1);
        author.setAuthorName("Test Author");

        when(authorService.findById(1)).thenReturn(author);

        mockMvc.perform(get("/author/showFormForUpdate").param("authorId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("author-form"))
                .andExpect(model().attributeExists("authors"))
                .andExpect(model().attribute("authors",
                        allOf(
                                hasProperty("id", is(1)),
                                hasProperty("authorName", is("Test Author"))
                        )
                ));

        verify(authorService, times(1)).findById(1);
    }

    @Test
    void saveAuthor_ShouldRedirectToList() throws Exception {
        Author author = new Author();
        author.setId(1);
        author.setAuthorName("Saved Author");

        mockMvc.perform(post("/author/save")
                        .flashAttr("authors", author))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/author/list"));

        verify(authorService, times(1)).save(author);
    }

    @Test
    void delete_ShouldRedirectToList() throws Exception {
        mockMvc.perform(get("/author/delete").param("authorId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/author/list"));

        verify(authorService, times(1)).deleteById(1);
    }

    @Test
    void showFormForUpdate_ShouldHandleNotFound() throws Exception {
        // Arrange
        when(authorService.findById(99))
                .thenThrow(new RuntimeException("Author not found"));

        // Act & Assert
        mockMvc.perform(get("/author/showFormForUpdate").param("authorId", "99"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", is("Author not found")));

        verify(authorService, times(1)).findById(99);
    }
}