package com.fzu.calculator;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fzu.calculator.repository.CalculationHistoryRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End to end tests of the REST API against a real Spring context and a real SQLite database.
 *
 * <p>The database file lives in {@code target/} so that the tests never touch the development
 * database, but the SQLite dialect, the JPA mapping and the SQL statements are identical to the
 * production configuration.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = CalculatorApiIntegrationTest.DatabaseDirectoryInitializer.class)
@TestPropertySource(properties = {
        "app.database.path=./target/test-data/calculator-test.db",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CalculatorApiIntegrationTest {

    /** The test database path, kept in one place so the initializer and the properties agree. */
    private static final String DATABASE_PATH = "./target/test-data/calculator-test.db";

    /**
     * SQLite creates the database file itself but not the folder that contains it, and the JDBC
     * driver connects before any application code runs. Therefore the directory is prepared while
     * the context is still being created.
     */
    static class DatabaseDirectoryInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            Path parent = Paths.get(DATABASE_PATH).toAbsolutePath().getParent();
            try {
                Files.createDirectories(parent);
            } catch (IOException ex) {
                throw new IllegalStateException("Could not create " + parent, ex);
            }
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CalculationHistoryRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private void calculate(String expression) throws Exception {
        mockMvc.perform(post("/api/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"" + expression + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("POST /api/calculate returns the backend computed result")
    void calculatesBasicExpression() throws Exception {
        mockMvc.perform(post("/api/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"12+8\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.expression", is("12+8")))
                .andExpect(jsonPath("$.result", is(20)))
                .andExpect(jsonPath("$.id", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("compound expressions are evaluated with the correct precedence")
    void calculatesCompoundExpression() throws Exception {
        mockMvc.perform(post("/api/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"(1+2)*3\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is(9)));
    }

    @Test
    @DisplayName("division by zero is reported as HTTP 400 with a business error code")
    void rejectsDivisionByZero() throws Exception {
        mockMvc.perform(post("/api/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"1/0\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errorCode", is("DIVISION_BY_ZERO")));
    }

    @Test
    @DisplayName("an invalid expression is reported as HTTP 400")
    void rejectsInvalidExpression() throws Exception {
        mockMvc.perform(post("/api/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"1+*2\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errorCode", is("INVALID_EXPRESSION")));
    }

    @Test
    @DisplayName("an empty expression is reported as HTTP 400")
    void rejectsEmptyExpression() throws Exception {
        mockMvc.perform(post("/api/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("EMPTY_EXPRESSION")));
    }

    @Test
    @DisplayName("a failed calculation is not written to the history table")
    void doesNotPersistFailedCalculations() throws Exception {
        mockMvc.perform(post("/api/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"1/0\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(0)))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/history returns the persisted records, newest first")
    void returnsHistory() throws Exception {
        calculate("1+1");
        calculate("2+2");
        calculate("3+3");

        mockMvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.total", is(3)))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].expression", is("3+3")))
                .andExpect(jsonPath("$.data[0].result", is(6)))
                .andExpect(jsonPath("$.data[0].createdAt").exists());
    }

    @Test
    @DisplayName("DELETE /api/history/{id} removes exactly one record from the database")
    void deletesOneRecord() throws Exception {
        calculate("1+1");
        calculate("2+2");

        Long idToDelete = repository.findAll().get(0).getId();

        mockMvc.perform(delete("/api/history/" + idToDelete))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.deletedId", is(idToDelete.intValue())));

        mockMvc.perform(get("/api/history"))
                .andExpect(jsonPath("$.total", is(1)));
    }

    @Test
    @DisplayName("deleting an unknown record returns HTTP 404")
    void deletingUnknownRecordReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/history/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("NOT_FOUND")));
    }

    @Test
    @DisplayName("GET /api/health reports the service as up")
    void reportsHealth() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    @DisplayName("user friendly operators coming from the front end are normalized")
    void normalizesFrontendInput() throws Exception {
        mockMvc.perform(post("/api/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"6×7\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expression", is("6*7")))
                .andExpect(jsonPath("$.result", is(42)));
    }
}
