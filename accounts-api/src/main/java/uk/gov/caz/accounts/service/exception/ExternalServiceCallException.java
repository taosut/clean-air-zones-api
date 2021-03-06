package uk.gov.caz.accounts.service.exception;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.caz.ApplicationRuntimeException;

/**
 * An exception wrapper for signifying an exception was encountered when invoking a remote API.
 */
@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE, reason = "Service unavailable")
public class ExternalServiceCallException extends ApplicationRuntimeException {

  private static final long serialVersionUID = -8036164741274827345L;

  public ExternalServiceCallException(Optional<String> message) {
    super(message.orElse("Service unavailable"));
  }

  public ExternalServiceCallException(String message) {
    super(message);
  }

  public ExternalServiceCallException() {
    this(Optional.empty());
  }
}
