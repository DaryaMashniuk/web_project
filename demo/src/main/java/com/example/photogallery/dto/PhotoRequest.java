package com.example.photogallery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на создание или полное обновление фотографии")
public class PhotoRequest {

    @Schema(description = "Название фотографии", example = "Закат на море")
    @NotBlank(message = "Название фотографии обязательно")
    private String title;

    @Schema(description = "Описание фотографии", example = "Снимок сделан летом на побережье")
    private String description;

    @Schema(description = "Автор фотографии", example = "Иван Петров")
    @NotBlank(message = "Автор обязателен")
    private String author;

    @Schema(description = "URL-ссылка на изображение", example = "https://example.com/photos/sunset.jpg")
    @NotBlank(message = "Ссылка на фотографию обязательна")
    private String imageUrl;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}