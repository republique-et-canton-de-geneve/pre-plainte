package ch.ge.police.infrastructure.ech051.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;

/**
 * Version JAXB du header eCH-0058. Chaque champ est annoté pour produire
 * l'élément XML correspondant lors de la sérialisation.
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class Ech0058MessageHeaderXml {

  public Ech0058MessageHeaderXml() {
    // JAXB requires an explicit no-arg constructor.
      }

  @XmlElement(name = "senderId", namespace = SepNamespaces.ECH_0058_NS)
  private String senderId;

  @XmlElement(name = "recipientId", namespace = SepNamespaces.ECH_0058_NS)
  private String recipientId;

  @XmlElement(name = "messageId", namespace = SepNamespaces.ECH_0058_NS)
  private String messageId;

  @XmlElement(name = "messageType", namespace = SepNamespaces.ECH_0058_NS)
  private String messageType;

  @XmlElement(name = "sendingApplication", namespace = SepNamespaces.ECH_0058_NS)
  private SendingApplicationXml sendingApplication;

  @XmlElement(name = "messageDate", namespace = SepNamespaces.ECH_0058_NS)
  private String messageDate;

  @XmlElement(name = "eventDate", namespace = SepNamespaces.ECH_0058_NS)
  private String eventDate;

  @XmlElement(name = "action", namespace = SepNamespaces.ECH_0058_NS)
  private String action;

  @XmlElement(name = "testDeliveryFlag", namespace = SepNamespaces.ECH_0058_NS)
  private boolean testDeliveryFlag;

  @Getter
  @Setter
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class SendingApplicationXml {
    public SendingApplicationXml() {
      // JAXB requires an explicit no-arg constructor.
          }

    @XmlElement(name = "manufacturer", namespace = SepNamespaces.ECH_0058_NS)
    private String manufacturer;

    @XmlElement(name = "product", namespace = SepNamespaces.ECH_0058_NS)
    private String product;

    @XmlElement(name = "productVersion", namespace = SepNamespaces.ECH_0058_NS)
    private String productVersion;
  }
}

