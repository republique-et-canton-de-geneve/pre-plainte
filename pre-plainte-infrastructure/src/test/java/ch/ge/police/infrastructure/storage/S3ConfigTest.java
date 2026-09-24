package ch.ge.police.infrastructure.storage;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache5.Apache5HttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class S3ConfigTest {

  @Test
  void s3HttpClient_buildsApacheClient() {
    S3Config config = new S3Config("https://s3.example.test", "ak", "sk");

    SdkHttpClient httpClient = config.s3HttpClient();

    assertNotNull(httpClient);
    assertInstanceOf(Apache5HttpClient.class, httpClient, "Expected Apache5HttpClient but got " + httpClient.getClass());
  }

  @Test
  void s3Client_buildsClient_andIfPossible_exposesEndpointAndRegion() {
    String endpoint = "https://s3.example.test";
    S3Config config = new S3Config(endpoint, "ak", "sk");
    SdkHttpClient httpClient = config.s3HttpClient();

    S3Client client = config.s3Client(httpClient);

    assertNotNull(client);
    Object serviceCfg = invokeIfExists(client, "serviceClientConfiguration");
    if (serviceCfg != null) {
      Object endpointOverride = invokeIfExists(serviceCfg, "endpointOverride");
      if (endpointOverride != null) {
        URI expected = URI.create(endpoint);

        if (endpointOverride instanceof URI uri) {
          assertEquals(expected, uri);
        } else if (endpointOverride instanceof java.util.Optional<?> opt) {
          assertEquals(expected, opt.orElseThrow());
        }
      }
      Object region = invokeIfExists(serviceCfg, "region");
      if (region != null) {
        if (region instanceof Region r) {
          assertEquals(Region.of("eu-west-1"), r);
        } else if (region instanceof String s) {
          assertEquals("eu-west-1", s);
        } else if (region instanceof java.util.Optional<?> opt) {
          Object v = opt.orElseThrow();
          if (v instanceof Region r2) {
            assertEquals(Region.of("eu-west-1"), r2);
          } else if (v instanceof String s2) {
            assertEquals("eu-west-1", s2);
          }
        }
      }
    }
    client.close();
    httpClient.close();
  }

  @Test
  @Timeout(15)
  void s3Client_shouldReadObjectThroughApacheHttpClient() throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    byte[] payload = "{\"demandeId\":\"draft-123\"}".getBytes(StandardCharsets.UTF_8);
    server.createContext("/test-bucket/preplainte/draft/draft-123.json", exchange -> {
      try (exchange) {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, payload.length);
        exchange.getResponseBody().write(payload);
      }
    });
    server.start();
    try {
      S3Config config = new S3Config("http://127.0.0.1:" + server.getAddress().getPort(), "ak", "sk");
      try (SdkHttpClient httpClient = config.s3HttpClient(); S3Client client = config.s3Client(httpClient)) {
        String result = client.getObjectAsBytes(request -> request.bucket("test-bucket")
          .key("preplainte/draft/draft-123.json")).asUtf8String();
        assertEquals(new String(payload, StandardCharsets.UTF_8), result);
      }
    } finally {
      server.stop(0);
    }
  }

  private static Object invokeIfExists(Object target, String methodName, Object... args) {
    try {
      Class<?>[] paramTypes = new Class<?>[args.length];
      for (int i = 0; i < args.length; i++) {
        paramTypes[i] = args[i].getClass();
      }
      Method m = target.getClass().getMethod(methodName, paramTypes);
      m.setAccessible(true);
      return m.invoke(target, args);
    } catch (NoSuchMethodException e) {
      return null;
    } catch (Exception e) {
      throw new AssertionError("Invocation failed for " + target.getClass().getName() + "#" + methodName, e);
    }
  }
}
