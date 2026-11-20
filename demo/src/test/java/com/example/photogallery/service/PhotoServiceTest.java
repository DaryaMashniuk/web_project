package com.example.photogallery.service;

import com.example.photogallery.dto.PhotoPatchRequest;
import com.example.photogallery.dto.PhotoRequest;
import com.example.photogallery.entity.Photo;
import com.example.photogallery.repository.PhotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock
    private PhotoRepository photoRepository;

    @InjectMocks
    private PhotoService photoService;

    private Photo testPhoto;

    @BeforeEach
    void setUp() {
        testPhoto = new Photo("Закат", "Тестовое описание", "Иван", "https://example.com/sunset.jpg");
        testPhoto.setId(1L);
    }

    @Test
    @DisplayName("Unit-тест: Создание новой фотографии")
    void createPhoto_Success() {
        PhotoRequest request = new PhotoRequest();
        request.setTitle("Рассвет");
        request.setDescription("Утро в горах");
        request.setAuthor("Анна");
        request.setImageUrl("https://example.com/sunrise.jpg");

        when(photoRepository.save(any(Photo.class))).thenReturn(testPhoto);

        Photo created = photoService.createPhoto(request);

        assertNotNull(created);
        verify(photoRepository, times(1)).save(any(Photo.class));
    }

    @Test
    @DisplayName("Unit-тест: Поиск фото по несуществующему ID вызывает 404")
    void getPhotoById_NotFound() {
        when(photoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> photoService.getPhotoById(999L));
    }

    @Test
    @DisplayName("Unit-тест: Частичное обновление фото (PATCH)")
    void patchPhoto_Success() {
        when(photoRepository.findById(1L)).thenReturn(Optional.of(testPhoto));
        when(photoRepository.save(any(Photo.class))).thenReturn(testPhoto);

        PhotoPatchRequest patchRequest = new PhotoPatchRequest();
        patchRequest.setTitle("Обновленный закат");

        Photo updated = photoService.patchPhoto(1L, patchRequest);

        assertEquals("Обновленный закат", updated.getTitle());
        verify(photoRepository).save(testPhoto);
    }

    @Test
    @DisplayName("Unit-тест: Удаление фото")
    void deletePhoto_Success() {
        when(photoRepository.findById(1L)).thenReturn(Optional.of(testPhoto));
        doNothing().when(photoRepository).delete(testPhoto);

        photoService.deletePhoto(1L);

        verify(photoRepository, times(1)).delete(testPhoto);
    }
}