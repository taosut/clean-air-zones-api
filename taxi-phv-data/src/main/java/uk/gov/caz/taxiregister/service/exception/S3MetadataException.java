package uk.gov.caz.taxiregister.service.exception;

public class S3MetadataException extends RuntimeException {
  public S3MetadataException(String message) {
    super(message);
  }
}
