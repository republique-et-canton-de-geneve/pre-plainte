package ch.ge.police.core.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.regex.Pattern;

final class Sha256CodeEncoder {

  private static final int SALT_BYTES = 16;
  private static final String PREFIX = "sha256$";
  private static final Pattern HASH_FORMAT = Pattern.compile("sha256\\$[0-9a-f]{32}\\$[0-9a-f]{64}");
  private static final HexFormat HEX = HexFormat.of();
  private final SecureRandom random = new SecureRandom();

  String encode(String code) {
    byte[] salt = new byte[SALT_BYTES];
    random.nextBytes(salt);
    return PREFIX + HEX.formatHex(salt) + "$" + HEX.formatHex(digest(salt, code));
  }

  boolean matches(String code, String encoded) {
    if (code == null || encoded == null || !HASH_FORMAT.matcher(encoded).matches()) {
      return false;
    }
    int separator = PREFIX.length() + SALT_BYTES * 2;
    byte[] salt = HEX.parseHex(encoded, PREFIX.length(), separator);
    byte[] expected = HEX.parseHex(encoded, separator + 1, encoded.length());
    return MessageDigest.isEqual(expected, digest(salt, code));
  }

  private static byte[] digest(byte[] salt, String code) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update(salt);
      return digest.digest(code.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 indisponible dans la JVM", e);
    }
  }
}
