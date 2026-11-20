package com.example.photogallery;

import com.example.photogallery.dto.*;
import com.example.photogallery.repository.DeviceSessionRepository;
import com.example.photogallery.repository.PhotoRepository;
import com.example.photogallery.repository.UserRepository;
import com.example.photogallery.utils.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
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
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("jwt.secret", () -> "test-super-secret-key-that-is-at-least-256-bits-long-hs256!");
        registry.add("jwt.access-token-expiration-ms", () -> "900000");
        registry.add("jwt.refresh-token-expiration-ms", () -> "604800000");
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceSessionRepository deviceSessionRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @BeforeEach
    void setUp() {
        // Надежная ручная сборка MockMvc с фильтрами безопасности Spring Security
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .addFilter(jwtAuthenticationFilter)
                .build();

        photoRepository.deleteAll();
        deviceSessionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Проверка middleware: запрос без токена к закрытым маршрутам возвращает 401")
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/photos"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("2. Полный сценарий Варианта 7: Мульти-девайс, создание фото и изоляция сессий")
    void testFullMultiDeviceWorkflow() throws Exception {
        // Шаг 1: Регистрация пользователя
        RegisterRequest register = new RegisterRequest();
        register.setUsername("photographer");
        register.setPassword("strongPassword123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        // Шаг 2: Вход с Устройства 1 (Ноутбук)
        LoginRequest laptopLogin = new LoginRequest();
        laptopLogin.setUsername("photographer");
        laptopLogin.setPassword("strongPassword123");
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

        // Шаг 3: Вход с Устройства 2 (Телефон)
        LoginRequest phoneLogin = new LoginRequest();
        phoneLogin.setUsername("photographer");
        phoneLogin.setPassword("strongPassword123");
        phoneLogin.setDeviceId("phone-pixel");

        MvcResult phoneResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(phoneLogin)))
                .andExpect(status().isOk())
                .andReturn();

        TokenResponse phoneTokens = objectMapper.readValue(
                phoneResult.getResponse().getContentAsString(),
                TokenResponse.class
        );

        assertEquals(2, deviceSessionRepository.count(), "Должно быть 2 активные сессии для разных устройств");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + laptopTokens.getAccessToken()))
                .andExpect(status().isOk());

        PhotoRequest photoRequest = new PhotoRequest();
        photoRequest.setTitle("Северное сияние");
        photoRequest.setDescription("Полярная ночь");
        photoRequest.setAuthor("photographer");
        photoRequest.setImageUrl("https://example.com/aurora.jpg");

        mockMvc.perform(post("/api/photos")
                        .header("Authorization", "Bearer " + laptopTokens.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(photoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Северное сияние"));

        mockMvc.perform(get("/api/photos")
                        .header("Authorization", "Bearer " + phoneTokens.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(laptopTokens.getRefreshToken());

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andReturn();

        TokenResponse newLaptopTokens = objectMapper.readValue(
                refreshResult.getResponse().getContentAsString(),
                TokenResponse.class
        );

        mockMvc.perform(get("/api/photos")
                        .header("Authorization", "Bearer " + laptopTokens.getAccessToken()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/photos")
                        .header("Authorization", "Bearer " + newLaptopTokens.getAccessToken()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/photos")
                        .header("Authorization", "Bearer " + phoneTokens.getAccessToken()))
                .andExpect(status().isOk());

        RefreshTokenRequest logoutRequest = new RefreshTokenRequest();
        logoutRequest.setRefreshToken(newLaptopTokens.getRefreshToken());

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk());

        assertEquals(1, deviceSessionRepository.count(), "После выхода ноутбука должна остаться сессия смартфона");
    }
}