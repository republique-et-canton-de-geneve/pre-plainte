package ch.ge.police.infrastructure.adapter.out;

import ch.ge.police.core.domain.model.ripol.Ripol;
import ch.ge.police.core.port.out.RipolPort;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
  private static final String GROUP_TYPE_VEHICLE_BRAND = "102";
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
    GROUP_TYPE_VEHICLE_BRAND,
    GROUP_TYPE_COULEUR,
    GROUP_TYPE_LIEU,
    GROUP_TYPE_CANTON
  );
  private static final String COL_CODEVALUE = "CODEVALUE";
  private static final String COL_TEXT_FR = "TEXT_FR";
  private static final String COL_TEXT_DE = "TEXT_DE";
  private static final String COL_GROUPTYPE = "GROUPTYPE";
  private static final String TRACE_ID = "traceId";
  private static final int SQLITE_HEADER_PROBE_BYTES = 128;
  private static final String SQLITE_MAGIC = "SQLite format 3";
  private static final String GIT_LFS_POINTER_PREFIX = "version https://git-lfs.github.com";
  private static final int LOCALE_FR = 3;
  private static final String SQL_SELECT_TEXT_FR =
    "COALESCE(NULLIF(trim(loc.TRANSLATION), ''), code.TEXT) AS TEXT_FR\n";
  private static final String SQL_JOIN_LOCALIZATION_FR =
    """
    LEFT JOIN TBLOCALIZATION loc ON loc.ROWID = (
      SELECT MIN(l.ROWID)
      FROM TBLOCALIZATION l
      WHERE l.PK = code.ID AND l.LOCALE_ID = 3
    )
    """;
  private static final Set<String> DEDUPLICATE_BY_LABEL_GROUP_TYPES = Set.of(
    GROUP_TYPE_VEHICULE,
    GROUP_TYPE_VEHICLE_BRAND,
    GROUP_TYPE_COULEUR
  );
  private static final String SQL_EXCLUDE_UNUSABLE_RIPOL_LABELS =
      " AND code.CODEVALUE NOT LIKE '90%'"
          + " AND NOT ("
          + " code.TEXT IS NOT NULL"
          + " AND length(trim(code.TEXT)) >= 10"
          + " AND trim(code.TEXT) NOT GLOB '*[^0-9]*'"
          + " )";

  private enum AllowedTable {
    INCIDENT_CODE(TABLE_INCIDENT_CODE),
    LOCALIZATION(TABLE_LOCALIZATION);

    private final String tableName;

    AllowedTable(String tableName) {
      this.tableName = tableName;
    }

    static AllowedTable parse(String tableName) {
      if (tableName == null || tableName.isBlank()) {
        throw new IllegalArgumentException("Le nom de la table ne peut pas être vide");
      }
      return switch (tableName.trim().toUpperCase(Locale.ROOT)) {
        case TABLE_INCIDENT_CODE -> INCIDENT_CODE;
        case TABLE_LOCALIZATION -> LOCALIZATION;
        default -> throw new IllegalArgumentException("Nom de table non autorisé");
      };
    }
  }

  private enum IncidentCodeUsabilityFilter {
    NONE("", "", ""),
    ACTIVE_ONLY(
      " AND CAST(code.ACTIVE AS INTEGER) = 1",
      " AND CAST(ACTIVE AS INTEGER) = 1",
      " WHERE CAST(ACTIVE AS INTEGER) = 1"
    ),
    SELECTABLE_ONLY(
      " AND CAST(code.SELECTABLE AS INTEGER) = 1",
      " AND CAST(SELECTABLE AS INTEGER) = 1",
      " WHERE CAST(SELECTABLE AS INTEGER) = 1"
    ),
    ACTIVE_AND_SELECTABLE(
      " AND CAST(code.ACTIVE AS INTEGER) = 1 AND CAST(code.SELECTABLE AS INTEGER) = 1",
      " AND CAST(ACTIVE AS INTEGER) = 1 AND CAST(SELECTABLE AS INTEGER) = 1",
      " WHERE CAST(ACTIVE AS INTEGER) = 1 AND CAST(SELECTABLE AS INTEGER) = 1"
    );

    private final String andAliasedFilter;
    private final String andUnaliasedFilter;
    private final String whereUnaliasedFilter;

    IncidentCodeUsabilityFilter(
        String andAliasedFilter,
        String andUnaliasedFilter,
        String whereUnaliasedFilter
    ) {
      this.andAliasedFilter = andAliasedFilter;
      this.andUnaliasedFilter = andUnaliasedFilter;
      this.whereUnaliasedFilter = whereUnaliasedFilter;
    }

    static IncidentCodeUsabilityFilter from(boolean hasActiveColumn, boolean hasSelectableColumn) {
      if (hasActiveColumn && hasSelectableColumn) {
        return ACTIVE_AND_SELECTABLE;
      }
      if (hasActiveColumn) {
        return ACTIVE_ONLY;
      }
      if (hasSelectableColumn) {
        return SELECTABLE_ONLY;
      }
      return NONE;
    }
  }

  private record RipolIncidentCodeQueries(
      String selectAllWithLimit,
      String selectDistinctGroupTypes,
      String selectByGroupTypeWithLimit,
      String codesByGroupType,
      String codesByGroupTypeSearch,
      String brandsByType,
      String brandsByTypeSearch,
      String modelsByBrand,
      String modelsByBrandSearch
  ) {
    private static RipolIncidentCodeQueries forFilter(IncidentCodeUsabilityFilter filter) {
      String andAliased = filter.andAliasedFilter;
      String andUnaliased = filter.andUnaliasedFilter;
      String whereUnaliased = filter.whereUnaliasedFilter;
      String exclude = SQL_EXCLUDE_UNUSABLE_RIPOL_LABELS;

      String codesByGroupType = """
          SELECT
              code.GROUPTYPE,
              code.CODEVALUE,
              code.TEXT AS TEXT_DE,
              """ + SQL_SELECT_TEXT_FR + """
          FROM TBINCIDENTCODE code
          """ + SQL_JOIN_LOCALIZATION_FR + """
          WHERE code.GROUPTYPE = ?
          """ + andAliased + exclude + """
          
          ORDER BY TEXT_FR ASC
          """;

      String codesByGroupTypeSearch = """
          SELECT
              code.GROUPTYPE,
              code.CODEVALUE,
              code.TEXT AS TEXT_DE,
              """ + SQL_SELECT_TEXT_FR + """
          FROM TBINCIDENTCODE code
          """ + SQL_JOIN_LOCALIZATION_FR + """
          WHERE code.GROUPTYPE = ?
            """ + andAliased + exclude + """
            AND (
              LOWER(code.TEXT) LIKE ?
              OR LOWER(COALESCE(NULLIF(trim(loc.TRANSLATION), ''), '')) LIKE ?
            )
          ORDER BY TEXT_FR ASC
          LIMIT ?
          """;

      String brandsByType = """
          SELECT
              ? AS GROUPTYPE,
              code.CODEVALUE,
              code.TEXT AS TEXT_DE,
              """ + SQL_SELECT_TEXT_FR + """
          FROM TBINCIDENTCODE code
          """ + SQL_JOIN_LOCALIZATION_FR + """
          WHERE code.MASTERTYPE = ?
            AND code.MASTERVALUE = ?
            """ + andAliased + exclude + """
          ORDER BY TEXT_FR ASC
          """;

      String brandsByTypeSearch = """
          SELECT
              ? AS GROUPTYPE,
              code.CODEVALUE,
              code.TEXT AS TEXT_DE,
              """ + SQL_SELECT_TEXT_FR + """
          FROM TBINCIDENTCODE code
          """ + SQL_JOIN_LOCALIZATION_FR + """
          WHERE code.MASTERTYPE = ?
            AND code.MASTERVALUE = ?
            """ + andAliased + exclude + """
            AND (
              LOWER(code.TEXT) LIKE ?
              OR LOWER(COALESCE(NULLIF(trim(loc.TRANSLATION), ''), '')) LIKE ?
            )
          ORDER BY TEXT_FR ASC
          LIMIT ?
          """;

      String modelsByBrand = """
          SELECT
              '185' AS GROUPTYPE,
              code.CODEVALUE,
              code.TEXT AS TEXT_DE,
              """ + SQL_SELECT_TEXT_FR + """
          FROM TBINCIDENTCODE code
          """ + SQL_JOIN_LOCALIZATION_FR + """
          WHERE code.MASTERTYPE = '185'
            AND code.MASTERVALUE = ?
            """ + andAliased + exclude + """
          ORDER BY TEXT_FR ASC
          """;

      String modelsByBrandSearch = """
          SELECT
              '185' AS GROUPTYPE,
              code.CODEVALUE,
              code.TEXT AS TEXT_DE,
              """ + SQL_SELECT_TEXT_FR + """
          FROM TBINCIDENTCODE code
          """ + SQL_JOIN_LOCALIZATION_FR + """
          WHERE code.MASTERTYPE = '185'
            AND code.MASTERVALUE = ?
            """ + andAliased + exclude + """
            AND (
              LOWER(code.TEXT) LIKE ?
              OR LOWER(COALESCE(NULLIF(trim(loc.TRANSLATION), ''), '')) LIKE ?
            )
          ORDER BY TEXT_FR ASC
          LIMIT ?
          """;

      return new RipolIncidentCodeQueries(
          "SELECT * FROM TBINCIDENTCODE" + whereUnaliased + " LIMIT ?",
          "SELECT DISTINCT GROUPTYPE FROM TBINCIDENTCODE" + whereUnaliased + " ORDER BY GROUPTYPE",
          "SELECT * FROM TBINCIDENTCODE WHERE GROUPTYPE = ?" + andUnaliased + " LIMIT ?",
          codesByGroupType,
          codesByGroupTypeSearch,
          brandsByType,
          brandsByTypeSearch,
          modelsByBrand,
          modelsByBrandSearch
      );
    }
  }

  private final JdbcTemplate jdbcTemplate;
  private final Map<String, List<Ripol>> codesByGroupTypeCache = new ConcurrentHashMap<>();
  private final Map<String, List<Ripol>> brandsByKeyCache = new ConcurrentHashMap<>();
  private final Map<String, List<Ripol>> modelsByBrandCache = new ConcurrentHashMap<>();
  private final RipolIncidentCodeQueries incidentCodeQueries;

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
    validateSqliteResource(tempFile, dbClasspathLocation);

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
    boolean incidentCodeHasActiveColumn = incidentCodeHasColumn("ACTIVE");
    boolean incidentCodeHasSelectableColumn = incidentCodeHasColumn("SELECTABLE");
    IncidentCodeUsabilityFilter incidentCodeFilter = IncidentCodeUsabilityFilter.from(
        incidentCodeHasActiveColumn,
        incidentCodeHasSelectableColumn
    );
    this.incidentCodeQueries = RipolIncidentCodeQueries.forFilter(incidentCodeFilter);
    log.info(
      "event=ripol_incident_code_filters traceId={} active={} selectable={}",
      MDC.get(TRACE_ID),
      incidentCodeHasActiveColumn,
      incidentCodeHasSelectableColumn
    );
    createSearchIndexes(incidentCodeFilter);
  }

  private static void validateSqliteResource(Path sqliteFile, String classpathLocation) throws IOException {
    byte[] header;
    try (InputStream in = Files.newInputStream(sqliteFile)) {
      header = in.readNBytes(SQLITE_HEADER_PROBE_BYTES);
    }
    if (header.length < SQLITE_MAGIC.length()) {
      throw new IllegalStateException(
        "Fichier RIPOL invalide ou vide dans les ressources : "
          + classpathLocation
          + " ("
          + header.length
          + " octets). Vérifier que Git LFS a bien été récupéré (git lfs pull) avant le build."
      );
    }
    String probe = new String(header, 0, Math.min(header.length, SQLITE_HEADER_PROBE_BYTES), StandardCharsets.US_ASCII);
    if (probe.startsWith(GIT_LFS_POINTER_PREFIX)) {
      throw new IllegalStateException(
        "Le fichier "
          + classpathLocation
          + " est un pointeur Git LFS, pas la base SQLite. Exécuter « git lfs pull » avant le build Maven."
      );
    }
    if (!probe.startsWith(SQLITE_MAGIC)) {
      throw new IllegalStateException(
        "Le fichier " + classpathLocation + " n'est pas une base SQLite valide (en-tête inattendu)."
      );
    }
  }

  private boolean incidentCodeHasColumn(String columnName) {
    try {
      return Boolean.TRUE.equals(
        jdbcTemplate.execute(
          (Connection conn) -> {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, TABLE_INCIDENT_CODE, columnName)) {
              return rs.next();
            }
          }
        )
      );
    } catch (DataAccessException e) {
      log.warn(
        "event=ripol_column_detection_failure traceId={} column={} error={}",
        MDC.get(TRACE_ID),
        columnName,
        e.getMessage(),
        e
      );
      return false;
    }
  }

  private void createSearchIndexes(IncidentCodeUsabilityFilter incidentCodeFilter) {
    try {
      jdbcTemplate.execute(
        "CREATE INDEX IF NOT EXISTS idx_tbin_grouptype ON TBINCIDENTCODE(GROUPTYPE)");
      jdbcTemplate.execute(
        "CREATE INDEX IF NOT EXISTS idx_tbin_master ON TBINCIDENTCODE(MASTERTYPE, MASTERVALUE)");
      jdbcTemplate.execute(
        "CREATE INDEX IF NOT EXISTS idx_tbin_grouptype_text ON TBINCIDENTCODE(GROUPTYPE, TEXT)");
      switch (incidentCodeFilter) {
        case ACTIVE_ONLY -> jdbcTemplate.execute(
            "CREATE INDEX IF NOT EXISTS idx_tbin_grouptype_usable ON TBINCIDENTCODE(GROUPTYPE) WHERE CAST(ACTIVE AS INTEGER) = 1");
        case SELECTABLE_ONLY -> jdbcTemplate.execute(
            "CREATE INDEX IF NOT EXISTS idx_tbin_grouptype_usable ON TBINCIDENTCODE(GROUPTYPE) WHERE CAST(SELECTABLE AS INTEGER) = 1");
        case ACTIVE_AND_SELECTABLE -> jdbcTemplate.execute(
            "CREATE INDEX IF NOT EXISTS idx_tbin_grouptype_usable ON TBINCIDENTCODE(GROUPTYPE) WHERE CAST(ACTIVE AS INTEGER) = 1 AND CAST(SELECTABLE AS INTEGER) = 1");
        case NONE -> { /* no partial index */ }
      }
      jdbcTemplate.execute(
        "CREATE INDEX IF NOT EXISTS idx_loc_locale_pk ON TBLOCALIZATION(LOCALE_ID, PK)");
    } catch (DataAccessException e) {
      log.warn(
        "event=ripol_index_creation_failure traceId={} error={}",
        MDC.get(TRACE_ID),
        e.getMessage(),
        e
      );
    }
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
    AllowedTable table = AllowedTable.parse(tableName);

    try {
      List<String> columns = jdbcTemplate.execute(
          (ConnectionCallback<List<String>>) conn -> extractColumns(conn, table.tableName));
      return columns != null ? columns : List.of();
    } catch (DataAccessException e) {
      log.error(
        "event=ripol_list_columns_failure traceId={} table={} error={}",
        MDC.get(TRACE_ID),
        table.tableName,
        e.getMessage(),
        e
      );
      throw new RipolAccessException("Erreur lors de la récupération des colonnes", e);
    }
  }

  @Override
  public List<Map<String, Object>> listTableContent(String tableName, int limit) {
    AllowedTable table = AllowedTable.parse(tableName);

    try {
      return switch (table) {
        case INCIDENT_CODE -> jdbcTemplate.query(
            incidentCodeQueries.selectAllWithLimit(),
            (rs, rowNum) -> mapRow(rs),
            limit
        );
        case LOCALIZATION -> jdbcTemplate.query(
            "SELECT * FROM TBLOCALIZATION LIMIT ?",
            (rs, rowNum) -> mapRow(rs),
            limit
        );
      };
    } catch (DataAccessException e) {
      log.error(
        "event=ripol_list_table_content_failure traceId={} table={} limit={} error={}",
        MDC.get(TRACE_ID),
        table.tableName,
        limit,
        e.getMessage(),
        e
      );
      throw new RipolAccessException("Erreur lors de la récupération du contenu", e);
    }
  }

  @Override
  public List<String> listDistinctGroupTypes(String tableName) {
    AllowedTable table = AllowedTable.parse(tableName);

    try {
      return switch (table) {
        case INCIDENT_CODE -> jdbcTemplate.queryForList(
            incidentCodeQueries.selectDistinctGroupTypes(),
            String.class
        );
        case LOCALIZATION -> jdbcTemplate.queryForList(
            "SELECT DISTINCT GROUPTYPE FROM TBLOCALIZATION ORDER BY GROUPTYPE",
            String.class
        );
      };
    } catch (DataAccessException e) {
      log.error(
        "event=ripol_list_group_types_failure traceId={} table={} error={}",
        MDC.get(TRACE_ID),
        table.tableName,
        e.getMessage(),
        e
      );
      throw new RipolAccessException("Erreur lors de la récupération des groupTypes", e);
    }
  }

  @Override
  public List<Map<String, Object>> listRowsByGroupType(String tableName, String groupType, int limit) {
    AllowedTable table = AllowedTable.parse(tableName);
    if (groupType == null || groupType.isBlank()) {
      throw new IllegalArgumentException("Le GROUPTYPE ne peut pas être vide");
    }

    try {
      return switch (table) {
        case INCIDENT_CODE -> jdbcTemplate.query(
            incidentCodeQueries.selectByGroupTypeWithLimit(),
            (rs, rowNum) -> mapRow(rs),
            groupType,
            limit
        );
        case LOCALIZATION -> jdbcTemplate.query(
            "SELECT * FROM TBLOCALIZATION WHERE GROUPTYPE = ? LIMIT ?",
            (rs, rowNum) -> mapRow(rs),
            groupType,
            limit
        );
      };
    } catch (DataAccessException e) {
      log.error(
        "event=ripol_list_rows_by_group_type_failure traceId={} table={} groupType={} limit={} error={}",
        MDC.get(TRACE_ID),
        table.tableName,
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
    try {
      List<Ripol> rows = jdbcTemplate.query(
          incidentCodeQueries.codesByGroupType(),
          RIPOL_ROW_MAPPER,
          groupType
      );
      if (DEDUPLICATE_BY_LABEL_GROUP_TYPES.contains(groupType)) {
        return deduplicateByDisplayLabel(rows);
      }
      return rows;
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

  private List<Ripol> queryCodesByGroupTypeWithSearch(String groupType, String likePattern) {
    List<Ripol> rows = jdbcTemplate.query(
      incidentCodeQueries.codesByGroupTypeSearch(),
      RIPOL_ROW_MAPPER,
      groupType,
      likePattern,
      likePattern,
      SEARCH_RESULT_LIMIT
    );
    if (DEDUPLICATE_BY_LABEL_GROUP_TYPES.contains(groupType)) {
      return deduplicateByDisplayLabel(rows);
    }
    return rows;
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
    try {
      List<Ripol> rows = jdbcTemplate.query(
          incidentCodeQueries.brandsByType(),
          RIPOL_ROW_MAPPER,
          masterType,
          masterType,
          masterValue
      );
      if (GROUP_TYPE_VEHICLE_BRAND.equals(masterType)) {
        return deduplicateByDisplayLabel(rows);
      }
      return rows;
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

  private List<Ripol> queryBrandsByTypeAndMasterTypeWithSearch(
      String masterValue, String masterType, String likePattern) {
    List<Ripol> rows = jdbcTemplate.query(
      incidentCodeQueries.brandsByTypeSearch(),
      RIPOL_ROW_MAPPER,
      masterType,
      masterType,
      masterValue,
      likePattern,
      likePattern,
      SEARCH_RESULT_LIMIT);
    if (GROUP_TYPE_VEHICLE_BRAND.equals(masterType)) {
      return deduplicateByDisplayLabel(rows);
    }
    return rows;
  }

  private List<Ripol> queryModelsByBrandWithSearch(String brandCode, String likePattern) {
    return jdbcTemplate.query(
      incidentCodeQueries.modelsByBrandSearch(),
      RIPOL_ROW_MAPPER,
      brandCode,
      likePattern,
      likePattern,
      SEARCH_RESULT_LIMIT
    );
  }

  private static String toPrefixLikePattern(String search) {
    return search.trim().toLowerCase(Locale.ROOT) + "%";
  }

  private static String toContainsLikePattern(String search) {
    return "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
  }

  @Override
  public List<Ripol> getModelsByBrand(String brandCode) {
    return modelsByBrandCache.computeIfAbsent(brandCode, this::queryModelsByBrand);
  }

  private List<Ripol> queryModelsByBrand(String brandCode) {
    try {
      return jdbcTemplate.query(
          incidentCodeQueries.modelsByBrand(),
          RIPOL_ROW_MAPPER,
          brandCode
      );
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
    if (search == null || search.isBlank()) {
      return List.of();
    }
    try {
      return searchWithPrefixThenContains(
        toPrefixLikePattern(search),
        toContainsLikePattern(search),
        pattern -> queryCodesByGroupTypeWithSearch(groupType, pattern)
      );
    } catch (DataAccessException e) {
      log.error(
        "event=ripol_search_codes_by_group_type_failure traceId={} groupType={} error={}",
        MDC.get(TRACE_ID),
        groupType,
        e.getMessage(),
        e
      );
      throw new RipolAccessException("Erreur lors de la recherche des codes RIPOL", e);
    }
  }

  @Override
  public List<Ripol> searchBrands(String masterValue, String masterType, String search) {
    if (search == null || search.isBlank()) {
      return List.of();
    }
    try {
      return searchWithPrefixThenContains(
        toPrefixLikePattern(search),
        toContainsLikePattern(search),
        pattern -> queryBrandsByTypeAndMasterTypeWithSearch(masterValue, masterType, pattern)
      );
    } catch (DataAccessException e) {
      log.error(
        "event=ripol_search_brands_failure traceId={} masterType={} masterValue={} error={}",
        MDC.get(TRACE_ID),
        masterType,
        masterValue,
        e.getMessage(),
        e
      );
      throw new RipolAccessException("Erreur lors de la recherche des marques RIPOL", e);
    }
  }

  @Override
  public List<Ripol> searchModels(String brandCode, String search) {
    if (search == null || search.isBlank()) {
      return List.of();
    }
    try {
      return searchWithPrefixThenContains(
        toPrefixLikePattern(search),
        toContainsLikePattern(search),
        pattern -> queryModelsByBrandWithSearch(brandCode, pattern)
      );
    } catch (DataAccessException e) {
      log.error(
        "event=ripol_search_models_failure traceId={} brandCode={} error={}",
        MDC.get(TRACE_ID),
        brandCode,
        e.getMessage(),
        e
      );
      throw new RipolAccessException("Erreur lors de la recherche des modèles RIPOL", e);
    }
  }

  private List<Ripol> searchWithPrefixThenContains(
      String prefixPattern,
      String containsPattern,
      java.util.function.Function<String, List<Ripol>> query
  ) {
    List<Ripol> results = query.apply(prefixPattern);
    if (!results.isEmpty() || prefixPattern.equals(containsPattern)) {
      return results;
    }
    return query.apply(containsPattern);
  }

  private List<Ripol> deduplicateByDisplayLabel(List<Ripol> items) {
    if (items == null || items.isEmpty()) {
      return List.of();
    }
    if (items.size() == 1) {
      return items;
    }
    Map<String, Ripol> uniqueByLabel = new LinkedHashMap<>();
    for (Ripol item : items) {
      uniqueByLabel.merge(displayLabelKey(item), item, this::preferCanonicalRipol);
    }
    return new ArrayList<>(uniqueByLabel.values());
  }

  private String displayLabelKey(Ripol ripol) {
    String label = ripol.labelFr();
    if (label == null || label.isBlank()) {
      label = ripol.labelDe();
    }
    if (label == null || label.isBlank()) {
      label = ripol.code();
    }
    return label.trim().toLowerCase(Locale.ROOT);
  }

  private Ripol preferCanonicalRipol(Ripol current, Ripol candidate) {
    return compareRipolCodes(current.code(), candidate.code()) <= 0 ? current : candidate;
  }

  private int compareRipolCodes(String left, String right) {
    if (left == null && right == null) {
      return 0;
    }
    if (left == null) {
      return 1;
    }
    if (right == null) {
      return -1;
    }
    try {
      return Long.compare(Long.parseLong(left), Long.parseLong(right));
    } catch (NumberFormatException ignored) {
      return left.compareToIgnoreCase(right);
    }
  }

  private Map<String, Object> mapRow(ResultSet rs) throws java.sql.SQLException {
    Map<String, Object> row = new LinkedHashMap<>();
    var metaData = rs.getMetaData();
    for (int i = 1; i <= metaData.getColumnCount(); i++) {
      row.put(metaData.getColumnName(i), rs.getObject(i));
    }
    return row;
  }

  private List<String> extractColumns(java.sql.Connection conn, String tableName) throws java.sql.SQLException {
    List<String> columns = new ArrayList<>();
    DatabaseMetaData metaData = conn.getMetaData();
    try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
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
        tableName
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
