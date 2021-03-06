package uk.gov.caz.psr.util;

import static com.google.common.collect.Lists.newArrayList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import uk.gov.caz.psr.dto.ChargeSettlementPaymentStatus;
import uk.gov.caz.psr.dto.InitiatePaymentRequest;
import uk.gov.caz.psr.dto.PaymentStatusErrorResponse;
import uk.gov.caz.psr.dto.PaymentStatusUpdateDetails;
import uk.gov.caz.psr.dto.Transaction;
import uk.gov.caz.psr.dto.directdebit.CreateDirectDebitPaymentRequest;
import uk.gov.caz.psr.model.directdebit.DirectDebitPayment;
import uk.gov.caz.psr.model.EntrantPayment;
import uk.gov.caz.psr.model.EntrantPaymentStatusUpdate;
import uk.gov.caz.psr.model.EntrantPaymentUpdateActor;
import uk.gov.caz.psr.model.ExternalPaymentDetails;
import uk.gov.caz.psr.model.ExternalPaymentStatus;
import uk.gov.caz.psr.model.InternalPaymentStatus;
import uk.gov.caz.psr.model.Payment;
import uk.gov.caz.psr.model.PaymentMethod;
import uk.gov.caz.psr.model.PaymentStatus;

public class TestObjectFactory {

  private static final Random random = new Random();
  private static final UUID ANY_CLEAN_AIR_ZONE =
      UUID.fromString("17e8e064-fcb9-11e9-995d-fb4ae3c787c6");

  public static UUID anyCleanAirZoneId() {
    return ANY_CLEAN_AIR_ZONE;
  }

  private static String randomVrn() {
    char[] value = {randomUppercase(), randomUppercase(), (char) (random.nextInt(10) + '0'),
        (char) (random.nextInt(10) + '0'), randomUppercase(), randomUppercase(), randomUppercase()};
    return new String(value);
  }

  private static String randomExternalId() {
    char[] value = {(char) (random.nextInt(10) + '0'), randomLowercase(), randomUppercase(),
        randomUppercase(), (char) (random.nextInt(10) + '0'), randomUppercase(), randomLowercase()};
    return new String(value);
  }

  private static char randomUppercase() {
    return (char) (random.nextInt(26) + (int) ('A'));
  }

  private static char randomLowercase() {
    return (char) (random.nextInt(26) + (int) ('a'));
  }

  private static class EntrantPaymentsBuilder {

    private final Collection<LocalDate> travelDates;
    private InternalPaymentStatus status;
    private int amount;
    private boolean withId;
    private String vrn;
    private UUID cleanAirZoneId;

    private EntrantPaymentsBuilder(Collection<LocalDate> travelDates) {
      this.travelDates = travelDates;
    }

    public static EntrantPaymentsBuilder forDays(Collection<LocalDate> days) {
      return new EntrantPaymentsBuilder(days);
    }

    public EntrantPaymentsBuilder withStatus(InternalPaymentStatus status) {
      this.status = status;
      return this;
    }

    public List<EntrantPayment> build() {
      int charge = amount / travelDates.size();
      return travelDates.stream()
          .map(travelDate -> EntrantPayment.builder()
              .cleanAirZoneEntrantPaymentId(withId ? UUID.randomUUID() : null)
              .internalPaymentStatus(status)
              .charge(charge)
              .travelDate(travelDate)
              .updateActor(EntrantPaymentUpdateActor.USER)
              .tariffCode("TARIFF_CODE")
              .cleanAirZoneId(cleanAirZoneId == null ? ANY_CLEAN_AIR_ZONE : cleanAirZoneId)
              .vrn(vrn == null ? randomVrn() : vrn)
              .caseReference(null)
              .build())
          .collect(Collectors.toList());
    }

    public EntrantPaymentsBuilder withTotal(Integer amount) {
      this.amount = amount;
      return this;
    }

    public EntrantPaymentsBuilder withVrn(String vrn) {
      this.vrn = vrn;
      return this;
    }

    public EntrantPaymentsBuilder withCazId(UUID cleanAirZoneId) {
      this.cleanAirZoneId = cleanAirZoneId;
      return this;
    }
  }

  public static class Payments {

    private static final Random random = new Random();
    private static final UUID ANY_CAZ_ID = UUID.fromString("dff092a3-7b80-4432-a4de-c09715743d06");

    public static Payment existing() {
      UUID paymentId = UUID.randomUUID();
      String externalId = randomExternalId();
      return forRandomDaysWithId(paymentId, externalId, null);
    }

    public static Payment forRandomDays() {
      return forRandomDaysWithId(null, null, null);
    }

    public static Payment forRandomDaysWithId(UUID paymentId, UUID cazIdentifier) {
      return forRandomDaysWithId(paymentId, null, cazIdentifier);
    }

