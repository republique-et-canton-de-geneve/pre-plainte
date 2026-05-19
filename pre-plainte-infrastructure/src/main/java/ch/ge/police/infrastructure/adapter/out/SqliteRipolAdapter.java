package ch.ge.police.infrastructure.adapter.out;

import ch.ge.police.core.domain.model.ripol.Ripol;
import ch.ge.police.core.port.out.RipolPort;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SqliteRipolAdapter implements RipolPort {

  private static final int SEARCH_RESULT_LIMIT = 50;
  private static final String CACHE_KEY_SEPARATOR = "|";
  private static final String TABLE_INCIDENT_CODE = "TBINCIDENTCODE";
  private static final String TABLE_LOCALIZATION = "TBLOCALIZATION";
  private static final String MASTER_TYPE_OBJETS = "183";
  private static final String GROUP_TYPE_SEXE = "geschlechtISO";
  private static final String GROUP_TYPE_NATIONALITE = "11";
  private static final String GROUP_TYPE_COMMUNE = "272";
  private static final String GROUP_TYPE_LIEU_ORIGINE = "271";
  private static final String GROUP_TYPE_DOCUMENT = "01M";
  private static final String GROUP_TYPE_OBJET = MASTER_TYPE_OBJETS;
  private static final String GROUP_TYPE_VEHICULE = "101";
  private static final String GROUP_TYPE_COULEUR = "103";
  private static final String GROUP_TYPE_LIEU = "390";
  private static final String GROUP_TYPE_CANTON = "1";
  private static final List<String> PRELOADED_GROUP_TYPES = List.of(
    GROUP_TYPE_SEXE,
    GROUP_TYPE_NATIONALITE,
    GROUP_TYPE_COMMUNE,
    GROUP_TYPE_LIEU_ORIGINE,
    GROUP_TYPE_DOCUMENT,
    GROUP_TYPE_OBJET,
    GROUP_TYPE_VEHICULE,
    GROUP_TYPE_COULEUR,
    GROUP_TYPE_LIEU,
    GROUP_TYPE_CANTON
  );
  private static final String COL_CODEVALUE = "CODEVALUE";
  private static final String COL_TEXT_FR = "TEXT_FR";
  private static final String COL_TEXT_DE = "TEXT_DE";
  private static final String COL_GROUPTYPE = "GROUPTYPE";
  private static final String TRACE_ID = "traceId";

  private final JdbcTemplate jdbcTemplate;
  private final Map<String, List<Ripol>> codesByGroupTypeCache = new ConcurrentHashMap<>();
  private final Map<String, List<Ripol>> brandsByKeyCache = new ConcurrentHashMap<>();
  private final Map<String, List<Ripol>> modelsByBrandCache = new ConcurrentHashMap<>();

  private static final RowMapper<Ripol> RIPOL_ROW_MAPPER = (rs, rowNum) ->
    new Ripol(
      rs.getString(COL_CODEVALUE),
      rs.getString(COL_TEXT_FR),
      rs.getString(COL_TEXT_DE),
      rs.getString(COL_GROUPTYPE)
    );

  public SqliteRipolAdapter(
    ResourceLoader resourceLoader,
    @Value("${sqlite.db.classpath-location:bdd/dbppel3}") String dbClasspathLocation
  ) throws IOException {
    Resource resource = resourceLoader.getResource("classpath:" + dbClasspathLocation);

    if (!resource.exists()) {
      throw new IllegalStateException("Fichier SQLite introuvable dans les ressources : " + dbClasspathLocation);
    }

    Path tempFile = Files.createTempFile("sqlite-db-", ".db");
    try (InputStream in = resource.getInputStream()) {
      Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
    }

    String jdbcUrl = "jdbc:sqlite:" + tempFile.toAbsolutePath();

    log.info(
      "event=ripol_sqlite_initialized traceId={} classpathLocation={} tempFile={}",
      MDC.get(TRACE_ID),
      dbClasspathLocation,
      tempFile
    );

    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.sqlite.JDBC");
    dataSource.setUrl(jdbcUrl);

    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @EventListener(ApplicationReadyEvent.class)
  void warmUpCacheAsync() {
    CompletableFuture.runAsync(this::warmUpCache);
  }

  private void warmUpCache() {
    long start = System.currentTimeMillis();

    log.info(
      "event=ripol_cache_warmup_start traceId={} groupTypeCount={}",
      MDC.get(TRACE_ID),
      PRELOADED_GROUP_TYPES.size()
    );

    for (String groupType : PRELOADED_GROUP_TYPES) {
      try {
        getCodesByGroupType(groupType);
      } catch (Exception e) {
        log.warn(
          "event=ripol_cache_warmup_failure traceId={} groupType={} error={}",
          MDC.get(TRACE_ID),
          groupType,
          e.getMessage(),
          e
        );
      }
    }

    long elapsed = System.currentTimeMillis() - start;

    log.info(
      "event=ripol_cache_warmup_success traceId={} durationMs={} cacheSize={}",
      MDC.get(TRACE_ID),
      elapsed,
      codesByGroupTypeCache.size()
    );
  }

  @Override
  public List<String> listTables() {
    try {
      return jdbcTemplate.execute((java.sql.Connection conn) -> {
        List<String> tables = new ArrayList<>();
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
          while (rs.next()) {
            tables.add(rs.getString("TABLE_NAME"));
          }
        }
        return tables;
      });
    } catch (DataAccessException e) {
      log.error("event=ripol_list_tables_failure traceId={} error={}", MDC.get(TRACE_ID), e.getMessage(), e);
      throw new RipolAccessException("Erreur lors de la récupération des tables", e);
    }
  }

  @Override
  public List<String> listColumns(String tableName) {
    String safeTableName = validateAndResolveTableName(tableName);

    try {
      List<String> columns = jdbcTemplate.execute((ConnectionCallback<List<String>>) conn -> extractColumns(conn, safeTableName));
      return columns != null ? columns : List.of();
    } catch (DataAccessException e) {
      log.error(
        "event=ripol_list_columns_failure traceId={} table={} error={}",
        MDC.get(TRACE_ID),
        safeTableName,
        e.getMessage(),
        e
      );
      throw new RipolAccessException("Erreur lors de la récupération des colonnes", e);
    }
  }

  @Override
  public List<Map<String, Object>> listTableContent(String tableName, int limit) {
    String safeTableName = validateAndResolveTableName(tableName);

    try {
      String sql = selectAllWithLimitSql(safeTableName);
      return jdbcTemplate.query(sql, (rs, rowNum) -> {
        Map<String, Object> row = new LinkedHashMap<>();
        var metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
          row.put(metaData.getColumnName(i), rs.getObject(i));
        }
        return row;
      }, limit);
    } catch (DataAccessException e) {
      log.error(
        "event=ripol_list_table_content_failure traceId={} table={} limit={} error={}",
        MDC.get(TRACE_ID),
        safeTableName,
        limit,
        e.getMessage(),
        e
      );
      throw new RipolAccessException("Erreur lors de la récupération du contenu", e);
    }
  }

  @Override
  public List<String> listDistinctGroupTypes(String tableName) {
    String safeTableName = validateAndResolveTableName(tableName);

    try {
      String sql = selectDistinctGroupTypesSql(safeTableName);
      return jdbcTemplate.queryForList(sql, String.class);
    } catch (DataAccessException e) {
      log.error(
        "event=ripol_list_group_types_failure traceId={} table={} error={}",
        MDC.get(TRACE_ID),
        safeTableName,
        e.getMessage(),
        e
      );
      throw new RipolAccessException("Erreur lors de la récupération des groupTypes", e);
    }
  }

  @Override
  public List<Map<String, Object>> listRowsByGroupType(String tableName, String groupType, int limit) {
    String safeTableName = validateAndResolveTableName(tableName);
    if (groupType == null || groupType.isBlank()) {
      throw new IllegalArgumentException("Le GROUPTYPE ne peut pas être vide");
    }

    try {
      String sql = selectByGroupTypeWithLimitSql(safeTableName);
      return jdbcTemplate.query(sql, (rs, rowNum) -> {
        Map<String, Object> row = new LinkedHashMap<>();
        var metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
          row.put(metaData.getColumnName(i), rs.getObject(i));
        }
        return row;
      }, groupType, limit);
    } catch (DataAccessException e) {
      log.error(
        "event=ripol_list_rows_by_group_type_failure traceId={} table={} groupType={} limit={} error={}",
        MDC.get(TRACE_ID),
        safeTableName,
        groupType,
        limit,
        e.getMessage(),
        e
      );
      throw new RipolAccessException("Erreur lors de la récupération des lignes par groupType", e);
    }
  }

  @Override
  public List<Ripol> getCodesByGroupType(String groupType) {
    return codesByGroupTypeCache.computeIfAbsent(groupType, this::queryCodesByGroupType);
  }

  private List<Ripol> queryCodesByGroupType(String groupType) {
    String sql = """
          SELECT
              code.GROUPTYPE,
              code.CODEVALUE,
              code.TEXT AS TEXT_DE,
              COALESCE(loc.TRANSLATION, '') AS TEXT_FR
          FROM TBINCIDENTCODE code
          LEFT JOIN TBLOCALIZATION loc ON loc.PK = code.ID AND loc.LOCALE_ID = 3
          WHERE code.GROUPTYPE = ?
          ORDER BY TEXT_FR ASC
      """;

    try {
      return jdbcTemplate.query(sql, RIPOL_ROW_MAPPER, groupType);
    } catch (DataAccessException e) {
      log.error(
        "event=ripol_get_codes_by_group_type_failure traceId={} groupType={} error={}",
        MDC.get(TRACE_ID),
        groupType,
        e.getMessage(),
        e
      );
      throw new RipolAccessException("Erreur lors de la récupération des codes RIPOL", e);
    }
  }

  @Override
  public List<Ripol> getBrandsByType(String masterValue) {
    return getBrandsByTypeAndMasterType(masterValue, MASTER_TYPE_OBJETS);
  }

  @Override
  public List<Ripol> getBrandsByTypeAndMasterType(String masterValue, String masterType) {
    String cacheKey = masterType + CACHE_KEY_SEPARATOR + masterValue;
    return brandsByKeyCache.computeIfAbsent(cacheKey, k -> queryBrandsByTypeAndMasterType(masterValue, masterType));
  }

  private List<Ripol> queryBrandsByTypeAndMasterType(String masterValue, String masterType) {
    String sql = """
          SELECT
              ? AS GROUPTYPE,
              code.CODEVALUE,
              code.TEXT AS TEXT_DE,
              COALESCE(loc.TRANSLATION, '') AS TEXT_FR
          FROM TBINCIDENTCODE code
          LEFT JOIN TBLOCALIZATION loc ON loc.PK = code.ID AND loc.LOCALE_ID = 3
          WHERE code.MASTERTYPE = ?
            AND code.MASTERVALUE = ?
          ORDER BY TEXT_FR ASC
      """;

    try {
      return jdbcTemplate.query(sql, RIPOL_ROW_MAPPER, masterType, masterType, masterValue);
    } catch (DataAccessException e) {
      log.error(
        "event=ripol_get_brands_failure traceId={} masterType={} masterValue={} error={}",
        MDC.get(TRACE_ID),
        masterType,
        masterValue,
        e.getMessage(),
        e
      );
      throw new RipolAccessException("Erreur lors de la récupération des marques", e);
    }
  }

  @Override
  public List<Ripol> getModelsByBrand(String brandCode) {
    return modelsByBrandCache.computeIfAbsent(brandCode, this::queryModelsByBrand);
  }

  private List<Ripol> queryModelsByBrand(String brandCode) {
    String sql = """
          SELECT
              '185' AS GROUPTYPE,
              code.CODEVALUE,
              code.TEXT AS TEXT_DE,
              COALESCE(loc.TRANSLATION, '') AS TEXT_FR
          FROM TBINCIDENTCODE code
          LEFT JOIN TBLOCALIZATION loc ON loc.PK = code.ID AND loc.LOCALE_ID = 3
          WHERE code.MASTERTYPE = '185'
            AND code.MASTERVALUE = ?
          ORDER BY TEXT_FR ASC
      """;

    try {
      return jdbcTemplate.query(sql, RIPOL_ROW_MAPPER, brandCode);
    } catch (DataAccessException e) {
      log.error(
        "event=ripol_get_models_failure traceId={} brandCode={} error={}",
        MDC.get(TRACE_ID),
        brandCode,
        e.getMessage(),
        e
      );
      throw new RipolAccessException("Erreur lors de la récupération des modèles", e);
    }
  }

  @Override
  public List<Ripol> searchCodesByGroupType(String groupType, String search) {
    List<Ripol> allCodes = getCodesByGroupType(groupType);
    String searchLower = search.toLowerCase(Locale.ROOT);
    return allCodes.stream().filter(r -> containsIgnoreCase(r, searchLower)).limit(SEARCH_RESULT_LIMIT).toList();
  }

  @Override
  public List<Ripol> searchBrands(String masterValue, String masterType, String search) {
    List<Ripol> allBrands = getBrandsByTypeAndMasterType(masterValue, masterType);
    String searchLower = search.toLowerCase(Locale.ROOT);
    return allBrands.stream().filter(r -> containsIgnoreCase(r, searchLower)).limit(SEARCH_RESULT_LIMIT).toList();
  }

  @Override
  public List<Ripol> searchModels(String brandCode, String search) {
    List<Ripol> allModels = getModelsByBrand(brandCode);
    String searchLower = search.toLowerCase(Locale.ROOT);
    return allModels.stream().filter(r -> containsIgnoreCase(r, searchLower)).limit(SEARCH_RESULT_LIMIT).toList();
  }

  private boolean containsIgnoreCase(Ripol ripol, String searchLower) {
    String textDe = ripol.labelDe();
    String textFr = ripol.labelFr();
    return (textDe != null && textDe.toLowerCase(Locale.ROOT).contains(searchLower))
      || (textFr != null && textFr.toLowerCase(Locale.ROOT).contains(searchLower));
  }

  private String validateAndResolveTableName(String tableName) {
    if (tableName == null || tableName.isBlank()) {
      throw new IllegalArgumentException("Le nom de la table ne peut pas être vide");
    }
    return switch (tableName.trim().toUpperCase(Locale.ROOT)) {
      case TABLE_INCIDENT_CODE -> TABLE_INCIDENT_CODE;
      case TABLE_LOCALIZATION -> TABLE_LOCALIZATION;
      default -> throw new IllegalArgumentException("Nom de table non autorisé");
    };
  }

  private String selectAllWithLimitSql(String safeTableName) {
    return switch (safeTableName) {
      case TABLE_INCIDENT_CODE -> "SELECT * FROM TBINCIDENTCODE LIMIT ?";
      case TABLE_LOCALIZATION -> "SELECT * FROM TBLOCALIZATION LIMIT ?";
      default -> throw new IllegalArgumentException("Nom de table non autorisé");
    };
  }

  private String selectDistinctGroupTypesSql(String safeTableName) {
    return switch (safeTableName) {
      case TABLE_INCIDENT_CODE -> "SELECT DISTINCT GROUPTYPE FROM TBINCIDENTCODE ORDER BY GROUPTYPE";
      case TABLE_LOCALIZATION -> "SELECT DISTINCT GROUPTYPE FROM TBLOCALIZATION ORDER BY GROUPTYPE";
      default -> throw new IllegalArgumentException("Nom de table non autorisé");
    };
  }

  private String selectByGroupTypeWithLimitSql(String safeTableName) {
    return switch (safeTableName) {
      case TABLE_INCIDENT_CODE -> "SELECT * FROM TBINCIDENTCODE WHERE GROUPTYPE = ? LIMIT ?";
      case TABLE_LOCALIZATION -> "SELECT * FROM TBLOCALIZATION WHERE GROUPTYPE = ? LIMIT ?";
      default -> throw new IllegalArgumentException("Nom de table non autorisé");
    };
  }

  private List<String> extractColumns(java.sql.Connection conn, String safeTableName) throws java.sql.SQLException {
    List<String> columns = new ArrayList<>();
    DatabaseMetaData metaData = conn.getMetaData();
    try (ResultSet rs = metaData.getColumns(null, null, safeTableName, null)) {
      while (rs.next()) {
        String columnName = rs.getString("COLUMN_NAME");
        String typeName = rs.getString("TYPE_NAME");
        columns.add(columnName + " (" + typeName + ")");
      }
    }
    if (columns.isEmpty()) {
      log.warn(
        "event=ripol_no_columns_found traceId={} table={}",
        MDC.get(TRACE_ID),
        safeTableName
      );
    }
    return columns;
  }

  public static class RipolAccessException extends RuntimeException {
    public RipolAccessException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
