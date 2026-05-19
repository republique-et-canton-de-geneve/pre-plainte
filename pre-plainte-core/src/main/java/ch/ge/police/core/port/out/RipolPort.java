package ch.ge.police.core.port.out;

import ch.ge.police.core.domain.model.ripol.Ripol;

import java.util.List;
import java.util.Map;

public interface RipolPort {

  List<String> listTables();

  List<String> listColumns(String tableName);

  List<Map<String, Object>> listTableContent(String tableName, int limit);

  List<String> listDistinctGroupTypes(String tableName);

  List<Map<String, Object>> listRowsByGroupType(String tableName, String groupType, int limit);

  List<Ripol> getCodesByGroupType(String groupType);

  List<Ripol> searchCodesByGroupType(String groupType, String search);

  List<Ripol> getBrandsByType(String masterValue);

  List<Ripol> getBrandsByTypeAndMasterType(String masterValue, String masterType);

  List<Ripol> searchBrands(String masterValue, String masterType, String search);

  List<Ripol> getModelsByBrand(String brandCode);

  List<Ripol> searchModels(String brandCode, String search);
}

