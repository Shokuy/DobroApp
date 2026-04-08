import os

from fastapi.testclient import TestClient

os.environ["DATABASE_URL"] = "sqlite+pysqlite:///./test.db"

from app.main import app  # noqa: E402


client = TestClient(app)


def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"


def test_role_sign_in_and_requests_flow():
    auth = client.post("/api/auth/role", json={"role": "pensioner", "full_name": "Мария"})
    assert auth.status_code == 200
    user = auth.json()
    created = client.post(
        "/api/requests",
        json={
            "title": "Тестовая заявка",
            "help_type": "groceries",
            "reward_coins": 35,
            "district": "Центральный",
            "address": "ул. Тестовая 1",
            "time": "Сегодня 19:00",
            "comment": "",
            "pensioner_id": user["user_id"],
            "pensioner_name": user["full_name"],
        },
    )
    assert created.status_code == 200
    assert created.json()["status"] == "open"
