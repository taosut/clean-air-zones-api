package uk.gov.caz.taxiregister.repository;

import com.google.common.annotations.VisibleForTesting;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import uk.gov.caz.taxiregister.model.LicensingAuthority;

/**
 * A class that manages access to {@link LicensingAuthority} entities in database.
 */
@Repository
public class LicensingAuthorityPostgresRepository {

  @VisibleForTesting
  static final String SELECT_ALL_SQL = "select "
      + "licence_authority_id, "
      + "licence_authority_name "
      + "from t_md_licensing_authority";

  @VisibleForTesting
  static final String SELECT_ALLOWED_TO_BE_MODIFIED_BY_SQL = SELECT_ALL_SQL
      + " where authorised_uploader_ids @> ARRAY[?::uuid]";

  private static final String SELECT_NAMES_BY_VRM = "select "
      + "distinct la.licence_authority_name "
      + "from t_md_licensing_authority la, t_md_taxi_phv taxi "
      + "where taxi.licence_authority_id = la.licence_authority_id "
      + "and taxi.vrm = ?";

  private static final RowMapper<LicensingAuthority> MAPPER = new LicensingAuthorityRowMapper();

  private final JdbcTemplate jdbcTemplate;

  /**
   * Creates an instance of {@link LicensingAuthorityPostgresRepository}.
   *
   * @param jdbcTemplate An instance of {@link JdbcTemplate}.
   */
  public LicensingAuthorityPostgresRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Finds all {@link LicensingAuthority} entities in the database. Returns them in a map that
   * assigns licensing authority name to {@link LicensingAuthority}
   *
   * @return {@link Map} containing all {@link LicensingAuthority} entities in the database. The map
   *     assigns licensing authority name to {@link LicensingAuthority}
   */
  public Map<String, LicensingAuthority> findAll() {
    return jdbcTemplate.query(SELECT_ALL_SQL, MAPPER).stream()
        .collect(Collectors.toMap(LicensingAuthority::getName, Function.identity()));
  }

  /**
   * Finds all {@link LicensingAuthority} entities in the database which {@code uploaderId} is
   * entitled to modify.
   *
   * @param uploaderId ID of an entity against which the search is performed.
   * @return A {@link List} containing all {@link LicensingAuthority} which the given {@code
   *     uploaderId} can modify.
   */
  public List<LicensingAuthority> findAllowedToBeModifiedBy(UUID uploaderId) {
    return jdbcTemplate.query(SELECT_ALLOWED_TO_BE_MODIFIED_BY_SQL,
        preparedStatement -> preparedStatement.setObject(1, uploaderId),
        MAPPER
    );
  }

  /**
   * Finds authority names by given vrm.
   *
   * @return {@link List} containing all authority names for given vrm.
   */
  public List<String> findAuthorityNamesByVrm(String vrm) {
    return jdbcTemplate.queryForList(SELECT_NAMES_BY_VRM, String.class, vrm);
  }

  @VisibleForTesting
  static class LicensingAuthorityRowMapper implements RowMapper<LicensingAuthority> {

    @Override
    public LicensingAuthority mapRow(ResultSet rs, int i) throws SQLException {
      return new LicensingAuthority(
          rs.getInt("licence_authority_id"),
          rs.getString("licence_authority_name")
      );
    }
  }
}
