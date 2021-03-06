package uk.gov.caz.taxiregister.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.gov.caz.taxiregister.model.registerjob.RegisterJobName;
import uk.gov.caz.taxiregister.model.registerjob.RegisterJobTrigger;

class RegisterJobNameGeneratorTest {

  @Test
  public void prefixesJobSuffixUsingTrigger() {
    // given
    String jobSuffix = "job";

    // when
    RegisterJobName registerJobName = new RegisterJobNameGenerator()
        .generate(jobSuffix, RegisterJobTrigger.CSV_FROM_S3);

    // then
    assertThat(registerJobName.getValue().matches("\\w{8}_\\w{9}_CSV_FROM_S3_job")).isTrue();
  }

  @Test
  public void whenSuffixIsEmptyItDoesNotAppendTrailingUnderscore() {
    // given
    String jobSuffix = "";

    // when
    RegisterJobName registerJobName = new RegisterJobNameGenerator()
        .generate(jobSuffix, RegisterJobTrigger.API_CALL);

    // then
    assertThat(registerJobName.getValue().matches("\\w{8}_\\w{9}_API_CALL")).isTrue();
  }

  @Test
  public void whenSuffixIsNullItDoesNotAppendTrailingUnderscore() {
    // given
    String jobSuffix = null;

    // when
    RegisterJobName registerJobName = new RegisterJobNameGenerator()
        .generate(jobSuffix, RegisterJobTrigger.CSV_FROM_S3);

    // then
    assertThat(registerJobName.getValue().matches("\\w{8}_\\w{9}_CSV_FROM_S3")).isTrue();
  }
}
