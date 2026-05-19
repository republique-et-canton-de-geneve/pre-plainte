package ch.ge.police.core.application.service;

import ch.ge.police.core.port.in.EsiriusUseCase;
import ch.ge.police.core.port.out.EsiriusPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsiriusService implements EsiriusUseCase {

private final EsiriusPort esiriusPort;

  @Override
  public List<Map<String, Object>> getAllSites() {
    log.info("Récupération des sites");
    return esiriusPort.getAllSites();
  }

  @Override
  public List<Map<String, Object>> getServiceListBySiteCode(String siteCode) {
    log.info("Récupération des services du site {}", siteCode);
    return esiriusPort.getServiceListBySiteCode(siteCode);
  }

  @Override
  public List<Map<String, Object>> getAvailabilityBySiteCode(String siteCode, String serviceId, String begin, String period) {
    log.info("Récupération des créneaux site={} service={}", siteCode, serviceId);
    return esiriusPort.getAvailabilityBySiteCode(siteCode, serviceId, begin, period);
  }

  @Override
  public Map<String, Object> createAppointment(Map<String, Object> appointmentData) {
    log.info("Création d’un rendez-vous");
    return esiriusPort.createAppointment(appointmentData);
  }

  @Override
  public Map<String, Object> getInfoByRdvCode(String codeRdv) {
    log.info("Consultation du rendez-vous {}", codeRdv);
    return esiriusPort.getInfoByRdvCode(codeRdv);
  }

  @Override
  public Map<String, Object> cancelAppointmentByRdvCode(String codeRdv) {
    log.info("Suppression du rendez-vous {}", codeRdv);
    return esiriusPort.cancelAppointmentByRdvCode(codeRdv);
  }

  @Override
  public Map<String, Object> updateAppointmentByRdvCode(Map<String, Object> appointmentData) {
    log.info("Mise à jour du rendez-vous");
    return esiriusPort.updateAppointmentByRdvCode(appointmentData);
  }
}
