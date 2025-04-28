package com.mnb.controller;

import com.mnb.entity.Book;
import com.mnb.exception.NotFoundException;
import com.mnb.service.BookService;
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
class BookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private Book book;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();

        book = new Book();
        book.setId(1);
        book.setBookName("Effective Java");
        book.setIsbn("978-0134685991");
    }

    @Test
    void listBooks_ShouldReturnViewWithBooks() throws Exception {
        Book book2 = new Book();
        book2.setId(2);
        List<Book> books = Arrays.asList(book, book2);
        when(bookService.findAll()).thenReturn(books);

        mockMvc.perform(get("/books/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("list-books"))
                .andExpect(model().attribute("books", hasSize(2)))
                .andExpect(model().attribute("books", hasItem(
                        allOf(
                                hasProperty("id", is(1)),
                                hasProperty("bookName", is("Effective Java"))
                        )
                )));

        verify(bookService, times(1)).findAll();
    }

    @Test
    void showFormForAdd_ShouldReturnEmptyBookForm() throws Exception {
        mockMvc.perform(get("/books/showFormForAdd"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-form"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attribute("books", hasProperty("id", nullValue())));
    }

    @Test
    void showFormForUpdate_ShouldReturnPopulatedBookForm() throws Exception {
        when(bookService.findById(1)).thenReturn(book);

        mockMvc.perform(get("/books/showFormForUpdate").param("bookId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-form"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attribute("books",
                        allOf(
                                hasProperty("id", is(1)),
                                hasProperty("bookName", is("Effective Java"))
                        )
                ));

        verify(bookService, times(1)).findById(1);
    }

    @Test
    void showFormForUpdate_ShouldHandleNotFound() throws Exception {
        when(bookService.findById(99))
                .thenThrow(new NotFoundException("Book not found with ID 99"));

        mockMvc.perform(get("/books/showFormForUpdate").param("bookId", "99"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", is("Book not found with ID 99")));

        verify(bookService, times(1)).findById(99);
    }

    @Test
    void saveBook_ShouldRedirectToList() throws Exception {
        mockMvc.perform(post("/books/save")
                        .flashAttr("books", book))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/list"));

        verify(bookService, times(1)).save(book);
    }

    @Test
    void delete_ShouldRedirectToList() throws Exception {
        mockMvc.perform(get("/books/delete").param("bookId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/list"));

        verify(bookService, times(1)).deleteById(1);
    }

    @Test
    void search_ShouldReturnMatchingBooks() throws Exception {
        String keyword = "Java";
        Book matchingBook = new Book();
        matchingBook.setId(1);
        matchingBook.setBookName("Java Programming");

        when(bookService.findBookByName(keyword)).thenReturn(List.of(matchingBook));

        mockMvc.perform(get("/books/search").param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(view().name("list-books"))
                .andExpect(model().attribute("books", hasSize(1)))
                .andExpect(model().attribute("books",
                        hasItem(hasProperty("bookName", containsString("Java")))
                ));

        verify(bookService, times(1)).findBookByName(keyword);
    }

    @Test
    void search_ShouldReturnEmptyList_WhenNoMatches() throws Exception {
        String keyword = "Nonexistent";
        when(bookService.findBookByName(keyword)).thenReturn(List.of());

        mockMvc.perform(get("/books/search").param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(view().name("list-books"))
                .andExpect(model().attribute("books", empty()));

        verify(bookService, times(1)).findBookByName(keyword);
    }
}