    public static Payment forRandomDaysWithId(UUID paymentId, String externalId,
        UUID cazIdentifier) {
      int daysSize = 5;
      LocalDate today = LocalDate.now();
      Set<LocalDate> localDates = new HashSet<>();
      while (localDates.size() != daysSize) {
        localDates.add(today.plusDays(random.nextInt(7)));
      }
      return forDays(localDates, paymentId, externalId, cazIdentifier);
    }

    public static Payment forDays(Collection<LocalDate> travelDates, UUID paymentId) {
      return forDays(travelDates, paymentId, null, ANY_CAZ_ID);
    }

    public static Payment forDays(Collection<LocalDate> travelDates, UUID paymentId,
        String externalId, UUID cazIdentifier) {
      List<EntrantPayment> entrantPayments = EntrantPaymentsBuilder.forDays(travelDates)
          .withTotal(travelDates.size() * 800)
          .withVrn(randomVrn())
          .withStatus(InternalPaymentStatus.NOT_PAID).withCazId(cazIdentifier).build();
      return createPaymentWith(entrantPayments, paymentId, externalId, cazIdentifier);
    }

    public static Payment forRequest(InitiatePaymentRequest request) {
      List<EntrantPayment> entrantPayments = Collections.emptyList();

      return createPaymentWith(entrantPayments, null, null, request.getCleanAirZoneId())
          .toBuilder()
          .totalPaid(request.getTransactions().stream().mapToInt(Transaction::getCharge).sum())
          .build();
    }

    public static Payment forDirectDebitRequest(CreateDirectDebitPaymentRequest request) {
      List<EntrantPayment> entrantPayments = Collections.emptyList();

      return createPaymentWith(entrantPayments, null, null, request.getCleanAirZoneId())
          .toBuilder()
          .paymentMethod(PaymentMethod.DIRECT_DEBIT)
          .paymentProviderMandateId("exampleMandateId")
          .totalPaid(request.getTransactions().stream().mapToInt(Transaction::getCharge).sum())
          .build();
    }

    public static Payment preparePayment(UUID paymentId) {
      return Payment.builder()
          .id(paymentId)
          .referenceNumber(1500L)
          .externalId("123")
          .externalPaymentStatus(ExternalPaymentStatus.CREATED)
          .paymentMethod(PaymentMethod.DIRECT_DEBIT)
          .submittedTimestamp(LocalDateTime.now())
          .telephonePayment(false)
          .totalPaid(40)
          .operatorId(UUID.randomUUID())
          .entrantPayments(newArrayList(EntrantPayments.anyPaid()))
          .build();
    }

    public static Payment preparePaymentWithTwoEntrantPayments(UUID paymentId) {
      return Payment.builder()
          .id(paymentId)
          .referenceNumber(1500L)
          .externalId("123")
          .externalPaymentStatus(ExternalPaymentStatus.CREATED)
          .paymentMethod(PaymentMethod.DIRECT_DEBIT)
          .submittedTimestamp(LocalDateTime.now())
          .telephonePayment(false)
          .totalPaid(40)
          .operatorId(UUID.randomUUID())
          .entrantPayments(newArrayList(EntrantPayments.anyPaid(), EntrantPayments.anyPaid()))
          .build();
    }

    private static Payment createPaymentWith(List<EntrantPayment> entrantPayments,
        UUID paymentId, String externalId, UUID cazIdentifier) {
      return Payment.builder()
          .id(paymentId)
          .externalId(externalId)
          .paymentMethod(PaymentMethod.CREDIT_DEBIT_CARD)
          .totalPaid(entrantPayments.stream().map(EntrantPayment::getCharge)
              .reduce(0, Integer::sum))
          .entrantPayments(entrantPayments)
          .externalPaymentStatus(ExternalPaymentStatus.INITIATED)
          .cleanAirZoneId(cazIdentifier)
          .build();
    }
  }

  public static class EntrantPayments {

    public static EntrantPayment anyNotPaid() {
      return EntrantPayment.builder().charge(100).travelDate(LocalDate.now())
          .cleanAirZoneId(UUID.randomUUID()).vrn("VRN123")
          .updateActor(EntrantPaymentUpdateActor.VCCS_API)
          .internalPaymentStatus(InternalPaymentStatus.NOT_PAID).build();
    }

    public static EntrantPayment anyPaid() {
      return EntrantPayment.builder().charge(100).travelDate(LocalDate.now())
          .cleanAirZoneId(UUID.randomUUID()).vrn("VRN123")
          .updateActor(EntrantPaymentUpdateActor.VCCS_API)
          .internalPaymentStatus(InternalPaymentStatus.PAID)
          .vrn("CAS310")
          .tariffCode("any-tariff-code")
          .build();
    }

