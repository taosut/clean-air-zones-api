package uk.gov.caz.psr.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.caz.psr.util.TestObjectFactory.Payments.preparePayment;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.caz.psr.model.Payment;
import uk.gov.caz.psr.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock
  private PaymentRepository paymentRepository;

  @InjectMocks
  private PaymentService paymentDetailsService;

  @Test
  void shouldReturnPayment() {
    // given
    UUID paymentId = UUID.randomUUID();
    given(paymentRepository.findById(any())).willReturn(Optional.of(preparePayment(paymentId)));

    // when
    Optional<Payment> payment = paymentDetailsService.getPayment(paymentId);

    // then
    assertThat(payment.isPresent()).isTrue();
  }

  @Test
  void shouldReturnOptionalEmpty() {
    // given
    given(paymentRepository.findById(any())).willReturn(Optional.empty());

    // when
    Optional<Payment> paymentDetails = paymentDetailsService.getPayment(UUID.randomUUID());

    // then
    assertThat(paymentDetails).isEmpty();
  }
}