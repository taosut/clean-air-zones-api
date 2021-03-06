package uk.gov.caz.retrofit.dto.validation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import uk.gov.caz.retrofit.dto.RetrofittedVehicleDto;
import uk.gov.caz.retrofit.model.ValidationError;

public class DateOfRetrofitInstallationValidator implements RetrofittedVehicleValidator {

  @VisibleForTesting
  static final String MISSING_DATE_MESSAGE = "Data does not include the 'date of retrofit "
      + "installation' field which is mandatory.";

  @VisibleForTesting
  static final String INVALID_DATE_FORMAT_MESSAGE = "Invalid format of date of retrofit "
      + "installation, should be ISO 8601.";

  @Override
  public List<ValidationError> validate(RetrofittedVehicleDto retrofittedVehicleDto) {
    ImmutableList.Builder<ValidationError> validationErrorsBuilder = ImmutableList.builder();
    DateOfRetrofitInstallationErrorResolver errorResolver =
        new DateOfRetrofitInstallationErrorResolver(retrofittedVehicleDto);

    String vrn = retrofittedVehicleDto.getVrn();
    String stringifiedDate = retrofittedVehicleDto.getDateOfRetrofitInstallation();

    tryParsingDate(stringifiedDate, vrn, errorResolver, validationErrorsBuilder);

    return validationErrorsBuilder.build();
  }

  private void tryParsingDate(String stringifiedDate, String vrn,
      DateOfRetrofitInstallationErrorResolver errorResolver,
      Builder<ValidationError> validationErrorsBuilder) {
    if (stringifiedDate == null) {
      validationErrorsBuilder.add(errorResolver.missing(vrn));
    } else {
      try {
        LocalDate.parse(stringifiedDate);
      } catch (DateTimeParseException e) {
        validationErrorsBuilder.add(errorResolver.invalidFormat(vrn));
      }
    }
  }

  private static class DateOfRetrofitInstallationErrorResolver extends ValidationErrorResolver {

    private DateOfRetrofitInstallationErrorResolver(RetrofittedVehicleDto retrofittedVehicleDto) {
      super(retrofittedVehicleDto);
    }

    private ValidationError missing(String vrn) {
      return missingFieldError(vrn, MISSING_DATE_MESSAGE);
    }

    private ValidationError invalidFormat(String vrn) {
      return valueError(vrn, INVALID_DATE_FORMAT_MESSAGE);
    }
  }
}
