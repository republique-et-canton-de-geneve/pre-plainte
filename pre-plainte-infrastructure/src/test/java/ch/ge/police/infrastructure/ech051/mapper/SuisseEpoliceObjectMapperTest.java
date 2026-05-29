package ch.ge.police.infrastructure.ech051.mapper;

import ch.ge.police.core.domain.model.event.IncidentBase;
import ch.ge.police.core.domain.model.event.dommagematerial.DommageMateriel;
import ch.ge.police.core.domain.model.event.dommagematerial.common.TypeDommage;
import ch.ge.police.core.domain.model.event.vol.Vol;
import ch.ge.police.core.domain.model.event.vol.common.ObjetIncident;
import ch.ge.police.infrastructure.ech051.Ech051Constants;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.Identification;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.ObjectItem;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolLocation;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.RipolReference;
import ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload.VehicleItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.COULEUR_VEHICULE;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.OBJET_COULEUR;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.OBJET_MARQUE;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.OBJET_MODELE;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.PLAQUE_CANTON;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.PLAQUE_PAYS;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.TYPE_OBJET;
import static ch.ge.police.infrastructure.ech051.Ech051Constants.RipolSourceTables.TYPE_VEHICULE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SuisseEpoliceObjectMapperTest {

  private SuisseEpoliceObjectMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new SuisseEpoliceObjectMapper();
  }

  @Test
  void shouldBuildObjectsFromVolForNonVehicleObjects() {
    Vol vol = mock(Vol.class);
    ObjetIncident object = mockObjet(false);
    when(vol.getObjetsVoles()).thenReturn(List.of(object));

    List<ObjectItem> result = mapper.buildObjectsFromIncident(vol);

    assertEquals(1, result.size());
    ObjectItem item = result.get(0);
    assertEquals(Ech051Constants.OBJECT_KEY_TIERS, item.getKey());
    assertEquals("1500", item.getRealValue());
    assertEquals("SER123", item.getNumeroSerie());
    assertEquals("GRAV123", item.getGravure());
    assertNotNull(item.getTypeOfObject());
    assertNotNull(item.getFabricant());
    assertNotNull(item.getModele());
    assertNotNull(item.getCouleur());
    assertNotNull(item.getCouleurSecondaire());
    assertNotNull(item.getIdentification());
    assertTrue(item.getAdditionalInformation().contains("Numéro de cadre inconnu"));
    assertTrue(item.getAdditionalInformation().contains("VIN inconnu"));
    assertTrue(item.getAdditionalInformation().contains("Numéro de plaque inconnu"));
  }

  @Test
  void shouldAssignDistinctXmlKeysForMultipleNonVehicleObjectsFromVol() {
    Vol vol = mock(Vol.class);
    ObjetIncident o1 = mockObjet(false);
    ObjetIncident o2 = mock(ObjetIncident.class);
    when(o2.isVehicleType()).thenReturn(false);
    when(o2.getTypeCode()).thenReturn("TYPE2");
    when(o2.getTypeLabel()).thenReturn("Type 2");
    when(vol.getObjetsVoles()).thenReturn(List.of(o1, o2));

    List<ObjectItem> result = mapper.buildObjectsFromIncident(vol);

    assertEquals(2, result.size());
    assertEquals(Ech051Constants.OBJECT_KEY_TIERS, result.get(0).getKey());
    assertEquals("51", result.get(1).getKey());
  }

  @Test
  void shouldAssignDistinctXmlKeysForMultipleVehiclesFromVol() {
    Vol vol = mock(Vol.class);
    ObjetIncident v1 = mockVehicleObjet();
    ObjetIncident v2 = mockVehicleObjet();
    when(vol.getObjetsVoles()).thenReturn(List.of(v1, v2));

    List<VehicleItem> result = mapper.buildVehiclesFromIncident(vol);

    assertEquals(2, result.size());
    assertEquals(Ech051Constants.VEHICLE_KEY, result.get(0).getKey());
    assertEquals("61", result.get(1).getKey());
  }

  @Test
  void shouldBuildVehiclesFromVolForVehicleObjects() {
    Vol vol = mock(Vol.class);
    ObjetIncident vehicle = mockVehicleObjet();
    when(vol.getObjetsVoles()).thenReturn(List.of(vehicle));

    List<VehicleItem> result = mapper.buildVehiclesFromIncident(vol);

    assertEquals(1, result.size());
    VehicleItem item = result.get(0);
    assertEquals(Ech051Constants.VEHICLE_KEY, item.getKey());
    assertEquals("VIN123", item.getVin());
    assertEquals("CADRE123", item.getFrameNumber());
    assertEquals("VELO123", item.getVelofinderId());
    assertNotNull(item.getTypeOfVehicle());
    assertNotNull(item.getMark());
    assertNotNull(item.getModelType());
    assertNotNull(item.getColour());
    assertNotNull(item.getColourSecondary());
    assertNotNull(item.getNumberPlate());
    assertEquals("GE123456", item.getNumberPlate().getNumber());
    assertEquals("CH", item.getNumberPlate().getCountry().getCode());
    assertEquals("GE", item.getNumberPlate().getCanton().getCode());
  }

  @Test
  void shouldBuildPlateTheftAsObjectAndNotVehicle() {
    ObjetIncident plaque = ObjetIncident.builder()
        .categorieObjet("plaque")
        .plaqueNumero("GE123456")
        .build();
    Vol vol = mockVolWith(plaque);

    List<ObjectItem> objects = mapper.buildObjectsFromIncident(vol);
    List<VehicleItem> vehicles = mapper.buildVehiclesFromIncident(vol);

    assertEquals(1, objects.size());
    assertTrue(vehicles.isEmpty());
    assertTrue(objects.get(0).getAdditionalInformation().contains("Numéro de plaque: GE123456"));
  }

  @Test
  void shouldBuildObjectsFromDommageWhenNonVehicleObjectsExist() {
    DommageMateriel dommage = mock(DommageMateriel.class);
    ObjetIncident object = mockObjet(false);
    when(dommage.getObjetDegrades()).thenReturn(List.of(object));

    List<ObjectItem> result = mapper.buildObjectsFromIncident(dommage);

    assertEquals(1, result.size());
    assertNotNull(result.get(0).getIdentification());
  }

  @Test
  void shouldBuildFallbackObjectFromDommageWhenNoNonVehicleObjectExists() {
    DommageMateriel dommage = mock(DommageMateriel.class);
    TypeDommage typeDommage = mock(TypeDommage.class);

    when(typeDommage.getLabel()).thenReturn("Vandalisme");
    when(dommage.getTypeDommage()).thenReturn(typeDommage);
    when(dommage.getDescription()).thenReturn("Vitre brisée");
    when(dommage.getObjetDegrades()).thenReturn(List.of());

    List<ObjectItem> result = mapper.buildObjectsFromIncident(dommage);

    assertEquals(1, result.size());
    ObjectItem item = result.get(0);
    assertEquals(Ech051Constants.OBJECT_KEY_TIERS, item.getKey());
    assertEquals("Vitre brisée", item.getAdditionalInformation());
    assertNotNull(item.getTypeOfObject());
    assertEquals(Ech051Constants.TYPE_OF_OBJECT_DOMMAGE_CODE, item.getTypeOfObject().getCode());
    assertEquals(Ech051Constants.TYPE_OF_OBJECT_DOMMAGE_LABEL, item.getTypeOfObject().getLabel());
  }

  @Test
  void shouldBuildVehiclesFromDommage() {
    DommageMateriel dommage = mock(DommageMateriel.class);
    ObjetIncident vehicle = mockVehicleObjet();
    when(dommage.getObjetDegrades()).thenReturn(List.of(vehicle));

    List<VehicleItem> result = mapper.buildVehiclesFromIncident(dommage);

    assertEquals(1, result.size());
    assertEquals("VIN123", result.get(0).getVin());
    assertNotNull(result.get(0).getNumberPlate());
  }

  @Test
  void shouldReturnEmptyListsForUnsupportedIncidentOrNullCollections() {
    IncidentBase incident = mock(IncidentBase.class);

    assertTrue(mapper.buildObjectsFromIncident(incident).isEmpty());
    assertTrue(mapper.buildVehiclesFromIncident(incident).isEmpty());

    Vol vol = mock(Vol.class);
    when(vol.getObjetsVoles()).thenReturn(null);
    assertTrue(mapper.buildObjectsFromIncident(vol).isEmpty());
    assertTrue(mapper.buildVehiclesFromIncident(vol).isEmpty());

    DommageMateriel dommage = mock(DommageMateriel.class);
    when(dommage.getObjetDegrades()).thenReturn(null);
    assertTrue(mapper.buildVehiclesFromIncident(dommage).isEmpty());
  }

  @Test
  void shouldBuildAllRipolReferences() {
    ObjetIncident objet = mockObjet(false);

    RipolReference objectType = mapper.buildObjectTypeReference(objet);
    RipolReference brand = mapper.buildBrandReference(objet);
    RipolReference model = mapper.buildModelReference(objet);
    RipolReference colour = mapper.buildColourReference(objet);
    RipolReference colour2 = mapper.buildColourSecondaireReference(objet);
    RipolReference vehicleType = mapper.buildVehicleTypeReference(objet);
    RipolReference vehicleBrand = mapper.buildBrandReference(objet);
    RipolReference vehicleModel = mapper.buildModelReference(objet);
    RipolReference vehicleColour = mapper.buildVehicleColourReference(objet);
    RipolReference vehicleColour2 = mapper.buildVehicleColourSecondaireReference(objet);

    assertEquals("TYPE1", objectType.getCode());
    assertEquals(TYPE_OBJET, objectType.getSourceTable());

    assertEquals("FAB1", brand.getCode());
    assertEquals(OBJET_MARQUE, brand.getSourceTable());

    assertEquals("MOD1", model.getCode());
    assertEquals(OBJET_MODELE, model.getSourceTable());

    assertEquals("COL1", colour.getCode());
    assertEquals(OBJET_COULEUR, colour.getSourceTable());

    assertEquals("COL2", colour2.getCode());
    assertEquals(OBJET_COULEUR, colour2.getSourceTable());

    assertEquals("TYPE1", vehicleType.getCode());
    assertEquals(TYPE_VEHICULE, vehicleType.getSourceTable());

    assertEquals("FAB1", vehicleBrand.getCode());
    assertEquals(OBJET_MARQUE, vehicleBrand.getSourceTable());

    assertEquals("MOD1", vehicleModel.getCode());
    assertEquals(OBJET_MODELE, vehicleModel.getSourceTable());

    assertEquals("COL1", vehicleColour.getCode());
    assertEquals(COULEUR_VEHICULE, vehicleColour.getSourceTable());

    assertEquals("COL2", vehicleColour2.getCode());
    assertEquals(COULEUR_VEHICULE, vehicleColour2.getSourceTable());
  }

  @Test
  void shouldReturnNullForMissingRipolReferenceData() {
    ObjetIncident objet = mock(ObjetIncident.class);

    assertNull(mapper.buildObjectTypeReference(objet));
    assertNull(mapper.buildBrandReference(objet));
    assertNull(mapper.buildModelReference(objet));
    assertNull(mapper.buildColourReference(objet));
    assertNull(mapper.buildColourSecondaireReference(objet));
    assertNull(mapper.buildVehicleTypeReference(objet));
    assertNull(mapper.buildBrandReference(objet));
    assertNull(mapper.buildModelReference(objet));
    assertNull(mapper.buildVehicleColourReference(objet));
    assertNull(mapper.buildVehicleColourSecondaireReference(objet));
  }

  @Test
  void shouldBuildIdentificationFromImeiOrSerialNumber() {
    ObjetIncident imeiObject = mock(ObjetIncident.class);
    when(imeiObject.getNumeroIMEI()).thenReturn("IMEI123");
    when(imeiObject.getNumeroSerie()).thenReturn("SER123");

    Identification identificationImei = mapper.buildIdentification(imeiObject);
    assertNotNull(identificationImei);
    assertEquals("IMEI", identificationImei.getType());
    assertEquals("IMEI123", identificationImei.getNumber());

    ObjetIncident serialObject = mock(ObjetIncident.class);
    when(serialObject.getNumeroSerie()).thenReturn("SER999");

    Identification identificationSerial = mapper.buildIdentification(serialObject);
    assertNotNull(identificationSerial);
    assertEquals("serialNumber", identificationSerial.getType());
    assertEquals("SER999", identificationSerial.getNumber());

    ObjetIncident emptyObject = mock(ObjetIncident.class);
    assertNull(mapper.buildIdentification(emptyObject));
  }

  @Test
  void shouldBuildAdditionalInfo() {
    ObjetIncident objet = mock(ObjetIncident.class);
    when(objet.isNumeroIMEIInconnu()).thenReturn(true);
    when(objet.getJustificationAbsenceIMEI()).thenReturn("introuvable");
    when(objet.isNumeroSerieInconnu()).thenReturn(true);
    when(objet.isNumeroCadreInconnu()).thenReturn(true);
    when(objet.isVinInconnu()).thenReturn(true);
    when(objet.isPlaqueInconnu()).thenReturn(true);

    String result = mapper.buildObjectAdditionalInfo(objet);

    assertNotNull(result);
    assertTrue(result.contains("IMEI inconnu: introuvable"));
    assertTrue(result.contains("Numéro de série inconnu"));
    assertTrue(result.contains("Numéro de cadre inconnu"));
    assertTrue(result.contains("VIN inconnu"));
    assertTrue(result.contains("Numéro de plaque inconnu"));
    assertEquals(result, mapper.buildVehicleAdditionalInfo(objet));
  }

  @Test
  void shouldReturnNullAdditionalInfoWhenNothingToAdd() {
    ObjetIncident objet = mock(ObjetIncident.class);

    assertNull(mapper.buildObjectAdditionalInfo(objet));
    assertNull(mapper.buildVehicleAdditionalInfo(objet));
  }

  @Test
  void shouldBuildNumberPlateCountryAndCantonOnlyWhenPlateExists() {
    ObjetIncident objet = mock(ObjetIncident.class);
    when(objet.isVehicleType()).thenReturn(true);
    when(objet.getTypeCode()).thenReturn("TYPE1");
    when(objet.getTypeLabel()).thenReturn("Vélo");
    when(objet.getPlaqueNumero()).thenReturn("VD123456");
    when(objet.getPlaquePaysCode()).thenReturn("CH");
    when(objet.getPlaquePaysLabel()).thenReturn("Suisse");
    when(objet.getPlaqueCantonCode()).thenReturn("VD");
    when(objet.getPlaqueCantonLabel()).thenReturn("Vaud");

    List<VehicleItem> result = mapper.buildVehiclesFromIncident(mockVolWith(objet));

    assertEquals(1, result.size());
    assertNotNull(result.get(0).getNumberPlate());
    assertEquals("VD123456", result.get(0).getNumberPlate().getNumber());

    RipolLocation country = result.get(0).getNumberPlate().getCountry();
    assertNotNull(country);
    assertEquals("CH", country.getCode());
    assertEquals("Suisse", country.getLabel());
    assertEquals(PLAQUE_PAYS, country.getSourceTable());

    RipolLocation canton = result.get(0).getNumberPlate().getCanton();
    assertNotNull(canton);
    assertEquals("VD", canton.getCode());
    assertEquals("Vaud", canton.getLabel());
    assertEquals(PLAQUE_CANTON, canton.getSourceTable());
  }

  @Test
  void shouldReturnNullNumberPlateWhenPlateNumberMissing() {
    ObjetIncident objet = mock(ObjetIncident.class);
    when(objet.isVehicleType()).thenReturn(true);
    when(objet.getPlaqueNumero()).thenReturn(" ");

    List<VehicleItem> result = mapper.buildVehiclesFromIncident(mockVolWith(objet));

    assertEquals(1, result.size());
    assertNull(result.get(0).getNumberPlate());
  }

  private static Vol mockVolWith(ObjetIncident objet) {
    Vol vol = mock(Vol.class);
    when(vol.getObjetsVoles()).thenReturn(List.of(objet));
    return vol;
  }

  private static ObjetIncident mockObjet(boolean vehicleType) {
    ObjetIncident objet = mock(ObjetIncident.class);
    when(objet.isVehicleType()).thenReturn(vehicleType);
    when(objet.getTypeCode()).thenReturn("TYPE1");
    when(objet.getTypeLabel()).thenReturn("Type label");
    when(objet.getFabricantCode()).thenReturn("FAB1");
    when(objet.getFabricantLabel()).thenReturn("Apple");
    when(objet.getFabricantAutre()).thenReturn("Autre fabricant");
    when(objet.getModeleCode()).thenReturn("MOD1");
    when(objet.getModeleLabel()).thenReturn("MacBook");
    when(objet.getModeleAutre()).thenReturn("Autre modèle");
    when(objet.getCouleurCode()).thenReturn("COL1");
    when(objet.getCouleurLabel()).thenReturn("Noir");
    when(objet.getCouleurSecondaireCode()).thenReturn("COL2");
    when(objet.getCouleurSecondaireLabel()).thenReturn("Gris");
    when(objet.getRealValue()).thenReturn("1500");
    when(objet.getPurchaseDate()).thenReturn("2024-01-01");
    when(objet.getNumeroSerie()).thenReturn("SER123");
    when(objet.getGravure()).thenReturn("GRAV123");
    when(objet.getNumeroIMEI()).thenReturn("IMEI123");
    when(objet.isNumeroIMEIInconnu()).thenReturn(false);
    when(objet.isNumeroSerieInconnu()).thenReturn(false);
    when(objet.isNumeroCadreInconnu()).thenReturn(true);
    when(objet.isVinInconnu()).thenReturn(true);
    when(objet.isPlaqueInconnu()).thenReturn(true);
    return objet;
  }

  private static ObjetIncident mockVehicleObjet() {
    ObjetIncident objet = mockObjet(true);
    when(objet.getVin()).thenReturn("VIN123");
    when(objet.getNumeroCadre()).thenReturn("CADRE123");
    when(objet.getVelofinderId()).thenReturn("VELO123");
    when(objet.getPlaqueNumero()).thenReturn("GE123456");
    when(objet.getPlaquePaysCode()).thenReturn("CH");
    when(objet.getPlaquePaysLabel()).thenReturn("Suisse");
    when(objet.getPlaqueCantonCode()).thenReturn("GE");
    when(objet.getPlaqueCantonLabel()).thenReturn("Genève");
    return objet;
  }
}
