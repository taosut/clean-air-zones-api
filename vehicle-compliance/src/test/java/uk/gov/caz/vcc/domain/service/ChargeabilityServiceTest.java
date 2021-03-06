package uk.gov.caz.vcc.domain.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.caz.vcc.domain.CazClass;
import uk.gov.caz.vcc.domain.TariffDetails;
import uk.gov.caz.vcc.domain.Vehicle;
import uk.gov.caz.vcc.domain.VehicleType;
import uk.gov.caz.vcc.domain.VehicleTypeCharge;

public class ChargeabilityServiceTest {
  
  ChargeabilityService chargeabilityService;
  
  private TariffDetails tariffDetails;
  private Vehicle testVehicle;
  
  @BeforeEach
  public void init() {
    UUID someUuid = UUID.randomUUID();
    
    chargeabilityService = new ChargeabilityService(someUuid.toString());
    
    testVehicle = new Vehicle();
    testVehicle.setTaxClass("someTaxClass");
    testVehicle.setIsTaxiOrPhv(false);
    
    tariffDetails = new TariffDetails();
    tariffDetails.setCazId(someUuid);
    tariffDetails.setName("Test CAZ");
  }
  
  @Test
  public void WavTaxiInLeedsNotChargeable() {
    testVehicle.setVehicleType(VehicleType.TAXI_OR_PHV);
    testVehicle.setIsTaxiOrPhv(true);
    testVehicle.setIsWav(true);
    tariffDetails.setTariff(CazClass.B); // Leeds implementing B class CAZ.
    setTariffRates(VehicleType.TAXI_OR_PHV, (float) 3.142);
    
    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);
    
