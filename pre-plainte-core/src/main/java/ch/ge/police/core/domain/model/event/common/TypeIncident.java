package ch.ge.police.core.domain.model.event.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum TypeIncident {
  VOL("V", "vol", "Vol"),
  DOMMAGE("D", "dommageMateriel", "Dommage"),
  CYBER("C", "cybercrime", "Cybercrime");

  private final String code;
  private final String jsonValue;
  private final String label;

  TypeIncident(String code, String jsonValue, String label) {
    this.code = code;
    this.jsonValue = jsonValue;
    this.label = label;
  }

  public String code() {
    return code;
  }

  public String label() { return label;}

  @JsonValue
  public String jsonValue() {
    return jsonValue;
  }

  @JsonCreator
  public static TypeIncident fromJson(String value) {
    return Arrays.stream(values())
      .filter(v -> v.jsonValue.equalsIgnoreCase(value))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("TypeIncident invalide : " + value));
  }
}
