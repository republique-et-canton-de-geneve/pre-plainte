package ch.ge.police.ui.controller;

import ch.ge.police.core.application.service.EmailChallengeService;
import ch.ge.police.core.domain.model.notification.EmailLanguage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/email-challenges")
@RequiredArgsConstructor
public class EmailChallengeController {

  private final EmailChallengeService service;

  @PostMapping("/request")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void request(@RequestBody RequestCodeRequest req, Locale locale) {
    service.request(req.email(), req.key(), EmailLanguage.fromLocale(locale));
  }

  @PostMapping("/verify")
  public EmailChallengeService.VerifyResult verify(@RequestBody VerifyCodeRequest req) {
    return service.verify(req.email(), req.key(), req.code());
  }

  public record RequestCodeRequest(
    String email,
    String key
  ) {}

  public record VerifyCodeRequest(
    String email,
    String key,
    String code
  ) {}
}
