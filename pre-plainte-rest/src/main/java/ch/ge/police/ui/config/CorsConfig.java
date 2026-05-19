package ch.ge.police.ui.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.stream.Stream;

@Configuration
public class CorsConfig {

  private final String[] allowedOriginPatterns;

  public CorsConfig(@Value("${PREPLAINTE_APP_OPENSHIFT_HOST_PATTERN:}") String prePlainteAppOpenShiftHostPattern) {
    this.allowedOriginPatterns = Stream.of(
        "http://localhost:*",
        prePlainteAppOpenShiftHostPattern.isBlank() ? "" : ("https://" + prePlainteAppOpenShiftHostPattern)
      )
      .map(String::trim)
      .filter(pattern -> !pattern.isEmpty())
      .toArray(String[]::new);
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
          .allowedOriginPatterns(allowedOriginPatterns)
          .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
          .allowedHeaders("*")
          .exposedHeaders("Authorization", "X-Captcha-Token", "x-captcha-token")
          .allowCredentials(true);
      }
    };
  }
}
