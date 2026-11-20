package com.example.photogallery.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на частичное обновление информации о фотографии")
public class PhotoPatchRequest {

    @Schema(description = "Новое название фотографии", example = "Ночное небо")
    private String title;

    @Schema(description = "Новое описание фотографии", example = "Обновленное описание")
    private String description;

    @Schema(description = "Новый автор", example = "Алексей Смирнов")
    private String author;

    @Schema(description = "Новая ссылка на изображение", example = "https://example.com/photos/night.jpg")
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