package ch.ge.police.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FriendlyCaptchaVerifierTest {

  private FriendlyCaptchaVerifier verifier;
  private RestTemplate restTemplate;

  private static final String SECRET = "sk_test_123";
  private static final String SITEKEY = "sitekey_abc";
  private static final String VERIFY_URL = "https://example.com";

  @BeforeEach
  void setUp() {
    verifier = new FriendlyCaptchaVerifier();
    ReflectionTestUtils.setField(verifier, "secret", SECRET);
    ReflectionTestUtils.setField(verifier, "sitekey", SITEKEY);
    ReflectionTestUtils.setField(verifier, "verifyUrl", VERIFY_URL);
    restTemplate = mock(RestTemplate.class);
    ReflectionTestUtils.setField(verifier, "restTemplate", restTemplate);
  }

  @Test
  void shouldReturnSuccess_whenApiReturnsSuccessTrue() {
    // given
    var responseBody = Map.of(
      "success", true,
      "errors", List.of("rate_limit_warning"),
      "data", Map.of("some", "payload")
    );
    when(restTemplate.exchange(
      eq(VERIFY_URL),
      eq(HttpMethod.POST),
      any(HttpEntity.class),
      any(ParameterizedTypeReference.class)
    ))
      .thenReturn(ResponseEntity.ok(responseBody));
    var result = verifier.verify("captchaToken123");
    assertThat(result.success()).isTrue();
    assertThat(result.errors()).containsExactly("rate_limit_warning");
    assertThat(result.data()).isInstanceOf(Map.class);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
    verify(restTemplate).exchange(
      eq(VERIFY_URL),
      eq(HttpMethod.POST),
      captor.capture(),
      any(ParameterizedTypeReference.class)
    );

    HttpEntity<Map<String, Object>> sent = captor.getValue();
    assertThat(sent.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    assertThat(sent.getHeaders().getFirst("X-API-Key")).isEqualTo(SECRET);
    assertThat(sent.getBody())
      .containsEntry("response", "captchaToken123")
      .containsEntry("sitekey", SITEKEY);
  }

  @Test
  void shouldReturnNoResponse_whenBodyIsNull() {
    when(restTemplate.exchange(
      eq(VERIFY_URL),
      eq(HttpMethod.POST),
      any(HttpEntity.class),
      any(ParameterizedTypeReference.class)
    ))
      .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));
    var result = verifier.verify("token");
    assertThat(result.success()).isFalse();
    assertThat(result.errors()).containsExactly("no_response");
    assertThat(result.data()).isNull();
  }

  @Test
  void shouldMapHttpClientErrorException_toHttpErrorCode() {
    var ex = HttpClientErrorException.create(
      HttpStatus.BAD_REQUEST, "Bad Request",
      HttpHeaders.EMPTY, "oops".getBytes(), null
    );
    when(restTemplate.exchange(
      eq(VERIFY_URL),
      eq(HttpMethod.POST),
      any(HttpEntity.class),
      any(ParameterizedTypeReference.class)
    ))
      .thenThrow(ex);
    var result = verifier.verify("token");
    assertThat(result.success()).isFalse();
    assertThat(result.errors()).hasSize(1);
    assertThat(result.errors().getFirst()).startsWith("http_error_");
    assertThat(result.data()).isNull();
  }

  @Test
  void shouldReturnUnexpectedError_onGenericException() {
    when(restTemplate.exchange(
      eq(VERIFY_URL),
      eq(HttpMethod.POST),
      any(HttpEntity.class),
      any(ParameterizedTypeReference.class)
    ))
      .thenThrow(new RuntimeException("boom"));
    var result = verifier.verify("token");
    assertThat(result.success()).isFalse();
    assertThat(result.errors()).containsExactly("unexpected_error");
    assertThat(result.data()).isNull();
  }
}
