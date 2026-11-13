package com.example.photogallery.service;

import com.example.photogallery.dto.PhotoPatchRequest;
import com.example.photogallery.dto.PhotoRequest;
import com.example.photogallery.entity.Photo;
import com.example.photogallery.repository.PhotoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;

    public PhotoService(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public List<Photo> getAllPhotos() {
        return photoRepository.findAll();
    }

    public Photo getPhotoById(Long id) {
        return photoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Фотография с id " + id + " не найдена"
                ));
    }

    public Photo createPhoto(PhotoRequest request) {
        Photo photo = new Photo(
                request.getTitle(),
                request.getDescription(),
                request.getAuthor(),
                request.getImageUrl()
        );
        return photoRepository.save(photo);
    }

    public Photo updatePhoto(Long id, PhotoRequest request) {
        Photo photo = getPhotoById(id);
        photo.setTitle(request.getTitle());
        photo.setDescription(request.getDescription());
        photo.setAuthor(request.getAuthor());
        photo.setImageUrl(request.getImageUrl());
        return photoRepository.save(photo);
    }

    public Photo patchPhoto(Long id, PhotoPatchRequest request) {
        Photo photo = getPhotoById(id);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            photo.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            photo.setDescription(request.getDescription());
        }
        if (request.getAuthor() != null && !request.getAuthor().isBlank()) {
            photo.setAuthor(request.getAuthor());
        }
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            photo.setImageUrl(request.getImageUrl());
        }

        return photoRepository.save(photo);
    }

    public void deletePhoto(Long id) {
        Photo photo = getPhotoById(id);
        photoRepository.delete(photo);
    }
}