package uk.gov.caz.taxiregister.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import uk.gov.caz.taxiregister.model.registerjob.RegisterJobStatus;
import uk.gov.caz.taxiregister.service.exception.RequiredLicenceTypesAbsentInDbException;
import uk.gov.caz.taxiregister.service.exception.S3InvalidUploaderIdFormatException;
import uk.gov.caz.taxiregister.service.exception.S3MaxFileSizeExceededException;
import uk.gov.caz.taxiregister.service.exception.S3MetadataException;

@ExtendWith(MockitoExtension.class)
class RegisterFromCsvExceptionResolverTest {

  private RegisterFromCsvExceptionResolver resolver = new RegisterFromCsvExceptionResolver();

  @Test
  public void shouldResolveS3NoSuchKeyValidationError() {
    // given
    NoSuchKeyException input = NoSuchKeyException.builder().build();

    // when
    RegisterResult result = resolver.resolve(input);
    RegisterJobStatus status = resolver.resolveToRegisterJobFailureStatus(input);

    //then
    then(result.getValidationErrors()).hasOnlyOneElementSatisfying(validationError -> {
      assertThat(validationError.getVrm()).isNull();
      assertThat(validationError.getTitle()).isEqualTo("S3 error");
      assertThat(validationError.getDetail())
          .isEqualTo("S3 bucket or file not found or not accessible");
    });
    assertThat(status).isEqualByComparingTo(RegisterJobStatus.STARTUP_FAILURE_NO_S3_BUCKET_OR_FILE);
  }

  @Test
  public void shouldResolveS3MetadataValidationError() {
    // given
    S3MetadataException input = new S3MetadataException("");

    // when
    RegisterResult result = resolver.resolve(input);
    RegisterJobStatus status = resolver.resolveToRegisterJobFailureStatus(input);

    //then
    then(result.getValidationErrors()).hasOnlyOneElementSatisfying(validationError -> {
      assertThat(validationError.getVrm()).isNull();
      assertThat(validationError.getTitle()).isEqualTo("S3 error");
      assertThat(validationError.getDetail())
          .isEqualTo("'uploader-id' not found in file's metadata");
    });
    assertThat(status).isEqualByComparingTo(RegisterJobStatus.STARTUP_FAILURE_NO_UPLOADER_ID);
  }

  @Test
  public void shouldResolveS3UploaderIdValidationError() {
    // given
    S3InvalidUploaderIdFormatException input = new S3InvalidUploaderIdFormatException();

    // when
    RegisterResult result = resolver.resolve(input);
    RegisterJobStatus status = resolver.resolveToRegisterJobFailureStatus(input);

    //then
    then(result.getValidationErrors()).hasOnlyOneElementSatisfying(validationError -> {
      assertThat(validationError.getVrm()).isNull();
      assertThat(validationError.getTitle()).isEqualTo("S3 error");
      assertThat(validationError.getDetail()).isEqualTo("Malformed ID of an entity which want to "
          + "register vehicles by CSV file. Expected a unique identifier (UUID)");
    });
    assertThat(status).isEqualByComparingTo(RegisterJobStatus.STARTUP_FAILURE_INVALID_UPLOADER_ID);
  }

  @Test
  public void shouldResolveS3MaxFileSizeExceededValidationError() {
    // given
    Exception input = new S3MaxFileSizeExceededException();

    // when
    RegisterResult result = resolver.resolve(input);
    RegisterJobStatus status = resolver.resolveToRegisterJobFailureStatus(input);

    //then
    then(result.getValidationErrors()).hasOnlyOneElementSatisfying(validationError -> {
      assertThat(validationError.getVrm()).isNull();
      assertThat(validationError.getTitle()).isEqualTo("S3 error");
      assertThat(validationError.getDetail())
          .isEqualTo("Uploaded file size exceeded \"Max size: 500MB\"");
    });
    assertThat(status).isEqualByComparingTo(RegisterJobStatus.STARTUP_FAILURE_TOO_LARGE_FILE);
  }

  @Test
  public void shouldResolveRequiredLicenceTypesAbsentInDbException() {
    // given
    Exception input = new RequiredLicenceTypesAbsentInDbException("");

    // when
    RegisterResult result = resolver.resolve(input);
    RegisterJobStatus status = resolver.resolveToRegisterJobFailureStatus(input);

    //then
    then(result.getValidationErrors()).hasOnlyOneElementSatisfying(validationError -> {
      assertThat(validationError.getVrm()).isNull();
      assertThat(validationError.getTitle()).isEqualTo("Internal error");
      assertThat(validationError.getDetail()).isEqualTo("An internal error occurred while "
          + "processing registration, please contact the system administrator");
    });
    assertThat(status).isEqualByComparingTo(RegisterJobStatus.STARTUP_FAILURE_MISSING_LICENCE_TYPES);
  }

  @Test
  public void shouldResolveUnknownException() {
    // given
    Exception input = new IllegalAccessException();

    // when
    RegisterResult result = resolver.resolve(input);
    RegisterJobStatus status = resolver.resolveToRegisterJobFailureStatus(input);

    //then
    then(result.getValidationErrors()).hasOnlyOneElementSatisfying(validationError -> {
      assertThat(validationError.getVrm()).isNull();
      assertThat(validationError.getTitle()).isEqualTo("Unknown error");
      assertThat(validationError.getDetail()).isEqualTo("Unknown error occurred while processing "
          + "registration");
    });
    assertThat(status).isEqualByComparingTo(RegisterJobStatus.UNKNOWN_FAILURE);
  }
}