package com.mnb;

import com.mnb.entity.Author;
import com.mnb.entity.Book;
import com.mnb.repository.AuthorRepository;
import com.mnb.service.AuthorServiceImpl;
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
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorServiceImpl authorService;

    private Author author;
    private Book book;

    @BeforeEach
    void setUp() {
        author = new Author();
        author.setId(1);
        author.setAuthorName("J.K. Rowling");
        author.setDescription("Author of Harry Potter series");

        book = new Book();
        book.setId(1);
        book.setBookName("Harry Potter and the Philosopher's Stone");
    }

    @Test
    void findAll_ShouldReturnAllAuthors() {
        Author author2 = new Author();
        author2.setId(2);
        List<Author> expectedAuthors = Arrays.asList(author, author2);
        when(authorRepository.findAll()).thenReturn(expectedAuthors);

        List<Author> result = authorService.findAll();

        assertEquals(2, result.size());
        assertTrue(result.containsAll(expectedAuthors));
        verify(authorRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnAuthor_WhenAuthorExists() {
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));

        Author result = authorService.findById(1);

        assertNotNull(result);
        assertEquals(author.getId(), result.getId());
        assertEquals(author.getAuthorName(), result.getAuthorName());
        verify(authorRepository, times(1)).findById(1);
    }

    @Test
    void findById_ShouldThrowException_WhenAuthorNotFound() {
        when(authorRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authorService.findById(99);
        });
        assertEquals("Did not find author id - 99", exception.getMessage());
        verify(authorRepository, times(1)).findById(99);
    }

    @Test
    void save_ShouldCallRepositorySave() {
        Author newAuthor = new Author();
        newAuthor.setAuthorName("George R.R. Martin");

        authorService.save(newAuthor);

        verify(authorRepository, times(1)).save(newAuthor);
    }

    @Test
    void deleteById_ShouldCallRepositoryDelete() {
        authorService.deleteById(1);

        verify(authorRepository, times(1)).deleteById(1);
    }

    @Test
    void addBook_ShouldAddBookToAuthor_WhenAuthorExists() {
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));
        int initialBookCount = author.getBooksList().size();

        authorService.addBook(author, book);

        assertEquals(initialBookCount + 1, author.getBooksList().size());
        assertTrue(author.getBooksList().contains(book));
        verify(authorRepository, times(1)).findById(1);
    }

    @Test
    void addBook_ShouldNotAddBook_WhenAuthorDoesNotExist() {
        Author nonExistingAuthor = new Author();
        nonExistingAuthor.setId(99);
        when(authorRepository.findById(99)).thenReturn(Optional.empty());

        authorService.addBook(nonExistingAuthor, book);

        verify(authorRepository, times(1)).findById(99);
    }

    @Test
    void getAuthor_ShouldReturnAuthor_WhenAuthorExists() {
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));

        Optional<Author> result = authorService.getAuthor(1);

        assertTrue(result.isPresent());
        assertEquals(author, result.get());
        verify(authorRepository, times(1)).findById(1);
    }

    @Test
    void getAuthor_ShouldReturnEmptyOptional_WhenAuthorDoesNotExist() {
        when(authorRepository.findById(99)).thenReturn(Optional.empty());

        Optional<Author> result = authorService.getAuthor(99);

        assertFalse(result.isPresent());
        verify(authorRepository, times(1)).findById(99);
    }

    @Test
    void addBook_ShouldMaintainBidirectionalRelationship() {
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));

        authorService.addBook(author, book);

        assertTrue(author.getBooksList().contains(book));
        assertEquals(author, book.getAuthor());
    }

    @Test
    void addBook_ShouldInitializeBooksListIfNull() {
        author.setBooksList(null);
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));

        authorService.addBook(author, book);

        assertNotNull(author.getBooksList());
        assertEquals(1, author.getBooksList().size());
        assertTrue(author.getBooksList().contains(book));
        verify(authorRepository, times(1)).findById(1);
    }
}