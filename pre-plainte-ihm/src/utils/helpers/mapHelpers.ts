import { POLICE_STATIONS, POLICE_STATION_NAME_VARIATIONS, LATITUDE_GE, LONGITUDE_GE } from "@/constants/constant";

export const getServiceCoordinates = (service: any): [number, number] | null => {
  if (!service?.name) {
    return null;
  }

  const serviceName = service.name.toLowerCase();

  for (const [stationKey, variations] of Object.entries(POLICE_STATION_NAME_VARIATIONS)) {
    for (const variation of variations) {
      if (serviceName.includes(variation)) {
        const stationData = POLICE_STATIONS[stationKey as keyof typeof POLICE_STATIONS];
        if (stationData) {
          return [stationData.lat, stationData.lng];
        }
      }
    }
  }

  return [LATITUDE_GE, LONGITUDE_GE];
};

export const getServiceAddress = (service: any): string | undefined => {
  if (!service?.name) {
    return "Adresse non disponible";
  }

  const serviceName = service.name.toLowerCase();

  for (const [stationKey, variations] of Object.entries(POLICE_STATION_NAME_VARIATIONS)) {
    for (const variation of variations) {
      if (serviceName.includes(variation)) {
        const stationData = POLICE_STATIONS[stationKey as keyof typeof POLICE_STATIONS];
        if (stationData) {
          return stationData.address;
        }
      }
    }
  }
};

export const calculateDistance = (lat1: number, lng1: number, lat2: number, lng2: number): number => {
  const R = 6371;
  const DEGREE = 180;
  const dLat = ((lat2 - lat1) * Math.PI) / DEGREE;
  const dLng = ((lng2 - lng1) * Math.PI) / DEGREE;
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos((lat1 * Math.PI) / DEGREE) * Math.cos((lat2 * Math.PI) / DEGREE) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
};

export const findNearestStation = (
  userCoords: [number, number],
  services: any[],
): { service: any; distance: number } | null => {
  if (!services || services.length === 0) {
    return null;
  }

  let nearestService = null;
  let minDistance = Infinity;

  services.forEach(service => {
    const serviceCoords = getServiceCoordinates(service);
    if (serviceCoords) {
      const distance = calculateDistance(userCoords[0], userCoords[1], serviceCoords[0], serviceCoords[1]);

      if (distance < minDistance) {
        minDistance = distance;
        nearestService = service;
      }
    }
  });

  return nearestService ? { service: nearestService, distance: minDistance } : null;
};

export const getUserLocation = (): Promise<[number, number]> => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error("La géolocalisation n'est pas supportée par ce navigateur"));
      return;
    }

    navigator.geolocation.getCurrentPosition(
      position => {
        const lat = position.coords.latitude;
        const lng = position.coords.longitude;
        resolve([lat, lng]);
      },
      error => {
        reject(error);
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 300000,
      },
    );
  });
};
