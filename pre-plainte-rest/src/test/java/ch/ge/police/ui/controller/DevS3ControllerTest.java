package ch.ge.police.ui.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DevS3ControllerStandaloneTest {

  private S3Client s3Client;
  private DevS3Controller controller;

  @BeforeEach
  void setUp() {
    s3Client = mock(S3Client.class);
    controller = new DevS3Controller(s3Client);

    setField(controller, "bucketName", "my-bucket");
    setField(controller, "s3DebugEnabled", true);
  }

  @Test
  void list_whenDebugDisabled_throwsAccessDenied() {
    setField(controller, "s3DebugEnabled", false);

    assertThrows(AccessDeniedException.class, () -> controller.list(null, null, null));
  }

  @Test
  void list_buildsRequest_defaultsAndCapsMaxKeys_andMapsItems() throws Exception {
    var obj1 = S3Object.builder().key("preplainte/draft/a.json").size(123L).lastModified(Instant.parse("2026-03-01T10:00:00Z")).eTag("\"etag1\"").build();

    var obj2 = S3Object.builder().key("preplainte/draft/b.json").size(456L).lastModified(Instant.parse("2026-03-02T10:00:00Z")).eTag("\"etag2\"").build();

    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(ListObjectsV2Response.builder().contents(List.of(obj1, obj2)).nextContinuationToken("NEXT").build());

    ArgumentCaptor<ListObjectsV2Request> reqCaptor = ArgumentCaptor.forClass(ListObjectsV2Request.class);
    var result = controller.list(null, null, "CONT");

    verify(s3Client).listObjectsV2(reqCaptor.capture());
    var req = reqCaptor.getValue();

    assertEquals("my-bucket", req.bucket());
    assertEquals("", req.prefix());
    assertEquals(1000, req.maxKeys());
    assertEquals("CONT", req.continuationToken());

    assertEquals("NEXT", result.nextContinuationToken());
    assertEquals(2, result.items().size());

    var i0 = result.items().getFirst();
    assertEquals("preplainte/draft/a.json", i0.key());
    assertEquals(123L, i0.size());
    assertEquals(Instant.parse("2026-03-01T10:00:00Z"), i0.lastModified());
    assertEquals("\"etag1\"", i0.eTag());
  }

  @Test
  void list_capsMaxKeysTo1000() throws Exception {
    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(ListObjectsV2Response.builder().contents(List.of()).build());

    ArgumentCaptor<ListObjectsV2Request> reqCaptor = ArgumentCaptor.forClass(ListObjectsV2Request.class);

    controller.list("preplainte/", 5000, null);

    verify(s3Client).listObjectsV2(reqCaptor.capture());
    assertEquals(1000, reqCaptor.getValue().maxKeys());
    assertEquals("preplainte/", reqCaptor.getValue().prefix());
  }

  @Test
  void download_whenDebugDisabled_throwsAccessDenied() {
    setField(controller, "s3DebugEnabled", false);

    assertThrows(AccessDeniedException.class, () -> controller.download("x", true));
  }

  @Test
  void download_decodesKey_stripsLeadingSlash_callsS3_andSetsContentDisposition_attachment() throws Exception {
    byte[] payload = "hello".getBytes(StandardCharsets.UTF_8);
    when(s3Client.getObject(any(GetObjectRequest.class)))
      .thenReturn(s3Stream(payload));

    ArgumentCaptor<GetObjectRequest> reqCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);

    String encodedKey = "/preplainte%2Fdraft%2FAEL-PPL-1.json";
    var resp = controller.download(encodedKey, true);

    verify(s3Client).getObject(reqCaptor.capture());
    var req = reqCaptor.getValue();

    assertEquals("my-bucket", req.bucket());
    assertEquals("preplainte/draft/AEL-PPL-1.json", req.key());

    assertEquals(200, resp.getStatusCode().value());
    assertNotNull(resp.getHeaders().getContentType());
    assertEquals("application/octet-stream", resp.getHeaders().getContentType().toString());

    String cd = resp.getHeaders().getFirst("Content-Disposition");
    assertNotNull(cd);

    String expectedFilename = URLEncoder.encode("AEL-PPL-1.json", StandardCharsets.UTF_8);
    assertEquals("attachment; filename*=UTF-8''" + expectedFilename, cd);
  }

  @Test
  void download_inline_setsInlineDisposition() throws Exception {
    when(s3Client.getObject(any(GetObjectRequest.class)))
      .thenReturn(s3Stream("x".getBytes(StandardCharsets.UTF_8)));

    var resp = controller.download("file.txt", false);

    String cd = resp.getHeaders().getFirst("Content-Disposition");
    assertNotNull(cd);

    String expectedFilename = URLEncoder.encode("file.txt", StandardCharsets.UTF_8);
    assertEquals("inline; filename*=UTF-8''" + expectedFilename, cd);
  }

  private static void setField(Object target, String fieldName, Object value) {
    try {
      Field f = target.getClass().getDeclaredField(fieldName);
      f.setAccessible(true);
      f.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException("Unable to set field " + fieldName, e);
    }
  }

  private static ResponseInputStream<GetObjectResponse> s3Stream(byte[] bytes) {
    return new ResponseInputStream<>(
      GetObjectResponse.builder().build(),
      AbortableInputStream.create(new ByteArrayInputStream(bytes))
    );
  }
}
