package ch.ge.police.core.port.out;

import java.util.List;
import java.util.Map;

public interface EsiriusPort {

  List<Map<String, Object>> getAllSites();
  List<Map<String, Object>> getServiceListBySiteCode(String siteCode);
  List<Map<String, Object>> getAvailabilityBySiteCode(String siteCode, String serviceId, String begin, String period);

  Map<String, Object> createAppointment(Map<String, Object> appointmentData);
  Map<String, Object> getInfoByRdvCode(String codeRdv);
  Map<String, Object> cancelAppointmentByRdvCode(String codeRdv);
  Map<String, Object> updateAppointmentByRdvCode(Map<String, Object> appointmentData);
}
