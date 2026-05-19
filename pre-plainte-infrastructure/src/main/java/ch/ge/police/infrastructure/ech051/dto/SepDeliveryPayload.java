package ch.ge.police.infrastructure.ech051.dto;

import lombok.Builder;
import lombok.Value;

/**
 * Représente l'enveloppe SEP finale (header eCH-0058 + document eCH-0051).
 * C'est la structure pivot avant marshalling XML.
 */
@Value
@Builder(toBuilder = true)
public class SepDeliveryPayload {
  Ech0058MessageHeader header;
  Ech0051DocumentPayload document;
}