    assertEquals(0, charge, 0.001);
  }
  
  @Test
  public void nullWavTaxiChargeable() {
    testVehicle.setVehicleType(VehicleType.TAXI_OR_PHV);
    testVehicle.setIsTaxiOrPhv(true);
    testVehicle.setIsWav(null);
    tariffDetails.setTariff(CazClass.B); // Leeds implementing B class CAZ.
    setTariffRates(VehicleType.TAXI_OR_PHV, (float) 3.142);
    
    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);
    
    assertEquals(3.142, charge, 0.001);    
  }
  
  @Test
  public void NotWavTaxiInLeedsChargeable() {
    testVehicle.setVehicleType(VehicleType.TAXI_OR_PHV);
    testVehicle.setIsTaxiOrPhv(true);
    testVehicle.setIsWav(false);
    tariffDetails.setTariff(CazClass.B);  // Leeds implementing B class CAZ.
    setTariffRates(VehicleType.TAXI_OR_PHV, (float) 3.142);
    
    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);
    
    assertEquals(3.142, charge, 0.001);
  }

  @Test
  public void disabledTaxClassNotCharegableIfCazNotCharging() {
    tariffDetails.setdisabledTaxClassChargeable(false);
    testVehicle.setTaxClass("DISABLED");
    
    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);
    
    assertEquals(0, charge, 0.001);
  }
  
  @Test
  public void nullTaxClassCaught() {
    tariffDetails.setdisabledTaxClassChargeable(false);
    tariffDetails.setTariff(CazClass.D);
    setTariffRates(VehicleType.PRIVATE_CAR, (float) 3.142);
    
    testVehicle.setVehicleType(VehicleType.PRIVATE_CAR);
    testVehicle.setTaxClass(null);
 
    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);
    
    assertEquals(3.142, charge, 0.001);
  }
  
  @Test
  public void zeroChargeFoundCorrectly() {
    tariffDetails.setTariff(CazClass.D);
    testVehicle.setVehicleType(VehicleType.PRIVATE_CAR);
    setTariffRates(VehicleType.PRIVATE_CAR, 0);
    
    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);

    assertEquals(0, charge, 0.01);
  }
  
  @Test
  void carChargedInDTariff() {
    tariffDetails.setTariff(CazClass.D);
    testVehicle.setVehicleType(VehicleType.PRIVATE_CAR);
    setTariffRates(VehicleType.PRIVATE_CAR, 50);

    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);

    assertEquals(50, charge, 0.01);
  }

  @Test
  void carNotChargedInCTariff() {
    tariffDetails.setTariff(CazClass.C);
    testVehicle.setVehicleType(VehicleType.PRIVATE_CAR);
    setTariffRates(VehicleType.PRIVATE_CAR, 50);

    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);

    assertEquals(0, charge, 0.01);
  }


  @Test
  void motorcycleChargedInDTariff() {
    tariffDetails.setTariff(CazClass.D);
    testVehicle.setVehicleType(VehicleType.MOTORCYCLE);
    setTariffRates(VehicleType.MOTORCYCLE, 50);

    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);

    assertEquals(50, charge, 0.01);
  }

  @Test
  void motorcycleNotChargedInCTariff() {
    tariffDetails.setTariff(CazClass.C);
    testVehicle.setVehicleType(VehicleType.MOTORCYCLE);
    setTariffRates(VehicleType.MOTORCYCLE, 50);

    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);

    assertEquals(0, charge, 0.01);
  }

  @Test
  void largeVanChargedInCTariff() {
    tariffDetails.setTariff(CazClass.C);
    testVehicle.setVehicleType(VehicleType.LARGE_VAN);
    setTariffRates(VehicleType.LARGE_VAN, 50);

    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);

    assertEquals(50, charge, 0.01);
  }

  @Test
  void largeVanNotChargedInBTariff() {
    tariffDetails.setTariff(CazClass.B);
    testVehicle.setVehicleType(VehicleType.LARGE_VAN);
    setTariffRates(VehicleType.LARGE_VAN, 50);

    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);

    assertEquals(0, charge, 0.01);
  }

  @Test
  void smallVanChargedInCTariff() {
    tariffDetails.setTariff(CazClass.C);
    testVehicle.setVehicleType(VehicleType.SMALL_VAN);
    setTariffRates(VehicleType.SMALL_VAN, 50);

    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);

    assertEquals(50, charge, 0.01);
  }

  @Test
  void smallVanNotChargedInBTariff() {
    tariffDetails.setTariff(CazClass.B);
    testVehicle.setVehicleType(VehicleType.SMALL_VAN);
    setTariffRates(VehicleType.SMALL_VAN, 50);

    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);

    assertEquals(0, charge, 0.01);
  }

  @Test
  void minibusChargedInCTariff() {
    tariffDetails.setTariff(CazClass.C);
    testVehicle.setVehicleType(VehicleType.MINIBUS);
    setTariffRates(VehicleType.MINIBUS, 50);

    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);

    assertEquals(50, charge, 0.01);
  }

  @Test
  void minibusNotChargedInBTariff() {
    tariffDetails.setTariff(CazClass.B);
    testVehicle.setVehicleType(VehicleType.MINIBUS);
    setTariffRates(VehicleType.MINIBUS, 50);

    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);

    assertEquals(0, charge, 0.01);
  }

  @Test
  void hgvChargedInBTariff() {
    tariffDetails.setTariff(CazClass.B);
    testVehicle.setVehicleType(VehicleType.HGV);
    setTariffRates(VehicleType.HGV, 50);

    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);

    assertEquals(50, charge, 0.01);
  }

  @Test
  void hgvNotChargedInATariff() {
    tariffDetails.setTariff(CazClass.A);
    testVehicle.setVehicleType(VehicleType.HGV);
    setTariffRates(VehicleType.HGV, 50);

    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);

    assertEquals(0, charge, 0.01);
  }

  @Test
  void coachChargedInATariff() {
    tariffDetails.setTariff(CazClass.A);
    testVehicle.setVehicleType(VehicleType.COACH);
    setTariffRates(VehicleType.COACH, 50);

    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);

    assertEquals(50, charge, 0.01);
  }

  @Test
  void taxiChargedInATariff() {
    tariffDetails.setTariff(CazClass.A);
    testVehicle.setVehicleType(VehicleType.BUS);
    setTariffRates(VehicleType.BUS, 50);

    float charge = chargeabilityService.getCharge(testVehicle, tariffDetails);

    assertEquals(50, charge, 0.01);
  }
  
  private void setTariffRates(VehicleType type, float charge) {
    tariffDetails.setRates(
        new ArrayList<VehicleTypeCharge>(Arrays.asList(new VehicleTypeCharge() {
          private static final long serialVersionUID = 1L;
          {
            setVehicleType(type);
            setCharge(charge);
          }
        })));
  }
}
