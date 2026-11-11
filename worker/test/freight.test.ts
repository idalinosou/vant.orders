import test from "node:test";
import assert from "node:assert/strict";
import { quoteFreight, regionOf } from "../src/freight.ts";

test("extrai a regiao a partir do primeiro digito do CEP", () => {
  assert.equal(regionOf("30130010"), 3);
  assert.equal(regionOf("20040020"), 2);
});

test("recusa CEP fora do formato de oito digitos", () => {
  assert.throws(() => regionOf("3013001"), /CEP invalido/);
});

test("cobra mais caro quando a rota cruza regioes distantes", () => {
  const perto = quoteFreight({
    orderId: "a",
    code: "ORD-1",
    originCep: "30130010",
    destinationCep: "31270010",
    weightKg: 100,
  });
  const longe = quoteFreight({
    orderId: "b",
    code: "ORD-2",
    originCep: "30130010",
    destinationCep: "90010150",
    weightKg: 100,
  });

  assert.ok(longe.freightCents > perto.freightCents);
  assert.ok(longe.estimatedDays > perto.estimatedDays);
});

test("escolhe a transportadora pela faixa de peso", () => {
  const leve = quoteFreight({ orderId: "a", code: "A", originCep: "30130010", destinationCep: "30140000", weightKg: 50 });
  const medio = quoteFreight({ orderId: "b", code: "B", originCep: "30130010", destinationCep: "30140000", weightKg: 400 });
  const pesado = quoteFreight({ orderId: "c", code: "C", originCep: "30130010", destinationCep: "30140000", weightKg: 1800 });

  assert.equal(leve.carrier, "Expresso Sul");
  assert.equal(medio.carrier, "Rodo Sul Cargas");
  assert.equal(pesado.carrier, "Frota Litoral");
});

test("arredonda o frete para centavos inteiros", () => {
  const quote = quoteFreight({ orderId: "a", code: "A", originCep: "30130010", destinationCep: "30140000", weightKg: 12.34 });
  assert.equal(Number.isInteger(quote.freightCents), true);
  assert.equal(quote.freightCents, 2849);
});

test("rejeita peso invalido", () => {
  assert.throws(
    () => quoteFreight({ orderId: "a", code: "A", originCep: "30130010", destinationCep: "30140000", weightKg: 0 }),
    /Peso invalido/,
  );
});
