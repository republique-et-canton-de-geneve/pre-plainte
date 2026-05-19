package ch.ge.police.infrastructure.ech051.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 * Représente l'élément racine <sep:delivery> utilisé lors du marshalling JAXB.
 * Il référence l'entête eCH-0058 et le contenu eCH-0051 selon les namespaces SEP.
 */
@Getter
@Setter
@XmlRootElement(name = "delivery", namespace = SepNamespaces.SEP_NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class SepDeliveryEnvelopeXml {

  public SepDeliveryEnvelopeXml() {
    // JAXB requires an explicit no-arg constructor.
      }

  @XmlElement(name = "header", namespace = SepNamespaces.SEP_NS)
  private Ech0058MessageHeaderXml header = new Ech0058MessageHeaderXml();

  @XmlElement(name = "content", namespace = SepNamespaces.SEP_NS)
  private SepContentWrapperXml content = new SepContentWrapperXml();
}

