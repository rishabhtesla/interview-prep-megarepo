import test from "node:test";
import assert from "node:assert/strict";
import { completedTotal, loadDashboard } from "../src/01-basics.mjs";
import { createApp } from "../src/app.mjs";

test("completedTotal ignores incomplete orders", () => {
  assert.equal(completedTotal([
    { amount: 10, completed: true },
    { amount: 5, completed: false },
  ]), 10);
});

test("loadDashboard starts independent requests and combines responses", async () => {
  const calls = [];
  const result = await loadDashboard(async (path) => {
    calls.push(path);
    return path === "/api/orders" ? ["order"] : ["alert"];
  });
  assert.deepEqual(calls, ["/api/orders", "/api/alerts"]);
  assert.deepEqual(result, { orders: ["order"], alerts: ["alert"] });
});

test("Express API validates and persists a valid request", async (t) => {
  const { app } = createApp();
  const server = app.listen(0, "127.0.0.1");
  await new Promise((resolve) => server.once("listening", resolve));
  t.after(() => server.close());
  const address = server.address();
  const baseUrl = `http://127.0.0.1:${address.port}`;

  const created = await fetch(`${baseUrl}/api/orders`, {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ customerId: "c1", amount: 42.5 }),
  });
  assert.equal(created.status, 201);
  assert.deepEqual(await created.json(), {
    id: "o1",
    customerId: "c1",
    amount: 42.5,
  });

  const invalid = await fetch(`${baseUrl}/api/orders`, {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ customerId: "", amount: -1 }),
  });
  assert.equal(invalid.status, 400);
});
