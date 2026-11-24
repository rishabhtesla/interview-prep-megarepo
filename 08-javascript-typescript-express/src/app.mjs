import express from "express";

export function createApp() {
  const app = express();
  const orders = [];

  app.use(express.json({ limit: "16kb" }));

  app.get("/health", (_request, response) => {
    response.json({ status: "UP" });
  });

  app.get("/api/orders", (_request, response) => {
    response.json(orders);
  });

  app.post("/api/orders", (request, response) => {
    const { customerId, amount } = request.body ?? {};
    if (typeof customerId !== "string" || customerId.length === 0
        || typeof amount !== "number" || !Number.isFinite(amount) || amount <= 0) {
      response.status(400).json({
        error: "customerId must be non-empty and amount must be a positive number",
      });
      return;
    }

    const order = { id: `o${orders.length + 1}`, customerId, amount };
    orders.push(order);
    response.status(201).json(order);
  });

  app.use((error, _request, response, _next) => {
    console.error(error);
    response.status(500).json({ error: "Internal server error" });
  });

  return { app, orders };
}
