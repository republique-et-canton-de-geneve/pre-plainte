package ch.ge.police.infrastructure.adapter.out;

import ch.ge.police.core.port.out.EsiriusPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Component
public class EsiriusWebClientAdapter implements EsiriusPort {

  private static final String DETAILS_KEY = "details";
  private static final String SSL_INITIALIZATION_ERROR_MESSAGE = "Erreur SSL eSirius";
  private static final int INTERNAL_SERVER_ERROR_CODE = 500;
  private static final int HTTP_STATUS_OK = 200;
  private static final int HTTP_STATUS_NO_CONTENT = 204;
  private static final int JSON_WRAPPER_TRIM_LENGTH = 2;
  private static final int IN_MEMORY_BUFFER_MEGABYTES = 16;
  private static final int KILOBYTE_SIZE = 1024;
  private static final int MAX_IN_MEMORY_SIZE = IN_MEMORY_BUFFER_MEGABYTES * KILOBYTE_SIZE * KILOBYTE_SIZE;
  private static final String LANGUAGE_HEADER = "language";
  private static final String COUNTRY_HEADER = "country";
  private static final String LANGUAGE_VALUE = "fr";
  private static final String COUNTRY_VALUE = "fr";
  private static final String TRACE_ID = "traceId";

  private final WebClient webClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${esirius.base_url}")
  private String baseUrl;

  public EsiriusWebClientAdapter(
    @Value("${esirius.base_url}") String baseUrl,
    @Value("${esirius.username}") String username,
    @Value("${esirius.password}") String password
  ) {
    try {
      SslContext sslContext = SslContextBuilder
        .forClient()
        .trustManager(InsecureTrustManagerFactory.INSTANCE)
        .build();

      HttpClient httpClient = HttpClient.create()
        .secure(ssl -> ssl.sslContext(sslContext));

      ExchangeStrategies strategies = ExchangeStrategies.builder()
        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
        .build();

      String basicAuthValue = Base64.getEncoder().encodeToString(
        (username + ":" + password).getBytes(StandardCharsets.UTF_8)
      );

      this.webClient = WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .exchangeStrategies(strategies)
        .baseUrl(baseUrl)
        .defaultHeader("Accept", "application/json; charset=utf-8")
        .defaultHeader("Content-Type", "application/json; charset=utf-8")
        .defaultHeader("Authorization", "Basic " + basicAuthValue)
        .build();
    } catch (Exception e) {
      throw new EsiriusInitializationException(SSL_INITIALIZATION_ERROR_MESSAGE, e);
    }
  }

  private void logRequestStart(HttpMethod method, String path) {
    if (log.isDebugEnabled()) {
      log.debug(
        "event=esirius_request_start traceId={} method={} path={}",
        MDC.get(TRACE_ID),
        method.name(),
        path
      );
    }
  }

  private void logRequestSuccess(HttpMethod method, String path, int status, Duration duration) {
    log.info(
      "event=esirius_request_success traceId={} method={} path={} status={} durationMs={}",
      MDC.get(TRACE_ID),
      method.name(),
      path,
      status,
      duration.toMillis()
    );
  }

  private void logRequestFailure(HttpMethod method, String path, int status, String details, Duration duration, Exception e) {
    log.error(
      "event=esirius_request_failure traceId={} method={} path={} status={} durationMs={} details={}",
      MDC.get(TRACE_ID),
      method.name(),
      path,
      status,
      duration.toMillis(),
      sanitize(details),
      e
    );
  }

  private String sanitize(String value) {
    if (value == null) {
      return null;
    }
    return value.replaceAll("[\\r\\n]+", " ");
  }

  private List<Map<String, Object>> getList(String path, boolean specialParsing) {
    long start = System.nanoTime();
    HttpMethod method = HttpMethod.GET;
    logRequestStart(method, path);

    try {
      String response = webClient.get()
        .uri(path)
        .retrieve()
        .bodyToMono(String.class)
        .block();

      Duration duration = Duration.ofNanos(System.nanoTime() - start);
      logRequestSuccess(method, path, HTTP_STATUS_OK, duration);

      if (response == null || response.isEmpty()) {
        return Collections.emptyList();
      }

      if (specialParsing && response.startsWith("[\"")) {
        response = response.substring(JSON_WRAPPER_TRIM_LENGTH, response.length() - JSON_WRAPPER_TRIM_LENGTH)
          .replace("\\\"", "\"");
      }

      return objectMapper.readValue(response, new TypeReference<>() {});
    } catch (WebClientResponseException e) {
      Duration duration = Duration.ofNanos(System.nanoTime() - start);
      logRequestFailure(method, path, e.getStatusCode().value(), e.getResponseBodyAsString(), duration, e);
      return List.of(Map.of("code", e.getStatusCode().value(), DETAILS_KEY, e.getResponseBodyAsString()));
    } catch (Exception e) {
      Duration duration = Duration.ofNanos(System.nanoTime() - start);
      logRequestFailure(method, path, INTERNAL_SERVER_ERROR_CODE, e.getMessage(), duration, e);
      return List.of(Map.of("code", INTERNAL_SERVER_ERROR_CODE, DETAILS_KEY, e.getMessage()));
    }
  }

