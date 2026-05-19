package ch.ge.police.ui.controller;

import ch.ge.police.core.port.out.RipolPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Controller pour l'exploration de la base SQLite (usage admin/debug).
 */
@RestController
@RequestMapping("/api/sqlite")
@RequiredArgsConstructor
public class SqliteMetadataController {

  private final RipolPort ripolPort;

  @GetMapping("/tables")
  public List<String> listTables() {
    return ripolPort.listTables();
  }

  @GetMapping("/columns")
  public List<String> listColumns(@RequestParam("tableName") String tableName) {
    try {
      return ripolPort.listColumns(tableName);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    } catch (RuntimeException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @GetMapping("/rows")
  public List<Map<String, Object>> listRows(
    @RequestParam("tableName") String tableName,
    @RequestParam(name = "limit", defaultValue = "100") int limit
  ) {
    try {
      return ripolPort.listTableContent(tableName, limit);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    } catch (RuntimeException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @GetMapping("/grouptypes")
  public List<String> listDistinctGroupTypes(
    @RequestParam("tableName") String tableName
  ) {
    try {
      return ripolPort.listDistinctGroupTypes(tableName);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    } catch (RuntimeException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @GetMapping("/rows/by-grouptype")
  public List<Map<String, Object>> listRowsByGroupType(
    @RequestParam("tableName") String tableName,
    @RequestParam("groupType") String groupType,
    @RequestParam(name = "limit", defaultValue = "100") int limit
  ) {
    try {
      return ripolPort.listRowsByGroupType(tableName, groupType, limit);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    } catch (RuntimeException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }
}
