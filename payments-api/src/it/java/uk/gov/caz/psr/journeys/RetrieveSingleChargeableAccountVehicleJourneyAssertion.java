package uk.gov.caz.psr.journeys;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import lombok.RequiredArgsConstructor;
import uk.gov.caz.correlationid.Constants;
import uk.gov.caz.psr.controller.AccountsController;
import uk.gov.caz.psr.dto.ChargeableAccountVehicleResponse;

@RequiredArgsConstructor
public class RetrieveSingleChargeableAccountVehicleJourneyAssertion {
  
  private String accountId;
  private String vrn;
  private String cleanAirZoneId;
  private ValidatableResponse response;
  private ChargeableAccountVehicleResponse responseDto;
  private static final String CORRELATION_ID = UUID.randomUUID().toString();

  public RetrieveSingleChargeableAccountVehicleJourneyAssertion forAccountId(String accountId) {
    this.accountId = accountId;
    return this;
  }

  public RetrieveSingleChargeableAccountVehicleJourneyAssertion forVrn(String vrn) {
    this.vrn = vrn;
    return this;
  }

  public RetrieveSingleChargeableAccountVehicleJourneyAssertion forCleanAirZoneId(String cleanAirZoneId) {
    this.cleanAirZoneId = cleanAirZoneId;
    return this;
  }
  
  public RetrieveSingleChargeableAccountVehicleJourneyAssertion 
  whenRequestIsMadeToRetrieveASingleChargeableAccountVehicle() {
    RestAssured.basePath = AccountsController.ACCOUNTS_PATH;
    this.response = RestAssured
      .given()
      .accept(MediaType.APPLICATION_JSON.toString())
      .contentType(MediaType.APPLICATION_JSON.toString())
      .pathParam("account_id", this.accountId)
      .pathParam("vrn", this.vrn)
      .queryParam("cleanAirZoneId", this.cleanAirZoneId)
      .header(Constants.X_CORRELATION_ID_HEADER, CORRELATION_ID)
      .when()
      .get("/{account_id}/chargeable-vehicles/{vrn}")
      .then();
    return this;
  }
  
  public RetrieveSingleChargeableAccountVehicleJourneyAssertion then() {
    return this;
  }

  public RetrieveSingleChargeableAccountVehicleJourneyAssertion responseIsReturnedWithHttpOkStatusCode() {
    this.responseDto = response.statusCode(HttpStatus.OK.value())
        .header(Constants.X_CORRELATION_ID_HEADER, CORRELATION_ID).extract()
        .as(ChargeableAccountVehicleResponse.class);
    return this;
  }
  
  public void responseIsReturnedWithHttp404StatusCode() {
    this.response.statusCode(404);
  }

  public void responseIsReturnedWithHttp400StatusCode() {
    this.response.statusCode(400);
  }
  
  public void responseContainsExpectedData(String expectedVrn, int detectedPaymentsSize) {
    assertEquals(this.responseDto.getChargeableAccountVehicles().getResults().get(0).getVrn(), expectedVrn);
    assertEquals(this.responseDto.getChargeableAccountVehicles().getResults().get(0).getPaidDates().size(), detectedPaymentsSize);
    assertNull(this.responseDto.getFirstVrn());
    assertNull(this.responseDto.getLastVrn());
  }
}
