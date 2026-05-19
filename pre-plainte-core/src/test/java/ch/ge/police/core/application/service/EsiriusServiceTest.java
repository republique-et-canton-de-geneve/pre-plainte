package ch.ge.police.core.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.ge.police.core.port.out.EsiriusPort;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EsiriusServiceTest {

  private EsiriusPort port;
  private EsiriusService service;

  @BeforeEach
  void setUp() {
    port = mock(EsiriusPort.class);
    service = new EsiriusService(port);
  }

  @Test
  void shouldReturnSitesFromPort() {
    List<Map<String, Object>> expected = List.of(Map.of("code", "PPEL"));
    when(port.getAllSites()).thenReturn(expected);

    List<Map<String, Object>> result = service.getAllSites();

    assertEquals(expected, result);
  }

  @Test
  void shouldReturnServicesBySiteCode() {
    List<Map<String, Object>> expected = List.of(Map.of("id", "149"));
    when(port.getServiceListBySiteCode("PPEL")).thenReturn(expected);

    List<Map<String, Object>> result = service.getServiceListBySiteCode("PPEL");

    assertEquals(expected, result);
  }

  @Test
  void shouldReturnAvailabilityBySiteAndService() {
    List<Map<String, Object>> expected = List.of(Map.of("creneau", "14:00"));
    when(port.getAvailabilityBySiteCode("PPEL", "149", "2025-10-21", "7"))
      .thenReturn(expected);

    List<Map<String, Object>> result =
      service.getAvailabilityBySiteCode("PPEL", "149", "2025-10-21", "7");

    assertEquals(expected, result);
  }

  @Test
  void shouldCreateAppointment() {
    Map<String, Object> appointment = Map.of("id", "123");
    when(port.createAppointment(appointment)).thenReturn(appointment);

    Map<String, Object> result = service.createAppointment(appointment);

    assertEquals(appointment, result);
  }

  @Test
  void shouldReturnAppointmentInfo() {
    Map<String, Object> info = Map.of("status", "confirmed");
    when(port.getInfoByRdvCode("ABC123")).thenReturn(info);

    Map<String, Object> result = service.getInfoByRdvCode("ABC123");

    assertEquals(info, result);
  }

  @Test
  void shouldCancelAppointment() {
    Map<String, Object> cancel = Map.of("status", "cancelled");
    when(port.cancelAppointmentByRdvCode("ABC123")).thenReturn(cancel);

    Map<String, Object> result = service.cancelAppointmentByRdvCode("ABC123");

    assertEquals(cancel, result);
  }

  @Test
  void shouldUpdateAppointment() {
    Map<String, Object> appointment = Map.of("id", "456");
    when(port.updateAppointmentByRdvCode(appointment)).thenReturn(appointment);

    Map<String, Object> result = service.updateAppointmentByRdvCode(appointment);

    assertEquals(appointment, result);
  }
}
