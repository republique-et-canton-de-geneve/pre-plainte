package ch.ge.police.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class FriendlyCaptchaInterceptor implements HandlerInterceptor {

  private final FriendlyCaptchaVerifier friendlyCaptchaVerifier;

  @Value("${captcha.enabled:true}")
  private boolean captchaEnabled;

  public FriendlyCaptchaInterceptor(FriendlyCaptchaVerifier friendlyCaptchaVerifier) {
    this.friendlyCaptchaVerifier = friendlyCaptchaVerifier;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    if (!captchaEnabled) {
      return true;
    }

    if (!"POST".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    String token = request.getHeader("X-Captcha-Token");
    if (token == null || token.isBlank()) {
      log.warn("Captcha rejeté: en-tête X-Captcha-Token absent ou vide — {} {}", request.getMethod(),
          request.getRequestURI());
      writeError(response, null);
      return false;
    }

    FriendlyCaptchaVerifier.CaptchaResult result = friendlyCaptchaVerifier.verify(token);

    if (result == null || !result.success()) {
      log.warn("Captcha rejeté: vérification échouée — {} {} errors={}", request.getMethod(), request.getRequestURI(),
          result != null ? result.errors() : null);
      writeError(response, result != null ? result.errors() : null);
      return false;
    }

    return true;
  }

  private void writeError(HttpServletResponse response, List<String> errors) throws IOException {
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    String details = (errors == null) ? "null" : errors.toString();
    String body = "{\"error\":\"Captcha invalide\", \"details\":\"" + details + "\"}";
    response.getWriter().write(HtmlUtils.htmlEscape(body));
  }
}
