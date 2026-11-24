"""Lesson 4: a small FastAPI request boundary."""

from __future__ import annotations

from decimal import Decimal
from uuid import uuid4

from fastapi import FastAPI, status
from pydantic import BaseModel, Field

app = FastAPI(title="Interview Python API", version="1.0")


class OrderRequest(BaseModel):
    customer_id: str = Field(min_length=1, max_length=40)
    amount: Decimal = Field(gt=0, max_digits=12, decimal_places=2)


class OrderResponse(OrderRequest):
    order_id: str


orders: list[OrderResponse] = []


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "UP"}


@app.get("/api/orders", response_model=list[OrderResponse])
def list_orders() -> list[OrderResponse]:
    return orders


@app.post(
    "/api/orders",
    response_model=OrderResponse,
    status_code=status.HTTP_201_CREATED,
)
def create_order(request: OrderRequest) -> OrderResponse:
    order = OrderResponse(order_id=str(uuid4()), **request.model_dump())
    orders.append(order)
    return order
