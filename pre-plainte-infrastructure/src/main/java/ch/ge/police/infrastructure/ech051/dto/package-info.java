@jakarta.xml.bind.annotation.XmlSchema(
    namespace = SepNamespaces.SEP_NS,
    xmlns = {
        @jakarta.xml.bind.annotation.XmlNs(prefix = "sep", namespaceURI = SepNamespaces.SEP_NS),
        @jakarta.xml.bind.annotation.XmlNs(prefix = "eCH-0051", namespaceURI = SepNamespaces.ECH_0051_NS),
        @jakarta.xml.bind.annotation.XmlNs(prefix = "eCH-0058", namespaceURI = SepNamespaces.ECH_0058_NS),
        @jakarta.xml.bind.annotation.XmlNs(prefix = "xmime", namespaceURI = SepNamespaces.XMIME_NS)
    },
    elementFormDefault = jakarta.xml.bind.annotation.XmlNsForm.QUALIFIED
)
package ch.ge.police.infrastructure.ech051.dto;

