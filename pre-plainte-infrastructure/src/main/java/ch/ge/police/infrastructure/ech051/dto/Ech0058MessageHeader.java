package ch.ge.police.infrastructure.ech051.dto;

import lombok.Builder;
import lombok.Value;

/**
 * Informations d'entête eCH-0058 pour compléter l'enveloppe SEP.
 * Ces données décrivent l'expéditeur, la cible et l'application source du message.
 */
@Value
@Builder(toBuilder = true)
public class Ech0058MessageHeader {
  String senderId;
  String recipientId;
  String messageId;
  String messageType;
  SendingApplication sendingApplication;
  String messageDate;
  String eventDate;
  String action;
  boolean testDeliveryFlag;

  @Value
  @Builder(toBuilder = true)
  public static class SendingApplication {
    String manufacturer;
    String product;
    String productVersion;
  }
}
