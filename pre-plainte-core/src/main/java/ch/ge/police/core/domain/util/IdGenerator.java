package ch.ge.police.core.domain.util;

import java.security.SecureRandom;

public final class IdGenerator {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final char[] ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

  private IdGenerator() {}

  public static String randomAlphanumeric(int length) {
    char[] buffer = new char[length];
    for (int i = 0; i < length; i++) {
      buffer[i] = ALPHANUM[RANDOM.nextInt(ALPHANUM.length)];
    }
    return new String(buffer);
  }
}
