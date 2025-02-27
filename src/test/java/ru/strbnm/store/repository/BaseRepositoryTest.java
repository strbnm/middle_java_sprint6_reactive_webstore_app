package ru.strbnm.store.repository;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

@DataJpaTest
@ActiveProfiles({"test"})
@SqlGroup({
  @Sql(
      value = {"/scripts/INIT_STORE_RECORD.sql"},
      executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
  @Sql(
      value = {"/scripts/CLEAN_STORE_RECORD.sql"},
      executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
})
public abstract class BaseRepositoryTest {}