    public static EntrantPayment forTravelDate(LocalDate travelDate, String vrn) {
      return EntrantPaymentsBuilder.forDays(Collections.singleton(travelDate))
          .withStatus(InternalPaymentStatus.PAID)
          .withVrn(vrn)
          .build()
          .get(0);
    }
  }

  public static class PaymentStatusUpdateDetailsFactory {

    public static PaymentStatusUpdateDetails anyWithStatus(String status) {
      return PaymentStatusUpdateDetails.builder()
          .caseReference("CaseReference")
          .paymentStatus(status)
          .dateOfCazEntry(LocalDate.now())
          .build();
    }

    public static PaymentStatusUpdateDetails refundedWithDateOfCazEntry(LocalDate date) {
      return PaymentStatusUpdateDetails.builder().caseReference("CaseReference")
          .paymentStatus("refunded")
          .dateOfCazEntry(date).build();
    }

    public static PaymentStatusUpdateDetails anyWithoutCaseReference() {
      return PaymentStatusUpdateDetails.builder()
          .paymentStatus("refunded")
          .dateOfCazEntry(LocalDate.now())
          .build();
    }

    public static PaymentStatusUpdateDetails anyWithoutDateOfCazEntry() {
      return PaymentStatusUpdateDetails.builder()
          .paymentStatus(ChargeSettlementPaymentStatus.REFUNDED.name())
          .caseReference("CaseReference")
          .build();
    }

    public static PaymentStatusUpdateDetails anyWithoutPaymentStatus() {
      return PaymentStatusUpdateDetails.builder()
          .caseReference("CaseReference")
          .dateOfCazEntry(LocalDate.now())
          .build();
    }

    public static PaymentStatusUpdateDetails withCaseReference(String caseReference) {
      return PaymentStatusUpdateDetails.builder()
          .paymentStatus("refunded")
          .caseReference(caseReference)
          .dateOfCazEntry(LocalDate.now())
          .build();
    }
  }

  public static class EntrantPaymentStatusUpdates {

    public static EntrantPaymentStatusUpdate any() {
      return EntrantPaymentStatusUpdate.builder().caseReference("CaseReference")
          .paymentStatus(InternalPaymentStatus.REFUNDED)
          .vrn("VRN123").dateOfCazEntry(LocalDate.now())
          .cleanAirZoneId(UUID.randomUUID())
          .build();
    }
  }

  public static class PaymentStatusFactory {

    public static PaymentStatus anyWithStatus(InternalPaymentStatus internalPaymentStatus) {
      return PaymentStatus.builder()
          .paymentMethod(PaymentMethod.CREDIT_DEBIT_CARD)
          .caseReference("any-valid-case-reference")
          .status(internalPaymentStatus)
          .externalId(UUID.randomUUID().toString())
          .build();
    }

    public static PaymentStatus with(InternalPaymentStatus internalPaymentStatus,
        String caseReference, String externalId, Long paymentReference, PaymentMethod paymentMethod,
        boolean telephonePayment) {
      return PaymentStatus.builder().caseReference(caseReference).paymentReference(paymentReference)
          .status(internalPaymentStatus)
          .externalId(externalId)
          .paymentMethod(paymentMethod)
          .telephonePayment(telephonePayment)
          .build();
    }
  }

  public static class PaymentStatusErrorFactory {

    public static PaymentStatusErrorResponse with(String title, String detail, String field) {
      return PaymentStatusErrorResponse.builder().title(title).detail(detail)
          .field(field).build();
    }
  }

  public static class ExternalPaymentDetailsFactory {

    public static ExternalPaymentDetails any() {
      return ExternalPaymentDetails.builder().email("example@email.com")
          .externalPaymentStatus(ExternalPaymentStatus.SUCCESS).build();
    }

    public static ExternalPaymentDetails anyWithStatus(
        ExternalPaymentStatus externalPaymentStatus) {
      return ExternalPaymentDetails.builder().email("example@email.com")
          .externalPaymentStatus(externalPaymentStatus).build();
    }
  }

  public static class DirectDebitPayments {

    public static DirectDebitPayment any() {
      return DirectDebitPayment.builder()
          .amount(1000)
          .mandateId("exampleMandateId")
          .status("success")
          .reference(UUID.randomUUID().toString())
          .paymentId(UUID.randomUUID().toString())
          .build();
    }

    public static DirectDebitPayment anyWithStatus(String status) {
      return DirectDebitPayment.builder()
          .amount(1000)
          .mandateId("exampleMandateId")
          .status(status)
          .reference(UUID.randomUUID().toString())
          .paymentId(UUID.randomUUID().toString())
          .build();
    }
  }

}
