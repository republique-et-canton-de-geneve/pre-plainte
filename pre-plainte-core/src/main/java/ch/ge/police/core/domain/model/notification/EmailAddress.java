package ch.ge.police.core.domain.model.notification;

public record EmailAddress(String value) {
  public EmailAddress {
    if (value == null || !value.contains("@")) {
      throw new IllegalArgumentException("Invalid email: " + value);
    }
  }
  @Override public String toString() { return value; }
}
