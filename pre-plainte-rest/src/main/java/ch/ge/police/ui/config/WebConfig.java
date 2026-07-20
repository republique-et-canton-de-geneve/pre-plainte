package ch.ge.police.ui.config;

import ch.ge.police.infrastructure.security.FriendlyCaptchaInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final FriendlyCaptchaInterceptor friendlyCaptchaInterceptor;

  public WebConfig(FriendlyCaptchaInterceptor friendlyCaptchaInterceptor) {
    this.friendlyCaptchaInterceptor = friendlyCaptchaInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(friendlyCaptchaInterceptor)
        .addPathPatterns(
            "/api/preplainte/draft",
            "/api/preplainte/soumission"
        );
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/{path:[^\\.]+}")
        .setViewName("forward:/index.html");
  }
}
