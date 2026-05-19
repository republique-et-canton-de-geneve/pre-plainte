package ch.ge.police.infrastructure.storage;

import java.net.URI;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.ProxyConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Slf4j
@Configuration
public class S3Config {
  private final String endpoint;
  private final String accessKey;
  private final String secretKey;

  public S3Config(@Value("${s3.endpoint}") String endpoint, @Value("${s3.access.key:xxxaccesskey}") String accessKey, @Value("${s3.secret.key:xxxsecretkey}") String secretKey) {
    this.endpoint = endpoint;
    this.accessKey = accessKey;
    this.secretKey = secretKey;
  }

  @Bean
  public SdkHttpClient s3HttpClient() {
    ProxyConfiguration proxyConfig = ProxyConfiguration.builder().useSystemPropertyValues(Boolean.FALSE).useEnvironmentVariableValues(Boolean.FALSE).build();
    return ApacheHttpClient.builder().proxyConfiguration(proxyConfig).build();
  }

  @Bean
  public S3Client s3Client(SdkHttpClient s3HttpClient) {
    return S3Client.builder().httpClient(s3HttpClient).endpointOverride(URI.create(endpoint)).region(Region.of("eu-west-1")).credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))).requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED).responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED).serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build()).build();
  }
}
