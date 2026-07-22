package com.gaiotti.zenith.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Exercises the real stack — HTTP, Spring Security with JWT, the service layer, JPA and the Flyway
 * migrations — against a throwaway PostgreSQL container.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
@Testcontainers
class LedgerFlowIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullFlow_transactionShowsUpInTheDashboard() throws Exception {
        String token = register("ana.it@example.com");
        long ledgerId = createLedger(token);
        long categoryId = createCategory(token, ledgerId);

        String transaction =
                """
                {"amount":1800.00,"type":"EXPENSE","date":"%s","categoryId":%d,"description":"Aluguel"}
                """
                        .formatted(LocalDate.now(), categoryId);
        mockMvc.perform(post("/api/v1/ledgers/" + ledgerId + "/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transaction))
                .andExpect(status().isCreated());

        JsonNode overview = getJson(
                token, "/api/v1/ledgers/" + ledgerId + "/dashboard/overview");
        assertThat(overview.get("totalExpense").decimalValue()).isEqualByComparingTo("1800.00");
        assertThat(overview.get("totalIncome").decimalValue()).isEqualByComparingTo("0");
    }

    @Test
    void authorization_intruderCannotReadAnotherLedger() throws Exception {
        String ownerToken = register("owner.it@example.com");
        long ledgerId = createLedger(ownerToken);

        String intruderToken = register("intruder.it@example.com");
        mockMvc.perform(get("/api/v1/ledgers/" + ledgerId + "/dashboard/overview")
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isForbidden());
    }

    private String register(String email) throws Exception {
        String body =
                """
                {"email":"%s","password":"senha123","displayName":"Pessoa Teste"}
                """
                        .formatted(email);
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private long createLedger(String token) throws Exception {
        String response = mockMvc.perform(post("/api/v1/ledgers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Casa Teste\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private long createCategory(String token, long ledgerId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/ledgers/" + ledgerId + "/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Moradia\",\"color\":\"#2E5D45\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private JsonNode getJson(String token, String path) throws Exception {
        String response = mockMvc.perform(
                        get(path).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }
}
