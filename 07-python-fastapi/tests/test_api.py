import unittest

from fastapi.testclient import TestClient

from app.main import app, orders


class ApiTest(unittest.TestCase):
    def setUp(self) -> None:
        orders.clear()
        self.client = TestClient(app)

    def test_create_and_list_order(self) -> None:
        created = self.client.post(
            "/api/orders",
            json={"customer_id": "c1", "amount": "42.50"},
        )
        self.assertEqual(created.status_code, 201)
        self.assertEqual(created.json()["customer_id"], "c1")
        self.assertEqual(len(self.client.get("/api/orders").json()), 1)

    def test_invalid_order_does_not_mutate_store(self) -> None:
        response = self.client.post(
            "/api/orders",
            json={"customer_id": "", "amount": -1},
        )
        self.assertEqual(response.status_code, 422)
        self.assertEqual(orders, [])
