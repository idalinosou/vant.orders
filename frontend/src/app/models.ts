export type OrderStatus = 'CREATED' | 'QUOTED' | 'DISPATCHED' | 'DELIVERED';

export interface Order {
  id: string;
  code: string;
  customerName: string;
  originCep: string;
  destinationCep: string;
  weightKg: number;
  status: OrderStatus;
  freightCents: number | null;
  carrier: string | null;
  estimatedDays: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface NewOrder {
  customerName: string;
  originCep: string;
  destinationCep: string;
  weightKg: number;
}

export const STATUS_FLOW: OrderStatus[] = ['CREATED', 'QUOTED', 'DISPATCHED', 'DELIVERED'];

export const STATUS_LABEL: Record<OrderStatus, string> = {
  CREATED: 'Criado',
  QUOTED: 'Cotado',
  DISPATCHED: 'Despachado',
  DELIVERED: 'Entregue',
};

export const NEXT_STATUS: Partial<Record<OrderStatus, OrderStatus>> = {
  QUOTED: 'DISPATCHED',
  DISPATCHED: 'DELIVERED',
};
