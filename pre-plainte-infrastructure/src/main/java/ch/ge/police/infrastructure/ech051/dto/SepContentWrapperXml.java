package ch.ge.police.infrastructure.ech051.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;

/**
 * Conteneur XML intermédiaire qui enveloppe le document eCH-0051 à l'intérieur du message SEP.
 * Il permet de contrôler le namespace appliqué au noeud <eCH-0051:document>.
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class SepContentWrapperXml {

  public SepContentWrapperXml() {
    // JAXB requires an explicit no-arg constructor.
      }

  @XmlElement(name = "document", namespace = SepNamespaces.ECH_0051_NS)
  private Ech0051DocumentXml document = new Ech0051DocumentXml();
}

