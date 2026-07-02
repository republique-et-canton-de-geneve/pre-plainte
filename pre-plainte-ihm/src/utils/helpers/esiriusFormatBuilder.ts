import type { SelectedCreneau } from "@/types/rendez-vous-interface";
import { toIsoDate } from "@/utils/helpers/dateHelpers.ts";
import { DAY_END, DAY_START, MONTH_END, MONTH_START, TIME_START, YEAR_END, YEAR_START } from "@/constants/constant.ts";

const CODE_PPEL = "PPEL";
const CRENEAU_TYPE_STATION = "STATION";

export function buildEsiriusPayload(demandeId: string | null, userData: any, selectedCreneau: any,) {
  const { beginDate, beginTime, endDate, endTime } = extractCreneauDates(selectedCreneau);
  return {
    beginDate,
    beginTime,
    endDate,
    endTime,
    user: mapEsiriusUser(userData, demandeId),
    serviceId: String(selectedCreneau?.serviceId ?? ""),
    siteCode: selectedCreneau?.siteCode ?? CODE_PPEL,
    resources: mapEsiriusResources(selectedCreneau),
    motives: [],
  };
}

function extractCreneauDates(selectedCreneau: any) {
  const beginDateTime = selectedCreneau?.beginDateTime ?? "";
  const endDateTime = selectedCreneau?.endDateTime ?? "";
  const beginDateRaw = beginDateTime.slice(YEAR_START, DAY_END);
  const endDateRaw = endDateTime.slice(YEAR_START, DAY_END);

  return {
    beginDate: formatDate(beginDateRaw),
    beginTime: formatTime(beginDateTime, "10:00"),
    endDate: formatDate(endDateRaw),
    endTime: formatTime(endDateTime, "10:30"),
  };
}

function formatDate(d: string) {
  if (!d || d.length < DAY_END) {
    return "";
  }
  return `${d.slice(YEAR_START, YEAR_END)}-${d.slice(MONTH_START, MONTH_END,)}-${d.slice(DAY_START, DAY_END)}`;
}

function formatTime(datetime: string, fallback: string) {
  return datetime?.slice(TIME_START)?.trim()?.replace("h", ":") || fallback;
}

function mapEsiriusUser(userData: any, demandeId: string | null) {
  const email = userData.email ?? "";
  return {
    lastName: userData.nom || email || demandeId || "",
    firstName: userData.prenom || "",
    personalIdentity: demandeId,
    fixPhone: "",
    birthday: toIsoDate(userData.dateNaissance),
    email,
    phone: normalizePhone(userData.telephone),
    address: mapAddress(userData),
  };
}

function normalizePhone(phone?: string) {
  return phone?.replaceAll(/\s+|\+(?=\d)/g, "");
}

function mapAddress(userData: any) {
  return {
    line1: userData.adresse ?? "",
    line2: "",
    zipCode: userData.npa ?? "",
    city: userData.localite ?? "",
    country: normalizeCountry(userData.pays),
  };
}

function normalizeCountry(country: string) {
  return country?.toLowerCase() === "ch" ? "suisse" : country ?? "suisse";
}

function mapEsiriusResources(selectedCreneau: any) {
  return {
    id: Number(selectedCreneau?.resource?.id ?? 0),
    key: String(selectedCreneau?.resource?.key ?? ""),
    type: selectedCreneau?.resource?.type ?? CRENEAU_TYPE_STATION,
    station: {},
  };
}

const formatDateForPayload = (dateTimeStr: string): string => {
  if (!dateTimeStr || dateTimeStr.length < DAY_END) {
    return "";
  }
  return `${dateTimeStr.slice(YEAR_START, YEAR_END)}-${dateTimeStr.slice(MONTH_START, MONTH_END)}-${dateTimeStr.slice(DAY_START, DAY_END)}`;
};

const formatTimeForPayload = (dateTimeStr: string): string => {
  if (!dateTimeStr || dateTimeStr.length < TIME_START) {
    return "10:00";
  }
  return dateTimeStr.substring(TIME_START).trim().replace("h", ":") || "10:00";
};

export function buildUpdateAppointmentPayload(appointment: any, creneau: SelectedCreneau,) {
  return {
    idSys: appointment.idSys,
    codeRDV: appointment.codeRDV,
    ...mapAppointmentDates(creneau),
    comment: appointment.comment ?? "",
    needsConfirmation: appointment.needsConfirmation ?? false,
    rdvChannel: appointment.rdvChannel ?? "EAPP0",
    user: mapAppointmentUser(appointment),
    serviceId: String(creneau.serviceId || appointment.serviceId),
    siteCode: creneau.siteCode || appointment.siteCode,
    siteIdSys: appointment.siteIdSys,
    resources: mapAppointmentResources(creneau),
    motives: appointment.motives ?? [],
  };
}

function mapAppointmentDates(creneau: SelectedCreneau) {
  return {
    beginDate: formatDateForPayload(creneau.beginDateTime),
    beginTime: formatTimeForPayload(creneau.beginDateTime),
    endDate: formatDateForPayload(creneau.endDateTime),
    endTime: formatTimeForPayload(creneau.endDateTime),
  };
}

function mapAppointmentUser(appointment: any) {
  return {
    idSys: appointment.user?.idSys,
    personalIdentity: appointment.user?.personalIdentity,
    fixPhone: appointment.user?.fixPhone,
    additionalPersonalIdentity: appointment.user?.additionalPersonalIdentity ?? [],
    lastName: appointment.user?.lastName,
    civility: appointment.user?.civility ?? "",
    firstName: appointment.user?.firstName,
    birthday: appointment.user?.birthday,
    email: appointment.user?.email,
    phone: appointment.user?.phone,
    address: mapAppointmentAddress(appointment.user),
  };
}

function mapAppointmentAddress(user: any) {
  return {
    line1: user?.address?.line1 ?? "",
    line2: user?.address?.line2 ?? "",
    zipCode: user?.address?.zipCode ?? "",
    city: user?.address?.city ?? "",
    country: user?.address?.country ?? "SUISSE",
  };
}

function mapAppointmentResources(creneau: SelectedCreneau) {
  return {
    id: Number(creneau?.resource?.id || 0),
    key: String(creneau?.resource?.key || ""),
    type: creneau?.resource?.type || CRENEAU_TYPE_STATION,
    name: creneau?.resource?.name || "",
    station: creneau?.resource?.station || {},
  };
}
