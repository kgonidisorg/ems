package com.ecogrid.ems.auth.integration;

import com.ecogrid.ems.auth.dto.RegisterRequest;
import com.ecogrid.ems.auth.entity.User;
import com.ecogrid.ems.auth.repository.UserRepository;
import com.ecogrid.ems.shared.dto.AuthRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Testcontainers
@Transactional
@DisplayName("Auth Service Integration Tests")
class AuthServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
    }

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should complete full user registration and authentication flow")
    void completeUserFlow_RegisterAndAuthenticate_Success() throws Exception {
        // Step 1: Register new user
        RegisterRequest registerRequest = new RegisterRequest(
            "integration@example.com",
            "StrongPassword123",
            "Integration",
            "Test",
            "OPERATOR"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andExpect(jsonPath("$.firstName").value("Integration"))
                .andExpect(jsonPath("$.lastName").value("Test"))
                .andExpect(jsonPath("$.role").value("OPERATOR"));

        // Verify user was saved to database
        var userOpt = userRepository.findByEmail("integration@example.com");
        assertTrue(userOpt.isPresent());
        User savedUser = userOpt.get();
        assertEquals("integration@example.com", savedUser.getEmail());
        assertEquals("Integration", savedUser.getFirstName());
        assertEquals("Test", savedUser.getLastName());
        assertEquals(User.UserRole.OPERATOR, savedUser.getRole());
        assertTrue(savedUser.isAccountEnabled());

        // Step 2: Authenticate with registered user
        AuthRequest authRequest = new AuthRequest("integration@example.com", "StrongPassword123");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.expiresIn").exists())
                .andExpect(jsonPath("$.user.email").value("integration@example.com"))
                .andExpect(jsonPath("$.user.firstName").value("Integration"))
                .andExpect(jsonPath("$.user.lastName").value("Test"))
                .andExpect(jsonPath("$.user.role").value("OPERATOR"));

        // Verify last login was updated
        var updatedUser = userRepository.findByEmail("integration@example.com").get();
        assertNotNull(updatedUser.getLastLogin());
    }

    @Test
    @DisplayName("Should fail authentication with wrong password")
    void authenticate_WrongPassword_ReturnsUnauthorized() throws Exception {
        // First register a user
        RegisterRequest registerRequest = new RegisterRequest(
            "wrongpass@example.com",
            "CorrectPassword123",
            "Wrong",
            "Pass",
            "VIEWER"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Try to authenticate with wrong password
        AuthRequest authRequest = new AuthRequest("wrongpass@example.com", "WrongPassword123");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should fail registration with duplicate email")
    void register_DuplicateEmail_ReturnsBadRequest() throws Exception {
        // Register first user
        RegisterRequest firstUser = new RegisterRequest(
            "duplicate@example.com",
            "FirstPassword123",
            "First",
            "User",
            "ADMIN"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isCreated());

        // Try to register second user with same email
        RegisterRequest duplicateUser = new RegisterRequest(
            "duplicate@example.com",
            "SecondPassword123",
            "Second",
            "User",
            "VIEWER"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        // Verify only one user exists in database
        var users = userRepository.findAll();
        assertEquals(1, users.size());
        assertEquals("First", users.get(0).getFirstName());
    }

    @Test
    @DisplayName("Should fail registration with weak password")
    void register_WeakPassword_ReturnsBadRequest() throws Exception {
        RegisterRequest weakPasswordRequest = new RegisterRequest(
            "weak@example.com",
            "weak",  // Weak password
            "Weak",
            "Password",
            "VIEWER"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(weakPasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        // Verify user was not created
        var userOpt = userRepository.findByEmail("weak@example.com");
        assertTrue(userOpt.isEmpty());
    }

        @Test
    @DisplayName("Should retrieve user profile successfully")
    void getUserProfile_ExistingUser_ReturnsUserInfo() throws Exception {
        // Register a user
        RegisterRequest registerRequest = new RegisterRequest(
            "profile@example.com",
            "ProfilePassword123",
            "Profile",
            "User",
            "OPERATOR"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Get user profile with mock authentication
        mockMvc.perform(get("/api/v1/auth/profile")
                .with(user("profile@example.com").roles("OPERATOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("profile@example.com"))
                .andExpect(jsonPath("$.firstName").value("Profile"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.role").value("OPERATOR"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("Should return 403 for unauthenticated profile request")
    void getUserProfile_NoAuthentication_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/auth/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should validate request body for registration")
    void register_InvalidRequestBody_ReturnsBadRequest() throws Exception {
        // Test with missing required fields
        String invalidJson = "{\"email\": \"\", \"password\": \"\"}";

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate request body for authentication")
    void authenticate_InvalidRequestBody_ReturnsBadRequest() throws Exception {
        // Test with missing required fields
        String invalidJson = "{\"email\": \"\"}";

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}