  private Map<String, Object> sendWithBody(HttpMethod method, String path, Map<String, Object> body) {
    long start = System.nanoTime();
    logRequestStart(method, path);

    try {
      String response = (method == HttpMethod.POST ? webClient.post() : webClient.put())
        .uri(path)
        .header(LANGUAGE_HEADER, LANGUAGE_VALUE)
        .header(COUNTRY_HEADER, COUNTRY_VALUE)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(String.class)
        .block();

      Duration duration = Duration.ofNanos(System.nanoTime() - start);
      logRequestSuccess(method, path, HTTP_STATUS_OK, duration);

      if (response == null || response.isEmpty()) {
        return Map.of("code", HTTP_STATUS_NO_CONTENT, "message", "Réponse vide");
      }

      if (!response.trim().startsWith("{") && !response.trim().startsWith("[")) {
        return Map.of("code", HTTP_STATUS_OK, "codeRdv", response);
      }

      return objectMapper.readValue(response, new TypeReference<>() {});
    } catch (WebClientResponseException e) {
      Duration duration = Duration.ofNanos(System.nanoTime() - start);
      logRequestFailure(method, path, e.getStatusCode().value(), e.getResponseBodyAsString(), duration, e);
      return Map.of("code", e.getStatusCode().value(), DETAILS_KEY, e.getResponseBodyAsString());
    } catch (Exception e) {
      Duration duration = Duration.ofNanos(System.nanoTime() - start);
      logRequestFailure(method, path, INTERNAL_SERVER_ERROR_CODE, e.getMessage(), duration, e);
      return Map.of("code", INTERNAL_SERVER_ERROR_CODE, DETAILS_KEY, e.getMessage());
    }
  }

  private Map<String, Object> sendWithoutBody(HttpMethod method, String path) {
    long start = System.nanoTime();
    logRequestStart(method, path);

    try {
      String response = switch (method.name()) {
        case "GET" -> webClient.get().uri(path).retrieve().bodyToMono(String.class).block();
        case "DELETE" -> webClient.delete().uri(path).retrieve().bodyToMono(String.class).block();
        default -> throw new IllegalArgumentException("Méthode non supportée");
      };

      Duration duration = Duration.ofNanos(System.nanoTime() - start);
      logRequestSuccess(method, path, HTTP_STATUS_OK, duration);

      if (response == null || response.isEmpty()) {
        return Map.of("code", HTTP_STATUS_NO_CONTENT, "message", "Annulation confirmée ! Votre demande a été traitée avec succès.");
      }

      return objectMapper.readValue(response, new TypeReference<>() {});
    } catch (WebClientResponseException e) {
      Duration duration = Duration.ofNanos(System.nanoTime() - start);
      logRequestFailure(method, path, e.getStatusCode().value(), e.getResponseBodyAsString(), duration, e);
      return Map.of("code", e.getStatusCode().value(), DETAILS_KEY, e.getResponseBodyAsString());
    } catch (Exception e) {
      Duration duration = Duration.ofNanos(System.nanoTime() - start);
      logRequestFailure(method, path, INTERNAL_SERVER_ERROR_CODE, e.getMessage(), duration, e);
      return Map.of("code", INTERNAL_SERVER_ERROR_CODE, DETAILS_KEY, e.getMessage());
    }
  }

  @Override
  public List<Map<String, Object>> getAllSites() {
    return getList("/ePlanning/webservices/api/sites", true);
  }

  @Override
  public List<Map<String, Object>> getServiceListBySiteCode(String siteCode) {
    return getList("/ePlanning/webservices/api/sites/" + siteCode + "/listServices", true);
  }

  @Override
  public List<Map<String, Object>> getAvailabilityBySiteCode(String siteCode, String serviceId, String begin, String period) {
    String path = String.format(
      "/ePlanning/webservices/api/sites/%s/services/%s/plannings/begins/%s/periods/%s/availabilities",
      siteCode, serviceId, begin, period
    );
    return getList(path, false);
  }

  @Override
  public Map<String, Object> createAppointment(Map<String, Object> appointmentData) {
    return sendWithBody(HttpMethod.POST, "/ePlanning/webservices/api/appointments", appointmentData);
  }

  @Override
  public Map<String, Object> updateAppointmentByRdvCode(Map<String, Object> appointmentData) {
    return sendWithBody(HttpMethod.PUT, "/ePlanning/webservices/api/appointments", appointmentData);
  }

  @Override
  public Map<String, Object> getInfoByRdvCode(String codeRdv) {
    return sendWithoutBody(HttpMethod.GET, "/ePlanning/webservices/api/appointments/" + codeRdv);
  }

  @Override
  public Map<String, Object> cancelAppointmentByRdvCode(String codeRdv) {
    return sendWithoutBody(HttpMethod.DELETE, "/ePlanning/webservices/api/appointments/" + codeRdv);
  }

  public static class EsiriusInitializationException extends RuntimeException {
    public EsiriusInitializationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
