import type { FreightQuote } from "./freight.ts";

export interface ApiConfig {
  apiUrl: string;
  internalToken: string;
}

export async function sendQuote(
  orderId: string,
  quote: FreightQuote,
  config: ApiConfig,
  http: typeof fetch = fetch,
): Promise<void> {
  const response = await http(`${config.apiUrl}/api/orders/${orderId}/quote`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-Internal-Token": config.internalToken,
    },
    body: JSON.stringify(quote),
  });

  if (!response.ok) {
    throw new Error(`Callback recusado (${response.status}): ${await response.text()}`);
  }
}
