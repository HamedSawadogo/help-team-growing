export type PaymentMethod = 'WAVE' | 'ORANGE_MONEY';

export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';

export interface Payment {
  id: number;
  clientId: number | null;
  clientFullName: string;
  amount: number;
  currency: string;
  method: PaymentMethod;
  status: PaymentStatus;
  description: string | null;
  balanceAfter: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface PaymentRequest {
  clientId?: number | null;
  amount: number;
  idempotencyKey: string;
  currency: string;
  method: PaymentMethod;
  description?: string | null;
}

export interface PageResponse<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
