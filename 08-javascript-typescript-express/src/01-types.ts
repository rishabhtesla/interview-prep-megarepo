export type OrderStatus = "pending" | "completed" | "cancelled";

export interface Order {
  readonly id: string;
  readonly customerId: string;
  readonly amount: number;
  status: OrderStatus;
}

export function total<T extends { amount: number }>(items: readonly T[]): number {
  return items.reduce((sum, item) => sum + item.amount, 0);
}

export function describeStatus(status: OrderStatus): string {
  switch (status) {
    case "pending":
      return "Waiting for processing";
    case "completed":
      return "Finished";
    case "cancelled":
      return "Cancelled";
  }
}

const example: Order = {
  id: "o1",
  customerId: "c1",
  amount: 10,
  status: "pending",
};

console.log(total([example]));
