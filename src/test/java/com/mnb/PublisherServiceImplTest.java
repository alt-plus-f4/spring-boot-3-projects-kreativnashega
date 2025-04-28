package com.mnb;

import com.mnb.entity.Publisher;
import com.mnb.exception.NotFoundException;
import com.mnb.repository.PublisherRepository;
import com.mnb.service.PublisherServiceImpl;
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
class PublisherServiceImplTest {

    @Mock
    private PublisherRepository publisherRepository;

    @InjectMocks
    private PublisherServiceImpl publisherService;

    private Publisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new Publisher();
        publisher.setId(1);
        publisher.setPublisherName("Penguin Books");
        publisher.setDescription("Major international publisher");
    }

    @Test
    void findAll_ShouldReturnAllPublishers() {
        Publisher publisher2 = new Publisher();
        publisher2.setId(2);
        List<Publisher> expectedPublishers = Arrays.asList(publisher, publisher2);
        when(publisherRepository.findAll()).thenReturn(expectedPublishers);

        List<Publisher> result = publisherService.findAll();

        assertEquals(2, result.size());
        assertTrue(result.containsAll(expectedPublishers));
        verify(publisherRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnPublisher_WhenPublisherExists() {
        when(publisherRepository.findById(1)).thenReturn(Optional.of(publisher));

        Publisher result = publisherService.findById(1);

        assertNotNull(result);
        assertEquals(publisher.getId(), result.getId());
        assertEquals(publisher.getPublisherName(), result.getPublisherName());
        assertEquals(publisher.getDescription(), result.getDescription());
        verify(publisherRepository, times(1)).findById(1);
    }

    @Test
    void findById_ShouldThrowNotFoundException_WhenPublisherDoesNotExist() {
        int nonExistentId = 99;
        when(publisherRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            publisherService.findById(nonExistentId);
        });

        assertEquals("Publisher not found  with ID 99", exception.getMessage());
        verify(publisherRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void save_ShouldCallRepositorySave() {
        Publisher newPublisher = new Publisher();
        newPublisher.setPublisherName("New Publisher");

        publisherService.save(newPublisher);

        verify(publisherRepository, times(1)).save(newPublisher);
    }

    @Test
    void save_ShouldUpdateExistingPublisher() {
        Publisher updatedPublisher = new Publisher();
        updatedPublisher.setId(1);
        updatedPublisher.setPublisherName("Updated Name");
        updatedPublisher.setDescription("Updated Description");

        publisherService.save(updatedPublisher);

        verify(publisherRepository, times(1)).save(updatedPublisher);
    }

    @Test
    void deleteById_ShouldCallRepositoryDelete() {
        publisherService.deleteById(1);

        verify(publisherRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteById_ShouldNotThrowException_WhenPublisherDoesNotExist() {
        doNothing().when(publisherRepository).deleteById(99);

        assertDoesNotThrow(() -> {
            publisherService.deleteById(99);
        });
        verify(publisherRepository, times(1)).deleteById(99);
    }

    @Test
    void save_ShouldPersistPublisherWithNullDescription() {
        Publisher publisherWithoutDescription = new Publisher();
        publisherWithoutDescription.setPublisherName("No Description Publisher");
        publisherWithoutDescription.setDescription(null);

        publisherService.save(publisherWithoutDescription);

        verify(publisherRepository, times(1)).save(publisherWithoutDescription);
    }

    @Test
    void save_ShouldPersistPublisherWithEmptyName() {
        Publisher emptyNamePublisher = new Publisher();
        emptyNamePublisher.setPublisherName("");
        emptyNamePublisher.setDescription("Empty name publisher");

        publisherService.save(emptyNamePublisher);

        verify(publisherRepository, times(1)).save(emptyNamePublisher);
    }
}
