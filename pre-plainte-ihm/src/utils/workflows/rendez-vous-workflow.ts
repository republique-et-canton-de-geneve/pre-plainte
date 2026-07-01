import { toIsoDate } from "@/utils/helpers/dateHelpers";
import { hasVehiculeVoleAvecPlaque } from "@/utils/helpers/volObjetVolHelpers";
import type { PrePlainteFormFields } from "@/types/pre-plainte.interface";

export interface RendezVousService {
  key: string;
  name?: string;
}

export interface RendezVousAvailabilityGroup {
  serviceId: string;
  serviceName?: string;
  availabilities?: RendezVousAvailability[];
}

export interface RendezVousAvailability {
  beginDateTime?: string;
  endDateTime?: string;
  serviceId?: string;
  siteCode?: string;
  resource?: { key?: string; name?: string };
}

export interface RendezVousWarning {
  type: "info" | "warning" | "error";
  messageKey: string;
}

const YEAR_START = 0;
const YEAR_END = 4;
const MONTH_START = 4;
const MONTH_END = 6;
const DAY_START = 6;
const DAY_END = 8;
const HOUR_MINUTE_START = 9;
const VEHICULE_PLAQUE_MAX_RENDEZ_VOUS_HOURS = 24;
const RENDEZ_VOUS_DATE_WINDOW_DAYS = 15;

export function filterServicesByIncident(
  services: RendezVousService[],
  availabilityGroups: RendezVousAvailabilityGroup[],
  typeIncident?: string,
): RendezVousService[] {
  const servicesAvecDispos = new Set(
    availabilityGroups
      .filter(a => a.availabilities && a.availabilities.length > 0)
      .map(a => a.serviceId),
  );

  const servicesFiltrables = services.filter(s => servicesAvecDispos.has(s.key));
  const incident = (typeIncident || "").toLowerCase();

  if (!incident) {
    return servicesFiltrables;
  }

  return servicesFiltrables.filter(service => matchIncidentWithService(incident, service.name || ""));
}

export function flattenAvailabilities(availabilityGroups: RendezVousAvailabilityGroup[]): RendezVousAvailability[] {
  return availabilityGroups.flatMap(serviceAvailabilities =>
    (serviceAvailabilities.availabilities || []).map(creneau => ({
      ...creneau,
      serviceId: creneau.serviceId ?? serviceAvailabilities.serviceId,
      siteCode: creneau.siteCode ?? "PPEL",
    })),
  );
}

export function filterCompatibleCreneaux(
  availabilityGroups: RendezVousAvailabilityGroup[],
  services: RendezVousService[],
  formData: Pick<PrePlainteFormFields, "typeIncident" | "isVehicle" | "categorieObjet" | "sousCategorie" | "plaqueInconnu" | "plaqueNumero" | "objetsVolesValides">,
  now = new Date(),
): RendezVousAvailability[] {
  const incident = (formData.typeIncident || "").toLowerCase();
  const minDate = new Date(now);
  minDate.setHours(minDate.getHours() + 1);

  const limiteVehiculeAvecPlaque = new Date(now);
  limiteVehiculeAvecPlaque.setHours(limiteVehiculeAvecPlaque.getHours() + VEHICULE_PLAQUE_MAX_RENDEZ_VOUS_HOURS);

  return flattenAvailabilities(availabilityGroups).filter(creneau => {
    const dateCreneau = parseCreneauDate(creneau.beginDateTime);
    if (!dateCreneau || dateCreneau <= minDate) {
      return false;
    }

    if (!isInRollingAppointmentWindow(dateCreneau, now)) {
      return false;
    }

    if (hasVehiculeVoleAvecPlaque(formData) && dateCreneau > limiteVehiculeAvecPlaque) {
      return false;
    }

    const service = services.find(s => s.key === creneau.serviceId);
    return matchIncidentWithService(incident, service?.name || "");
  });
}

