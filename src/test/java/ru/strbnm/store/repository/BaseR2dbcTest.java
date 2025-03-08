package ru.strbnm.store.repository;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import ru.strbnm.store.config.LiquibaseConfig;

import java.nio.file.Files;
import java.nio.file.Paths;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataR2dbcTest
@Import(LiquibaseConfig.class)
public abstract class BaseR2dbcTest {

    @Autowired
    private DatabaseClient databaseClient;
    @Autowired
    SpringLiquibase liquibase;

    private static final String INIT_SCRIPT_PATH = "src/test/resources/scripts/INIT_STORE_RECORD.sql";
    private static final String CLEAN_SCRIPT_PATH = "src/test/resources/scripts/CLEAN_STORE_RECORD.sql";

    @BeforeAll
    void setupSchema() throws LiquibaseException {
        liquibase.afterPropertiesSet(); // Запускаем Liquibase вручную
        databaseClient.sql("SELECT 1").fetch().rowsUpdated().block(); // Ждем завершения
    }

    @BeforeEach
    void setupDatabase() {
        if (databaseClient == null) {
            throw new IllegalStateException("DatabaseClient не инициализирован. Проверьте конфигурацию тестов.");
        }
        executeSqlScript(INIT_SCRIPT_PATH).block();
    }

    @AfterEach
    void cleanupDatabase() {
        if (databaseClient == null) {
            throw new IllegalStateException("DatabaseClient не инициализирован. Проверьте конфигурацию тестов.");
        }
        executeSqlScript(CLEAN_SCRIPT_PATH).block();
    }

    private Mono<Void> executeSqlScript(String scriptPath) {
        try {
            String sql = new String(Files.readAllBytes(Paths.get(scriptPath)));
            return databaseClient.sql(sql).then();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при выполнении SQL-скрипта: " + scriptPath, e);
        }
    }
}
