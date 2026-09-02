package ch.ge.police.infrastructure.adapter.out;

import ch.ge.police.core.domain.model.ripol.Ripol;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteRipolAdapterTest {

  private static Path sqliteFile;
  private static Path brokenSqliteFile;

  @BeforeAll
  static void createSqliteDb() throws Exception {
    sqliteFile = Files.createTempFile("ripol-test-", ".db");
    sqliteFile.toFile().deleteOnExit();
    Class.forName("org.sqlite.JDBC");
    String jdbcUrl = "jdbc:sqlite:" + sqliteFile.toAbsolutePath();

    try (Connection conn = DriverManager.getConnection(jdbcUrl); Statement st = conn.createStatement()) {
      st.execute("""
          CREATE TABLE TBINCIDENTCODE (
            ID          INTEGER PRIMARY KEY,
            GROUPTYPE   TEXT,
            CODEVALUE   TEXT,
            TEXT        TEXT,
            MASTERTYPE  TEXT,
            MASTERVALUE TEXT
          )
        """);

      st.execute("""
          CREATE TABLE TBLOCALIZATION (
            PK          INTEGER,
            GROUPTYPE   TEXT,
            LOCALE_ID   INTEGER,
            TRANSLATION TEXT
          )
        """);

      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, MASTERTYPE, MASTERVALUE)
          VALUES (1, '11', 'CH', 'Schweiz', NULL, NULL)
        """);
      st.execute("""
          INSERT INTO TBLOCALIZATION (PK, GROUPTYPE, LOCALE_ID, TRANSLATION)
          VALUES (1, '11', 3, 'Suisse')
        """);

      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, MASTERTYPE, MASTERVALUE)
          VALUES (2, '11', 'FR', 'Frankreich', NULL, NULL)
        """);
      st.execute("""
          INSERT INTO TBLOCALIZATION (PK, GROUPTYPE, LOCALE_ID, TRANSLATION)
          VALUES (2, '11', 3, 'France')
        """);

      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, MASTERTYPE, MASTERVALUE)
          VALUES (3, '11', 'DE', 'Deutschland', NULL, NULL)
        """);

      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, MASTERTYPE, MASTERVALUE)
          VALUES (4, '11', 'IT', NULL, NULL, NULL)
        """);
      st.execute("""
          INSERT INTO TBLOCALIZATION (PK, GROUPTYPE, LOCALE_ID, TRANSLATION)
          VALUES (4, '11', 3, 'Italie')
        """);

      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, MASTERTYPE, MASTERVALUE)
          VALUES (10, 'geschlechtISO', 'M', 'Männlich', NULL, NULL)
        """);
      st.execute("""
          INSERT INTO TBLOCALIZATION (PK, GROUPTYPE, LOCALE_ID, TRANSLATION)
          VALUES (10, 'geschlechtISO', 3, 'Masculin')
        """);

      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, MASTERTYPE, MASTERVALUE)
          VALUES (20, '183', 'PHONE', 'Telefon', NULL, NULL)
        """);
      st.execute("""
          INSERT INTO TBLOCALIZATION (PK, GROUPTYPE, LOCALE_ID, TRANSLATION)
          VALUES (20, '183', 3, 'Téléphone')
        """);

      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, MASTERTYPE, MASTERVALUE)
          VALUES (21, '185', 'APPLE', 'Apple', '183', 'PHONE')
        """);
      st.execute("""
          INSERT INTO TBLOCALIZATION (PK, GROUPTYPE, LOCALE_ID, TRANSLATION)
          VALUES (21, '185', 3, 'Apple')
        """);

      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, MASTERTYPE, MASTERVALUE)
          VALUES (22, '185', 'SAMSUNG', 'Samsung', '183', 'PHONE')
        """);
      st.execute("""
          INSERT INTO TBLOCALIZATION (PK, GROUPTYPE, LOCALE_ID, TRANSLATION)
          VALUES (22, '185', 3, 'Samsung')
        """);

      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, MASTERTYPE, MASTERVALUE)
          VALUES (23, '999', 'IPHONE15', 'iPhone 15', '185', 'APPLE')
        """);
      st.execute("""
          INSERT INTO TBLOCALIZATION (PK, GROUPTYPE, LOCALE_ID, TRANSLATION)
          VALUES (23, '999', 3, 'iPhone 15')
        """);

      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, MASTERTYPE, MASTERVALUE)
          VALUES (24, '999', 'IPHONESE', 'iPhone SE', '185', 'APPLE')
        """);
      st.execute("""
          INSERT INTO TBLOCALIZATION (PK, GROUPTYPE, LOCALE_ID, TRANSLATION)
          VALUES (24, '999', 3, 'iPhone SE')
        """);

      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, MASTERTYPE, MASTERVALUE)
          VALUES (30, 'vehicle-brand', 'BMW', 'BMW', '101', 'CAR')
        """);
      st.execute("""
          INSERT INTO TBLOCALIZATION (PK, GROUPTYPE, LOCALE_ID, TRANSLATION)
          VALUES (30, 'vehicle-brand', 3, 'BMW')
        """);
    }

    brokenSqliteFile = Files.createTempFile("ripol-broken-test-", ".db");
    brokenSqliteFile.toFile().deleteOnExit();
    String brokenJdbcUrl = "jdbc:sqlite:" + brokenSqliteFile.toAbsolutePath();

    try (Connection conn = DriverManager.getConnection(brokenJdbcUrl); Statement st = conn.createStatement()) {
      st.execute("""
          CREATE TABLE TBINCIDENTCODE (
            ID INTEGER PRIMARY KEY,
            CODEVALUE TEXT
          )
        """);
      st.execute("""
          CREATE TABLE TBLOCALIZATION (
            PK INTEGER,
            LOCALE_ID INTEGER,
            TRANSLATION TEXT
          )
        """);
    }
  }

  @Test
  void listTables_shouldContainExpectedTables() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<String> tables = adapter.listTables();

    assertTrue(tables.contains("TBINCIDENTCODE"), "TBINCIDENTCODE missing, tables=" + tables);
    assertTrue(tables.contains("TBLOCALIZATION"), "TBLOCALIZATION missing, tables=" + tables);
  }

  @Test
  void listColumns_shouldReturnColumnsForWhitelistedTable() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<String> cols = adapter.listColumns("TBINCIDENTCODE");

    String joined = String.join(" | ", cols).toUpperCase(Locale.ROOT);
    assertTrue(joined.contains("CODEVALUE"), "Expected CODEVALUE in columns: " + cols);
    assertTrue(joined.contains("GROUPTYPE"), "Expected GROUPTYPE in columns: " + cols);
    assertTrue(joined.contains("TEXT"), "Expected TEXT in columns: " + cols);
  }

  @Test
  void listColumns_shouldAcceptLowercaseWhitelistedTable() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<String> cols = adapter.listColumns("tbincidentcode");

    assertFalse(cols.isEmpty());
  }

  @Test
  void getCodesByGroupType_shouldJoinLocalization_andCacheResult() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<Ripol> first = adapter.getCodesByGroupType("11");
    List<Ripol> second = adapter.getCodesByGroupType("11");

    assertSame(first, second);
    assertEquals(4, first.size());

    var byCode = first.stream().collect(java.util.stream.Collectors.toMap(Ripol::code, r -> r));

    assertEquals("France", byCode.get("FR").labelFr());
    assertEquals("Frankreich", byCode.get("FR").labelDe());

    assertEquals("Suisse", byCode.get("CH").labelFr());
    assertEquals("Schweiz", byCode.get("CH").labelDe());

    assertEquals("Deutschland", byCode.get("DE").labelFr());
    assertEquals("Deutschland", byCode.get("DE").labelDe());

    assertEquals("Italie", byCode.get("IT").labelFr());
    assertNull(byCode.get("IT").labelDe());
  }

  @ParameterizedTest
  @CsvSource({
    "sui, CH",
    "frank, FR",
    "ital, IT"
  })
  void searchCodesByGroupType_shouldFilterCaseInsensitiveOnFrenchAndGermanLabels(String search, String expectedCode)
    throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<Ripol> res = adapter.searchCodesByGroupType("11", search);

    assertEquals(1, res.size());
    assertEquals(expectedCode, res.getFirst().code());
  }

  @Test
  void searchCodesByGroupType_shouldReturnEmptyListWhenNoMatch() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<Ripol> res = adapter.searchCodesByGroupType("11", "zzz");

    assertTrue(res.isEmpty());
  }

  @Test
  void listDistinctGroupTypes_shouldWorkOnIncidentCodeTable() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<String> groupTypes = adapter.listDistinctGroupTypes("TBINCIDENTCODE");

    assertTrue(groupTypes.contains("11"));
    assertTrue(groupTypes.contains("geschlechtISO"));
    assertTrue(groupTypes.contains("183"));
  }

  @Test
  void listDistinctGroupTypes_shouldWorkOnLocalizationTable() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<String> groupTypes = adapter.listDistinctGroupTypes("TBLOCALIZATION");

    assertTrue(groupTypes.contains("11"));
    assertTrue(groupTypes.contains("geschlechtISO"));
  }

  @Test
  void listTableContent_shouldReturnRowsWithLimit_forIncidentCode() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<Map<String, Object>> rows = adapter.listTableContent("TBINCIDENTCODE", 2);

    assertEquals(2, rows.size());
    assertTrue(rows.getFirst().containsKey("GROUPTYPE"));
    assertTrue(rows.getFirst().containsKey("CODEVALUE"));
  }

  @Test
  void listTableContent_shouldReturnRowsWithLimit_forLocalization() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<Map<String, Object>> rows = adapter.listTableContent("TBLOCALIZATION", 2);

    assertEquals(2, rows.size());
    assertTrue(rows.getFirst().containsKey("PK"));
    assertTrue(rows.getFirst().containsKey("TRANSLATION"));
  }

  @Test
  void listRowsByGroupType_shouldReturnMatchingRows() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<Map<String, Object>> rows = adapter.listRowsByGroupType("TBINCIDENTCODE", "11", 10);

    assertEquals(4, rows.size());
    assertTrue(rows.stream().allMatch(r -> "11".equals(r.get("GROUPTYPE"))));
  }

  @Test
  void listRowsByGroupType_shouldRejectBlankGroupType() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    assertThrows(IllegalArgumentException.class, () -> adapter.listRowsByGroupType("TBINCIDENTCODE", "   ", 10));
  }

  @Test
  void getBrandsByType_shouldReturnBrandsForObjectType_andCacheResult() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<Ripol> first = adapter.getBrandsByType("PHONE");
    List<Ripol> second = adapter.getBrandsByType("PHONE");

    assertSame(first, second);
    assertEquals(2, first.size());

    var byCode = first.stream().collect(java.util.stream.Collectors.toMap(Ripol::code, r -> r));
    assertEquals("Apple", byCode.get("APPLE").labelFr());
    assertEquals("Samsung", byCode.get("SAMSUNG").labelFr());
    assertTrue(first.stream().allMatch(r -> "183".equals(r.groupeType())));
  }

  @Test
  void getBrandsByTypeAndMasterType_shouldReturnVehicleBrands() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<Ripol> brands = adapter.getBrandsByTypeAndMasterType("CAR", "101");

    assertEquals(1, brands.size());
    assertEquals("BMW", brands.getFirst().code());
    assertEquals("101", brands.getFirst().groupeType());
  }

  @Test
  void searchBrands_shouldFilterCaseInsensitive() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<Ripol> brands = adapter.searchBrands("PHONE", "183", "app");

    assertEquals(1, brands.size());
    assertEquals("APPLE", brands.getFirst().code());
  }

  @Test
  void searchBrands_shouldReturnEmptyListWhenNoMatch() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<Ripol> brands = adapter.searchBrands("PHONE", "183", "zzz");

    assertTrue(brands.isEmpty());
  }

  @Test
  void getModelsByBrand_shouldReturnModels_andCacheResult() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<Ripol> first = adapter.getModelsByBrand("APPLE");
    List<Ripol> second = adapter.getModelsByBrand("APPLE");

    assertSame(first, second);
    assertEquals(2, first.size());
    assertTrue(first.stream().allMatch(r -> "185".equals(r.groupeType())));
  }

  @Test
  void searchModels_shouldFilterCaseInsensitive() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<Ripol> models = adapter.searchModels("APPLE", "se");

    assertEquals(1, models.size());
    assertEquals("IPHONESE", models.getFirst().code());
  }

  @Test
  void searchModels_shouldReturnEmptyListWhenNoMatch() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<Ripol> models = adapter.searchModels("APPLE", "zzz");

    assertTrue(models.isEmpty());
  }

  @Test
  void getCodesByGroupType_shouldReturnEmptyListForUnknownGroupType() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<Ripol> res = adapter.getCodesByGroupType("UNKNOWN");

    assertTrue(res.isEmpty());
  }

  @Test
  void listColumns_shouldRejectNonWhitelistedTable() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    assertThrows(IllegalArgumentException.class, () -> adapter.listColumns("TBHACK"));
  }

  @Test
  void listColumns_shouldRejectBlankTableName() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    assertThrows(IllegalArgumentException.class, () -> adapter.listColumns(" "));
  }

  @Test
  void listTableContent_shouldRejectNonWhitelistedTable() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    assertThrows(IllegalArgumentException.class, () -> adapter.listTableContent("TBHACK", 10));
  }

  @Test
  void listTableContent_shouldRejectBlankTableName() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    assertThrows(IllegalArgumentException.class, () -> adapter.listTableContent("   ", 10));
  }

  @Test
  void listDistinctGroupTypes_shouldRejectNonWhitelistedTable() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    assertThrows(IllegalArgumentException.class, () -> adapter.listDistinctGroupTypes("TBHACK"));
  }

  @Test
  void listDistinctGroupTypes_shouldRejectBlankTableName() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    assertThrows(IllegalArgumentException.class, () -> adapter.listDistinctGroupTypes("   "));
  }

  @Test
  void listRowsByGroupType_shouldRejectNonWhitelistedTable() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    assertThrows(IllegalArgumentException.class, () -> adapter.listRowsByGroupType("TBHACK", "11", 10));
  }

  @Test
  void listRowsByGroupType_shouldRejectBlankTableName() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    assertThrows(IllegalArgumentException.class, () -> adapter.listRowsByGroupType("   ", "11", 10));
  }

  @Test
  void constructor_shouldFailWhenSourceIsUnavailable() {
    RipolDatabaseSource source = sourceVers(Path.of("does-not-exist.db"));

    assertThrows(IOException.class, () -> new SqliteRipolAdapter(source));
  }

  @Test
  void constructor_shouldFailWhenSourceIsNotASqliteDatabase() throws Exception {
    Path fichier = Files.createTempFile("ripol-invalide-", ".db");
    fichier.toFile().deleteOnExit();
    Files.writeString(fichier, "contenu qui n'est pas une base SQLite valide");

    assertThrows(IllegalStateException.class, () -> new SqliteRipolAdapter(sourceVers(fichier)));
  }

  @Test
  void getCodesByGroupType_shouldWrapJdbcErrorsInRipolAccessException() throws Exception {
    SqliteRipolAdapter adapter = newBrokenAdapterPointingToTempDb();

    assertThrows(SqliteRipolAdapter.RipolAccessException.class, () -> adapter.getCodesByGroupType("11"));
  }

  @Test
  void getBrandsByTypeAndMasterType_shouldWrapJdbcErrorsInRipolAccessException() throws Exception {
    SqliteRipolAdapter adapter = newBrokenAdapterPointingToTempDb();

    assertThrows(SqliteRipolAdapter.RipolAccessException.class, () -> adapter.getBrandsByTypeAndMasterType("PHONE", "183"));
  }

  @Test
  void getModelsByBrand_shouldWrapJdbcErrorsInRipolAccessException() throws Exception {
    SqliteRipolAdapter adapter = newBrokenAdapterPointingToTempDb();

    assertThrows(SqliteRipolAdapter.RipolAccessException.class, () -> adapter.getModelsByBrand("APPLE"));
  }

  @Test
  void listDistinctGroupTypes_shouldWrapJdbcErrorsInRipolAccessException_whenSchemaIsBroken() throws Exception {
    SqliteRipolAdapter adapter = newBrokenAdapterPointingToTempDb();

    assertThrows(SqliteRipolAdapter.RipolAccessException.class, () -> adapter.listDistinctGroupTypes("TBLOCALIZATION"));
  }

  @Test
  void listRowsByGroupType_shouldWrapJdbcErrorsInRipolAccessException_whenSchemaIsBroken() throws Exception {
    SqliteRipolAdapter adapter = newBrokenAdapterPointingToTempDb();

    assertThrows(SqliteRipolAdapter.RipolAccessException.class, () -> adapter.listRowsByGroupType("TBLOCALIZATION", "11", 10));
  }

  @Test
  void getCodesByGroupType_shouldNotDuplicateWhenSeveralFrenchLocalizationsExist() throws Exception {
    Path db = Files.createTempFile("ripol-dup-loc-", ".db");
    db.toFile().deleteOnExit();
    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + db.toAbsolutePath());
        Statement st = conn.createStatement()) {
      st.execute("""
          CREATE TABLE TBINCIDENTCODE (
            ID INTEGER PRIMARY KEY, GROUPTYPE TEXT, CODEVALUE TEXT, TEXT TEXT,
            MASTERTYPE TEXT, MASTERVALUE TEXT
          )
        """);
      st.execute("""
          CREATE TABLE TBLOCALIZATION (
            PK INTEGER, GROUPTYPE TEXT, LOCALE_ID INTEGER, TRANSLATION TEXT
          )
        """);
      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, MASTERTYPE, MASTERVALUE)
          VALUES (10, 'geschlechtISO', '1', 'Männlich', NULL, NULL)
        """);
      st.execute("""
          INSERT INTO TBLOCALIZATION (PK, GROUPTYPE, LOCALE_ID, TRANSLATION) VALUES
          (10, 'geschlechtISO', 3, 'Masculin'),
          (10, 'geschlechtISO', 3, 'Masculin bis')
        """);
    }

    SqliteRipolAdapter adapter = new SqliteRipolAdapter(sourceVers(db));

    List<Ripol> sexes = adapter.getCodesByGroupType("geschlechtISO");
    assertEquals(1, sexes.size());
    assertEquals("1", sexes.getFirst().code());
  }

  @Test
  void warmUpCache_shouldPreloadConfiguredGroupTypesWithoutThrowing() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    Method method = SqliteRipolAdapter.class.getDeclaredMethod("warmUpCache");
    method.setAccessible(true);

    assertDoesNotThrow(() -> method.invoke(adapter));

    assertFalse(adapter.getCodesByGroupType("11").isEmpty());
    assertFalse(adapter.getCodesByGroupType("geschlechtISO").isEmpty());
  }

  @Test
  void warmUpCacheAsync_shouldStartWithoutThrowing() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    Method method = SqliteRipolAdapter.class.getDeclaredMethod("warmUpCacheAsync");
    method.setAccessible(true);

    assertDoesNotThrow(() -> method.invoke(adapter));
  }

  @Test
  void warmUpCache_shouldSwallowExceptionsWhenPreloadingFails() throws Exception {
    SqliteRipolAdapter adapter = newBrokenAdapterPointingToTempDb();

    Method method = SqliteRipolAdapter.class.getDeclaredMethod("warmUpCache");
    method.setAccessible(true);

    assertDoesNotThrow(() -> method.invoke(adapter));
  }

  @Test
  void getBrandsByTypeAndMasterType_shouldReturnEmptyListForUnknownMasterValue() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<Ripol> brands = adapter.getBrandsByTypeAndMasterType("UNKNOWN", "183");

    assertTrue(brands.isEmpty());
  }

  @Test
  void getModelsByBrand_shouldReturnEmptyListForUnknownBrand() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<Ripol> models = adapter.getModelsByBrand("UNKNOWN");

    assertTrue(models.isEmpty());
  }

  @Test
  void getBrandsByTypeAndMasterType_shouldCacheResult() throws Exception {
    SqliteRipolAdapter adapter = newAdapterPointingToTempDb();

    List<Ripol> first = adapter.getBrandsByTypeAndMasterType("PHONE", "183");
    List<Ripol> second = adapter.getBrandsByTypeAndMasterType("PHONE", "183");

    assertSame(first, second);
  }

  @Test
  void getCodesByGroupType_shouldDeduplicateVehicleBrandsWithSameDisplayLabel() throws Exception {
    Path db = Files.createTempFile("ripol-dedup-test-", ".db");
    db.toFile().deleteOnExit();
    String jdbcUrl = "jdbc:sqlite:" + db.toAbsolutePath();

    try (Connection conn = DriverManager.getConnection(jdbcUrl); Statement st = conn.createStatement()) {
      st.execute("""
          CREATE TABLE TBINCIDENTCODE (
            ID INTEGER PRIMARY KEY,
            GROUPTYPE TEXT,
            CODEVALUE TEXT,
            TEXT TEXT,
            MASTERTYPE TEXT,
            MASTERVALUE TEXT
          )
        """);
      st.execute("""
          CREATE TABLE TBLOCALIZATION (
            PK INTEGER,
            GROUPTYPE TEXT,
            LOCALE_ID INTEGER,
            TRANSLATION TEXT
          )
        """);
      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT)
          VALUES (1, '102', '26662', 'AUDI')
        """);
      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT)
          VALUES (2, '102', '6604', 'AUDI')
        """);
      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT)
          VALUES (3, '102', '3780', 'BMW')
        """);
    }

    SqliteRipolAdapter adapter = new SqliteRipolAdapter(sourceVers(db));

    List<Ripol> brands = adapter.getCodesByGroupType("102");

    assertEquals(2, brands.size());
    assertEquals("6604", brands.stream().filter(r -> "AUDI".equals(r.labelDe())).findFirst().orElseThrow().code());
    assertTrue(brands.stream().anyMatch(r -> "BMW".equals(r.labelDe())));
  }

  @Test
  void getCodesByGroupType_shouldPreferFrenchLabelForVehicleColoursAndExcludeSystemCodes() throws Exception {
    Path db = Files.createTempFile("ripol-colour-fr-test-", ".db");
    db.toFile().deleteOnExit();
    String jdbcUrl = "jdbc:sqlite:" + db.toAbsolutePath();

    try (Connection conn = DriverManager.getConnection(jdbcUrl); Statement st = conn.createStatement()) {
      st.execute("""
          CREATE TABLE TBINCIDENTCODE (
            ID INTEGER PRIMARY KEY,
            GROUPTYPE TEXT,
            CODEVALUE TEXT,
            TEXT TEXT
          )
        """);
      st.execute("""
          CREATE TABLE TBLOCALIZATION (
            PK INTEGER,
            GROUPTYPE TEXT,
            LOCALE_ID INTEGER,
            TRANSLATION TEXT
          )
        """);
      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT)
          VALUES (1, '235', '5001', 'Effektlack')
        """);
      st.execute("""
          INSERT INTO TBLOCALIZATION (PK, GROUPTYPE, LOCALE_ID, TRANSLATION)
          VALUES (1, '235', 3, 'Effet laqué')
        """);
      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT)
          VALUES (2, '235', '9012', 'Eintrag gültig')
        """);
      st.execute("""
          INSERT INTO TBLOCALIZATION (PK, GROUPTYPE, LOCALE_ID, TRANSLATION)
          VALUES (2, '235', 3, 'Eintrag gültig')
        """);
    }

    SqliteRipolAdapter adapter = new SqliteRipolAdapter(sourceVers(db));

    List<Ripol> colours = adapter.getCodesByGroupType("235");

    assertEquals(1, colours.size());
    assertEquals("5001", colours.getFirst().code());
    assertEquals("Effet laqué", colours.getFirst().labelFr());
    assertEquals("Effektlack", colours.getFirst().labelDe());
  }

  @Test
  void getBrandsByTypeAndMasterType_shouldExcludeLongNumericOnlyModelLabels() throws Exception {
    Path db = Files.createTempFile("ripol-model-numeric-test-", ".db");
    db.toFile().deleteOnExit();
    String jdbcUrl = "jdbc:sqlite:" + db.toAbsolutePath();

    try (Connection conn = DriverManager.getConnection(jdbcUrl); Statement st = conn.createStatement()) {
      st.execute("""
          CREATE TABLE TBINCIDENTCODE (
            ID INTEGER PRIMARY KEY,
            GROUPTYPE TEXT,
            CODEVALUE TEXT,
            TEXT TEXT,
            MASTERTYPE TEXT,
            MASTERVALUE TEXT
          )
        """);
      st.execute("""
          CREATE TABLE TBLOCALIZATION (
            PK INTEGER,
            GROUPTYPE TEXT,
            LOCALE_ID INTEGER,
            TRANSLATION TEXT
          )
        """);
      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, MASTERTYPE, MASTERVALUE)
          VALUES (1, '198', '176', '100 Avant Quattro', '102', '4196')
        """);
      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, MASTERTYPE, MASTERVALUE)
          VALUES (2, '198', '104274', '123456789012345678901234567890', '102', '4196')
        """);
    }

    SqliteRipolAdapter adapter = new SqliteRipolAdapter(sourceVers(db));

    List<Ripol> models = adapter.getBrandsByTypeAndMasterType("4196", "102");

    assertEquals(1, models.size());
    assertEquals("176", models.getFirst().code());
    assertEquals("100 Avant Quattro", models.getFirst().labelDe());
  }

  @Test
  void getCodesByGroupType_shouldExcludeInactiveRowsWhenOnlyActiveColumnPresent() throws Exception {
    SqliteRipolAdapter adapter = newAdapterWithUsabilityColumns(
        "ACTIVE TEXT",
        "ACTIVE",
        "'1'",
        "'0'"
    );

    List<Ripol> codes = adapter.getCodesByGroupType("11");

    assertEquals(1, codes.size());
    assertEquals("CH", codes.getFirst().code());
  }

  @Test
  void getCodesByGroupType_shouldExcludeUnselectableRowsWhenOnlySelectableColumnPresent() throws Exception {
    SqliteRipolAdapter adapter = newAdapterWithUsabilityColumns(
        "SELECTABLE TEXT",
        "SELECTABLE",
        "'1'",
        "'0'"
    );

    List<Ripol> codes = adapter.getCodesByGroupType("11");

    assertEquals(1, codes.size());
    assertEquals("CH", codes.getFirst().code());
  }

  @Test
  void getCodesByGroupType_shouldExcludeInactiveRowsWhenActiveColumnPresent() throws Exception {
    Path db = Files.createTempFile("ripol-usable-test-", ".db");
    db.toFile().deleteOnExit();
    String jdbcUrl = "jdbc:sqlite:" + db.toAbsolutePath();

    try (Connection conn = DriverManager.getConnection(jdbcUrl); Statement st = conn.createStatement()) {
      st.execute("""
          CREATE TABLE TBINCIDENTCODE (
            ID INTEGER PRIMARY KEY,
            GROUPTYPE TEXT,
            CODEVALUE TEXT,
            TEXT TEXT,
            ACTIVE TEXT,
            SELECTABLE TEXT
          )
        """);
      st.execute("""
          CREATE TABLE TBLOCALIZATION (
            PK INTEGER,
            GROUPTYPE TEXT,
            LOCALE_ID INTEGER,
            TRANSLATION TEXT
          )
        """);
      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, ACTIVE, SELECTABLE)
          VALUES (1, '11', 'CH', 'Schweiz', '1', '1')
        """);
      st.execute("""
          INSERT INTO TBLOCALIZATION (PK, GROUPTYPE, LOCALE_ID, TRANSLATION)
          VALUES (1, '11', 3, 'Suisse')
        """);
      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, ACTIVE, SELECTABLE)
          VALUES (2, '11', 'XX', 'Inactive', '0', '1')
        """);
      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, ACTIVE, SELECTABLE)
          VALUES (3, '11', 'YY', 'Not selectable', '1', '0')
        """);
    }

    SqliteRipolAdapter adapter = new SqliteRipolAdapter(sourceVers(db));

    List<Ripol> codes = adapter.getCodesByGroupType("11");

    assertEquals(1, codes.size());
    assertEquals("CH", codes.getFirst().code());
  }

  private static SqliteRipolAdapter newAdapterWithUsabilityColumns(
      String extraColumnDefinitions,
      String extraColumnNames,
      String keptExtraValues,
      String rejectedExtraValues
  ) throws Exception {
    Path db = Files.createTempFile("ripol-usability-test-", ".db");
    db.toFile().deleteOnExit();
    String jdbcUrl = "jdbc:sqlite:" + db.toAbsolutePath();

    try (Connection conn = DriverManager.getConnection(jdbcUrl); Statement st = conn.createStatement()) {
      st.execute("""
          CREATE TABLE TBINCIDENTCODE (
            ID INTEGER PRIMARY KEY,
            GROUPTYPE TEXT,
            CODEVALUE TEXT,
            TEXT TEXT,
            MASTERTYPE TEXT,
            MASTERVALUE TEXT,
            %s
          )
        """.formatted(extraColumnDefinitions));
      st.execute("""
          CREATE TABLE TBLOCALIZATION (
            PK INTEGER,
            GROUPTYPE TEXT,
            LOCALE_ID INTEGER,
            TRANSLATION TEXT
          )
        """);
      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, %s)
          VALUES (1, '11', 'CH', 'Schweiz', %s)
        """.formatted(extraColumnNames, keptExtraValues));
      st.execute("""
          INSERT INTO TBLOCALIZATION (PK, GROUPTYPE, LOCALE_ID, TRANSLATION)
          VALUES (1, '11', 3, 'Suisse')
        """);
      st.execute("""
          INSERT INTO TBINCIDENTCODE (ID, GROUPTYPE, CODEVALUE, TEXT, %s)
          VALUES (2, '11', 'XX', 'Filtered', %s)
        """.formatted(extraColumnNames, rejectedExtraValues));
    }

    return new SqliteRipolAdapter(sourceVers(db));
  }

  private static SqliteRipolAdapter newAdapterPointingToTempDb() throws Exception {
    return new SqliteRipolAdapter(sourceVers(sqliteFile));
  }

  private static SqliteRipolAdapter newBrokenAdapterPointingToTempDb() throws Exception {
    return new SqliteRipolAdapter(sourceVers(brokenSqliteFile));
  }

  private static RipolDatabaseSource sourceVers(Path fichier) {
    return new RipolDatabaseSource() {
      @Override
      public InputStream ouvrirFlux() throws IOException {
        return Files.newInputStream(fichier);
      }

      @Override
      public String description() {
        return "test:" + fichier;
      }
    };
  }
}
