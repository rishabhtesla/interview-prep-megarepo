const orders = [
  { id: "o1", customerId: "c1", amount: 10, completed: true },
  { id: "o2", customerId: "c1", amount: 5, completed: false },
  { id: "o3", customerId: "c2", amount: 8, completed: true },
];

export function completedTotal(items) {
  return items
    .filter((order) => order.completed)
    .reduce((total, order) => total + order.amount, 0);
}

export async function loadDashboard(load) {
  const [ordersResponse, alertsResponse] = await Promise.all([
    load("/api/orders"),
    load("/api/alerts"),
  ]);
  return { orders: ordersResponse, alerts: alertsResponse };
}

function main() {
  const completed = orders.filter(({ completed }) => completed);
  const summary = {
    ids: completed.map(({ id }) => id),
    total: completedTotal(orders),
  };
  console.log(summary);
}

if (import.meta.url === `file://${process.argv[1]}`) {
  main();
}
