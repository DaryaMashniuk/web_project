package com.example.photogallery.controller;

import com.example.photogallery.dto.PhotoPatchRequest;
import com.example.photogallery.dto.PhotoRequest;
import com.example.photogallery.entity.Photo;
import com.example.photogallery.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @Operation(summary = "Получить все фотографии", description = "Возвращает список всех фотографий из галереи")
    @ApiResponse(responseCode = "200", description = "Список фотографий успешно получен")
    @GetMapping
    public ResponseEntity<List<Photo>> getAllPhotos() {
        return ResponseEntity.ok(photoService.getAllPhotos());
    }

    @Operation(summary = "Получить фотографию по ID", description = "Возвращает информацию об одной фотографии по её идентификатору")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Фотография найдена"),
            @ApiResponse(responseCode = "404", description = "Фотография не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Photo> getPhotoById(
            @Parameter(description = "ID фотографии", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(photoService.getPhotoById(id));
    }

    @Operation(summary = "Добавить новую фотографию", description = "Создаёт (загружает) запись о новой фотографии в фотогалерею")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Фотография успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные")
    })
    @PostMapping
    public ResponseEntity<Photo> createPhoto(@Valid @RequestBody PhotoRequest request) {
        Photo created = photoService.createPhoto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Полностью обновить фотографию", description = "Перезаписывает все поля информации о выбранной фотографии")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Фотография успешно обновлена"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
            @ApiResponse(responseCode = "404", description = "Фотография не найдена")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Photo> updatePhoto(
            @Parameter(description = "ID фотографии", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody PhotoRequest request) {
        return ResponseEntity.ok(photoService.updatePhoto(id, request));
    }

    @Operation(summary = "Частично обновить фотографию", description = "Обновляет только переданные поля фотографии")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Фотография успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Фотография не найдена")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Photo> patchPhoto(
            @Parameter(description = "ID фотографии", example = "1")
            @PathVariable Long id,
            @RequestBody PhotoPatchRequest request) {
        return ResponseEntity.ok(photoService.patchPhoto(id, request));
    }

    @Operation(summary = "Удалить фотографию", description = "Удаляет фотографию из базы данных по её ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Фотография успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Фотография не найдена")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(
            @Parameter(description = "ID фотографии", example = "1")
            @PathVariable Long id) {
        photoService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Photo> uploadPhotoFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("author") String author) throws IOException {

        Path uploadDir = Paths.get("uploads");
        Files.createDirectories(uploadDir);

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        PhotoRequest request = new PhotoRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setAuthor(author);
        request.setImageUrl("/uploads/" + fileName);

        return ResponseEntity.status(HttpStatus.CREATED).body(photoService.createPhoto(request));
    }
}