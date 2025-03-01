package ru.strbnm.store.controller;

import java.nio.file.Path;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

@SpringBootTest
@SqlGroup({
  @Sql(
      value = {"/scripts/INIT_STORE_RECORD.sql"},
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
  @Sql(
      value = {"/scripts/CLEAN_STORE_RECORD.sql"},
      executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class BaseControllersIntegrationTest {
  @TempDir
  static Path tempDir; // Временная директория для сохранения загруженных изображений постов

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("app.product.image.dir", () -> tempDir.toString());
  }
}
