package ch.ge.police.rest.controller;

import ch.ge.police.core.port.in.EsiriusUseCase;
import ch.ge.police.ui.controller.EsiriusController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EsiriusControllerTest {

  private MockMvc mockMvc;
  private EsiriusUseCase esiriusUseCase;
  private final ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    esiriusUseCase = Mockito.mock(EsiriusUseCase.class);
    EsiriusController controller = new EsiriusController(esiriusUseCase);

    mockMvc = MockMvcBuilders
      .standaloneSetup(controller)
      .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter())
      .build();
  }

  @Test
  void testGetAllSites() throws Exception {
    when(esiriusUseCase.getAllSites()).thenReturn(List.of(Map.of("code", "PPEL", "nom", "Police")));

    mockMvc.perform(get("/api/esirius/sites"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].code").value("PPEL"))
      .andExpect(jsonPath("$[0].nom").value("Police"));
  }

  @Test
  void testGetServiceListBySiteCode() throws Exception {
    when(esiriusUseCase.getServiceListBySiteCode("PPEL"))
      .thenReturn(List.of(Map.of("id", "149", "libelle", "Accueil")));

    mockMvc.perform(get("/api/esirius/sites/PPEL/listServices"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id").value("149"))
      .andExpect(jsonPath("$[0].libelle").value("Accueil"));
  }

  @Test
  void testGetAvailabilityBySiteCode() throws Exception {
    when(esiriusUseCase.getAvailabilityBySiteCode("PPEL", "149", "2025-10-23", "P7D"))
      .thenReturn(List.of(Map.of("date", "2025-10-23", "heure", "14:00")));

    mockMvc.perform(get("/api/esirius/sites/PPEL/services/149/plannings/begins/2025-10-23/periods/P7D/availabilities"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].date").value("2025-10-23"))
      .andExpect(jsonPath("$[0].heure").value("14:00"));
  }

  @Test
  void testCreateAppointment() throws Exception {
    Map<String, Object> input = Map.of("comment", "Rendez-vous test");
    Map<String, Object> output = Map.of("id", "123", "status", "created");
    when(esiriusUseCase.createAppointment(any(Map.class))).thenReturn(output);

    mockMvc.perform(post("/api/esirius/appointments")
        .contentType("application/json")
        .content(mapper.writeValueAsString(input)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value("123"))
      .andExpect(jsonPath("$.status").value("created"));
  }

  @Test
  void testGetInfoByRdvCode() throws Exception {
    when(esiriusUseCase.getInfoByRdvCode("ABC123")).thenReturn(Map.of("status", "confirmed"));

    mockMvc.perform(get("/api/esirius/appointments/ABC123"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("confirmed"));
  }

  @Test
  void testCancelAppointment() throws Exception {
    when(esiriusUseCase.cancelAppointmentByRdvCode("ABC123"))
      .thenReturn(Map.of("status", "cancelled"));

    mockMvc.perform(delete("/api/esirius/appointments/ABC123"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("cancelled"));
  }

  @Test
  void testUpdateAppointment() throws Exception {
    Map<String, Object> input = Map.of("id", "456");
    Map<String, Object> output = Map.of("id", "456", "status", "updated");
    when(esiriusUseCase.updateAppointmentByRdvCode(any(Map.class))).thenReturn(output);

    mockMvc.perform(put("/api/esirius/appointments")
        .contentType("application/json")
        .content(mapper.writeValueAsString(input)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value("456"))
      .andExpect(jsonPath("$.status").value("updated"));
  }
}