export function filterCreneauxByPosteAndDate(
  availabilityGroups: RendezVousAvailabilityGroup[],
  compatibleCreneaux: RendezVousAvailability[],
  poste?: RendezVousService | null,
  dateSouhaitee?: string,
): RendezVousAvailability[] {
  const idsCompatibles = new Set(compatibleCreneaux.map(getCreneauKey));
  const groups = poste
    ? availabilityGroups.filter(a => a.serviceId === poste.key || a.serviceName?.includes(poste.name || ""))
    : availabilityGroups;

  return flattenAvailabilities(groups).filter(
    c => idsCompatibles.has(getCreneauKey(c)) && isSameSelectedDate(c.beginDateTime, dateSouhaitee),
  );
}

export function getRendezVousWarning(
  formData: Pick<PrePlainteFormFields, "typeIncident" | "isVehicle" | "categorieObjet" | "sousCategorie" | "plaqueInconnu" | "plaqueNumero" | "objetsVolesValides">,
  aucunCreneauVehiculeAvecPlaque: boolean,
): RendezVousWarning | null {
  if (formData.typeIncident !== "vol") {
    return null;
  }

  if (aucunCreneauVehiculeAvecPlaque) {
    return {
      type: "warning",
      messageKey: "rendezVous.warningVolVehiculePlaqueSansCreneau",
    };
  }

  if (hasVehiculeVoleAvecPlaque(formData)) {
    return {
      type: "error",
      messageKey: "rendezVous.warningVolVehiculePlaque",
    };
  }

  return {
    type: "info",
    messageKey: "rendezVous.warningAutreVol",
  };
}

export function getCreneauKey(creneau: RendezVousAvailability): string {
  return `${creneau.serviceId ?? ""}|${creneau.beginDateTime ?? ""}|${creneau.resource?.key ?? ""}`;
}

export function parseCreneauDate(beginDateTime?: string): Date | null {
  if (!beginDateTime) {
    return null;
  }

  const dateStr = `${beginDateTime.slice(YEAR_START, YEAR_END)}-${beginDateTime.slice(MONTH_START, MONTH_END)}-${beginDateTime.slice(DAY_START, DAY_END)}T${beginDateTime.slice(HOUR_MINUTE_START)}:00`;
  const date = new Date(dateStr);
  return Number.isNaN(date.getTime()) ? null : date;
}

export function isSameSelectedDate(beginDateTime: string | undefined, selectedDate?: string): boolean {
  if (!selectedDate || !beginDateTime) {
    return true;
  }

  const dateCreneauJour = `${beginDateTime.slice(YEAR_START, YEAR_END)}-${beginDateTime.slice(MONTH_START, MONTH_END)}-${beginDateTime.slice(DAY_START, DAY_END)}`;
  return dateCreneauJour === (toIsoDate(selectedDate) ?? selectedDate);
}

export function isInRollingAppointmentWindow(date: Date, now = new Date()): boolean {
  const start = new Date(now);
  start.setHours(0, 0, 0, 0);
  const end = new Date(start);
  end.setDate(start.getDate() + RENDEZ_VOUS_DATE_WINDOW_DAYS - 1);
  const dateOnly = new Date(date);
  dateOnly.setHours(0, 0, 0, 0);
  return dateOnly >= start && dateOnly <= end;
}

export function matchIncidentWithService(incident: string, serviceName: string): boolean {
  const normalizedIncident = incident.toLowerCase();
  const normalizedServiceName = serviceName.toLowerCase();

  if (!normalizedIncident) {
    return true;
  }

  if (normalizedIncident.includes("vol")) {
    return normalizedServiceName.includes("vol");
  }

  if (normalizedIncident.includes("degat") || normalizedIncident.includes("dommage")) {
    return normalizedServiceName.includes("dommage");
  }

  if (normalizedIncident.includes("cyber")) {
    return normalizedServiceName.includes("cybercrime");
  }

  if (normalizedIncident.includes("dommage-cybercrime")) {
    return normalizedServiceName.includes("dommage") || normalizedServiceName.includes("cybercrime");
  }

  return false;
}
