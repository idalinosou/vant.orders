export interface OrderCreatedEvent {
  orderId: string;
  code: string;
  originCep: string;
  destinationCep: string;
  weightKg: number;
}

export interface FreightQuote {
  freightCents: number;
  carrier: string;
  estimatedDays: number;
}

const BASE_CENTS = 1800;
const CENTS_PER_KG = 85;

const ZONE_FACTORS = [1, 1.18, 1.35, 1.55, 1.75];

export function regionOf(cep: string): number {
  if (!/^\d{8}$/.test(cep)) {
    throw new Error(`CEP invalido: ${cep}`);
  }
  return Number(cep[0]);
}

export function quoteFreight(event: OrderCreatedEvent): FreightQuote {
  if (!(event.weightKg > 0)) {
    throw new Error(`Peso invalido: ${event.weightKg}`);
  }

  const zoneDistance = Math.abs(regionOf(event.originCep) - regionOf(event.destinationCep));
  const factor = ZONE_FACTORS[Math.min(zoneDistance, ZONE_FACTORS.length - 1)];
  const rawCents = (BASE_CENTS + event.weightKg * CENTS_PER_KG) * factor;

  return {
    freightCents: Math.round(rawCents),
    carrier: pickCarrier(event.weightKg),
    estimatedDays: Math.min(8, 2 + zoneDistance),
  };
}

function pickCarrier(weightKg: number): string {
  if (weightKg >= 1000) return "Frota Litoral";
  if (weightKg >= 300) return "Rodo Sul Cargas";
  return "Expresso Sul";
}
