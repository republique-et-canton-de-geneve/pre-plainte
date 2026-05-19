package ch.ge.police.infrastructure.adapter.out;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EsiriusWebClientAdapterTest {

  private static final String BASE_URL = "https://ignored";
  private static final String USERNAME = "u";
  private static final String PASSWORD = "p";

  @Test
  void getAllSites_shouldApplySpecialParsing_andParseJson() {
    EsiriusWebClientAdapter adapter = newAdapter();

    WebClient webClient = mockWebClientGetReturning("[\"[{\\\"code\\\":\\\"SITE1\\\"}]\"]");
    ReflectionTestUtils.setField(adapter, "webClient", webClient);

    List<Map<String, Object>> sites = adapter.getAllSites();

    assertEquals(1, sites.size());
    assertEquals("SITE1", String.valueOf(sites.getFirst().get("code")));
  }

  @Test
  void getAllSites_shouldReturnEmptyListWhenResponseIsEmpty() {
    EsiriusWebClientAdapter adapter = newAdapter();

    WebClient webClient = mockWebClientGetReturning("");
    ReflectionTestUtils.setField(adapter, "webClient", webClient);

    List<Map<String, Object>> sites = adapter.getAllSites();

    assertTrue(sites.isEmpty());
  }

  @Test
  void getServiceListBySiteCode_shouldParseJsonWithoutSpecialParsing() {
    EsiriusWebClientAdapter adapter = newAdapter();

    WebClient webClient = mockWebClientGetReturning("[{\"code\":\"SERVICE1\"}]");
    ReflectionTestUtils.setField(adapter, "webClient", webClient);

    List<Map<String, Object>> services = adapter.getServiceListBySiteCode("SITE1");

    assertEquals(1, services.size());
    assertEquals("SERVICE1", String.valueOf(services.getFirst().get("code")));
  }

  @Test
  void getAvailabilityBySiteCode_shouldParseJsonWithoutSpecialParsing() {
    EsiriusWebClientAdapter adapter = newAdapter();

    WebClient webClient = mockWebClientGetReturning("[{\"slot\":\"2026-04-07T09:00\"}]");
    ReflectionTestUtils.setField(adapter, "webClient", webClient);

    List<Map<String, Object>> availabilities = adapter.getAvailabilityBySiteCode("SITE1", "S1", "2026-04-07", "7");

    assertEquals(1, availabilities.size());
    assertEquals("2026-04-07T09:00", String.valueOf(availabilities.getFirst().get("slot")));
  }

  @Test
  void getInfoByRdvCode_whenWebClientResponseException_shouldReturnCodeAndDetails() {
    EsiriusWebClientAdapter adapter = newAdapter();

    WebClientResponseException ex = WebClientResponseException.create(400, "Bad Request", null, "oops".getBytes(), null);

    WebClient webClient = mockWebClientGetError(ex);
    ReflectionTestUtils.setField(adapter, "webClient", webClient);

    Map<String, Object> res = adapter.getInfoByRdvCode("RDVBAD");

    assertEquals(400, ((Number) res.get("code")).intValue());
    assertEquals("oops", String.valueOf(res.get("details")));
  }

  @Test
  void getInfoByRdvCode_whenGenericException_shouldReturnInternalServerError() {
    EsiriusWebClientAdapter adapter = newAdapter();

    WebClient webClient = mockWebClientGetError(new RuntimeException("boom"));
    ReflectionTestUtils.setField(adapter, "webClient", webClient);

    Map<String, Object> res = adapter.getInfoByRdvCode("RDVERR");

    assertEquals(500, ((Number) res.get("code")).intValue());
    assertEquals("boom", String.valueOf(res.get("details")));
  }

  @Test
  void createAppointment_whenResponseIsPlainText_shouldReturnCodeRdv() {
    EsiriusWebClientAdapter adapter = newAdapter();

    WebClient webClient = mockWebClientSendWithBodyReturning(true, "RDV123456");
    ReflectionTestUtils.setField(adapter, "webClient", webClient);

    Map<String, Object> res = adapter.createAppointment(Map.of("x", "y"));

    assertEquals(200, ((Number) res.get("code")).intValue());
    assertEquals("RDV123456", String.valueOf(res.get("codeRdv")));
  }

  @Test
  void createAppointment_whenResponseIsJson_shouldParseJson() {
    EsiriusWebClientAdapter adapter = newAdapter();

    WebClient webClient = mockWebClientSendWithBodyReturning(true, "{\"code\":200,\"status\":\"OK\"}");
    ReflectionTestUtils.setField(adapter, "webClient", webClient);

    Map<String, Object> res = adapter.createAppointment(Map.of("x", "y"));

    assertEquals(200, ((Number) res.get("code")).intValue());
    assertEquals("OK", String.valueOf(res.get("status")));
  }

  @Test
  void createAppointment_whenResponseIsEmpty_shouldReturnNoContentMessage() {
    EsiriusWebClientAdapter adapter = newAdapter();

    WebClient webClient = mockWebClientSendWithBodyReturning(true, "");
    ReflectionTestUtils.setField(adapter, "webClient", webClient);

    Map<String, Object> res = adapter.createAppointment(Map.of("x", "y"));

    assertEquals(204, ((Number) res.get("code")).intValue());
    assertEquals("Réponse vide", String.valueOf(res.get("message")));
  }

  @Test
  void createAppointment_whenWebClientResponseException_shouldReturnCodeAndDetails() {
    EsiriusWebClientAdapter adapter = newAdapter();

    WebClientResponseException ex = WebClientResponseException.create(409, "Conflict", null, "duplicate".getBytes(), null);

    WebClient webClient = mockWebClientSendWithBodyError(true, ex);
    ReflectionTestUtils.setField(adapter, "webClient", webClient);

    Map<String, Object> res = adapter.createAppointment(Map.of("x", "y"));

    assertEquals(409, ((Number) res.get("code")).intValue());
    assertEquals("duplicate", String.valueOf(res.get("details")));
  }

  @Test
  void createAppointment_whenGenericException_shouldReturnInternalServerError() {
    EsiriusWebClientAdapter adapter = newAdapter();

    WebClient webClient = mockWebClientSendWithBodyError(true, new RuntimeException("boom"));
    ReflectionTestUtils.setField(adapter, "webClient", webClient);

    Map<String, Object> res = adapter.createAppointment(Map.of("x", "y"));

    assertEquals(500, ((Number) res.get("code")).intValue());
    assertEquals("boom", String.valueOf(res.get("details")));
  }

  @Test
  void updateAppointmentByRdvCode_whenResponseIsJson_shouldParseJson() {
    EsiriusWebClientAdapter adapter = newAdapter();

    WebClient webClient = mockWebClientSendWithBodyReturning(false, "{\"code\":200,\"updated\":true}");
    ReflectionTestUtils.setField(adapter, "webClient", webClient);

    Map<String, Object> res = adapter.updateAppointmentByRdvCode(Map.of("x", "y"));

    assertEquals(200, ((Number) res.get("code")).intValue());
    assertEquals(Boolean.TRUE, res.get("updated"));
  }

  @Test
  void updateAppointmentByRdvCode_whenWebClientResponseException_shouldReturnCodeAndDetails() {
    EsiriusWebClientAdapter adapter = newAdapter();

    WebClientResponseException ex = WebClientResponseException.create(422, "Unprocessable Entity", null, "invalid".getBytes(), null);

    WebClient webClient = mockWebClientSendWithBodyError(false, ex);
    ReflectionTestUtils.setField(adapter, "webClient", webClient);

    Map<String, Object> res = adapter.updateAppointmentByRdvCode(Map.of("x", "y"));

    assertEquals(422, ((Number) res.get("code")).intValue());
    assertEquals("invalid", String.valueOf(res.get("details")));
  }

  @Test
  void cancelAppointment_whenEmptyBody_shouldReturnNoContentMessage() {
    EsiriusWebClientAdapter adapter = newAdapter();

    WebClient webClient = mockWebClientDeleteReturning("");
    ReflectionTestUtils.setField(adapter, "webClient", webClient);

    Map<String, Object> res = adapter.cancelAppointmentByRdvCode("RDVEMPTY");

    assertEquals(204, ((Number) res.get("code")).intValue());
    assertTrue(String.valueOf(res.get("message")).contains("Annulation confirmée"));
  }

  @Test
  void cancelAppointment_whenJsonBody_shouldParseJson() {
    EsiriusWebClientAdapter adapter = newAdapter();

    WebClient webClient = mockWebClientDeleteReturning("{\"code\":200,\"cancelled\":true}");
    ReflectionTestUtils.setField(adapter, "webClient", webClient);

    Map<String, Object> res = adapter.cancelAppointmentByRdvCode("RDVOK");

    assertEquals(200, ((Number) res.get("code")).intValue());
    assertEquals(Boolean.TRUE, res.get("cancelled"));
  }

  @Test
  void cancelAppointment_whenGenericException_shouldReturnInternalServerError() {
    EsiriusWebClientAdapter adapter = newAdapter();

    WebClient webClient = mockWebClientDeleteError(new RuntimeException("boom"));
    ReflectionTestUtils.setField(adapter, "webClient", webClient);

    Map<String, Object> res = adapter.cancelAppointmentByRdvCode("RDVERR");

    assertEquals(500, ((Number) res.get("code")).intValue());
    assertEquals("boom", String.valueOf(res.get("details")));
  }

  @Test
  void cancelAppointment_whenWebClientResponseException_shouldReturnCodeAndDetails() {
    EsiriusWebClientAdapter adapter = newAdapter();

    WebClientResponseException ex = WebClientResponseException.create(404, "Not Found", null, "missing".getBytes(), null);

    WebClient webClient = mockWebClientDeleteError(ex);
    ReflectionTestUtils.setField(adapter, "webClient", webClient);

    Map<String, Object> res = adapter.cancelAppointmentByRdvCode("RDV404");

    assertEquals(404, ((Number) res.get("code")).intValue());
    assertEquals("missing", String.valueOf(res.get("details")));
  }

  @Test
  void sanitize_shouldReturnNullWhenValueIsNull() throws Exception {
    EsiriusWebClientAdapter adapter = newAdapter();

    Method method = EsiriusWebClientAdapter.class.getDeclaredMethod("sanitize", String.class);
    method.setAccessible(true);

    Object result = method.invoke(adapter, new Object[]{null});

    assertNull(result);
  }

  @Test
  void sanitize_shouldReplaceNewLinesWithSpaces() throws Exception {
    EsiriusWebClientAdapter adapter = newAdapter();

    Method method = EsiriusWebClientAdapter.class.getDeclaredMethod("sanitize", String.class);
    method.setAccessible(true);

    Object result = method.invoke(adapter, "line1\r\nline2\nline3");

    assertEquals("line1 line2 line3", result);
  }

  private static EsiriusWebClientAdapter newAdapter() {
    return new EsiriusWebClientAdapter(BASE_URL, USERNAME, PASSWORD);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static WebClient mockWebClientGetReturning(String responseBody) {
    WebClient webClient = mock(WebClient.class);

    WebClient.RequestHeadersUriSpec getSpec = mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    when(webClient.get()).thenReturn(getSpec);
    when(getSpec.uri(anyString())).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseBody));

    return webClient;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static WebClient mockWebClientGetError(RuntimeException ex) {
    WebClient webClient = mock(WebClient.class);

    WebClient.RequestHeadersUriSpec getSpec = mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    when(webClient.get()).thenReturn(getSpec);
    when(getSpec.uri(anyString())).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(ex));

    return webClient;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static WebClient mockWebClientDeleteReturning(String responseBody) {
    WebClient webClient = mock(WebClient.class);

    WebClient.RequestHeadersUriSpec deleteSpec = mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    when(webClient.delete()).thenReturn(deleteSpec);
    when(deleteSpec.uri(anyString())).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseBody));

    return webClient;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static WebClient mockWebClientDeleteError(RuntimeException ex) {
    WebClient webClient = mock(WebClient.class);

    WebClient.RequestHeadersUriSpec deleteSpec = mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    when(webClient.delete()).thenReturn(deleteSpec);
    when(deleteSpec.uri(anyString())).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(ex));

    return webClient;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static WebClient mockWebClientSendWithBodyReturning(boolean isPost, String responseBody) {
    WebClient webClient = mock(WebClient.class);

    WebClient.RequestBodyUriSpec bodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
    WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    if (isPost) {
      when(webClient.post()).thenReturn(bodyUriSpec);
    } else {
      when(webClient.put()).thenReturn(bodyUriSpec);
    }

    when(bodyUriSpec.uri(anyString())).thenReturn(bodyUriSpec);
    when(bodyUriSpec.header(anyString(), anyString())).thenReturn(bodyUriSpec);
    when(bodyUriSpec.bodyValue(any())).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseBody));

    return webClient;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static WebClient mockWebClientSendWithBodyError(boolean isPost, RuntimeException ex) {
    WebClient webClient = mock(WebClient.class);

    WebClient.RequestBodyUriSpec bodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
    WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    if (isPost) {
      when(webClient.post()).thenReturn(bodyUriSpec);
    } else {
      when(webClient.put()).thenReturn(bodyUriSpec);
    }

    when(bodyUriSpec.uri(anyString())).thenReturn(bodyUriSpec);
    when(bodyUriSpec.header(anyString(), anyString())).thenReturn(bodyUriSpec);
    when(bodyUriSpec.bodyValue(any())).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(ex));

    return webClient;
  }
}
