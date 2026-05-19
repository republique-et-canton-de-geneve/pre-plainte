package ch.ge.police.ui.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

  private final String backendUrl;
  private final String captchaSitekey;
  private final boolean captchaEnabled;

  public ConfigController(
      @Value("${backend.url}") String backendUrl,
      @Value("${friendlycaptcha.sitekey}") String captchaSitekey,
      @Value("${captcha.enabled:false}") boolean captchaEnabled
  ) {
    this.backendUrl = backendUrl;
    this.captchaSitekey = captchaSitekey;
    this.captchaEnabled = captchaEnabled;
  }

  @GetMapping
  public Map<String, String> getConfig() {
    return Map.of(
      "backendUrl", backendUrl,
      "captchaSitekey", captchaSitekey,
      "captchaEnabled", Boolean.toString(captchaEnabled),
      "environment", System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "local")
    );
  }
}
