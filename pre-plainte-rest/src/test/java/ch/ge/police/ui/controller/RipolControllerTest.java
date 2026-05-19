package ch.ge.police.ui.controller;

import ch.ge.police.core.domain.model.ripol.Ripol;
import ch.ge.police.core.port.out.RipolPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RipolControllerTest {

  private RipolPort ripolPort;
  private RipolController controller;

  @BeforeEach
  void setUp() {
    ripolPort = mock(RipolPort.class);
    controller = new RipolController(ripolPort);
  }

  @Test
  void getCodes_withSearch_callsSearchCodesByGroupType() {
    List<Ripol> expected = List.of(mock(Ripol.class));

    when(ripolPort.searchCodesByGroupType("group", "search")).thenReturn(expected);

    List<Ripol> result = controller.getCodes("group", "search");

    assertSame(expected, result);

    verify(ripolPort).searchCodesByGroupType("group", "search");
  }

  @Test
  void getCodes_withoutSearch_callsGetCodesByGroupType() {
    List<Ripol> expected = List.of(mock(Ripol.class));

    when(ripolPort.getCodesByGroupType("group")).thenReturn(expected);

    List<Ripol> result = controller.getCodes("group", null);

    assertSame(expected, result);

    verify(ripolPort).getCodesByGroupType("group");
  }

  @Test
  void getGroupTypes_returnsDistinctGroupTypes() {
    List<String> expected = List.of("A", "B");

    when(ripolPort.listDistinctGroupTypes(RipolController.TBINCIDENTCODE))
      .thenReturn(expected);

    List<String> result = controller.getGroupTypes();

    assertSame(expected, result);

    verify(ripolPort).listDistinctGroupTypes(RipolController.TBINCIDENTCODE);
  }

  @Test
  void getBrands_withSearch_callsSearchBrands() {
    List<Ripol> expected = List.of(mock(Ripol.class));

    when(ripolPort.searchBrands("master", "183", "search")).thenReturn(expected);

    List<Ripol> result = controller.getBrands("master", "183", "search");

    assertSame(expected, result);

    verify(ripolPort).searchBrands("master", "183", "search");
  }

  @Test
  void getBrands_withoutSearch_callsGetBrandsByTypeAndMasterType() {
    List<Ripol> expected = List.of(mock(Ripol.class));

    when(ripolPort.getBrandsByTypeAndMasterType("master", "183"))
      .thenReturn(expected);

    List<Ripol> result = controller.getBrands("master", "183", null);

    assertSame(expected, result);

    verify(ripolPort).getBrandsByTypeAndMasterType("master", "183");
  }

  @Test
  void getModels_withSearch_callsSearchModels() {
    List<Ripol> expected = List.of(mock(Ripol.class));

    when(ripolPort.searchModels("brand", "search")).thenReturn(expected);

    List<Ripol> result = controller.getModels("brand", "search");

    assertSame(expected, result);

    verify(ripolPort).searchModels("brand", "search");
  }

  @Test
  void getModels_withoutSearch_callsGetModelsByBrand() {
    List<Ripol> expected = List.of(mock(Ripol.class));

    when(ripolPort.getModelsByBrand("brand")).thenReturn(expected);

    List<Ripol> result = controller.getModels("brand", null);

    assertSame(expected, result);

    verify(ripolPort).getModelsByBrand("brand");
  }

  @Test
  void getPhoneBrands_withSearch_callsSearchBrands() {
    List<Ripol> expected = List.of(mock(Ripol.class));

    when(ripolPort.searchBrands(
      RipolController.CODE_TELEPHONE_MOBILE,
      RipolController.MASTER_TYPE_OBJETS,
      "search")).thenReturn(expected);

    List<Ripol> result = controller.getPhoneBrands("search");

    assertSame(expected, result);

    verify(ripolPort).searchBrands(
      RipolController.CODE_TELEPHONE_MOBILE,
      RipolController.MASTER_TYPE_OBJETS,
      "search");
  }

  @Test
  void getPhoneBrands_withoutSearch_callsGetBrandsByType() {
    List<Ripol> expected = List.of(mock(Ripol.class));

    when(ripolPort.getBrandsByType(RipolController.CODE_TELEPHONE_MOBILE))
      .thenReturn(expected);

    List<Ripol> result = controller.getPhoneBrands(null);

    assertSame(expected, result);

    verify(ripolPort).getBrandsByType(RipolController.CODE_TELEPHONE_MOBILE);
  }

  @Test
  void getVehicleBrands_withSearch_callsSearchBrands() {
    List<Ripol> expected = List.of(mock(Ripol.class));

    when(ripolPort.searchBrands(
      "vehicle",
      RipolController.MASTER_TYPE_VEHICULES,
      "search")).thenReturn(expected);

    List<Ripol> result = controller.getVehicleBrands("vehicle", "search");

    assertSame(expected, result);

    verify(ripolPort).searchBrands(
      "vehicle",
      RipolController.MASTER_TYPE_VEHICULES,
      "search");
  }

  @Test
  void getVehicleBrands_withoutSearch_callsGetBrandsByTypeAndMasterType() {
    List<Ripol> expected = List.of(mock(Ripol.class));

    when(ripolPort.getBrandsByTypeAndMasterType(
      "vehicle",
      RipolController.MASTER_TYPE_VEHICULES)).thenReturn(expected);

    List<Ripol> result = controller.getVehicleBrands("vehicle", null);

    assertSame(expected, result);

    verify(ripolPort).getBrandsByTypeAndMasterType(
      "vehicle",
      RipolController.MASTER_TYPE_VEHICULES);
  }

  @Test
  void endpointsWithoutSearch_callGetCodesByGroupType() {
    List<Ripol> expected = List.of(mock(Ripol.class));

    when(ripolPort.getCodesByGroupType(RipolController.MASTER_TYPE_OBJETS))
      .thenReturn(expected);
    when(ripolPort.getCodesByGroupType(RipolController.MASTER_TYPE_VEHICULES))
      .thenReturn(expected);
    when(ripolPort.getCodesByGroupType(RipolController.GROUP_TYPE_SEXE))
      .thenReturn(expected);
    when(ripolPort.getCodesByGroupType(RipolController.GROUP_TYPE_NATIONALITE))
      .thenReturn(expected);
    when(ripolPort.getCodesByGroupType(RipolController.GROUP_TYPE_COMMUNE))
      .thenReturn(expected);
    when(ripolPort.getCodesByGroupType(RipolController.GROUP_TYPE_LIEU_ORIGINE))
      .thenReturn(expected);
    when(ripolPort.getCodesByGroupType(RipolController.GROUP_TYPE_TYPE_DOCUMENT))
      .thenReturn(expected);
    when(ripolPort.getCodesByGroupType(RipolController.GROUP_TYPE_LIEU))
      .thenReturn(expected);
    when(ripolPort.getCodesByGroupType(RipolController.GROUP_TYPE_MODE_OPERATOIRE))
      .thenReturn(expected);
    when(ripolPort.getCodesByGroupType(RipolController.GROUP_TYPE_COULEUR))
      .thenReturn(expected);
    when(ripolPort.getCodesByGroupType(RipolController.GROUP_TYPE_CANTON))
      .thenReturn(expected);

    assertSame(expected, controller.getObjectTypes(null));
    assertSame(expected, controller.getVehicleTypes(null));
    assertSame(expected, controller.getSexes());
    assertSame(expected, controller.getNationalities(null));
    assertSame(expected, controller.getCommunes(null));
    assertSame(expected, controller.getLieuxOrigine(null));
    assertSame(expected, controller.getDocumentTypes(null));
    assertSame(expected, controller.getLocationTypes(null));
    assertSame(expected, controller.getModusOperandi(null));
    assertSame(expected, controller.getObjectColours(null));
    assertSame(expected, controller.getVehicleColours(null));
    assertSame(expected, controller.getCantons(null));

    verify(ripolPort).getCodesByGroupType(RipolController.MASTER_TYPE_OBJETS);
    verify(ripolPort).getCodesByGroupType(RipolController.MASTER_TYPE_VEHICULES);
    verify(ripolPort).getCodesByGroupType(RipolController.GROUP_TYPE_SEXE);
    verify(ripolPort).getCodesByGroupType(RipolController.GROUP_TYPE_NATIONALITE);
    verify(ripolPort).getCodesByGroupType(RipolController.GROUP_TYPE_COMMUNE);
    verify(ripolPort).getCodesByGroupType(RipolController.GROUP_TYPE_LIEU_ORIGINE);
    verify(ripolPort).getCodesByGroupType(RipolController.GROUP_TYPE_TYPE_DOCUMENT);
    verify(ripolPort).getCodesByGroupType(RipolController.GROUP_TYPE_LIEU);
    verify(ripolPort).getCodesByGroupType(RipolController.GROUP_TYPE_MODE_OPERATOIRE);

    verify(ripolPort).getCodesByGroupType(RipolController.GROUP_TYPE_CANTON);

    verify(ripolPort, org.mockito.Mockito.times(2))
      .getCodesByGroupType(RipolController.GROUP_TYPE_COULEUR);
  }

  @Test
  void endpointsWithSearch_callSearchCodesByGroupType() {
    List<Ripol> expected = List.of(mock(Ripol.class));

    when(ripolPort.searchCodesByGroupType(RipolController.MASTER_TYPE_OBJETS, "search"))
      .thenReturn(expected);
    when(ripolPort.searchCodesByGroupType(RipolController.MASTER_TYPE_VEHICULES, "search"))
      .thenReturn(expected);
    when(ripolPort.searchCodesByGroupType(RipolController.GROUP_TYPE_NATIONALITE, "search"))
      .thenReturn(expected);
    when(ripolPort.searchCodesByGroupType(RipolController.GROUP_TYPE_COMMUNE, "search"))
      .thenReturn(expected);
    when(ripolPort.searchCodesByGroupType(RipolController.GROUP_TYPE_LIEU_ORIGINE, "search"))
      .thenReturn(expected);
    when(ripolPort.searchCodesByGroupType(RipolController.GROUP_TYPE_TYPE_DOCUMENT, "search"))
      .thenReturn(expected);
    when(ripolPort.searchCodesByGroupType(RipolController.GROUP_TYPE_LIEU, "search"))
      .thenReturn(expected);
    when(ripolPort.searchCodesByGroupType(RipolController.GROUP_TYPE_MODE_OPERATOIRE, "search"))
      .thenReturn(expected);
    when(ripolPort.searchCodesByGroupType(RipolController.GROUP_TYPE_COULEUR, "search"))
      .thenReturn(expected);
    when(ripolPort.searchCodesByGroupType(RipolController.GROUP_TYPE_CANTON, "search"))
      .thenReturn(expected);

    assertSame(expected, controller.getObjectTypes("search"));
    assertSame(expected, controller.getVehicleTypes("search"));
    assertSame(expected, controller.getNationalities("search"));
    assertSame(expected, controller.getCommunes("search"));
    assertSame(expected, controller.getLieuxOrigine("search"));
    assertSame(expected, controller.getDocumentTypes("search"));
    assertSame(expected, controller.getLocationTypes("search"));
    assertSame(expected, controller.getModusOperandi("search"));
    assertSame(expected, controller.getObjectColours("search"));
    assertSame(expected, controller.getVehicleColours("search"));
    assertSame(expected, controller.getCantons("search"));

    verify(ripolPort).searchCodesByGroupType(RipolController.MASTER_TYPE_OBJETS, "search");
    verify(ripolPort).searchCodesByGroupType(RipolController.MASTER_TYPE_VEHICULES, "search");
    verify(ripolPort).searchCodesByGroupType(RipolController.GROUP_TYPE_NATIONALITE, "search");
    verify(ripolPort).searchCodesByGroupType(RipolController.GROUP_TYPE_COMMUNE, "search");
    verify(ripolPort).searchCodesByGroupType(RipolController.GROUP_TYPE_LIEU_ORIGINE, "search");
    verify(ripolPort).searchCodesByGroupType(RipolController.GROUP_TYPE_TYPE_DOCUMENT, "search");
    verify(ripolPort).searchCodesByGroupType(RipolController.GROUP_TYPE_LIEU, "search");
    verify(ripolPort).searchCodesByGroupType(RipolController.GROUP_TYPE_MODE_OPERATOIRE, "search");
    verify(ripolPort).searchCodesByGroupType(RipolController.GROUP_TYPE_CANTON, "search");

    verify(ripolPort, org.mockito.Mockito.times(2))
      .searchCodesByGroupType(RipolController.GROUP_TYPE_COULEUR, "search");
  }

  @Test
  void phoneAndVehicleModels_delegateToPrivateMethod() {
    List<Ripol> expected = List.of(mock(Ripol.class));

    when(ripolPort.searchModels("brand", "search")).thenReturn(expected);
    when(ripolPort.getModelsByBrand("brand")).thenReturn(expected);

    assertSame(expected, controller.getPhoneModels("brand", "search"));
    assertSame(expected, controller.getVehicleModels("brand", "search"));

    assertSame(expected, controller.getPhoneModels("brand", null));
    assertSame(expected, controller.getVehicleModels("brand", null));

    verify(ripolPort, org.mockito.Mockito.times(2))
      .searchModels("brand", "search");

    verify(ripolPort, org.mockito.Mockito.times(2))
      .getModelsByBrand("brand");
  }
}
