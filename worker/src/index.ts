import amqp from "amqplib";
import type { ChannelModel, Channel, ConsumeMessage } from "amqplib";
import { quoteFreight } from "./freight.ts";
import type { OrderCreatedEvent } from "./freight.ts";
import { sendQuote } from "./api.ts";
import type { ApiConfig } from "./api.ts";

const config = {
  rabbitUrl: process.env.RABBIT_URL ?? "amqp://guest:guest@localhost:5672",
  exchange: process.env.ORDERS_EXCHANGE ?? "orders.exchange",
  queue: process.env.FREIGHT_QUEUE ?? "freight.queue",
  routingKey: process.env.ORDER_ROUTING_KEY ?? "order.created",
  api: {
    apiUrl: process.env.API_URL ?? "http://localhost:8080",
    internalToken: process.env.INTERNAL_TOKEN ?? "dev-internal-token",
  } satisfies ApiConfig,
};

async function handleMessage(message: ConsumeMessage, channel: Channel): Promise<void> {
  const event = JSON.parse(message.content.toString("utf8")) as OrderCreatedEvent;

  try {
    const quote = quoteFreight(event);
    await sendQuote(event.orderId, quote, config.api);
    channel.ack(message);
    log("cotacao enviada", event.code, quote);
  } catch (error) {
    const reason = error instanceof Error ? error.message : String(error);
    if (message.fields.redelivered) {
      channel.nack(message, false, false);
      log("mensagem descartada apos reentrega", event.code, { reason });
      return;
    }
    channel.nack(message, false, true);
    log("falha ao processar, devolvendo para a fila", event.code, { reason });
  }
}

function log(message: string, code: string, extra?: unknown): void {
  const line = `[worker] ${message} ${code}`;
  if (extra) {
    console.log(line, JSON.stringify(extra));
  } else {
    console.log(line);
  }
}

async function main(): Promise<void> {
  const connection: ChannelModel = await amqp.connect(config.rabbitUrl);
  const channel = await connection.createChannel();
  await channel.assertExchange(config.exchange, "topic", { durable: true });
  await channel.assertQueue(config.queue, { durable: true });
  await channel.bindQueue(config.queue, config.exchange, config.routingKey);
  await channel.prefetch(10);

  console.log(`[worker] consumindo ${config.queue} de ${config.exchange}`);

  await channel.consume(config.queue, (message) => {
    if (!message) return;
    void handleMessage(message, channel);
  });

  const shutdown = async () => {
    console.log("[worker] encerrando");
    await channel.close();
    await connection.close();
    process.exit(0);
  };

  process.on("SIGINT", shutdown);
  process.on("SIGTERM", shutdown);
}

main().catch((error: unknown) => {
  console.error("[worker] falhou ao iniciar", error);
  process.exit(1);
});
