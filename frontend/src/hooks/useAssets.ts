import {
  useQuery,
  keepPreviousData,
} from '@tanstack/react-query';
import api from '../api/client';
import { ASSETS } from '../api/endpoints';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

export type AssetType =
  | 'Computer'
  | 'NetworkEquipment'
  | 'Monitor'
  | 'Printer'
  | 'Phone'
  | 'Peripheral'
  | 'Software';

export interface Asset {
  id: string;
  assetType: AssetType;
  name: string;
  entityId: string;
  serial: string;
  otherSerial: string;
  stateId: string;
  locationId: string;
  userId: string;
  groupId: string;
  manufacturerId: string;
  modelId: string;
  isDeleted: boolean;
  computerDetails?: ComputerDetails;
  networkPorts: NetworkPort[];
  infocom: Infocom | null;
  contractIds: string[];
  createdAt: string;
  updatedAt: string;
}

export interface ComputerDetails {
  cpus: string[];
  ram: string[];
  hdds: string[];
  gpus: string[];
}

export interface NetworkPort {
  id: string;
  name: string;
  macAddress: string;
  ipAddress: string;
  vlan: string;
  connectionType: string;
}

export interface Infocom {
  purchaseDate: string | null;
  purchasePrice: number | null;
  warrantyExpiry: string | null;
  orderNumber: string | null;
  deliveryDate: string | null;
}

export interface Software {
  id: string;
  name: string;
  manufacturerId: string;
  categoryId: string;
  installationsCount: number;
  licensesCount: number;
}

export interface SoftwareLicense {
  id: string;
  name: string;
  softwareId: string;
  softwareName?: string;
  licenseType: string;
  serial: string;
  totalSeats: number;
  usedSeats: number;
  remainingSeats: number;
  expiryDate: string | null;
}

export interface AssetListParams {
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: 'asc' | 'desc';
  assetType?: AssetType;
  entityId?: string;
  stateId?: string;
  locationId?: string;
  userId?: string;
  search?: string;
}

export interface SoftwareListParams {
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: 'asc' | 'desc';
  search?: string;
}

export interface LicenseListParams {
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: 'asc' | 'desc';
  softwareId?: string;
  search?: string;
}

// ---------------------------------------------------------------------------
// Query key factory
// ---------------------------------------------------------------------------
export const assetKeys = {
  all: ['assets'] as const,
  lists: () => [...assetKeys.all, 'list'] as const,
  list: (params: AssetListParams) => [...assetKeys.lists(), params] as const,
  byType: (type: AssetType, params: AssetListParams) =>
    [...assetKeys.all, 'byType', type, params] as const,
  details: () => [...assetKeys.all, 'detail'] as const,
  detail: (type: string, id: string | number) =>
    [...assetKeys.details(), type, id] as const,
  software: (params: SoftwareListParams) =>
    [...assetKeys.all, 'software', params] as const,
  licenses: (params: LicenseListParams) =>
    [...assetKeys.all, 'licenses', params] as const,
};

// ---------------------------------------------------------------------------
// Hooks
// ---------------------------------------------------------------------------

/** Paginated asset list (all types) */
export function useAssetList(params: AssetListParams = {}) {
  return useQuery({
    queryKey: assetKeys.list(params),
    queryFn: () =>
      api.get<Asset[]>(ASSETS.LIST, params as Record<string, unknown>),
    placeholderData: keepPreviousData,
  });
}

/** Paginated asset list filtered by type */
export function useAssetListByType(
  type: AssetType,
  params: AssetListParams = {},
) {
  return useQuery({
    queryKey: assetKeys.byType(type, params),
    queryFn: () =>
      api.get<Asset[]>(
        ASSETS.BY_TYPE(type),
        params as Record<string, unknown>,
      ),
    placeholderData: keepPreviousData,
    enabled: Boolean(type),
  });
}

/** Single asset detail (with all tab data) */
export function useAssetDetail(type: string, id: string | number) {
  return useQuery({
    queryKey: assetKeys.detail(type, id),
    queryFn: () => api.get<Asset>(ASSETS.DETAIL(type, id)),
    enabled: Boolean(type) && Boolean(id),
  });
}

/** Paginated software list */
export function useSoftwareList(params: SoftwareListParams = {}) {
  return useQuery({
    queryKey: assetKeys.software(params),
    queryFn: () =>
      api.get<Software[]>(
        ASSETS.SOFTWARE,
        params as Record<string, unknown>,
      ),
    placeholderData: keepPreviousData,
  });
}

/** Paginated license list */
export function useLicenseList(params: LicenseListParams = {}) {
  return useQuery({
    queryKey: assetKeys.licenses(params),
    queryFn: () =>
      api.get<SoftwareLicense[]>(
        ASSETS.LICENSES,
        params as Record<string, unknown>,
      ),
    placeholderData: keepPreviousData,
  });
}
