import type { SelectedCreneau } from "@/types/rendez-vous-interface";
import { toIsoDate } from "@/utils/helpers/dateHelpers.ts";

const CODE_PPEL = "PPEL";
const CRENEAU_TYPE_STATION = "STATION";

export function buildEsiriusPayload(demandeId: string | null, userData: any, selectedCreneau: any) {
  const beginDateTime = selectedCreneau?.beginDateTime ?? "";
  const endDateTime = selectedCreneau?.endDateTime ?? "";

  const beginDate = beginDateTime.slice(0, 8);
  const beginTime = beginDateTime.slice(9).trim().replace("h", ":");
  const endDate = endDateTime.slice(0, 8);
  const endTime = endDateTime.slice(9).trim().replace("h", ":");

  const formatDate = (d: string) => {
    if (!d || d.length < 8) {
      return "";
    }
    return `${d.slice(0, 4)}-${d.slice(4, 6)}-${d.slice(6, 8)}`;
  };

  return {
    beginDate: formatDate(beginDate),
    beginTime: beginTime ?? "10:00",
    endDate: formatDate(endDate),
    endTime: endTime ?? "10:30",

    user: {
      lastName: userData.nom,
      firstName: userData.prenom,
      personalIdentity: demandeId,
      birthday: toIsoDate(userData.dateNaissance),
      email: userData.email,
      phone: userData.telephone?.replaceAll(/\s+|\+(?=\d)/g, ""),

      address: {
        line1: userData.adresse ?? "",
        line2: "",
        zipCode: userData.npa ?? "",
        city: userData.localite ?? "",
        country: userData.pays?.toLowerCase() === "ch" ? "suisse" : userData.pays ?? "suisse",
      },
    },

    serviceId: String(selectedCreneau?.serviceId ?? ""),
    siteCode: selectedCreneau?.siteCode ?? CODE_PPEL,

    resources: {
      id: Number(selectedCreneau?.resource?.id ?? 0),
      key: String(selectedCreneau?.resource?.key ?? ""),
      type: selectedCreneau?.resource?.type ?? CRENEAU_TYPE_STATION,
      station: {},
    },

    motives: [],
  };
}

const formatDateForPayload = (dateTimeStr: string): string => {
  if (!dateTimeStr || dateTimeStr.length < 8) {
    return "";
  }
  return `${dateTimeStr.slice(0, 4)}-${dateTimeStr.slice(4, 6)}-${dateTimeStr.slice(6, 8)}`;
};

const formatTimeForPayload = (dateTimeStr: string): string => {
  if (!dateTimeStr || dateTimeStr.length < 9) {
    return "10:00";
  }
  return dateTimeStr.substring(9).trim().replace("h", ":") || "10:00";
};

export function buildUpdateAppointmentPayload(appointment: any, creneau: SelectedCreneau) {
  return {
    idSys: appointment.idSys,
    codeRDV: appointment.codeRDV,
    beginDate: formatDateForPayload(creneau.beginDateTime),
    beginTime: formatTimeForPayload(creneau.beginDateTime),
    endDate: formatDateForPayload(creneau.endDateTime),
    endTime: formatTimeForPayload(creneau.endDateTime),
    comment: appointment.comment ?? "",
    needsConfirmation: appointment.needsConfirmation ?? false,
    rdvChannel: appointment.rdvChannel ?? "EAPP0",
    user: {
      idSys: appointment.user?.idSys,
      personalIdentity: appointment.user?.personalIdentity,
      additionalPersonalIdentity: appointment.user?.additionalPersonalIdentity ?? [],
      lastName: appointment.user?.lastName,
      civility: appointment.user?.civility ?? "",
      firstName: appointment.user?.firstName,
      birthday: appointment.user?.birthday,
      email: appointment.user?.email,
      phone: appointment.user?.phone,
      address: {
        line1: appointment.user?.address?.line1 ?? "",
        line2: appointment.user?.address?.line2 ?? "",
        zipCode: appointment.user?.address?.zipCode ?? "",
        city: appointment.user?.address?.city ?? "",
        country: appointment.user?.address?.country ?? "SUISSE",
      },
    },
    serviceId: String(creneau.serviceId || appointment.serviceId),
    siteCode: creneau.siteCode || appointment.siteCode,
    siteIdSys: appointment.siteIdSys,
    resources: {
      id: Number(creneau.resource?.id || 0),
      key: String(creneau.resource?.key || ""),
      type: creneau.resource?.type || CRENEAU_TYPE_STATION,
      name: creneau.resource?.name || "",
      station: creneau.resource?.station || {},
    },
    motives: appointment.motives ?? [],
  };
}
