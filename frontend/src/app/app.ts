import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { NEXT_STATUS, Order, OrderStatus, STATUS_FLOW, STATUS_LABEL } from './models';
import { OrdersService } from './orders.service';

type Filter = 'ALL' | OrderStatus;

@Component({
  selector: 'app-root',
  imports: [CurrencyPipe, DatePipe, DecimalPipe],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly service = inject(OrdersService);
  protected readonly statusLabel = STATUS_LABEL;
  protected readonly flow = STATUS_FLOW;
  protected readonly filters: Filter[] = ['ALL', ...STATUS_FLOW];

  protected readonly orders = signal<Order[]>([]);
  protected readonly loading = signal(true);
  protected readonly busy = signal(false);
  protected readonly feedback = signal('');
  protected readonly feedbackKind = signal<'ok' | 'error'>('ok');
  protected readonly selectedId = signal<string | null>(null);
  protected readonly statusFilter = signal<Filter>('ALL');
  protected readonly search = signal('');

  protected readonly customerName = signal('');
  protected readonly originCep = signal('');
  protected readonly destinationCep = signal('');
  protected readonly weightKg = signal('');

  protected readonly touched = signal<Record<string, boolean>>({});

  protected readonly errors = computed<Record<string, string>>(() => {
    const found: Record<string, string> = {};
    if (this.customerName().trim().length < 3) {
      found['customerName'] = 'Informe ao menos 3 caracteres';
    }
    if (!/^\d{8}$/.test(this.originCep())) {
      found['originCep'] = 'CEP com 8 digitos';
    }
    if (!/^\d{8}$/.test(this.destinationCep())) {
      found['destinationCep'] = 'CEP com 8 digitos';
    }
    const weight = Number(this.weightKg().replace(',', '.'));
    if (!Number.isFinite(weight) || weight <= 0) {
      found['weightKg'] = 'Peso maior que zero';
    }
    return found;
  });

  protected readonly formValid = computed(() => Object.keys(this.errors()).length === 0);

  protected fieldError(field: 'customerName' | 'originCep' | 'destinationCep' | 'weightKg'): string | null {
    return this.touched()[field] ? (this.errors()[field] ?? null) : null;
  }

  protected markTouched(field: 'customerName' | 'originCep' | 'destinationCep' | 'weightKg'): void {
    this.touched.update((state) => ({ ...state, [field]: true }));
  }

  protected readonly filtered = computed(() => {
    const term = this.search().trim().toLowerCase();
    const status = this.statusFilter();
    return this.orders().filter((order) => {
      const matchesStatus = status === 'ALL' || order.status === status;
      const matchesTerm =
        !term ||
        order.customerName.toLowerCase().includes(term) ||
        order.code.toLowerCase().includes(term) ||
        order.destinationCep.includes(term);
      return matchesStatus && matchesTerm;
    });
  });

  protected readonly counts = computed(() => {
    const base: Record<Filter, number> = {
      ALL: this.orders().length,
      CREATED: 0,
      QUOTED: 0,
      DISPATCHED: 0,
      DELIVERED: 0,
    };
    for (const order of this.orders()) {
      base[order.status] += 1;
    }
    return base;
  });

  protected readonly selected = computed(
    () => this.orders().find((order) => order.id === this.selectedId()) ?? null,
  );

  constructor() {
    void this.load();
  }

  protected async load(): Promise<void> {
    this.loading.set(true);
    try {
      this.orders.set(await this.service.list());
    } finally {
      this.loading.set(false);
    }
  }

  protected async submit(event: Event): Promise<void> {
    event.preventDefault();
    if (!this.formValid() || this.busy()) {
      return;
    }

    this.busy.set(true);
    try {
      const order = await this.service.create({
        customerName: this.customerName().trim(),
        originCep: this.originCep(),
        destinationCep: this.destinationCep(),
        weightKg: Number(this.weightKg().replace(',', '.')),
      });

      this.resetForm();
      await this.load();
      this.notify(`Pedido ${order.code} criado e publicado na fila`);
      this.selectedId.set(order.id);

      if (this.service.demo()) {
        setTimeout(() => {
          void this.service.quoteInDemo(order.id).then(() => this.load());
        }, 1500);
      }
    } catch (error) {
      this.notify(this.describe(error), 'error');
    } finally {
      this.busy.set(false);
    }
  }

  protected async advance(order: Order): Promise<void> {
    const target = NEXT_STATUS[order.status];
    if (!target || this.busy()) {
      return;
    }

    this.busy.set(true);
    try {
      await this.service.advance(order, target);
      await this.load();
      this.notify(`${order.code} agora esta ${STATUS_LABEL[target].toLowerCase()}`);
    } catch (error) {
      this.notify(this.describe(error), 'error');
    } finally {
      this.busy.set(false);
    }
  }

  protected nextStatus(order: Order): OrderStatus | undefined {
    return NEXT_STATUS[order.status];
  }

  protected select(order: Order): void {
    this.selectedId.set(order.id);
  }

  protected closeDetail(): void {
    this.selectedId.set(null);
  }

  protected formatCep(cep: string): string {
    return cep.length === 8 ? `${cep.slice(0, 5)}-${cep.slice(5)}` : cep;
  }

  protected money(cents: number | null): number {
    return (cents ?? 0) / 100;
  }

  protected stepIndex(status: OrderStatus): number {
    return this.flow.indexOf(status);
  }

  protected digitsOnly(target: EventTarget | null): string {
    const input = target as HTMLInputElement | null;
    return (input?.value ?? '').replace(/\D/g, '').slice(0, 8);
  }

  protected value(event: Event): string {
    return (event.target as HTMLInputElement | null)?.value ?? '';
  }

  private resetForm(): void {
    this.customerName.set('');
    this.originCep.set('');
    this.destinationCep.set('');
    this.weightKg.set('');
  }

  private notify(message: string, kind: 'ok' | 'error' = 'ok'): void {
    this.feedback.set(message);
    this.feedbackKind.set(kind);
    setTimeout(() => this.feedback.set(''), 4200);
  }

  private describe(error: unknown): string {
    const detail = (error as { error?: { detail?: string } })?.error?.detail;
    return detail ?? 'Nao foi possivel concluir a operacao';
  }
}
