package ch.ge.police.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FriendlyCaptchaInterceptorTest {

  private MockMvc mockMvc;
  private FriendlyCaptchaVerifier verifier;
  private FriendlyCaptchaInterceptor interceptor;

  @RestController
  static class DummyController {
    @PostMapping({"/api/test", "/api/esirius/appointments"})
    public String test() {
      return "OK";
    }
  }

  @BeforeEach
  void setup() {
    verifier = Mockito.mock(FriendlyCaptchaVerifier.class);

    interceptor = new FriendlyCaptchaInterceptor(verifier);

    ReflectionTestUtils.setField(interceptor, "captchaEnabled", true);

    mockMvc = MockMvcBuilders
      .standaloneSetup(new DummyController())
      .addInterceptors(interceptor)
      .build();
  }

  @Test
  void shouldRejectRequestWhenCaptchaMissing() throws Exception {
    mockMvc.perform(post("/api/test")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectRequestWhenCaptchaInvalid() throws Exception {
    Mockito.when(verifier.verify("fake-token"))
      .thenReturn(new FriendlyCaptchaVerifier.CaptchaResult(false, List.of("invalid_or_expired"), null));

    mockMvc.perform(post("/api/test")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-Captcha-Token", "fake-token"))
      .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectRequestWhenCaptchaVerifyReturnsNull() throws Exception {
    Mockito.when(verifier.verify("null-result-token")).thenReturn(null);

    mockMvc.perform(post("/api/test")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-Captcha-Token", "null-result-token"))
      .andExpect(status().isBadRequest());
  }

  @Test
  void shouldAllowRequestWhenCaptchaValid() throws Exception {
    Mockito.when(verifier.verify("valid-token"))
      .thenReturn(new FriendlyCaptchaVerifier.CaptchaResult(true, List.of(), null));

    mockMvc.perform(post("/api/test")
        .contentType(MediaType.APPLICATION_JSON)
        .header("X-Captcha-Token", "valid-token"))
      .andExpect(status().isOk());
  }

  @Test
  void shouldAllowBypassWhenEnabled() throws Exception {
    ReflectionTestUtils.setField(interceptor, "captchaEnabled", false);

    mockMvc.perform(post("/api/test")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk());
  }

  @Test
  void shouldRejectBypassWhenDisabled() throws Exception {
    ReflectionTestUtils.setField(interceptor, "captchaEnabled", true);

    mockMvc.perform(post("/api/test")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }
}
