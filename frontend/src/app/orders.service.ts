import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { firstValueFrom, timeout } from 'rxjs';
import { demoOrderFrom, demoQuoteFor, seedDemoOrders } from './demo-orders';
import { NewOrder, Order, OrderStatus } from './models';

function resolveApiBase(): string {
  const configured = (globalThis as { __VANT_ORDERS_API__?: string }).__VANT_ORDERS_API__;
  if (configured) {
    return configured.replace(/\/$/, '');
  }
  if (location.hostname === 'localhost' || location.hostname === '127.0.0.1') {
    return 'http://localhost:8080';
  }
  return location.origin;
}

@Injectable({ providedIn: 'root' })
export class OrdersService {
  private readonly http = inject(HttpClient);
  private readonly apiBase = resolveApiBase();
  private demoOrders: Order[] = [];

  readonly demo = signal(false);

  get apiUrl(): string {
    return this.apiBase;
  }

  get docsUrl(): string {
    return `${this.apiBase}/swagger-ui.html`;
  }

  async list(): Promise<Order[]> {
    if (this.demo()) {
      return this.snapshot();
    }

    try {
      const orders = await firstValueFrom(
        this.http.get<Order[]>(`${this.apiBase}/api/orders`).pipe(timeout(3000)),
      );
      return orders;
    } catch {
      this.demo.set(true);
      this.demoOrders = seedDemoOrders();
      return this.snapshot();
    }
  }

  async create(payload: NewOrder): Promise<Order> {
    if (this.demo()) {
      const order = demoOrderFrom(payload);
      this.demoOrders = [order, ...this.demoOrders];
      return order;
    }

    return firstValueFrom(this.http.post<Order>(`${this.apiBase}/api/orders`, payload));
  }

  async advance(order: Order, status: OrderStatus): Promise<void> {
    if (this.demo()) {
      this.demoOrders = this.demoOrders.map((item) =>
        item.id === order.id ? { ...item, status, updatedAt: new Date().toISOString() } : item,
      );
      return;
    }

    await firstValueFrom(
      this.http.patch<Order>(`${this.apiBase}/api/orders/${order.id}/status`, { status }),
    );
  }

  async quoteInDemo(orderId: string): Promise<void> {
    const target = this.demoOrders.find((item) => item.id === orderId);
    if (!target || target.status !== 'CREATED') {
      return;
    }

    const quote = demoQuoteFor(target);
    this.demoOrders = this.demoOrders.map((item) =>
      item.id === orderId ? { ...item, ...quote, updatedAt: new Date().toISOString() } : item,
    );
  }

  private snapshot(): Order[] {
    return this.demoOrders.map((order) => ({ ...order }));
  }
}
