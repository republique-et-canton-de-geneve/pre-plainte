package ch.ge.police.ui.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/dev-s3")
@RequiredArgsConstructor
public class DevS3Controller {
  private final S3Client s3Client;

  @Value("${s3.bucket.name}")
  private String bucketName;
  @Value("${s3.debug.enabled}")
  private boolean s3DebugEnabled;
  private static final int DEFAULT_MAX_KEYS = 1000;

  public record S3ObjectItem(String key, long size, Instant lastModified, String eTag) {
  }

  public record ListResult(List<S3ObjectItem> items, String nextContinuationToken) {
  }

  @GetMapping("/objects")
  public ListResult list(@RequestParam(required = false) String prefix, @RequestParam(required = false) Integer maxKeys, @RequestParam(required = false) String continuationToken) throws AccessDeniedException {
    if (!s3DebugEnabled) {
      throw new AccessDeniedException("Unavailable");
    }
    var req = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix == null ? "" : prefix).maxKeys(maxKeys == null ? DEFAULT_MAX_KEYS : Math.min(maxKeys, DEFAULT_MAX_KEYS)).continuationToken(continuationToken).build();
    var res = s3Client.listObjectsV2(req);
    var items = res.contents().stream().map(o -> new S3ObjectItem(o.key(), o.size(), o.lastModified(), o.eTag())).toList();

    return new ListResult(items, res.nextContinuationToken());
  }


  @GetMapping("/objects/{*key}")
  public ResponseEntity<InputStreamResource> download(@PathVariable String key, @RequestParam(defaultValue = "true") boolean asAttachment) throws AccessDeniedException {
    if (!s3DebugEnabled) {
      throw new AccessDeniedException("Unavailable");
    }
    key = java.net.URLDecoder.decode(key, StandardCharsets.UTF_8);
    while (key.startsWith("/")) {
      key = key.substring(1);
    }

    InputStream in = s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(key).build());

    String filename = key.contains("/") ? key.substring(key.lastIndexOf('/') + 1) : key;
    String safeFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.add(HttpHeaders.CONTENT_DISPOSITION, (asAttachment ? "attachment" : "inline") + "; filename*=UTF-8''" + safeFilename);

    return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
  }
}
