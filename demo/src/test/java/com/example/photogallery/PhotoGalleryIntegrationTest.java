package com.example.photogallery;

import com.example.photogallery.dto.LoginRequest;
import com.example.photogallery.dto.PhotoRequest;
import com.example.photogallery.dto.RefreshTokenRequest;
import com.example.photogallery.dto.RegisterRequest;
import com.example.photogallery.dto.TokenResponse;
import com.example.photogallery.repository.DeviceSessionRepository;
import com.example.photogallery.repository.PhotoRepository;
import com.example.photogallery.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class PhotoGalleryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("photogallery_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceSessionRepository deviceSessionRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @BeforeEach
    void clearDatabase() {
        photoRepository.deleteAll();
        deviceSessionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Проверка защиты: запрос без Bearer токена возвращает 401 Unauthorized")
    void testUnauthorizedAccessToPhotos() throws Exception {
        mockMvc.perform(get("/api/photos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("2. Вариант 7: Мульти-девайс вход, генерация сессий устройств в PostgreSQL и создание фото")
    void testMultiDeviceLoginAndPhotoCreation() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("photographer");
        register.setPassword("securePassword123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        LoginRequest laptopLogin = new LoginRequest();
        laptopLogin.setUsername("photographer");
        laptopLogin.setPassword("securePassword123");
        laptopLogin.setDeviceId("laptop-macbook");

        MvcResult laptopResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(laptopLogin)))
                .andExpect(status().isOk())
                .andReturn();

        TokenResponse laptopTokens = objectMapper.readValue(
                laptopResult.getResponse().getContentAsString(),
                TokenResponse.class
        );

        LoginRequest phoneLogin = new LoginRequest();
        phoneLogin.setUsername("photographer");
        phoneLogin.setPassword("securePassword123");
        phoneLogin.setDeviceId("phone-android");

        MvcResult phoneResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(phoneLogin)))
                .andExpect(status().isOk())
                .andReturn();

        TokenResponse phoneTokens = objectMapper.readValue(
                phoneResult.getResponse().getContentAsString(),
                TokenResponse.class
        );

        assertEquals(2, deviceSessionRepository.count(),
                "В БД должно быть ровно две активные сессии для разных устройств одного пользователя");

        PhotoRequest photoRequest = new PhotoRequest();
        photoRequest.setTitle("Северное сияние");
        photoRequest.setDescription("Снято за полярным кругом");
        photoRequest.setAuthor("photographer");
        photoRequest.setImageUrl("https://example.com/aurora.jpg");

        mockMvc.perform(post("/api/photos")
                        .header("Authorization", "Bearer " + laptopTokens.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(photoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Северное сияние"))
                .andExpect(jsonPath("$.author").value("photographer"));

        mockMvc.perform(get("/api/photos")
                        .header("Authorization", "Bearer " + phoneTokens.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Северное сияние"));

        RefreshTokenRequest logoutRequest = new RefreshTokenRequest();
        logoutRequest.setRefreshToken(laptopTokens.getRefreshToken());

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk());

        assertEquals(1, deviceSessionRepository.count(),
                "После логаута ноутбука должна остаться сессия смартфона");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isUnauthorized());
    }
}