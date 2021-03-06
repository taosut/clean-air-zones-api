package uk.gov.caz.retrofit.service;

import com.google.common.base.Preconditions;
import java.util.List;
import uk.gov.caz.retrofit.dto.RetrofittedVehicleDto;
import uk.gov.caz.retrofit.model.CsvFindResult;
import uk.gov.caz.retrofit.model.ValidationError;
import uk.gov.caz.retrofit.repository.RetrofittedVehicleDtoCsvRepository;

/**
 * Class which is responsible for registering vehicles whose data is located at S3.
 */
public class RegisterFromCsvCommand extends AbstractRegisterCommand {

  private final String bucket;
  private final String filename;

  private final RetrofittedVehicleDtoCsvRepository csvRepository;

  private CsvFindResult csvFindResult;

  /**
   * Creates an instance of {@link RegisterFromCsvCommand}.
   */
  public RegisterFromCsvCommand(RegisterServicesContext registerServicesContext, int registerJobId,
      String correlationId, String bucket, String filename) {
    super(registerServicesContext, registerJobId, correlationId);
    this.bucket = bucket;
    this.filename = filename;
    this.csvRepository = registerServicesContext.getCsvRepository();
  }

  @Override
  public void beforeExecute() {
    csvFindResult = csvRepository.findAll(bucket, filename);
  }

  @Override
  public List<RetrofittedVehicleDto> getVehiclesToRegister() {
    checkCsvParseResultsPresentPrecondition();
    return csvFindResult.getVehicles();
  }

  @Override
  List<ValidationError> getParseValidationErrors() {
    checkCsvParseResultsPresentPrecondition();
    return csvFindResult.getValidationErrors();
  }

  private void checkCsvParseResultsPresentPrecondition() {
    Preconditions.checkState(csvFindResult != null, "CSV parse results need to obtained first");
  }
}
