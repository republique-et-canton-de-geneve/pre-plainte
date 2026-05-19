package ch.ge.police.ui.controller;

import ch.ge.police.core.port.in.EsiriusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/esirius")
@RequiredArgsConstructor
public class EsiriusController {

  private final EsiriusUseCase esiriusUseCase;

  @GetMapping("/sites")
  public List<Map<String, Object>> getAllSites() {
    return esiriusUseCase.getAllSites();
  }

  @GetMapping("/sites/{siteCode}/listServices")
  public List<Map<String, Object>> getServiceListBySiteCode(@PathVariable String siteCode) {
    return esiriusUseCase.getServiceListBySiteCode(siteCode);
  }

  @GetMapping("/sites/{siteCode}/services/{serviceId}/plannings/begins/{begin}/periods/{period}/availabilities")
  public List<Map<String, Object>> getAvailabilityBySiteCode(
    @PathVariable String siteCode,
    @PathVariable String serviceId,
    @PathVariable String begin,
    @PathVariable String period
  ) {
    return esiriusUseCase.getAvailabilityBySiteCode(siteCode, serviceId, begin, period);
  }

  @PostMapping("/appointments")
  public Map<String, Object> createAppointment(@RequestBody Map<String, Object> appointmentData) {
    return esiriusUseCase.createAppointment(appointmentData);
  }

  @GetMapping("/appointments/{codeRdv}")
  public Map<String, Object> getInfoByRdvCode(@PathVariable String codeRdv) {
    return esiriusUseCase.getInfoByRdvCode(codeRdv);
  }

  @DeleteMapping("/appointments/{codeRdv}")
  public Map<String, Object> cancelAppointmentByRdvCode(@PathVariable String codeRdv) {
    return esiriusUseCase.cancelAppointmentByRdvCode(codeRdv);
  }

  @PutMapping("/appointments")
  public Map<String, Object> updateAppointmentByRdvCode(@RequestBody Map<String, Object> appointmentData) {
    return esiriusUseCase.updateAppointmentByRdvCode(appointmentData);
  }
}
