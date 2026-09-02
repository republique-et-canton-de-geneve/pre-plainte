package ch.ge.police.infrastructure.adapter.out;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.ge.police.core.domain.exception.S3AccessException;
import ch.ge.police.core.domain.exception.S3NotFoundException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
class S3RipolDatabaseSourceTest {

  @Mock S3Client s3Client;

  private S3RipolDatabaseSource source;

  @BeforeEach
  void setUp() {
    source = new S3RipolDatabaseSource(s3Client, "mon-bucket", "dbppel3");
  }

  @Test
  void ouvrirFlux_recupereObjetALaRacineDuBucket() throws Exception {
    byte[] contenu = "SQLite format 3".getBytes(StandardCharsets.US_ASCII);
    when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(reponse(contenu));

    try (InputStream flux = source.ouvrirFlux()) {
      assertArrayEquals(contenu, flux.readAllBytes());
    }

    ArgumentCaptor<GetObjectRequest> reqCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
    verify(s3Client).getObject(reqCaptor.capture());
    assertEquals("mon-bucket", reqCaptor.getValue().bucket());
    assertEquals("dbppel3", reqCaptor.getValue().key());
  }

  @Test
  void ouvrirFlux_whenNoSuchKey_throwsS3NotFoundException() {
    when(s3Client.getObject(any(GetObjectRequest.class)))
      .thenThrow(NoSuchKeyException.builder().message("not found").build());

    assertThrows(S3NotFoundException.class, () -> source.ouvrirFlux());
  }

  @Test
  void ouvrirFlux_whenS3Exception404_throwsS3NotFoundException() {
    when(s3Client.getObject(any(GetObjectRequest.class)))
      .thenThrow(S3Exception.builder().statusCode(404).message("not found").build());

    assertThrows(S3NotFoundException.class, () -> source.ouvrirFlux());
  }

  @Test
  void ouvrirFlux_whenS3ExceptionNot404_throwsS3AccessException() {
    when(s3Client.getObject(any(GetObjectRequest.class)))
      .thenThrow(S3Exception.builder().statusCode(500).message("boom").build());

    assertThrows(S3AccessException.class, () -> source.ouvrirFlux());
  }

  @Test
  void description_exposeBucketEtCle() {
    assertEquals("s3://mon-bucket/dbppel3", source.description());
  }

  private static ResponseInputStream<GetObjectResponse> reponse(byte[] contenu) {
    return new ResponseInputStream<>(
      GetObjectResponse.builder().build(),
      AbortableInputStream.create(new ByteArrayInputStream(contenu))
    );
  }
}
