package ch.ge.police.ui.main;

import ch.ge.police.core.domain.model.PrePlainte;
import ch.ge.police.core.domain.model.common.EmailChallenge;
import ch.ge.police.core.domain.model.event.common.TypeIncident;
import ch.ge.police.core.domain.model.event.vol.Vol;
import ch.ge.police.infrastructure.adapter.out.SqliteRipolAdapter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.s3.S3Client;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
  classes = PpelFormulaireApiApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = {"esirius.base_url=http://localhost", "captcha.enabled=false"}
)
class BootMigrationTest {

  @Autowired
  private JsonMapper mapper;

  @Autowired
  private Environment environment;

  @MockitoBean
  private S3Client s3Client;

  @MockitoBean
  private SqliteRipolAdapter ripolAdapter;

  @Test
  void applicationShouldServeJsonConfiguration() throws Exception {
    String port = environment.getRequiredProperty("local.server.port");
    try (HttpClient client = HttpClient.newHttpClient()) {
      HttpResponse<String> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/config")).GET().build(),
        HttpResponse.BodyHandlers.ofString()
      );
      assertEquals(200, response.statusCode());
      assertTrue(response.headers().firstValue("Content-Type").orElseThrow().contains("application/json"));
      assertEquals("false", mapper.readTree(response.body()).get("captchaEnabled").asText());
    }
  }

  @Test
  void mapperShouldReadExistingDraftsAndPreserveIncidentTypes() {
    String draft = """
      {"demandeId":"draft-123","incident":{"typeIncident":"vol",
       "details":{"typeIncident":"vol","dateDebutEvent":"2026-09-24"}}}
      """;
    PrePlainte plainte = mapper.readValue(draft, PrePlainte.class);
    assertEquals("draft-123", plainte.getDemandeId());
    assertInstanceOf(Vol.class, plainte.getIncident().getDetails());
    assertEquals(TypeIncident.VOL, plainte.getIncident().getTypeIncident());
    PrePlainte restored = mapper.readValue(mapper.writeValueAsBytes(plainte), PrePlainte.class);
    assertEquals("2026-09-24", restored.getIncident().getDetails().getDateDebutEvent());
    assertEquals(TypeIncident.VOL, restored.getIncident().getTypeIncident());
  }

  @Test
  void mapperShouldPreserveEmailChallengeDatesAndHashes() {
    Instant createdAt = Instant.parse("2026-09-24T12:00:00Z");
    EmailChallenge challenge = EmailChallenge.builder()
      .email("test@example.test").codeHash("existing-bcrypt-hash")
      .createdAt(createdAt).expiresAt(createdAt.plusSeconds(3600)).attempts(2).build();
    String json = mapper.writeValueAsString(challenge);
    assertEquals("2026-09-24T12:00:00Z", mapper.readTree(json).get("createdAt").asText());
    EmailChallenge restored = mapper.readValue(json, EmailChallenge.class);
    assertEquals(createdAt, restored.getCreatedAt());
    assertEquals(challenge.getExpiresAt(), restored.getExpiresAt());
    assertEquals(challenge.getCodeHash(), restored.getCodeHash());
    assertEquals(2, restored.getAttempts());
  }
}
