package ch.ge.police.ui.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CorsConfigTest.TestConfig.class)
@WebAppConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CorsConfigTest {

  private MockMvc mockMvc;

  @Configuration
  @EnableWebMvc
  @Import(CorsConfig.class)
  static class TestConfig {
    @Bean TestApiController testApiController() { return new TestApiController(); }
    @Bean PublicController publicController() { return new PublicController(); }
  }

  @RestController
  static class TestApiController {
    @GetMapping(path = "/api/test", produces = MediaType.TEXT_PLAIN_VALUE)
    public String ok() { return "ok"; }
  }

  @RestController
  static class PublicController {
    @GetMapping(path = "/public/test", produces = MediaType.TEXT_PLAIN_VALUE)
    public String ok() { return "ok"; }
  }

  private static final String ALLOWED_ORIGIN = "http://localhost:5173";
  private static final String NOT_ALLOWED_ORIGIN = "https://evil.example.com";

  @BeforeEach
  void setup(WebApplicationContext wac) {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  @DisplayName("OPTIONS /api/** avec origin autorisé -> 200 + ACAO + methods + credentials")
  void preflightAllowed() throws Exception {
    var res = mockMvc.perform(options("/api/test")
                                .header("Origin", ALLOWED_ORIGIN)
                                .header("Access-Control-Request-Method", "GET")
                                .header("Access-Control-Request-Headers", "X-Requested-With"))
                     .andExpect(status().isOk())
                     .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN))
                     .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                     .andExpect(header().exists("Access-Control-Allow-Methods"))
                     .andExpect(header().exists("Vary"))
                     .andReturn()
                     .getResponse();

    var allowMethods = res.getHeader("Access-Control-Allow-Methods");
    org.assertj.core.api.Assertions.assertThat(allowMethods)
                                   .contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
    org.assertj.core.api.Assertions.assertThat(res.getHeaders("Vary"))
                                   .anyMatch(v -> v.contains("Origin"));
  }

  @Test
  @DisplayName("OPTIONS /api/** avec origin non autorisé -> 403")
  void preflightDenied() throws Exception {
    mockMvc.perform(options("/api/test")
                      .header("Origin", NOT_ALLOWED_ORIGIN)
                      .header("Access-Control-Request-Method", "GET"))
           .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /api/** origin autorisé -> 200 + ACAO + expose headers + credentials")
  void actualAllowed() throws Exception {
    mockMvc.perform(get("/api/test").header("Origin", ALLOWED_ORIGIN))
           .andExpect(status().isOk())
           .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN))
           .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
           .andExpect(header().string("Access-Control-Expose-Headers",
                                      allOf(
                                        containsString("Authorization"),
                                        containsString("X-Captcha-Token"),
                                        containsString("x-captcha-token")
                                      )));
  }

  @Test
  @DisplayName("GET /public/** (hors /api/**) -> pas d’en-têtes CORS ajoutés")
  void actualOutsideMapping() throws Exception {
    mockMvc.perform(get("/public/test").header("Origin", ALLOWED_ORIGIN))
           .andExpect(status().isOk())
           .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
           .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"))
           .andExpect(header().doesNotExist("Access-Control-Expose-Headers"));
  }

  @Test
  @DisplayName("GET /api/** avec origin non autorisé -> pas d’ACAO")
  void actualNotAllowedOrigin() throws Exception {
    mockMvc.perform(get("/api/test").header("Origin", NOT_ALLOWED_ORIGIN))
           .andExpect(status().isForbidden())
           .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
           .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"))
           .andExpect(header().doesNotExist("Access-Control-Expose-Headers"));
  }
}
