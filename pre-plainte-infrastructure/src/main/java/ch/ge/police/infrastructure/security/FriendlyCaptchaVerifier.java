package ch.ge.police.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class FriendlyCaptchaVerifier {

  private static final Logger log = LoggerFactory.getLogger(FriendlyCaptchaVerifier.class);

  @Value("${friendlycaptcha.secret}")
  private String secret = "";

  @Value("${friendlycaptcha.sitekey}")
  private String sitekey = "";

  @Value("${friendlycaptcha.verify-url}")
  private String verifyUrl = "";

  private final RestTemplate restTemplate = new RestTemplate();

  public CaptchaResult verify(String captchaToken) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("X-API-Key", secret);


      Map<String, Object> body = Map.of(
        "response", captchaToken,
        "sitekey", sitekey
      );

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

      ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
        verifyUrl,
        HttpMethod.POST,
        request,
        new ParameterizedTypeReference<Map<String, Object>>() {}
      );

      Map<String, Object> responseBody = response.getBody();
      if (responseBody == null) {
        return new CaptchaResult(false, List.of("no_response"), null);
      }

      Boolean success = (Boolean) responseBody.get("success");

      Object errorsObj = responseBody.get("errors");
      List<String> errors = extractErrors(errorsObj);

      Object dataObj = responseBody.get("data");
      log.info("FriendlyCaptcha verify result: success={}, errors={}, data={}", success, errors, dataObj);

      return new CaptchaResult(Boolean.TRUE.equals(success), errors, dataObj);

    } catch (HttpClientErrorException e) {
      log.error("FriendlyCaptcha verification failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
      return new CaptchaResult(false, List.of("http_error_" + e.getStatusCode()), null);
    } catch (Exception e) {
      log.error("Unexpected error while verifying captcha", e);
      return new CaptchaResult(false, List.of("unexpected_error"), null);
    }
  }

  private List<String> extractErrors(Object errorsObj) {
    if (!(errorsObj instanceof List<?> errorsList)) {
      return Collections.emptyList();
    }
    return errorsList.stream()
      .filter(String.class::isInstance)
      .map(String.class::cast)
      .toList();
  }

  public record CaptchaResult(boolean success, List<String> errors, Object data) {}
}
