package ch.ge.police.core.domain.model.notification;

public enum TemplateEmail {
  BROUILLON("brouillon"),
  CHALLENGE("challenge");

  private final String code;

  TemplateEmail(String code) {
    this.code = code;
  }

  public String templatePath(EmailLanguage language) {
    return "mail/" + code + "/template_" + language.code() + ".html";
  }
}
