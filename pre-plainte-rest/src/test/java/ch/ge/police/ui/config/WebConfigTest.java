package ch.ge.police.ui.config;

import ch.ge.police.infrastructure.security.FriendlyCaptchaInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebConfigTest.TestConfig.class)
@WebAppConfiguration
class WebConfigTest {

  private MockMvc mockMvc;
  private WebApplicationContext wac;

  @Configuration
  @EnableWebMvc
  @Import(WebConfig.class)
  static class TestConfig {
    @Bean
    FriendlyCaptchaInterceptor captchaInterceptor() {
      return mock(FriendlyCaptchaInterceptor.class, Answers.RETURNS_DEFAULTS);
    }
    @Bean ApiController apiController() { return new ApiController(); }
    @Bean PublicController publicController() { return new PublicController(); }
  }

  @RestController
  static class ApiController {
    @GetMapping(value = "/api/test", produces = MediaType.TEXT_PLAIN_VALUE)
    public String ok() { return "ok"; }
  }

  @RestController
  static class PublicController {
    @GetMapping(value = "/public/test", produces = MediaType.TEXT_PLAIN_VALUE)
    public String ok() { return "ok"; }
  }

  @BeforeEach
  void setup(WebApplicationContext wac) {
    this.wac = wac;
    this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  @DisplayName("Quand le captcha est désactivé, l'intercepteur n'est pas présent dans la chaîne (ni /api/** ni /public/**)")
  void interceptorNotPresentWhenDisabled() throws Exception {
    var mapping = wac.getBean(RequestMappingHandlerMapping.class);
    var interceptor = wac.getBean(FriendlyCaptchaInterceptor.class);

    var chainApi = getChain(mapping, "GET", "/api/test");
    assertThat(chainApi).isNotNull();
    assertThat(Arrays.asList(chainApi.getInterceptors())).doesNotContain(interceptor);

    var chainPublic = getChain(mapping, "GET", "/public/test");
    assertThat(chainPublic).isNotNull();
    assertThat(Arrays.asList(chainPublic.getInterceptors())).doesNotContain(interceptor);
  }

  private HandlerExecutionChain getChain(RequestMappingHandlerMapping mapping, String method, String uri) throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest(method, uri);
    req.setServletPath(uri);
    return mapping.getHandler(req);
  }

  @Test
  @DisplayName("Sur /api/** l'intercepteur n'est pas invoqué quand il est désactivé (200)")
  void interceptorNotInvokedWhenDisabled() throws Exception {
    var interceptor = wac.getBean(FriendlyCaptchaInterceptor.class);

    mockMvc.perform(get("/api/test"))
           .andExpect(status().isOk())
           .andExpect(content().string("ok"));

    verify(interceptor, never()).preHandle(any(), any(), any());
  }
}
