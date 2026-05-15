export interface PasskeyDto {
  id: string;
  deviceName: string;
  createdAt: string;
  lastUsedAt: string | null;
  backedUp: boolean;
  transports: string[];
  aaguid: string | null;
}
