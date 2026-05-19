export interface AddressResult {
  label: string;
  streetNumber?: string;
  street: string;
  postalCode: string;
  locality: string;
  countryCode: string;
  municipality: string;
  canton?: string;
}

export interface AddressSearchParams {
  searchText: string;
  limit?: number;
  country?: string;
}
