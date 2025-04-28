package com.mnb;

import com.mnb.entity.Author;
import com.mnb.entity.Book;
import com.mnb.entity.Publisher;
import com.mnb.exception.NotFoundException;
import com.mnb.repository.BookRepository;
import com.mnb.service.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1);
        book.setBookName("The Lord of the Rings");
        book.setIsbn("978-0544003415");
        book.setBooksAuthor("J.R.R. Tolkien");
    }

    @Test
    void findAll_ShouldReturnAllBooks() {
        Book book2 = new Book();
        book2.setId(2);
        List<Book> expectedBooks = Arrays.asList(book, book2);
        when(bookRepository.findAll()).thenReturn(expectedBooks);

        List<Book> result = bookService.findAll();

        assertEquals(2, result.size());
        assertTrue(result.containsAll(expectedBooks));
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnBook_WhenBookExists() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        Book result = bookService.findById(1);

        assertNotNull(result);
        assertEquals(book.getId(), result.getId());
        assertEquals(book.getBookName(), result.getBookName());
        verify(bookRepository, times(1)).findById(1);
    }

    @Test
    void findById_ShouldThrowNotFoundException_WhenBookDoesNotExist() {
        when(bookRepository.findById(99)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookService.findById(99);
        });
        assertEquals(" not found  with ID 99", exception.getMessage());
        verify(bookRepository, times(1)).findById(99);
    }

    @Test
    void save_ShouldCallRepositorySave() {
        Book newBook = new Book();
        newBook.setBookName("New Book");

        bookService.save(newBook);

        verify(bookRepository, times(1)).save(newBook);
    }

    @Test
    void deleteById_ShouldCallRepositoryDelete() {
        bookService.deleteById(1);

        verify(bookRepository, times(1)).deleteById(1);
    }

    @Test
    void findBookByName_ShouldReturnMatchingBooks() {
        String keyword = "Ring";
        Book matchingBook1 = new Book();
        matchingBook1.setId(1);
        matchingBook1.setBookName("The Lord of the Rings");

        Book matchingBook2 = new Book();
        matchingBook2.setId(2);
        matchingBook2.setIsbn("123-RING-456");

        List<Book> expectedBooks = Arrays.asList(matchingBook1, matchingBook2);
        when(bookRepository.findByName(keyword)).thenReturn(expectedBooks);

        List<Book> result = bookService.findBookByName(keyword);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(b -> b.getBookName().contains(keyword)));
        verify(bookRepository, times(1)).findByName(keyword);
    }

    @Test
    void findBookByName_ShouldReturnEmptyList_WhenNoMatchesFound() {
        String keyword = "Nonexistent";
        when(bookRepository.findByName(keyword)).thenReturn(List.of());

        List<Book> result = bookService.findBookByName(keyword);

        assertTrue(result.isEmpty());
        verify(bookRepository, times(1)).findByName(keyword);
    }

    @Test
    void save_ShouldPersistAuthorRelationship() {
        Author author = new Author();
        author.setId(1);
        book.setAuthor(author);

        bookService.save(book);

        verify(bookRepository, times(1)).save(book);
        assertEquals(author, book.getAuthor());
    }

    @Test
    void save_ShouldPersistPublisherRelationship() {
        Publisher publisher = new Publisher();
        publisher.setId(1);
        book.setPublisher(publisher);

        bookService.save(book);

        verify(bookRepository, times(1)).save(book);
        assertEquals(publisher, book.getPublisher());
    }
}