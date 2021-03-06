package uk.gov.caz.taxiregister.service;

import com.google.common.base.Strings;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import uk.gov.caz.taxiregister.model.registerjob.RegisterJobName;
import uk.gov.caz.taxiregister.model.registerjob.RegisterJobTrigger;

/**
 * Generates full Register Job name that can be used by polling and logging operations.
 */
@Component
public class RegisterJobNameGenerator {

  private static final String FORMATTER_PATTERN = "yyyyMMdd_HHmmssSSS_";
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(FORMATTER_PATTERN);

  /**
   * Generate full Register Job name that can be used by polling and logging operations.
   *
   * @param suffix Register Job suffix that we want to have at the end of job name. Can be empty
   *     or null and if so it will be ignored.
   * @param registerJobTrigger Informs name generator what was the trigger so it can append
   *     proper part to Register Job Name.
   * @return Full Register Job name that can be used by polling and logging operations.
   */
  public RegisterJobName generate(String suffix, RegisterJobTrigger registerJobTrigger) {
    String jobName = formattedNow()
        + registerJobTrigger.name()
        + (Strings.isNullOrEmpty(suffix) ? "" : "_" + suffix);
    return new RegisterJobName(jobName);
  }

  private String formattedNow() {
    LocalDateTime now = LocalDateTime.now();
    return FORMATTER.format(now);
  }
}
