package uk.gov.caz.accounts.service.generatecsv;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CsvFileNameGeneratorTest {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
      .ofPattern("yyyyMMdd_HHmm");

  private CsvFileNameGenerator csvFileNameGenerator = new CsvFileNameGenerator();

  @Test
  public void shouldGenerateFileName() {
    // given
    UUID accountId = UUID.fromString("468f01a6-9e4d-4c68-9e96-88bf69fae9da");

    // when
    String fileName = csvFileNameGenerator.generate(accountId);

    // then
    assertThat(fileName)
        .contains("fleet-")
        .contains("88bf69fae9da-")
        .contains(DATE_TIME_FORMATTER.format(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)))
        .contains(".csv");
  }
}