import { NewOrder, Order, OrderStatus } from './models';

const HOUR = 60 * 60 * 1000;
const DAY = 24 * HOUR;

function iso(offsetMs: number): string {
  return new Date(Date.now() - offsetMs).toISOString();
}

export function seedDemoOrders(): Order[] {
  return [
    {
      id: 'demo-1',
      code: 'ORD-260104-C9XD',
      customerName: 'Cerealista Boa Safra',
      originCep: '78110000',
      destinationCep: '38400000',
      weightKg: 310.75,
      status: 'CREATED',
      freightCents: null,
      carrier: null,
      estimatedDays: null,
      createdAt: iso(0.7 * HOUR),
      updatedAt: iso(0.7 * HOUR),
    },
    {
      id: 'demo-2',
      code: 'ORD-260101-8K2A',
      customerName: 'Transportes Vale Verde',
      originCep: '30130010',
      destinationCep: '20040020',
      weightKg: 480,
      status: 'QUOTED',
      freightCents: 129900,
      carrier: 'Rodo Sul Cargas',
      estimatedDays: 3,
      createdAt: iso(6 * HOUR),
      updatedAt: iso(5 * HOUR),
    },
    {
      id: 'demo-3',
      code: 'ORD-260102-M4TR',
      customerName: 'Distribuidora Serra Azul',
      originCep: '31270010',
      destinationCep: '29090500',
      weightKg: 1250.5,
      status: 'DISPATCHED',
      freightCents: 248000,
      carrier: 'Frota Litoral',
      estimatedDays: 4,
      createdAt: iso(30 * HOUR),
      updatedAt: iso(20 * HOUR),
    },
    {
      id: 'demo-4',
      code: 'ORD-260103-Q7BZ',
      customerName: 'Comercial Pampa',
      originCep: '90010150',
      destinationCep: '80010010',
      weightKg: 92.3,
      status: 'DELIVERED',
      freightCents: 48900,
      carrier: 'Expresso Sul',
      estimatedDays: 2,
      createdAt: iso(4 * DAY),
      updatedAt: iso(2 * DAY),
    },
  ];
}

export function demoOrderFrom(payload: NewOrder): Order {
  const suffix = Math.random().toString(36).slice(2, 6).toUpperCase();
  const now = new Date();
  const stamp = `${String(now.getFullYear()).slice(2)}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`;

  return {
    id: `demo-${crypto.randomUUID()}`,
    code: `ORD-${stamp}-${suffix}`,
    customerName: payload.customerName,
    originCep: payload.originCep,
    destinationCep: payload.destinationCep,
    weightKg: payload.weightKg,
    status: 'CREATED',
    freightCents: null,
    carrier: null,
    estimatedDays: null,
    createdAt: now.toISOString(),
    updatedAt: now.toISOString(),
  };
}

export function demoQuoteFor(order: Order): Pick<Order, 'freightCents' | 'carrier' | 'estimatedDays' | 'status'> {
  const zoneDistance = Math.abs(Number(order.originCep[0]) - Number(order.destinationCep[0]));
  const factor = [1, 1.18, 1.35, 1.55, 1.75][Math.min(zoneDistance, 4)];
  const cents = Math.round((1800 + order.weightKg * 85) * factor);
  const carrier = order.weightKg >= 1000 ? 'Frota Litoral' : order.weightKg >= 300 ? 'Rodo Sul Cargas' : 'Expresso Sul';

  return {
    freightCents: cents,
    carrier,
    estimatedDays: Math.min(8, 2 + zoneDistance),
    status: 'QUOTED' as OrderStatus,
  };
}
