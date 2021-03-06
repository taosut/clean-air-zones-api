package uk.gov.caz.psr.service;

import com.google.common.base.Preconditions;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import uk.gov.caz.definitions.dto.CacheableResponseDto;
import uk.gov.caz.definitions.dto.CleanAirZoneDto;
import uk.gov.caz.definitions.dto.CleanAirZonesDto;
import uk.gov.caz.psr.repository.VccsRepository;
import uk.gov.caz.psr.repository.exception.CleanAirZoneNotFoundException;

/**
 * Class responsible to call vccs for clean air zones and find a name of selected zone in the
 * response list.
 */
@Service
@AllArgsConstructor
@Slf4j
public class CleanAirZoneService {

  private final VccsRepository vccsRepository;

  /**
   * Gets clean air zones from VCCS.
   *
   * @return {@link CleanAirZonesDto} A list of parsed Clean Air Zones
   */
  @Cacheable(value = "cleanAirZones")
  public CacheableResponseDto<CleanAirZonesDto> fetchAll() {
    try {
      log.debug("Fetching all clean air zones from VCCS");
      Response<CleanAirZonesDto> response = vccsRepository.findCleanAirZonesSync();
      return CacheableResponseDto.<CleanAirZonesDto>builder()
          .code(response.code()).body(response.body()).build();
    } finally {
      log.debug("Fetching all clean air zones from VCCS: finish");
    }
  }

  /**
   * Gets name of clean air zone name from vccs.
   *
   * @param cleanAirZoneId id of clean air zone
   * @return {@link String} if the CleanAirZone exist
   */
  @Cacheable(value = "tariffs")
  public String fetch(UUID cleanAirZoneId) {
    Preconditions.checkNotNull(cleanAirZoneId, "cleanAirZoneId cannot be null");

    try {
      log.debug("Get all cleanAirZones name: start");
      Response<CleanAirZonesDto> cleanAirZonesResponse =
          vccsRepository.findCleanAirZonesSync();
      CleanAirZonesDto response = cleanAirZonesResponse.body();
      return findCleanAirZoneName(response, cleanAirZoneId);
    } finally {
      log.debug("Get all cleanAirZones name: finish");
    }
  }

  /**
   * Method which finds name of CleanAirZone with provided ID from VCCS response.
   *
   * @param response Response from VCCS
   * @param cleanAirZoneId id of Clean Air Zone which we want to find
   * @return Found CleanAirZone name.
   * @throws CleanAirZoneNotFoundException if Clean Air Zone was not found.
   */
  public String findCleanAirZoneName(CleanAirZonesDto response, UUID cleanAirZoneId) {
    CleanAirZoneDto cleanAirZone = response.getCleanAirZones().stream()
        .filter(fetch -> fetch.getCleanAirZoneId().equals(cleanAirZoneId))
        .findFirst()
        .orElseThrow(() -> new CleanAirZoneNotFoundException(cleanAirZoneId));
    return cleanAirZone.getName();
  }

  /**
   * Service layer implementation for evicting a cached clean-air-zones from redis.
   */
  @CacheEvict(value = {"cleanAirZones", "tariffs"}, allEntries = true)
  public void cacheEvictCleanAirZones() {
    log.info("Evicting cached clean-air-zones.");
  }
}
